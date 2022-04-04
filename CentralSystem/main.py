import sys
sys.path.insert(1, '../GrpcContract/target')

import grpc
from concurrent import futures
import communication_pb2_grpc as pb2_grpc
import communication_pb2 as pb2
import numpy as np
import services




def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    pb2_grpc.add_CameraToCentralSystemServiceServicer_to_server(services.CameraToCentralSystemService(), server)
    pb2_grpc.add_SmartphoneAppToCentralSystemServiceServicer_to_server(services.SmartphoneAppToCentralSystemService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    print("Running Central System")
    serve()