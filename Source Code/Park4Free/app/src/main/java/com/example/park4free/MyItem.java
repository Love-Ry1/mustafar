package com.example.park4free;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

//test
public class MyItem implements ClusterItem {

    private LatLng position;
    public String title;
    private String snippet;
    public int parkingIcon;

    public MyItem(double lat, double lng){
        position = new LatLng(lat,lng);
        title = null;
        snippet = null;
    }

    public MyItem(double lat, double lng, String title, String snippet, int profilePhoto){
        position = new LatLng(lat,lng);
        this.title = title;
        this.snippet = snippet;
        this.parkingIcon = profilePhoto;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    /**
     * Set the title of the marker
     * @param title string to be set as title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the description of the marker
     * @param snippet string to be set as snippet
     */
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
