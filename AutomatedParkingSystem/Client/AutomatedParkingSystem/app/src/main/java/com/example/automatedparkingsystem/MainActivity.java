package com.example.automatedparkingsystem;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    Enter Server IP and Port
    EditText ipText;
    EditText portText;

    //    Parking Numbers;
    private Button btnOne;
    private Button btnTwo;
    private Button btnThree;
    private Button btnFour;
    private Button buttonRequest;

    //    Get IP Address of Device
    Button obtainBtn;
    TextView obtainIP;

    //    Response Text
    TextView responseText;


    final Handler handler = new Handler();
    private boolean end = false;

    //    String Declaration;
    String ipAddressG;
    String passBtnValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Set Variables for IP and Port
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);

//        Set Variables for Request and Response
        buttonRequest = findViewById(R.id.requestBtn);
        responseText = findViewById(R.id.responseText);


//        Parking Slot buttons
        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);
        btnThree = findViewById(R.id.btnThree);
        btnFour = findViewById(R.id.btnFour);

//          Obtain IP field and button
        obtainIP = findViewById(R.id.obtainIpText);
        obtainBtn = findViewById(R.id.obtainBtn);

        Log.d("DEBUG", "onCreate: ");

//        Set OnclickListener for Start and Stop Button
        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);

//        Set new OnclickListener for Obtain IP Button
        obtainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                netWordDetect();
            }
        });
    }

    //Check the internet connection.
    private void netWordDetect() {

        boolean WIFI = false;
        boolean MOBILE = false;
        ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = new NetworkInfo[0];
        if (CM != null) {
            networkInfo = CM.getAllNetworkInfo();
        }

        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    WIFI = true;

            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    MOBILE = true;
        }

        if (WIFI) {
            ipAddressG = getDeviceIpWiFiData();
            obtainIP.setText(ipAddressG);
        }

        if (MOBILE) {
            ipAddressG = getDeviceIpMobileData();
            obtainIP.setText(ipAddressG);
        }

    }

    public String getDeviceIpMobileData() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }

    public String getDeviceIpWiFiData() {

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    private void startServerSocket() {

        Thread thread = new Thread(new Runnable() {

            private String stringData = null;

            @Override
            public void run() {

                try {
                    ServerSocket ss = new ServerSocket(7800);
                    Log.d("DEBUG", "PORT OPEN BHAU");

                    while (!end) {
                        //Server is waiting for client here, if needed
                        Socket s = ss.accept();
                        Log.d("DEBUG", "Waiting for Accept BHAU");
                        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        PrintWriter output = new PrintWriter(s.getOutputStream());

                        stringData = input.readLine();
                        Log.d("DEBUG", "Bagh kahi aala ahe ka?");
                        Log.d("DEBUG", stringData);
                        output.println("FROM SERVER - " + stringData.toUpperCase());
                        output.flush();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        upDateInputDate(stringData);

//                        updateUI(stringData);
                        if (stringData.equalsIgnoreCase("STOP")) {
                            end = true;
                            output.close();
                            s.close();
                            break;
                        }

                        output.close();
                        s.close();
                    }
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void upDateInputDate(String stringData) {
        Log.d("In Update method", stringData);
        String[] data = stringData.split("#");
        if (data[0].contains("Status") && !data[1].contains("NA")) {
            String[] slots = data[1].split(",");
            ArrayList<String> available = new ArrayList<>();
            for (String slot : slots) {
                if (slot.contains("A")) {
                    String[] slotNumbers = slot.split("|");
                    String slotNumber = slotNumbers[1];
                    available.add(slotNumber);
                }
            }
            String listString = "";
            for (String s : available) {
                listString += s + "\t";
            }
            Log.d("DEBUG ", available.toString());
            updateUI(listString);
        } else if (data[0].equals("Booking")) {
            String[] bookingData = stringData.split("#");
            String resultBookingData = bookingData[1];
            updateUI(resultBookingData);
            Log.d("Booking", resultBookingData);
        } else if (data[0].equals("Error")) {
            String[] errorData = stringData.split("#");
            String resultBookingData = errorData[1];
            updateUI(resultBookingData);
            Log.d("Error", resultBookingData);
        }
    }

    private void updateUI(final String stringData) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                String s = "";
                if (stringData.equals("Success"))
                {
                    responseText.setText("Booking : " + stringData);
                    responseText.setTextColor(getResources().getColor(R.color.green));

                } if (stringData.equals("Fail")) {
                    responseText.setText("Booking : " + stringData);
                    responseText.setTextColor(getResources().getColor(R.color.red));
//                    resultText.setText(s);
                }

                if (stringData.equals("Status"))
                {
                    responseText.setText("Error : " + stringData);
                    responseText.setTextColor(getResources().getColor(R.color.green));

                } if (stringData.equals("Booking")) {
                    responseText.setText("Error : " + stringData);
                    responseText.setTextColor(getResources().getColor(R.color.red));
                }


                if ((stringData.contains("3"))) {
                    btnThree.setEnabled(true);
                    responseText.setText(s);
                } else {
                    btnThree.setEnabled(false);
                }

                if ((stringData.contains("4"))) {
                    btnFour.setEnabled(true);
                    responseText.setText(s);
                } else {
                    btnFour.setEnabled(false);
                }
                if ((stringData.contains("1"))) {
                    btnOne.setEnabled(true);
                    responseText.setText(s);
                } else {

                    btnOne.setEnabled(false);
                }
                if ((stringData.contains("2"))) {
                    btnTwo.setEnabled(true);
                    responseText.setText(s);
                } else {
                    btnTwo.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

//                Select 1st Parking slot;
            case R.id.btnOne:

                btnOne.setEnabled(false);
                Log.d("DEBUG", "value send ----" + btnOne.getText().toString());
                getDataFromButton(btnOne.getText().toString());
                break;

//                Select 2nd Parking slot;
            case R.id.btnTwo:

                btnTwo.setEnabled(false);
                Log.d("DEBUG", "value send ----" + btnTwo.getText().toString());
                getDataFromButton(btnTwo.getText().toString());
                break;

//                Select 3rd Parking slot;
            case R.id.btnThree:

                btnThree.setEnabled(false);
                Log.d("DEBUG", "value send ----" + btnThree.getText().toString());
                getDataFromButton(btnThree.getText().toString());
                break;

//                Select 4th Parking slot;
            case R.id.btnFour:

                btnFour.setEnabled(false);
                Log.d("DEBUG", "value send ----" + btnFour.getText().toString());
                getDataFromButton(btnFour.getText().toString());
                break;
        }

    }

//    Set Parking slot to pass
    public void getDataFromButton(String btnValue) {
        passBtnValue = "";
        passBtnValue = btnValue;
        Log.d("DEBUG", "value received  ----" + passBtnValue);
    }


    public void send(View v) {

        responseText.setText("");
        MessageSender messageSender = new MessageSender();
        startServerSocket();
        messageSender.execute("Status#NA", ipText.getText().toString(), portText.getText().toString());
    }

    public void requestBooking(View requestView) {

        responseText.setText("");
        RequestSender requestSender = new RequestSender();

        Log.d("DEBUG", "request to send ----" + passBtnValue);
        Log.d("DEBUG", "ip to send ----" + ipText.getText().toString());
        Log.d("DEBUG", "port to send ----" + portText.getText().toString());

        requestSender.execute("Booking#" + passBtnValue, ipText.getText().toString(), portText.getText().toString());

    }

}
