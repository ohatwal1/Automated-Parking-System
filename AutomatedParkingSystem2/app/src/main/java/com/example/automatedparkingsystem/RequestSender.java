package com.example.automatedparkingsystem;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestSender extends AsyncTask<String, Void, Void> {
    Socket s;
    DataOutputStream dos;
    PrintWriter pw;

    @Override
    protected Void doInBackground(String... voids) {

        String requestNumber = voids[0];
        String ipAddress = voids[1];
        int portNum = Integer.parseInt(voids[2]);

        Log.d("REQUEST", "request received ----" + requestNumber);
        Log.d("REQUEST", "IP received ----" + requestNumber);
        Log.d("REQUEST", "Port Number received ----" + voids[2]);

        try
        {
            s = new Socket(ipAddress, portNum);
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        try {
                pw = new PrintWriter(s.getOutputStream());
                pw.write(requestNumber);
                pw.flush();
                pw.close();

        }catch(IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
