def processImage(image):
    """Receives an image and returns a tuple containing a list with all identified
    items and a list of their locations"""

    """Look through the image and look for objects in it
    Add all the identified objects to a list and also their locations, the BoundingBox"""

    identifiedItems = []
    locations = []

    classId, confs, boundingBox = net.detect(image, confThreshold=thres)

    """here it needs to look through a dictionary?? of saved footage"""

    if image.confs > 90%:
        identifiedItems.append(image.classID)
        locations.append(image.boundingBox)
    
    else:
        continue
