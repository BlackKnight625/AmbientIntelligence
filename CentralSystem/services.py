import sys

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import imageProcessing
import footageStorage as fs


# Global Variables

footageStorage = fs.loadFootageStorage()

# Service implementations

"""This class handles messages that were sent from the Surveillance Cameras (Camera App)"""
class CameraToCentralSystemServiceService(pb2_grpc.CameraToCentralSystemServiceServicer):
    footageReceived = 0

    def __init__(self, *args, **kwargs):
        pass

    def send_footage(self, request, context):
        img_bytes = request.picture
        timestamp = request.time

        image = imageProcessing.getImageFromBytes(img_bytes)
        items, locations = imageProcessing.processImage(image)
        print("Items:")
        print(items)
        print("\n")
        print("Locations:")
        print(locations)
        print("\n")

        print("Received footage! Size: ", len(img_bytes))

        for i in range(len(items)):
            item, boundingBox = items[i], locations[i]
            footageStorage.insertPicture(getItemName(item), image, timestamp, boundingBox)

        self.footageReceived += 1

        if(self.footageReceived % 10):
            # Time to save all footage
            fs.saveFootageStorage(footageStorage)

        return pb2.FootageAck()

"""This class handles messages that were sent from the SmartPhone App"""
class SmartphoneAppToCentralSystemService(pb2_grpc.SmartphoneAppToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def locateItem(self, itemId, context): # Returns VideoFootage
        id = itemId.id
        videoFootageResponse = pb2.VideoFootage()

        lastSeenFootage = footageStorage.getLastSeenFootageAndInformation(itemId)

        for footage in lastSeenFootage:
            pass

        pass

    def photoTaken(self, footage, context): # Returns PhotoResponse
        pass

    def confirmItemInsertion(self, itemID, context): # Returns Ack
        pass

    def searchItem(self, searchParameters, context): # Returns SearchResponse
        pass

    def trackItem(self, itemID, context): # Returns Ack
        pass

    def untrackItem(self, itemID, context): # Returns Ack
        pass

    def lockItem(self, itemID, context): # Returns Ack
        pass

    def unlockItem(self, itemID, context): # Returns Ack
        pass

    def removeItem(self, itemID, context): # Returns Ack
        pass

