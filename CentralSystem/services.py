import sys

import cv2

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import imageProcessing
import footageStorage as fs
import itemsStorage as iS
import time

from RWLock import RWLock

# Global Variables

footageLock = RWLock()
footageStorage = fs.loadFootageStorage()
itemsLock = RWLock()
items_storage = iS.loadItemsStorage()

lockedItemsMovedLock = RWLock()
lockedItemsMoved = []

lastFootageReceivedTimeLock = RWLock()
lastFootageReceivedTime = time.time()
lastFootageReceivedTimeout = 10

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
        print("Items:")
        print(items)
        print("\n")
        print("Locations:")
        print(locations)
        print("\n")

        print("Received footage! Size: ", len(img_bytes))

        # Storing picture information
        for i in range(len(items)):
            item, boundingBox = getItemName(items[i]), locations[i]

            if items_storage.has_item(item):
                if items_storage.isLocked(item):
                    # The item seen is locked. Checking if it moved
                    lastSeenFootage = footageStorage.getLastSeenFootageAndInformation(item)

                    if lastSeenFootage != None:
                        # This item has been seen before
                        lastBoundingBox = lastSeenFootage[-1].boundingBox # Fetching the BB from when the item was last seen

                        if boundingBox != lastBoundingBox:
                            # Bounding box has moved, therefore, the item moved
                            lockedItemsMovedLock.w_acquire()
                            lockedItemsMoved.append(item)
                            lockedItemsMovedLock.w_release()

                footageLock.w_acquire()
                footageStorage.insertPicture(getItemName(item), image, timestamp, boundingBox)
                footageLock.w_release()

        self.footageReceived += 1

        if (self.footageReceived % 10):
            # Time to save all footage and items
            footageLock.r_acquire()
            fs.saveFootageStorage(footageStorage)
            footageLock.r_release()

            itemsLock.r_acquire()
            iS.saveItemsStorage(items_storage)
            itemsLock.r_release()

        lastFootageReceivedTimeLock.w_acquire()
        global lastFootageReceivedTime # Needed to change this global variable on the next line
        lastFootageReceivedTime = time.time()
        lastFootageReceivedTimeLock.w_release()

        return pb2.FootageAck()


"""This class handles messages that were sent from the SmartPhone App"""


class SmartphoneAppToCentralSystemService(pb2_grpc.SmartphoneAppToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def locateItem(self, itemId, context):  # Returns VideoFootage
        id = itemId.id
        videoFootageResponse = pb2.VideoFootage()

        footageLock.r_acquire()

        lastSeenFootage = footageStorage.getLastSeenFootageAndInformation(id)

        pictures = []
        boundingBoxes = []

        for footage in lastSeenFootage:
            picture = pb2.Footage()

            boundingBox = getGrpcBoundingBoxFromCv2(footage.boundingBox)

            picture.timestamp = footage.timestamp
            picture.picture = imageProcessing.getBytesFromImage(footage.picture)

            pictures.append(picture)
            boundingBoxes.append(boundingBox)

        footageLock.r_release()

        videoFootageResponse.pictures[:] = pictures
        videoFootageResponse.itemBoundingBoxes[:] = boundingBoxes

        return videoFootageResponse

    def photoTaken(self, footage, context):  # Returns PhotoResponse
        img_bytes = footage.picture

        items, _ = imageProcessing.processImage(imageProcessing.getImageFromBytes(img_bytes))
        photoResponse = pb2.PhotoResponse()
        if len(items) == 0:
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.NO_ITEM_FOUND
        elif len(items) > 1:
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.MULTIPLE_ITEMS_FOUND
        elif items_storage.has_item(getItemName(items[0])):
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.ITEM_ALREADY_EXISTS
        else:
            photoResponse.newItemId = getItemName(items[0])
            photoResponse.status = pb2.PhotoResponse.OK
        return photoResponse

    def confirmItemInsertion(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.insertItem(itemID.id, True, False)
        itemsLock.w_release()

        return pb2.Ack()

    def searchItem(self, searchParameters, context):  # Returns SearchResponse
        searchResponse = pb2.SearchResponse()

        itemsLock.r_acquire()
        searchResponse.searchResults[:] = items_storage.get_search_results(searchParameters)
        itemsLock.r_release()

        return searchResponse

    def trackItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setTracked(itemID.id, True)
        itemsLock.w_release()

        return pb2.Ack()

    def untrackItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setTracked(itemID.id, False)
        itemsLock.w_release()

        return pb2.Ack()

    def lockItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setLocked(itemID.id, True)
        itemsLock.w_release()

        return pb2.Ack()

    def unlockItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.setLocked(itemID.id, False)
        itemsLock.w_release()

        return pb2.Ack()

    def removeItem(self, itemID, context):  # Returns Ack
        itemsLock.w_acquire()
        items_storage.removeItem(itemID.id)
        itemsLock.w_release()

        return pb2.Ack()

    def statusRequest(self, request, context):
        response = pb2.StatusResponse()

        lockedItemsMovedLock.r_acquire()

        if len(lockedItemsMoved) != 0:
            # Locked items have been moved

            itemIdList = pb2.ItemIdList()
            itemIdList.items[:] = lockedItemsMoved

            lockedItemsMovedLock.r_release()

            # Clearing all items off the list
            lockedItemsMovedLock.w_lock()
            lockedItemsMoved.clear()
            lockedItemsMovedLock.w_release()

            response.status = pb2.StatusResponse.LOCKED_ITEMS_MOVED
            response.movedLockedItems = itemIdList
        else:
            lockedItemsMovedLock.r_release()

            # No locked items have moved. Checking last time footage was received

            lastFootageReceivedTimeLock.r_acquire()
            timeDifference = time.time() - lastFootageReceivedTime
            lastFootageReceivedTimeLock.r_release()

            if timeDifference >= lastFootageReceivedTimeout:
                # Camera hasn't sent footage in a long time

                response.status = pb2.StatusResponse.LOCKED_ITEMS_MOVED
                response.offCameraInfo = "Surveillance Camera has been offline for " + str(timeDifference)
            else:
                # There's nothing wrong
                lastFootageReceivedTimeLock.r_release()

                response.status = pb2.StatusResponse.OK
                response.ok = pb2.Ack()

        return response


def getGrpcBoundingBoxFromCv2(cvsBoundingBox):
    boundingBox = pb2.BoundingBox()
    pointHigh = pb2.Point()
    pointLow = pb2.Point()

    pointHigh.x = cvsBoundingBox[0] + cvsBoundingBox[2]  # x_min + width
    pointHigh.y = cvsBoundingBox[1] + cvsBoundingBox[3]  # y_min + height
    pointLow.x = cvsBoundingBox[1]  # x_min
    pointLow.y = cvsBoundingBox[1]  # y_min

    boundingBox.high = pointHigh
    boundingBox.low = pointLow

    return boundingBox
