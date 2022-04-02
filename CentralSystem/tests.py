import unittest
import imageProcessing
import footageStorage as fs
from footageStorage import footageStorage as storage
import imageProcessing as processing
import cv2

class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, True)  # add assertion here

class ImageProcessingTests(unittest.TestCase):
    def test_readImage(self):
        image = processing.getImageFromBytesFile("../PictureReceivingTest/images/2,56,45.imageBytes")

        cv2.namedWindow("output", cv2.WINDOW_NORMAL)  # Create window with freedom of dimensions
        canva = cv2.resize(image, (960, 540))  # Resize image

        cv2.imshow("Message", canva)
        cv2.waitKey(0)

class ImageStorageTests(unittest.TestCase):
    footage = 0

    def create_storage(self):
        self.footage = storage.Footage()

    def inser_simple_image(self):
        self.create_storage()

        self.footage.insertPicture("abc", )


if __name__ == '__main__':
    unittest.main()
