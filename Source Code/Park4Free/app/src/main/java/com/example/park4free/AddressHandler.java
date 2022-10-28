package com.example.park4free;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Ran as a thread in onCreate to initialize addresses1
 */
public class AddressHandler extends Thread {
    private ArrayList<String> array;
    private Context context;
    private MapsActivity main;

    /**
     * Initializes a adresshandler
     * @param mainMaps MapsActivity containing addresses1 that you want to assign the array created in readinText to
     * @param context Provides access to information about the application state
     */
    public AddressHandler(MapsActivity mainMaps, Context context) {
        this.context = context;
        this.main = mainMaps;
    }

    /**
     * Used to run readInText on start method call in thread with AddressHandler
     */
    @Override
    public void run() {
        readinText();

    }

     // Gets Addresses as strings added to this.array from Addresses.txt in the assets folder
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

    /**
     * Returns this.array
     */
    public ArrayList<String> returnArray() {
        return array;}
}
