import unittest
import imageProcessing
import footageStorage as fs
from footageStorage import footageStorage as storage
import imageProcessing as processing
import cv2

testPictureFilename = "../PictureReceivingTest/images/2,56,45.imageBytes"
simpleItemId = "abc"

class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, True)  # add assertion here

class ImageProcessingTests(unittest.TestCase):
    def test_readImage(self):
        image = processing.getImageFromBytesFile(testPictureFilename)

        cv2.namedWindow("output", cv2.WINDOW_NORMAL)  # Create window with freedom of dimensions
        canva = cv2.resize(image, (960, 540))  # Resize image

        cv2.imshow("Message", canva)
        cv2.waitKey(0)

class TimeStamp:
    year = 2000
    month = 1
    day = 5
    hour = 12
    minutes = 30
    seconds = 20
    
    def __eq__(self, other):
        return isinstance(other, TimeStamp) and other.year == self.year and other.month == self.month and other.day == self.day \
               and other.hour == self.hour and other.minutes == self.minutes and other.seconds == self.seconds

class ImageStorageTests(unittest.TestCase):
    footage = 0

    def create_storage(self):
        self.footage = fs.FootageStorage()

    def insert_simple_image(self, timeStamp = TimeStamp()):
        self.create_storage()

        self.footage.insertPicture(simpleItemId, imageProcessing.getImageFromBytesFile(testPictureFilename), timeStamp, ((1, 2), (2, 3)))

    def test_insert_image_test(self):
        self.create_storage()
        self.insert_simple_image()

        lastFootage = self.footage.getLastSeenFootageAndInformation(simpleItemId)

        self.assertEqual(len(lastFootage), 1)
        self.assertTrue((lastFootage[0][0] == imageProcessing.getImageFromBytesFile(testPictureFilename)).all())
        self.assertEqual(lastFootage[0][1], TimeStamp())
        self.assertEqual(lastFootage[0][2], ((1, 2), (2, 3)))

    def test_insert_images_test(self):
        self.create_storage()

        timeStamp1 = TimeStamp()
        timeStamp2 = TimeStamp()
        timeStamp3 = TimeStamp()
        timeStamp4 = TimeStamp()

        timeStamp1.seconds = 20
        timeStamp2.seconds = 30
        timeStamp3.seconds = 31
        timeStamp4.seconds = 32

        self.insert_simple_image(timeStamp1)
        self.insert_simple_image(timeStamp2)
        self.insert_simple_image(timeStamp3)
        self.insert_simple_image(timeStamp4)

        lastFootage = self.footage.getLastSeenFootageAndInformation(simpleItemId)

        self.assertEqual(len(lastFootage), 3)
        self.assertEqual(lastFootage[0][1], timeStamp2)
        self.assertEqual(lastFootage[1][1], timeStamp3)
        self.assertEqual(lastFootage[2][1], timeStamp4)

if __name__ == '__main__':
    unittest.main()
