import unittest
import imageProcessing
import footageStorage as fs
import itemsStorage as iS
import imageProcessing as processing
import cv2
import services
import time
import communication_pb2 as pb2

testPictureFilename = "../PictureReceivingTest/images/2,56,45.imageBytes"
testPictureFilename2 = "../PictureReceivingTest/images/2,56,46.imageBytes"
testPictureFilename3 = "../PictureReceivingTest/images/2,56,48.imageBytes"
testPictureFilename4 = "../PictureReceivingTest/images/person.jpg"
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
        return isinstance(other,
                          TimeStamp) and other.year == self.year and other.month == self.month and other.day == self.day \
               and other.hour == self.hour and other.minutes == self.minutes and other.seconds == self.seconds

    def __str__(self):
        return str(self.hour) + "h" + str(self.minutes) + "m" + str(self.seconds) + "s"

    def __repr__(self):
        return self.__str__()

    def toGrpc(self):
        timestamp = pb2.Timestamp()

        timestamp.year = self.year
        timestamp.month = self.month
        timestamp.day = self.day
        timestamp.hour = self.hour
        timestamp.minutes = self.minutes
        timestamp.seconds = self.seconds

        return timestamp

class ImageStorageTests(unittest.TestCase):
    def create_storage(self):
        self.footage = fs.FootageStorage()

    def insert_simple_image(self, timeStamp=TimeStamp()):
        self.footage.insertPicture(simpleItemId, imageProcessing.getImageFromBytesFile(testPictureFilename), timeStamp,
                                   ((1, 2), (2, 3)))

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


class ServiceTests(unittest.TestCase):
    def setUp(self):
        self.cameraService = services.CameraToCentralSystemService()
        self.smartphoneService = services.SmartphoneAppToCentralSystemService()

        iS.filename = "data/itemStorageTest.pickle"
        fs.filename = "data/footageStorage.pickle"
        services.footageStorage = fs.FootageStorage()
        services.items_storage = iS.ItemsStorage()
        services.lockedItemsMoved.clear()
        services.lastFootageReceivedTime = time.time()

        self.cameraService.footageReceived = 0

    def addLockedItemMoved(self, itemId):
        services.lockedItemsMoved.append(itemId)

    def insertItem(self, itemId, locked, tracked):
        services.items_storage.inserItem(itemId, locked, tracked)

    def insertPicture(self, imageFilename, timestamp=TimeStamp()):
        footage = pb2.Footage()

        footage.picture = imageProcessing.getImageBytesFromBytesFile(imageFilename)
        footage.time.CopyFrom(timestamp.toGrpc())

        self.cameraService.send_footage(footage, None)

    def test_sendCameraPicture(self):
        self.insertPicture(testPictureFilename2)

        self.assertEqual(self.cameraService.footageReceived, 1)

    def test_sendCameraPictureWithTrackedItem(self):
        self.insertItem("person", False, True)
        self.insertPicture(testPictureFilename2)

        self.assertEqual(self.cameraService.footageReceived, 1)
        self.assertTrue(services.items_storage.has_item("person"))
        self.assertEqual(len(services.footageStorage.getLastSeenFootageAndInformation("person")), 1)

    def test_sendCameraTwoPicturesWithTrackedItem(self):
        self.insertItem("person", False, True)
        self.insertPicture(testPictureFilename2)
        self.insertPicture(testPictureFilename3)

        self.assertEqual(self.cameraService.footageReceived, 2)
        self.assertTrue(services.items_storage.has_item("person"))
        self.assertEqual(len(services.footageStorage.getLastSeenFootageAndInformation("person")), 2)

    def test_trackItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        self.insertItem(itemId.id, False, False)

        self.smartphoneService.trackItem(itemId, None)

        self.assertTrue(services.items_storage.has_item(itemId.id))
        self.assertTrue(services.items_storage.isTracked(itemId.id))
        self.assertTrue(not services.items_storage.isLocked(itemId.id))

    def test_untrackItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        self.insertItem(itemId.id, False, True)

        self.smartphoneService.untrackItem(itemId, None)

        self.assertTrue(services.items_storage.has_item(itemId.id))
        self.assertTrue(not services.items_storage.isTracked(itemId.id))
        self.assertTrue(not services.items_storage.isLocked(itemId.id))

    def test_lockItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        self.insertItem(itemId.id, False, True)

        self.smartphoneService.lockItem(itemId, None)

        self.assertTrue(services.items_storage.has_item(itemId.id))
        self.assertTrue(services.items_storage.isTracked(itemId.id))
        self.assertTrue(services.items_storage.isLocked(itemId.id))

    def test_unlockItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        self.insertItem(itemId.id, True, True)

        self.smartphoneService.unlockItem(itemId, None)

        self.assertTrue(services.items_storage.has_item(itemId.id))
        self.assertTrue(services.items_storage.isTracked(itemId.id))
        self.assertTrue(not services.items_storage.isLocked(itemId.id))

    def test_removeItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        self.insertItem(itemId.id, False, True)
        self.insertPicture(testPictureFilename2)
        
        self.smartphoneService.removeItem(itemId, None)

        self.assertTrue(not services.items_storage.has_item(itemId.id))
        self.assertEqual(services.footageStorage.getLastSeenFootageAndInformation(itemId.id), None)

    def test_locateItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        timestamp1 = TimeStamp()
        timestamp2 = TimeStamp()

        timestamp2.seconds += 1
        timestamps = [timestamp1, timestamp2]

        self.insertItem(itemId.id, False, True)
        self.insertPicture(testPictureFilename4, timestamp1)
        self.insertPicture(testPictureFilename3, timestamp2)

        insertedFootage = services.footageStorage.getLastSeenFootageAndInformation(itemId.id)

        videoFootage = self.smartphoneService.locateItem(itemId, None)

        self.assertEqual(len(videoFootage.pictures), 2)
        self.assertEqual(len(videoFootage.itemBoundingBoxes), 2)

        for i in range(2):
            # Checking if timestamps are the same
            self.assertEqual(videoFootage.pictures[i].time.year, timestamps[i].year)
            self.assertEqual(videoFootage.pictures[i].time.month, timestamps[i].month)
            self.assertEqual(videoFootage.pictures[i].time.day, timestamps[i].day)
            self.assertEqual(videoFootage.pictures[i].time.hour, timestamps[i].hour)
            self.assertEqual(videoFootage.pictures[i].time.minutes, timestamps[i].minutes)
            self.assertEqual(videoFootage.pictures[i].time.seconds, timestamps[i].seconds)

            # Checking if the bounding boxes are the same
            self.assertTrue((services.getCv2BoundingBoxFromGrpc(videoFootage.itemBoundingBoxes[i]) == insertedFootage[i].boundingBox).all())

        # The following code lets us visualize the first image with the bounding box around the identified person

        """img = imageProcessing.getImageFromBytes(videoFootage.pictures[0].picture)

        cv2.namedWindow("output", cv2.WINDOW_NORMAL)  # Create window with freedom of dimensions
        cv2.rectangle(img, insertedFootage[0].boundingBox, color=(0, 255, 0), thickness=2)
        canva = cv2.resize(img, (960, 540))  # Resize image

        print("Inserted picture's bounding box: ", insertedFootage[0].boundingBox)


        cv2.imshow("Message", canva)
        cv2.waitKey(0)"""

    def test_searchItem(self):
        itemId = pb2.ItemId()
        itemId.id = "person"
        searchParameters = pb2.SearchParameters()
        searchParameters.itemName = itemId.id
        
        self.insertItem(itemId.id, False, True)
        self.insertItem("zebra", False, True)
        self.insertItem("playstation", True, True)
        
        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 1)
        self.assertEqual(searchResponse.searchResults[0].itemId.id, itemId.id)
        self.assertEqual(searchResponse.searchResults[0].tracked, True)
        self.assertEqual(searchResponse.searchResults[0].locked, False)

    def test_searchAllItems(self):
        searchParameters = pb2.SearchParameters()
        searchParameters.itemName = ""

        self.insertItem("person", False, True)
        self.insertItem("zebra", False, True)
        self.insertItem("playstation", True, True)

        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 3)

    def test_searchItemsWithFirstLetterZ(self):
        searchParameters = pb2.SearchParameters()
        searchParameters.itemName = "z"

        self.insertItem("person", False, True)
        self.insertItem("zebra", False, True)
        self.insertItem("zorro", True, True)

        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 2)

    def test_statusRequestOK(self):
        statusResponse = self.smartphoneService.statusRequest(pb2.StatusRequest(), None)

        self.assertEqual(statusResponse.status, pb2.StatusResponse.OK)
        self.assertTrue(statusResponse.HasField("ok"))

    def test_statusRequestCameraOff(self):
        services.lastFootageReceivedTime -= (services.lastFootageReceivedTimeout + 1)

        statusResponse = self.smartphoneService.statusRequest(pb2.StatusRequest(), None)

        self.assertEqual(statusResponse.status, pb2.StatusResponse.CAMERA_TURNED_OFF)
        self.assertTrue(statusResponse.HasField("offCameraInfo"))

    def test_statusRequestLockedItemsMoved(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        timestamp1 = TimeStamp()
        timestamp2 = TimeStamp()

        timestamp2.seconds += 1

        self.insertItem(itemId.id, True, True) # Inserting locked item
        self.insertPicture(testPictureFilename4, timestamp1)
        self.insertPicture(testPictureFilename3, timestamp2)

        self.assertEqual(len(services.lockedItemsMoved), 1)

        statusResponse = self.smartphoneService.statusRequest(pb2.StatusRequest(), None)

        self.assertEqual(statusResponse.status, pb2.StatusResponse.LOCKED_ITEMS_MOVED)
        self.assertTrue(statusResponse.HasField("movedLockedItems"))
        self.assertEqual(len(statusResponse.movedLockedItems.items), 1)
        self.assertEqual(statusResponse.movedLockedItems.items[0].id, itemId.id)

if __name__ == '__main__':
    unittest.main()
