import sys
import pickle
import os
from os.path import exists as file_exists

sys.path.insert(1, '../GrpcContract/target')

lock = "locked"
track = "tracked"
image = "image"
name = "name"
filename = "data/itemStorage.pickle"

def loadItemsStorage():
    if file_exists(filename):
        file = open(filename, 'rb')
        footage = pickle.load(file)
        file.close()

        return footage
    else:
        return ItemsStorage()

def saveItemsStorage(itemsStorage):
    os.makedirs("data", exist_ok=True)
    file = open(filename, 'wb')
    pickle.dump(itemsStorage, file)
    file.close()

class ItemsStorage:
    def __init__(self):
        self.dic = {}

    def insertItem(self, itemId, locked: bool, tracked: bool, img, itemName: str):
        #if itemId in self.dic:
        #    raise Exception(pb2.PhotoResponse.ITEM_ALREADY_EXISTS)
        self.dic[itemId] = {}
        self.dic[itemId][track] = tracked
        self.dic[itemId][lock] = locked
        self.dic[itemId][image] = img
        self.dic[itemId][name] = itemName

    def removeItem(self, itemId):
        del self.dic[itemId]

    def isTracked(self, itemId):
        return self.dic[itemId][track]

    def isLocked(self, itemId):
        return self.dic[itemId][lock]

    def setTracked(self, itemId, tracked):
        self.dic[itemId][track] = tracked

    def setLocked(self, itemId, locked):
        self.dic[itemId][lock] = locked

    def getAllItemsAsList(self):
        res = []
        for i in self.dic:
            item = [i] + [self.dic[i][lock]] + [self.dic[i][track]] + [self.dic[i][image]] + [self.dic[i][name]]
            res.append(item)
        return res

    def getName(self, itemId):
        return self.dic[itemId][name]

    def has_item(self, itemId):
        return itemId in self.dic

    def get_search_results(self, searchParameters: str):
        searchParameters = searchParameters.lower()
        res = []
        items = [self.dic[item][name].lower().find(searchParameters) for item in self.dic]
        ix = 0
        for i in self.dic:
            if items[ix] == 0:
                item = [i] + [self.dic[i][lock]] + [self.dic[i][track]] + [self.dic[i][image]] + [self.dic[i][name]]
                res.append(item)
            ix += 1
        return res