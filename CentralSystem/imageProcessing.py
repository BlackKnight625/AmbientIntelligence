import io
from PIL import Image
import cv2
from cv2 import boundingRect
import numpy as np


def processImage(image):
    """Receives an image and returns a tuple containing a list with all identified
    items and a list of their locations"""

    configPath = "../archive/ssd_mobilenet_v3_large_coco_2020_01_14.pbtxt"
    weightsPath = "../archive/frozen_inference_graph.pb"

    net = cv2.dnn_DetectionModel(weightsPath, configPath)
    net.setInputSize(320, 320)
    net.setInputScale(1.0/127.5)
    net.setInputMean(127.5)
    net.setInputSwapRB(True)

    classIds, _, boundingBox = net.detect(image, confThreshold=0.5)
    return classIds, boundingBox


def getImageFromBytes(bytes):
    return cv2.imdecode(np.frombuffer(bytes, np.uint8), -1)

def getBytesFromImage(image):
    success, bytes = cv2.imencode(".jpg", image)

    if success:
        return bytes.tobytes()

def getImageFromBytesFile(imageFilename):
    imageFileObj = open(imageFilename, "rb")
    imageBinaryBytes = imageFileObj.read()
    imageFileObj.close()

    return getImageFromBytes(imageBinaryBytes)
