import sys
from GrpcContract.target.communication_pb2 import PhotoResponse
from itemsStorage import ItemsStorage

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import imageProcessing
import footageStorage as fs

classNames = []
classFile = "../archive/coco.names"
with open(classFile, "rt") as f:
    classNames = f.read().rstrip("\n").split("\n")

def getItemName(classId):
    return classNames[classId - 1]


"""This class handles messages that were sent from the Surveillance Cameras (Camera App)"""
class CameraToCentralSystemServiceService(pb2_grpc.CameraToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def send_footage(self, request, context):
        img_bytes = request.picture

        items, locations = imageProcessing.processImage(imageProcessing.getImageFromBytes(img_bytes))
        print("Items:")
        print(items)
        print("\n")
        print("Locations:")
        print(locations)
        print("\n")

        print("Received footage! Size: ", len(img_bytes))

        for i in range(len(items)):
            item, boundingBox = items[i], locations[i]

        return pb2.FootageAck()

"""This class handles messages that were sent from the SmartPhone App"""
class SmartphoneAppToCentralSystemService(pb2_grpc.SmartphoneAppToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        self.iems_storage = ItemsStorage()

    def locateItem(self, itemID, context): # Returns VideoFootage
        id = itemID.id
        pass

    def photoTaken(self, footage, context): # Returns PhotoResponse
        img_bytes = footage.picture

        items, _ = imageProcessing.processImage(imageProcessing.getImageFromBytes(img_bytes))
        photoResponse = pb2.PhotoResponse() 
        if len(items) == 0:
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.NO_ITEM_FOUND
        elif len(items) > 1:
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.MULTIPLE_ITEMS_FOUND
        elif self.items_storage.has_item(getItemName(items[0])):
            photoResponse.newItemId = ""
            photoResponse.status = pb2.PhotoResponse.ITEM_ALREADY_EXISTS
        else:
            photoResponse.newItemId = getItemName(items[0])
            photoResponse.status = pb2.PhotoResponse.OK
        return photoResponse

    def confirmItemInsertion(self, itemID, context): # Returns Ack
        self.items_storage.insertItem(itemID.id, True, False)
        return pb2.Ack()

    def searchItem(self, searchParameters, context): # Returns SearchResponse
        searchResponse = pb2.SearchResponse()
        searchResponse.searchResults = self.items_storage.get_search_results(searchParameters)
        return searchResponse

    def trackItem(self, itemID, context): # Returns Ack
        self.items_storage.setTracked(itemID.id, True)
        return pb2.Ack()

    def untrackItem(self, itemID, context): # Returns Ack
        self.items_storage.setTracked(itemID.id, False)
        return pb2.Ack()

    def lockItem(self, itemID, context): # Returns Ack
        self.items_storage.setLocked(itemID.id, True)
        return pb2.Ack()

    def unlockItem(self, itemID, context): # Returns Ack
        self.items_storage.setLocked(itemID.id, False)
        return pb2.Ack()

    def removeItem(self, itemID, context): # Returns Ack
        self.items_storage.removeItem(itemID.id)
        return pb2.Ack()

