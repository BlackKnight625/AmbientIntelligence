import sys

import cv2

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import imageProcessing
import footageStorage as fs
import itemsStorage as iS
import time
import numpy as np

from RWLock import RWLock

# Global Variables

footageLock = RWLock()
footageStorage = fs.loadFootageStorage()
itemsLock = RWLock()
items_storage = iS.loadItemsStorage()

lockedItemsMovedLock = RWLock()
lockedItemsMoved = set()

lastFootageReceivedTimeLock = RWLock()
lastFootageReceivedTime = time.time()
lastFootageReceivedTimeout = 10

smartphoneAppConnected = False
lastKeepaliveReceivedTime = -1
lastKeepaliveReceivedTimeout = 10

pictures = []
boundingBoxes = []
frameIdx = 0

# Service implementations
classNames = []
classFile = "../archive/coco.names"
with open(classFile, "rt") as f:
    classNames = f.read().rstrip("\n").split("\n")


def getItemName(classId):
    return classNames[classId - 1]


"""This class handles messages that were sent from the Surveillance Cameras (Camera App)"""


class CameraToCentralSystemService(pb2_grpc.CameraToCentralSystemServiceServicer):
    footageReceived = 0

    def __init__(self, *args, **kwargs):
        pass

    def send_footage(self, request, context):
        img_bytes = request.picture
        timestamp = request.time

        # Processing the received image
        image = imageProcessing.getImageFromBytes(img_bytes)
        items, locations = imageProcessing.processImage(image)

        #print("Locations:")
        #print(locations)
        print("Received footage! Size: ", len(img_bytes), ", footage number ", self.footageReceived)
        print("Items:", [getItemName(item) for item in items])

        seenItems = set()

        # Storing picture information
        for i in range(len(items)):
            item, boundingBox = getItemName(items[i]), locations[i]

            if item in seenItems:
                # Item has already been seen in the picture, meaning there's at least 2 items of the same type in the picture.
                # Ignoring similar items
                continue

            seenItems.add(item)

            if items_storage.has_item(item) and items_storage.isTracked(item):
                if items_storage.isLocked(item):
                    # The item seen is locked. Checking if it moved
                    lastSeenFootage = footageStorage.getLastSeenFootageAndInformation(item)

                    if lastSeenFootage is not None:
                        # This item has been seen before
                        lastBoundingBox = lastSeenFootage[
                            -1].boundingBox  # Fetching the BB from when the item was last seen

                        if (boundingBox != lastBoundingBox).any():
                            # Bounding box has moved, therefore, the item moved
                            lockedItemsMovedLock.w_acquire()
                            lockedItemsMoved.add(items_storage.getName(item))
                            lockedItemsMovedLock.w_release()

                footageLock.w_acquire()
                footageStorage.insertPicture(item, image, timestamp, boundingBox)
                footageLock.w_release()

        self.footageReceived += 1

        if self.footageReceived % 10 == 0:
            # Time to save all footage and items
            print("Saving all footage and item information in files")
            footageLock.r_acquire()
            fs.saveFootageStorage(footageStorage)
            footageLock.r_release()

            itemsLock.r_acquire()
            iS.saveItemsStorage(items_storage)
            itemsLock.r_release()

        lastFootageReceivedTimeLock.w_acquire()
        global lastFootageReceivedTime  # Needed to change this global variable on the next line
        lastFootageReceivedTime = time.time()
        lastFootageReceivedTimeLock.w_release()

        return pb2.FootageAck()


"""This class handles messages that were sent from the SmartPhone App"""


class SmartphoneAppToCentralSystemService(pb2_grpc.SmartphoneAppToCentralSystemServiceServicer):

    def greet(self, request, context):
        print("Greetings!")
        return pb2.Ack()

    def keepAlive(self, request, context):
        global smartphoneAppConnected, lastKeepaliveReceivedTime

        keepaliveResponse = pb2.KeepAliveResponse()

        if smartphoneAppConnected:
            keepaliveResponse.status = pb2.KeepAliveResponse.OK
        else:
            keepaliveResponse.status = pb2.KeepAliveResponse.SYSTEM_STOPPED_SENDING_STATUS_RESPONSES

        smartphoneAppConnected = True
        lastKeepaliveReceivedTime = time.time()

        return keepaliveResponse

    def locateItem(self, itemId, context):  # Returns VideoFootage
        videoFootageResponse = pb2.VideoFootage()

        footageLock.r_acquire()

        lastSeenFootage = footageStorage.getLastSeenFootageAndInformation(itemId.id)

        global pictures, boundingBoxes, frameIdx
        pictures = []
        boundingBoxes = []
        frameIdx = 0

        if lastSeenFootage is not None:
            print("doing stuff")
            for footage in lastSeenFootage:
                picture = pb2.Footage()

                boundingBox = getGrpcBoundingBoxFromCv2(footage.boundingBox)

                picture.time.CopyFrom(footage.timestamp)
                picture.picture = imageProcessing.getBytesFromImage(footage.picture)

                pictures.append(picture)
                boundingBoxes.append(boundingBox)

        footageLock.r_release()

        #videoFootageResponse.pictures.extend(pictures)
        #videoFootageResponse.itemBoundingBoxes.extend(boundingBoxes)
        videoFootageResponse.footageSize = len(pictures)

        print(len(pictures))
        print("Locate item: Bounding boxes- ", [getCv2BoundingBoxFromGrpc(bb) for bb in boundingBoxes])

        return videoFootageResponse

    def nextFrame(self, frameRequest, context):
        global frameIdx
        frame = pb2.Frame()
        frame.picture.CopyFrom(pictures[frameIdx])
        frame.box.CopyFrom(boundingBoxes[frameIdx])
        frameIdx += 1
        return frame

    def photoTaken(self, photoRequest, context):  # Returns PhotoResponse
        footage = photoRequest.footage
        img_bytes = footage.picture
        image = imageProcessing.getImageFromBytes(img_bytes)

        items, boundingBoxes = imageProcessing.processImage(image)
        photoResponse = pb2.PhotoResponse()
        if len(items) == 0:
            photoResponse.newItemId.id = ""
            photoResponse.status = pb2.PhotoResponse.NO_ITEM_FOUND
        elif len(items) > 1:
            photoResponse.newItemId.id = ""
            photoResponse.status = pb2.PhotoResponse.MULTIPLE_ITEMS_FOUND
        elif items_storage.has_item(getItemName(items[0])):
            photoResponse.newItemId.id = ""
            photoResponse.status = pb2.PhotoResponse.ITEM_ALREADY_EXISTS
        else:
            newId = getItemName(items[0])
            photoResponse.newItemId.id = newId
            photoResponse.status = pb2.PhotoResponse.OK

            # Getting a new image containing just the section that has the identified item
            itemImage = getSubRect(image, boundingBoxes[0])

            itemsLock.w_acquire()
            items_storage.insertItem(newId, False, True, itemImage, photoRequest.itemName)
            itemsLock.w_release()

        print("Photo taken: Status- ", photoResponse.status, ", new id- ", photoResponse.newItemId.id, ", name- ", photoRequest.itemName)

        return photoResponse

    def searchItem(self, searchParameters, context):  # Returns SearchResponse
        searchResponse = pb2.SearchResponse()
        itemInformations = []

        itemsLock.r_acquire()
        for quintuplet in items_storage.get_search_results(searchParameters.itemName):
            itemInformation = pb2.ItemInformation()
            itemId = pb2.ItemId()

            itemId.id = quintuplet[0]

            itemInformation.itemId.CopyFrom(itemId)
            itemInformation.locked = quintuplet[1]
            itemInformation.tracked = quintuplet[2]
            itemInformation.image = imageProcessing.getBytesFromImage(quintuplet[3])
            itemInformation.name = quintuplet[4]

            itemInformations.append(itemInformation)

        itemsLock.r_release()

        searchResponse.searchResults.extend(itemInformations)

        print("Search item: Search parameters- ", searchParameters.itemName, ", search results- ", [i.name for i in itemInformations])

        return searchResponse

    def trackItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setTracked(itemID.id, True)
        itemsLock.w_release()

        print("Track item: ", itemID.id)

        return pb2.Ack()

    def untrackItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setTracked(itemID.id, False)
        itemsLock.w_release()

        print("Untrack item: ", itemID.id)

        return pb2.Ack()

    def lockItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setLocked(itemID.id, True)
        itemsLock.w_release()

        print("Lock item: ", itemID.id)

        return pb2.Ack()

    def unlockItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setLocked(itemID.id, False)
        itemsLock.w_release()

        print("Unlock item: ", itemID.id)

        return pb2.Ack()

    def removeItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.removeItem(itemID.id)
        itemsLock.w_release()

        footageLock.w_acquire()
        footageStorage.removeData(itemID.id)
        footageLock.w_release()

        print("Remove item: ", itemID.id)

        return pb2.Ack()

    def statusRequest(self, request, context):
        global smartphoneAppConnected, lastKeepaliveReceivedTime
        while smartphoneAppConnected:
            response = pb2.StatusResponse()

            lockedItemsMovedLock.r_acquire()

            if len(lockedItemsMoved) != 0:
                # Locked items have been moved

                print("Status Request: Locked items moved- ", lockedItemsMoved)

                itemNameList = pb2.ItemNameList()

                itemNameList.itemNames.extend(lockedItemsMoved)

                lockedItemsMovedLock.r_release()

                # Clearing all items off the list
                lockedItemsMovedLock.w_acquire()
                lockedItemsMoved.clear()
                lockedItemsMovedLock.w_release()

                response.status = pb2.StatusResponse.LOCKED_ITEMS_MOVED
                response.movedLockedItems.CopyFrom(itemNameList)
            else:
                lockedItemsMovedLock.r_release()

                # No locked items have moved. Checking last time footage was received

                lastFootageReceivedTimeLock.r_acquire()
                timeDifference = time.time() - lastFootageReceivedTime
                lastFootageReceivedTimeLock.r_release()

                if timeDifference >= lastFootageReceivedTimeout:
                    # Camera hasn't sent footage in a long time

                    response.status = pb2.StatusResponse.CAMERA_TURNED_OFF
                    response.offCameraInfo = "Surveillance Camera has been offline for " + str(timeDifference)
                else:
                    # There's nothing wrong
                    response.status = pb2.StatusResponse.OK
                    response.ok.CopyFrom(pb2.Ack())

            yield response

            # Checking if the Smartphone App is still alive

            if lastKeepaliveReceivedTime != -1 and \
                    (time.time() - lastKeepaliveReceivedTime) > lastFootageReceivedTimeout:
                smartphoneAppConnected = False
                lastKeepaliveReceivedTime = -1
                continue

            time.sleep(1)  # Sleeping for 1 second so that status responses are sent every second


def getGrpcBoundingBoxFromCv2(cvsBoundingBox):
    boundingBox = pb2.BoundingBox()
    pointHigh = pb2.Point()
    pointLow = pb2.Point()

    pointHigh.x = cvsBoundingBox[0] + cvsBoundingBox[2]  # x_min + width
    pointHigh.y = cvsBoundingBox[1] + cvsBoundingBox[3]  # y_min + height
    pointLow.x = cvsBoundingBox[0]  # x_min
    pointLow.y = cvsBoundingBox[1]  # y_min

    boundingBox.high.CopyFrom(pointHigh)
    boundingBox.low.CopyFrom(pointLow)

    return boundingBox


def getCv2BoundingBoxFromGrpc(grpcBoundingBox):
    return np.array([
        grpcBoundingBox.low.x,
        grpcBoundingBox.low.y,
        grpcBoundingBox.high.x - grpcBoundingBox.low.x,
        grpcBoundingBox.high.y - grpcBoundingBox.low.y
    ])


def getSubRect(image, rect):
    x, y, w, h = rect
    return image[y: y + h, x: x + w]
