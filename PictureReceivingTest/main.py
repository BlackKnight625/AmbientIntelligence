import sys
sys.path.insert(1, '../GrpcContract/target')

import grpc
from concurrent import futures
import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import cv2
import numpy as np
import os

class CameraToCentralSystemServiceService(pb2_grpc.CameraToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def send_footage(self, footage, context):
        img_bytes = footage.picture
        timeStamp = footage.time
        img = cv2.imdecode(np.frombuffer(img_bytes, np.uint8), -1)

        cv2.namedWindow("output", cv2.WINDOW_NORMAL)    # Create window with freedom of dimensions
        canva = cv2.resize(img, (960, 540))                # Resize image

        cv2.imshow("Message", canva)
        cv2.waitKey(1)

        print("Received footage! Size: ", len(img_bytes), ", Timestamp: ", timeStamp.minutes, "m ", timeStamp.seconds, "s")

        imageFileName = 'images/' + str(timeStamp.hour) + "," + str(timeStamp.minutes) + "," + str(timeStamp.seconds) + '.imageBytes'

        os.makedirs(os.path.dirname(imageFileName), exist_ok=True) #Creating directories for the file

        with open(imageFileName, 'wb') as file:
            file.write(img_bytes)

        return pb2.FootageAck()

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_CameraToCentralSystemServiceServicer_to_server(CameraToCentralSystemServiceService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    print("Running Central System")
    serve()