import pickle
import os
from os.path import exists as file_exists
from typing import Optional

maxFootageSeconds = 5
filename = "data/footageStorage.pickle"


def loadFootageStorage():
    if file_exists(filename):
        file = open(filename, 'rb')
        footage = pickle.load(file)
        file.close()

        return footage
    else:
        return FootageStorage()


def saveFootageStorage(footageStorage):
    os.makedirs("data", exist_ok=True)
    file = open(filename, 'wb')
    pickle.dump(footageStorage, file)
    file.close()

class Footage:

    def __init__(self, picture, timestamp, boundingBox):
        self.picture = picture
        self.timestamp = timestamp
        self.boundingBox = boundingBox

class FootageStorage:
    def __init__(self):
        pass
        # Maps itemIds to lists. Such lists contain footage, which holds a picture, a timestamp
        # and a bounding box that captures the item of the corresponding itemId
        self.map = {}

    def insertPicture(self, itemId, picture, timestamp, boundingBox):
        if itemId in self.map:
            # Item already has footage associated with it
            footage = self.map[itemId]

            # Removing old footage of the item
            self.removeOutdatedPictures(itemId, timestamp)
        else:
            # Item doesn't have footage associated with it
            footage = []
            self.map[itemId] = footage

        footage.append(Footage(picture, timestamp, boundingBox))

    def removeData(self, itemId):
        if itemId in self.map:
            del self.map[itemId]

    def removeOutdatedPictures(self, itemId, currentTimestamp):
        footage = self.map[itemId]

        for i in range(len(footage) - 1, 0, -1):
            timestamp = footage[i].timestamp

            if (isTimeDifferenceTooLarge(currentTimestamp, timestamp)):
                # Time difference between this picture's timestamp and the current timestamp is over the threshold
                del footage[i]

    def getLastSeenFootageAndInformation(self, itemId) -> Optional[list[Footage]]:
        """Returns a list of tuples, whose 1st element is a picture, 2nd element is the picture's timestamp,
        and 3rd element is a bounding box that captures the item associated with the corresponding itemID"""

        if itemId in self.map:
            return self.map[itemId]
        else:
            return None

def isTimeDifferenceTooLarge(timestamp1, timestamp2) -> bool:
    if timestamp1.year != timestamp2.year or timestamp1.month != timestamp2.month or \
            timestamp1.day != timestamp2.day or timestamp1.hour != timestamp2.hour:
        return True
    else:
        # Only minutes or seconds are different
        seconds = abs(timestamp1.minutes - timestamp2.minutes) * 60 + abs(timestamp1.seconds - timestamp2.seconds)

        return seconds > maxFootageSeconds
