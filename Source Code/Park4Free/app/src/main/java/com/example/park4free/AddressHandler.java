package com.example.park4free;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AddressHandler extends Thread {
    private ArrayList<String> array;
    private Context context;
    private MapsActivity main;


    public AddressHandler(MapsActivity mainMaps, Context context) {
        this.context = context;
        this.main = mainMaps;
    }




    @Override
    public void run() {
        readinText();

    }

    private void readinText() {
        array = new ArrayList<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("Addresses.txt")));
            String line;
            while ((line = reader.readLine()) != null) {

                array.add(line);

            }
            reader.close();
            main.setAddresses(array);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<String> returnArray() {
        return array;}
}
