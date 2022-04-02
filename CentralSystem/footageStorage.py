import pickle
from os.path import exists as file_exists

maxFootageSeconds = 5
filename = "data/footageStorage.pickle"


def loadFootageStorage():
    if file_exists(filename):
        return pickle.load(open(filename, 'r+b'))
    else:
        return FootageStorage()


def saveFootageStorage():
    pickle.dump(footageStorage, open(filename, 'w+b'))

class FootageStorage:
    # Maps itemIds to lists. Such lists contain tuples, where the 1st element is a picture and the 2nd is a timestamp
    # an the 3rd is a bounding box that captures the item of the corresponding itemId
    map = {}

    def __init__(self):
        pass

    def insertPicture(self, itemId, picture, timestamp, boundingBox):
        if itemId in map:
            # Item already has footage associated with it
            footage = map[itemId]

            # Removing old footage of the item
            self.removeOutdatedPictures(itemId, timestamp)
        else:
            # Item doesn't have footage associated with it
            footage = []
            map[itemId] = footage

        footage.append((picture, timestamp, boundingBox))

    def removeOutdatedPictures(self, itemId, currentTimestamp):
        footage = map[itemId]

        for i in range(len(footage)):
            timestamp = footage[i][1]

            if (isTimeDifferenceTooLarge(currentTimestamp, timestamp)):
                # Time difference between this picture's timestamp and the current timestamp is over the threshold
                del footage[i]

    def getLastSeenFootageAndInformation(self, itemId):
        """Returns a list of tuples, whose 1st element is a picture, 2nd element is the picture's timestamp,
        and 3rd element is a bounding box that captures the item associated with the corresponding itemID"""

        if itemId in map:
            return map[itemId]
        else:
            return []


footageStorage = loadFootageStorage()

def isTimeDifferenceTooLarge(timestamp1, timestamp2):
    if timestamp1.year != timestamp2.year or timestamp1.month != timestamp2.month or \
            timestamp1.day != timestamp2.day or timestamp1.hour != timestamp2.hour:
        return True
    else:
        # Only minutes or seconds are different
        seconds = abs(timestamp1.minutes - timestamp2.minutes) * 60 + abs(timestamp1.seconds - timestamp2.seconds)

        return seconds > maxFootageSeconds
