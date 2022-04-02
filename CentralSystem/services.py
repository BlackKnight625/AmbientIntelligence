import sys

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import imageProcessing
import footageStorage as fs
from footageStorage import footageStorage as storage

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

        return pb2.FootageAck()

"""This class handles messages that were sent from the SmartPhone App"""
class SmartphoneAppToCentralSystemService(pb2_grpc.SmartphoneAppToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def locateItem(self, itemID, context): # Returns 
        id = itemID.id
        pass

    def photoTaken(self, footage, context): # Returns
        pass

    def confirmItemInsertion(self, itemID, context): # Returns
        pass

    def searchItem(self, searchParameters, context): # Returns
        pass

    def trackItem(self, itemID, context): # Returns
        pass

    def untrackItem(self, itemID, context): # Returns
        pass

    def lockItem(self, itemID, context): # Returns
        pass

    def unlockItem(self, itemID, context): # Returns
        pass

    def removeItem(self, itemID, context): # Returns
        pass

