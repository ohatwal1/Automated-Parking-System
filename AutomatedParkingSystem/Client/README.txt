Steps to Compile and Run the Android application code:

1. Import the Automated Parking System project in Android Studio.
2. Once import is complete select android device and run MainActivity in Android Studio
3. You can also create APK file and install it on device. For better features/results use actual device 
   instead of the simulator, since simulator does not have its own ip address.
  
   
   
Brief description of the client code:

1. GET - GET buuton is used to get the current IP address of the device.
2. IP Address - Enter the IP address of the server to connect (used for data transmission) 
3. PORT - Enter the PORT number where server is listening (used for data transmission)    
4. CONNECT - Once you enter valid IP and PORT client will connect to Server and request for 
   Status of the available slot
5. Parking Slots - Based on the availability of the slotes Button 1,2,3,4 will be enabled or disabled 
6. Based on available slot select the parking slot number and enter REQUEST
7. REQUEST - This button is used to make the reversation of the slot, once you request slot response will   
   will be displayed below.
8.REFRESH - This button is used to update the available parking slots
