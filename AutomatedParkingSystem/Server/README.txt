Steps to install wiringpi library on RaspberryPi

We have used wiringpi library which lets you access the RaspberryPi GPIO through C/C++ code.
Here is a link which explains all the required steps to install wiringpi.   
http://wiringpi.com/download-and-install/


Steps to Compile and Run the server code:

1. Make sure that both WirelessNetworksProject.cpp and Makefile are present in the same directory
2. Make sure wiringpi library is installed & lib path is set to point to a correct location
3. To compile the code execute the command "make" 
   To clean the object and executable files execute the command "make clean" 
   To run executable file execute the command "make run"  (this command will first compile the CPP file and then it will run the executable file) 
   
   
Brief description of the server code:

1. We have connected the four IR sensors on pins 27,28,29 and 25 resp.
2. There are two types of requests which server can process  a) Status Request b) Booking Request
3. Server code will read values of IR sensors and form a status message packet which will have information about 
   the Slot number and Its availability status.
4. The server will also process the booking request received from the client(Mobile Application). It will check the status of IR sensor connected 
   at the chosen parking slot and it will do the reservation. If the slot is already occupied it will send the "Booking Failure" as the response.
5. Server code creates a new socket for each incoming connection and it will close the socket once the request is fully processed and response 
   is sent back to the client.
    