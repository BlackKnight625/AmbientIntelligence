import io
from PIL import Image
import cv2
import numpy as np


def processImage(image):
    """Receives an image and returns a tuple containing a list with all identified
    items and a list of their locations"""
    pass

def getImageFromBytes(bytes):
    return cv2.imdecode(np.frombuffer(bytes, np.uint8), -1)

def getImageFromBytesFile(imageFilename):
    imageFileObj = open(imageFilename, "rb")
    imageBinaryBytes = imageFileObj.read()
    imageFileObj.close()

    return getImageFromBytes(imageBinaryBytes)