import sys

sys.path.insert(1, '../GrpcContract/target')

import communication_pb2 as pb2

lock = "locked"
track = "tracked"

def loadItemsStorage():
    pass

def saveItemsStorage():
    pass

class ItemsStorage:
    def __init__(self):
        self.dic = {}

    def inserItem(self, itemId, locked, tracked):
        #if itemId in self.dic:
        #    raise Exception(pb2.PhotoResponse.ITEM_ALREADY_EXISTS)
        self.dic[itemId] = {}
        self.dic[itemId][track] = tracked
        self.dic[itemId][lock] = locked

    def removeItem(self, itemId):
        del self.dic[itemId]

    def isTracked(self, itemId):
        return self.dic[itemId][track]

    def isLocked(self, itemId):
        return self.dic[itemId][lock]

    def setTracked(self, itemId, locked):
        self.dic[itemId][track] = locked

    def setLocked(self, itemId, locked):
        self.dic[itemId][lock] = locked

    def getAllItemsAsList(self):
        res = []
        for i in self.dic:
            item = [i] + list(self.dic[i].values())
            res.append(item)
        return res

    def has_item(self, itemId):
        return itemId in self.dic

    def get_search_results(self, searchParameters):
        res = []
        items = [item.find(searchParameters) for item in list(self.dic.keys())]
        ix = 0
        for i in self.dic:
            if items[ix] == 0:
                item = [i] + list(self.dic[i].values())
                res.append(item)
            ix += 1
        return res