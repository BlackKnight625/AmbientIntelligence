 
 
 Go to "GrpcContract". 
 Make sure you have, Java, Python and Maven installed. Make sure your default Python environment has the grpc library/package.
 If you're on Windows, double click "createJavaCode.bat" and "createPythonCode.bat".
 If you're on Mac or Linux, open the 2 files with a text editor, copy the content and paste it in a Command Line.
 These scripts will generate the necessary code for the 3 modules to communicate with each-other.

 To launch the Central System:
    1. Make sure you have the following libraries/packages in your Python environment: OpenCV, grpc, numpy. If when launching more libraries/packages are missing, install them.
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