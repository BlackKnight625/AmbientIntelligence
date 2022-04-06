 
 
 Go to "GrpcContract". 
 Make sure you have, Java, Python and Maven installed. Make sure your default Python environment has the grpcio and grpcio-tools libraries/packages.
 If you're on Windows, double click "createJavaCode.bat" and "createPythonCode.bat".
 If you're on Mac or Linux, open the 2 files with a text editor, copy the content and paste it in a Command Line.
 These scripts will generate the necessary code for the 3 modules to communicate with each-other.

 To launch the Central System:
    1. Make sure you have the following libraries/packages in your Python environment: opencv-python, grpcio, grpcio-tools, numpy. If when launching more libraries/packages are missing, install them.
    2. Go to "CentralSystem" using a command line
    3. Run "python main.py"

 To launch the Camera App:
    1. Connect an Android phone to your computer via a USB cable.
    2. Open Android Studio.
    3. Open the project under "CameraApp" using Android Studio.
    4. Run the App on your USB connected phone.
    5. After these steps, the App will remain on your phone, so next time you want to launch it, you don't need to do the previous steps. Clicking on the App on your phone is enough.
    6. Insert the Central System IP and Port.
    7. Put your phone in a place where it can see the area you'll be interacting with

 To launch the Smartphone App (MOMS):
    1. Connect a different Android phone to your computer via a USB cable.
    2. Open Android Studio.
    3. Open the project under "MasterOfMissingStuff" using Android Studio.
    4. Run the App on your USB connected phone.
    5. After these steps, the App will remain on your phone, so next time you want to launch it, you don't need to do the previous steps. Clicking on the App on your phone is enough.
    6. Insert the Central System IP and Port.
    7. Try out the following functionalities:
        7.1. Insert an item to be tracked:
            7.1.1. Click on "Add Item"
            7.1.2. Click on "Add picture"
            7.1.3. Take a picture of an item (such as glasses, scissors, hair brush, tia, mouse). For better results, place the item on a uniformly colored surface.
            7.1.4. Make sure there are no errors. Name the identified item and press "Ok"
            7.1.5. You can now change some options, such as disabling the item from being tracked, and tag it as "locked", so that you get warned if the item moves
        7.2. Locate an item
            7.2.1. Click on "Search for item"
            7.2.2. By default, all added items will show up (even those who are no longer being tracked). Click on "Search for item" text box to search for a specific item
            7.2.3. Type the name that you gave on 7.1.4 to the item photographed in 7.1.3
            7.2.4. Click on the item
            7.2.5. Click on "locate"
            7.2.6. You will see footage of the last 5 seconds showing where the item was last seen.
        7.3. Lock an item
            7.3.1. Click on "Search for item"
            7.3.2. Click on an item in the items list or search for a specific item like in 7.2.2
            7.3.3. Click on "lock" in case the toggle button is turned off
            7.3.4. Move the item that's being surveilled by the camera
            7.3.5. You will receive a notification saying that the locked item has been moved