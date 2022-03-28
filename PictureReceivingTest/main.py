import sys
sys.path.insert(1, '../GrpcContract/target')

import grpc
from concurrent import futures
import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import cv2
import numpy as np

class CameraToCentralSystemServiceService(pb2_grpc.CameraToCentralSystemServiceServicer):
    def __init__(self, *args, **kwargs):
        pass

    def send_footage(self, request, context):
        img_bytes = request.picture
        ts = request.time
        img = cv2.imdecode(np.frombuffer(img_bytes, np.uint8), -1)

        cv2.imshow("Message", img)
        cv2.waitKey(1)

        #print("Received footage! Size: ", len(img_bytes))

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