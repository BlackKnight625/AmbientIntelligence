import unittest
import imageProcessing
import footageStorage as fs
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

    def __init__(self):
        self.year = 2000
        self.month = 1
        self.day = 5
        self.hour = 12
        self.minutes = 30
        self.seconds = 20
    
    def __eq__(self, other):
        return isinstance(other, TimeStamp) and other.year == self.year and other.month == self.month and other.day == self.day \
               and other.hour == self.hour and other.minutes == self.minutes and other.seconds == self.seconds

    def __str__(self):
        return str(self.hour) + "h" + str(self.minutes) + "m" + str(self.seconds) + "s"

    def __repr__(self):
        return self.__str__()

class ImageStorageTests(unittest.TestCase):
    def create_storage(self):
        self.footage = fs.FootageStorage()

    def insert_simple_image(self, timeStamp = TimeStamp()):
        self.footage.insertPicture(simpleItemId, imageProcessing.getImageFromBytesFile(testPictureFilename), timeStamp, ((1, 2), (2, 3)))

    def test_insert_image_test(self):
        self.create_storage()
        self.insert_simple_image()

        lastFootage = self.footage.getLastSeenFootageAndInformation(simpleItemId)

        self.assertEqual(len(lastFootage), 1)
        self.assertTrue((lastFootage[0].picture == imageProcessing.getImageFromBytesFile(testPictureFilename)).all())
        self.assertEqual(lastFootage[0].timestamp, TimeStamp())
        self.assertEqual(lastFootage[0].boundingBox, ((1, 2), (2, 3)))

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
        self.assertEqual(lastFootage[0].timestamp, timeStamp2)
        self.assertEqual(lastFootage[1].timestamp, timeStamp3)
        self.assertEqual(lastFootage[2].timestamp, timeStamp4)

    def test_save_and_load(self):
        self.create_storage()
        self.insert_simple_image()

        fs.saveFootageStorage(self.footage)

        otherFootage = fs.loadFootageStorage().getLastSeenFootageAndInformation(simpleItemId)
        lastFootage = self.footage.getLastSeenFootageAndInformation(simpleItemId)

        self.assertTrue((lastFootage[0].picture == otherFootage[0].picture).all())
        self.assertEqual(lastFootage[0].timestamp, otherFootage[0].timestamp)
        self.assertEqual(lastFootage[0].boundingBox, otherFootage[0].boundingBox)

    def test_print_saved_info(self):
        lastFootage = fs.loadFootageStorage().getLastSeenFootageAndInformation(simpleItemId)

        print("Image: ", lastFootage[0].picture)
        print("Timestamp: ", lastFootage[0].timestamp)
        print("BoundingBox: ", lastFootage[0].boundingBox)

if __name__ == '__main__':
    unittest.main()
