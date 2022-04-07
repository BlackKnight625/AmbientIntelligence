import unittest
import imageProcessing
import footageStorage as fs
import itemsStorage as iS
import imageProcessing as processing
import cv2
import services
import time
import communication_pb2 as pb2
import numpy as np
from os import listdir
import random

testPictureFilename = "../PictureReceivingTest/images/2,56,45.imageBytes"
testPictureFilename2 = "../PictureReceivingTest/images/2,56,46.imageBytes"
testPictureFilename3 = "../PictureReceivingTest/images/2,56,48.imageBytes"
singlePersonFilename = "../PictureReceivingTest/images/person.jpg"
nothingFilename = "../PictureReceivingTest/images/nothing.jpg"
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
    def setUp(self) -> None:
        iS.filename = "data/itemStorageTest.pickle"
        fs.filename = "data/footageStorageTest.pickle"

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


class ServiceTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        services.smartphoneAppConnected = True

    def setUp(self):
        self.cameraService = services.CameraToCentralSystemService()
        self.smartphoneService = services.SmartphoneAppToCentralSystemService()

        iS.filename = "data/itemStorageTest.pickle"
        fs.filename = "data/footageStorageTest.pickle"
        services.footageStorage = fs.FootageStorage()
        services.items_storage = iS.ItemsStorage()
        services.lockedItemsMoved.clear()
        services.lastFootageReceivedTime = time.time()

        self.cameraService.footageReceived = 0

    def addLockedItemMoved(self, itemId):
        services.lockedItemsMoved.append(itemId)

    def insertItem(self, itemId, locked, tracked, image=np.zeros(16), name="name"):
        services.items_storage.insertItem(itemId, locked, tracked, image, name)

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
        self.insertPicture(singlePersonFilename, timestamp1)
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
            self.assertTrue((services.getCv2BoundingBoxFromGrpc(videoFootage.itemBoundingBoxes[i]) == insertedFootage[
                i].boundingBox).all())

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

        self.insertItem(itemId.id, False, True, image = np.ones(16), name = itemId.id)
        self.insertItem("zebra", False, True, name = "zebra")
        self.insertItem("playstation", True, True, name = "playstation")

        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 1)
        self.assertEqual(searchResponse.searchResults[0].itemId.id, itemId.id)
        self.assertEqual(searchResponse.searchResults[0].tracked, True)
        self.assertEqual(searchResponse.searchResults[0].locked, False)
        self.assertTrue((imageProcessing.getImageFromBytes(searchResponse.searchResults[0].image) == np.ones(16)).all())
        self.assertEqual(searchResponse.searchResults[0].name, itemId.id)

    def test_searchAllItems(self):
        searchParameters = pb2.SearchParameters()
        searchParameters.itemName = ""

        self.insertItem("person", False, True, name="person")
        self.insertItem("zebra", False, True, name="zebra")
        self.insertItem("playstation", True, True, name="playstation")

        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 3)

    def test_searchItemsWithFirstLetterZ(self):
        searchParameters = pb2.SearchParameters()
        searchParameters.itemName = "z"

        self.insertItem("person", False, True, name="person")
        self.insertItem("zebra", False, True, name="zebra")
        self.insertItem("playstation", True, True, name="zorro")

        searchResponse = self.smartphoneService.searchItem(searchParameters, None)

        self.assertEqual(len(searchResponse.searchResults), 2)

    def test_statusRequestOK(self):
        statusResponse = next(self.smartphoneService.statusRequest(pb2.StatusRequest(), None))

        self.assertEqual(statusResponse.status, pb2.StatusResponse.OK)
        self.assertTrue(statusResponse.HasField("ok"))

    def test_statusRequestCameraOff(self):
        services.lastFootageReceivedTime -= (services.lastFootageReceivedTimeout + 1)

        statusResponse = next(self.smartphoneService.statusRequest(pb2.StatusRequest(), None))

        self.assertEqual(statusResponse.status, pb2.StatusResponse.CAMERA_TURNED_OFF)
        self.assertTrue(statusResponse.HasField("offCameraInfo"))

    def test_statusRequestLockedItemsMoved(self):
        itemId = pb2.ItemId()
        itemId.id = "person"

        timestamp1 = TimeStamp()
        timestamp2 = TimeStamp()

        timestamp2.seconds += 1

        self.insertItem(itemId.id, True, True, name = itemId.id)  # Inserting locked item
        self.insertPicture(singlePersonFilename, timestamp1)
        self.insertPicture(testPictureFilename3, timestamp2)

        self.assertEqual(len(services.lockedItemsMoved), 1)

        statusResponse = next(self.smartphoneService.statusRequest(pb2.StatusRequest(), None))

        self.assertEqual(statusResponse.status, pb2.StatusResponse.LOCKED_ITEMS_MOVED)
        self.assertTrue(statusResponse.HasField("movedLockedItems"))
        self.assertEqual(len(statusResponse.movedLockedItems.itemNames), 1)
        self.assertEqual(statusResponse.movedLockedItems.itemNames[0], itemId.id)

    def test_photoTakenAndConfirmInsertion(self):
        footage = pb2.Footage()
        timestamp = TimeStamp()

        footage.picture = imageProcessing.getImageBytesFromBytesFile(singlePersonFilename)
        footage.time.CopyFrom(timestamp.toGrpc())

        photoResponse = self.smartphoneService.photoTaken(footage, None)

        self.assertEqual(photoResponse.status, pb2.PhotoResponse.OK)
        self.assertEqual(photoResponse.newItemId.id, "person")

        # Photo sent, photoResponse received. Preparing for confirmItemInsertion

        itemInformation = pb2.ItemInformation()

        itemInformation.itemId.CopyFrom(photoResponse.newItemId)
        itemInformation.tracked = True
        itemInformation.locked = False
        itemInformation.image = imageProcessing.getBytesFromImage(np.zeros(1))  # Doesn't matter. Central system already has the item's image
        itemInformation.name = "jeff"

        self.smartphoneService.confirmItemInsertion(itemInformation, None)

        self.assertEqual(len(services.items_storage.getAllItemsAsList()), 1)
        self.assertTrue(services.items_storage.has_item(itemInformation.itemId.id))

        newInformation = services.items_storage.getAllItemsAsList()[0]

        self.assertEqual(newInformation[1], itemInformation.tracked)
        self.assertEqual(newInformation[2], itemInformation.locked)
        self.assertEqual(newInformation[4], itemInformation.name)

        # The following code lets us visualize the image associated with the inserted item

        """
        img = newInformation[3]

        cv2.namedWindow("output", cv2.WINDOW_NORMAL)  # Create window with freedom of dimensions

        cv2.imshow("Message", img)
        cv2.waitKey(0)
        """
        
    def test_photoTakenNoItemFound(self):
        footage = pb2.Footage()
        timestamp = TimeStamp()

        footage.picture = imageProcessing.getImageBytesFromBytesFile(nothingFilename)
        footage.time.CopyFrom(timestamp.toGrpc())

        photoResponse = self.smartphoneService.photoTaken(footage, None)

        self.assertEqual(photoResponse.status, pb2.PhotoResponse.NO_ITEM_FOUND)

    def test_photoTakenMultipleItemsFound(self):
        footage = pb2.Footage()
        timestamp = TimeStamp()

        footage.picture = imageProcessing.getImageBytesFromBytesFile(testPictureFilename2)
        footage.time.CopyFrom(timestamp.toGrpc())

        photoResponse = self.smartphoneService.photoTaken(footage, None)

        self.assertEqual(photoResponse.status, pb2.PhotoResponse.MULTIPLE_ITEMS_FOUND)

    def test_photoTakenItemAlreadyExists(self):
        footage = pb2.Footage()
        timestamp = TimeStamp()

        self.insertItem("person", False, True)

        footage.picture = imageProcessing.getImageBytesFromBytesFile(singlePersonFilename)
        footage.time.CopyFrom(timestamp.toGrpc())

        photoResponse = self.smartphoneService.photoTaken(footage, None)

        self.assertEqual(photoResponse.status, pb2.PhotoResponse.ITEM_ALREADY_EXISTS)


def get_class_ids():
    classFile = "../archive/coco.names"
    with open(classFile, "rt") as f:
        return f.read().rstrip("\n").split("\n")

def get_images():
    images_folder = "../test/images/"
    image_names = [f for f in listdir(images_folder)]
    images = []
    for img in image_names:
        file = open(images_folder + img, "rb")
        images.append(imageProcessing.getImageFromBytes(file.read()))
        file.close()
    return images

def get_names():
    classFile = "../test/names.txt"
    with open(classFile, "rt") as f:
        return f.read().rstrip("\n").split("\n")

class PopulateStorageTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.classIds = get_class_ids()
        cls.images = get_images()
        cls.names = get_names() 

    def test_populate_storage(self):
        storage = iS.ItemsStorage()
        items = 10
        for _ in range(items):
            name = random.choice(self.names)
            img = random.choice(self.images)
            id = random.choice(self.classIds)
            self.classIds.remove(id)
            lock = random.randint(0,1) == 1 
            track = random.randint(0,1) == 1
            storage.insertItem(id, lock, track, img, name)
        iS.saveItemsStorage(storage)

if __name__ == '__main__':
    unittest.main()
