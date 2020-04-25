#include <iostream> 
#include <stdio.h> 
#include <string>
#include <cstring>
#include <ifaddrs.h> 
#include <time.h> 

#include <unistd.h> 
#include <sys/socket.h> 
#include <stdlib.h> 
#include <netinet/in.h> 
#include <arpa/inet.h>
#include <fstream> 
#include <map>
#include <bits/stdc++.h> 
#include <wiringPi.h>

using namespace std;

#define PORT 8080 

int IRSensor_1 = 27;  
int IRSensor_2 = 28;  
int IRSensor_3 = 29;  
int IRSensor_4 = 25;  

int Value;


map<int, char> SensorStatus; 
map<int, char> BookingStatus; 
map<int, time_t> BookingTimeout; 

string ResultBuffer;

void setup()
{

	pinMode(IRSensor_1,INPUT);
	pinMode(IRSensor_2,INPUT);
    pinMode(IRSensor_3,INPUT);
    pinMode(IRSensor_4,INPUT);
}

void InitializeData()
{
    SensorStatus.insert(make_pair(1,'A'));
    SensorStatus.insert(make_pair(2,'A'));
    SensorStatus.insert(make_pair(3,'A'));
    SensorStatus.insert(make_pair(4,'A'));

    BookingStatus.insert(make_pair(1,'A'));
    BookingStatus.insert(make_pair(2,'A'));
    BookingStatus.insert(make_pair(3,'A'));
    BookingStatus.insert(make_pair(4,'A'));
    
    BookingTimeout.insert(make_pair(1,0));
    BookingTimeout.insert(make_pair(2,0));
    BookingTimeout.insert(make_pair(3,0));
    BookingTimeout.insert(make_pair(4,0));
    
}

void ResetSensorData()
{
    SensorStatus[1] = 'A';
    SensorStatus[2] = 'A';
    SensorStatus[3] = 'A';
    SensorStatus[4] = 'A';
}

void GetSensorStatus()
{
    int SlotNo = 0;
    time_t booking_time;
    time_t local_time;
    
    Value = digitalRead(IRSensor_1);
    if(Value == 0)
        SensorStatus[1] = 'B';
    Value = digitalRead(IRSensor_2);
    if(Value == 0)
        SensorStatus[2] = 'B';
    Value = digitalRead(IRSensor_3);
    if(Value == 0)
        SensorStatus[3] = 'B';
    Value = digitalRead(IRSensor_4);
    if(Value == 0)
        SensorStatus[4] = 'B';    
        
    local_time = time(NULL);
    for(SlotNo = 1; SlotNo < 5; SlotNo++)    
    {
        //printf("local_time : %ld \n", local_time);
        //printf("BookingTimeout[SlotNo] : %ld \n", BookingTimeout[SlotNo]);
        
       if((local_time - BookingTimeout[SlotNo]) > 15)
       {
            if((BookingStatus[SlotNo] == 'B') && (SensorStatus[SlotNo] != 'B'))
              BookingStatus[SlotNo] = 'A';
       }
    }
}

void GenerateMessage()
{
    int count = 0;
    
    ResultBuffer.clear();
    ResultBuffer = "Status#";
    for(count = 1; count < 5; count++)
    {
        ResultBuffer += to_string(count);
        ResultBuffer += '|';
        
        if(BookingStatus[count] == 'A' && SensorStatus[count] == 'A')
        {   
            ResultBuffer += 'A';
            cout << "Slot No " << count << " is Available"<< endl;
        }
        else
        {
            ResultBuffer += 'B';
            cout << "Slot No " << count << " is Occupied"<< endl;
        }
        
        if(count < 4)
            ResultBuffer += ',';
    }
    
}

bool MakeBooking(int SlotNo)
{
    GetSensorStatus();
    if(SlotNo > 0 && SlotNo < 5)
    {
        if(BookingStatus[SlotNo] == 'A' && SensorStatus[SlotNo] == 'A')
        {
            BookingStatus[SlotNo] = 'B';
            BookingTimeout[SlotNo] = time(NULL);
            //printf("BookingTimeout[SlotNo] : %ld \n", BookingTimeout[SlotNo]);
            return true;
        }
    }
    return false;
}

int main() 
{
    if(wiringPiSetup()<0){
		cout<<"setup wiring pi failed"<<endl;
		return 1;
	}
    setup();
    
    InitializeData();
    GenerateMessage();
    
    int server_fd, new_socket, server_valread;
    int client_fd;
    struct sockaddr_in serv_addr, cli_addr;
    int opt = 1;
    int serv_addrlen = sizeof (serv_addr);
    
    char buffer[1024] = {0};
    char StatusMsg[1024] = {0};
    
    char* handle; 
    char *Error = new char[1000];
    char *Success = new char[1000];
    
    const char delimiter = '#';
    char *token;
    bool flag = true;
    socklen_t addr_size = sizeof(struct sockaddr_in);
    
    strcpy(Error, "Error#Booking");
    strcpy(Success, "Booking#Success");
    
    // Creating socket file descriptor 
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    if (inet_pton(AF_INET, "192.168.1.129", &cli_addr.sin_addr) <= 0) {
        printf("\nInvalid address/ Address not supported \n");
        return -1;
    }

    // Forcefully attaching socket to the port 8080 
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT,
            &opt, sizeof (opt))) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(PORT);

    // Forcefully attaching socket to the port 8080 
    if (bind(server_fd, (struct sockaddr *) &serv_addr,
            sizeof (serv_addr)) < 0) {
        perror("bind failed");
        exit(EXIT_FAILURE);
    }
    if (listen(server_fd, 3) < 0) {
        perror("listen");
        exit(EXIT_FAILURE);
    }
    
    while (1) {
        
        
        if ((new_socket = accept(server_fd, (struct sockaddr *) &serv_addr,
                (socklen_t*) & serv_addrlen)) < 0) {
            perror("accept");
            exit(EXIT_FAILURE);
        }

        memset(buffer, 0, sizeof (buffer));
        
        getpeername(new_socket, (struct sockaddr*)&cli_addr, &addr_size);
        
        if ((client_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
            printf("\n Socket creation error \n");
            return -1;
        }

        cli_addr.sin_family = AF_INET;
        cli_addr.sin_port = htons(7800);
        
        /*if (inet_pton(AF_INET, "192.168.1.129", &cli_addr.sin_addr) <= 0) {
            printf("\nInvalid address/ Address not supported \n");
            return -1;
        }*/

        if (connect(client_fd, (struct sockaddr *) &cli_addr, sizeof (cli_addr)) < 0) 
        {
            printf("\nConnection Failed \n");
            //return -1;
        }

        server_valread = read(new_socket, buffer, 1024);
        if (server_valread > 0) 
        {
            token = strtok(buffer, "#");
            if(strcmp(token, "Status") == 0)
            {
                cout << endl << endl << "Check Status request received !!" << endl;
                ResetSensorData();
                GetSensorStatus();
                GenerateMessage();
                memset(StatusMsg, 0, sizeof(StatusMsg));
                strcpy(StatusMsg, ResultBuffer.c_str()); 
                
                cout << "Msg Sent : " << StatusMsg << endl;
                
                send(client_fd, StatusMsg, strlen(StatusMsg), 0);
            }
            else if (strcmp(token, "Booking") == 0)
            {
                cout << endl << "Booking request received !!" << endl;
                token = strtok(NULL, "#");
                if(MakeBooking(atoi(token)))
                {
                    send(client_fd, Success, strlen(Success), 0);
                    cout << "Booking successful for Slot No : " << token << endl;
                }
                else
                {
                    send(client_fd, Error, strlen(Error), 0);
                    cout << "Failed to book a slot"  << endl;
                }
            }
            else
                send(client_fd, Error, strlen(Error), 0);
            
            close(client_fd);
        }
    }



    return 0;
} 
