package com.example.park4free;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    public List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int length;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.text_row_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String address = mData.get(position);
        holder.myTextView.setText(address);
    }

    // total number of rows

    /**
     * Gets the total number of rows
     * @return the size of mData
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Sets the length to the input filterlist and assigns the input filterlist to mData
     * @param filteredList the list that will be assigned to mData
     */
    public void setItemModels(List<String> filteredList){
        length = filteredList.size();
        this.mData = (ArrayList<String>) filteredList;
        notifyDataSetChanged();
    }

    /**
     * Returns mData
     * @return mData to be returned
     */
    public List<String> getmData() {return mData;}


    /**
     * stores and recycles views as they are scrolled off screen
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        /**
         * Used to initialize a ViewHolderObject
         * @param itemView the inflated view created in onCreateViewHolder
         */
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.addressName);
            itemView.setOnClickListener(this);
        }

        /**
         * Performs onItemClick if possible
         * @param view the inflated view
         */
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    //dubbel kolla
    /**
     * Gets item at a given index in mData
     * @param id
     * @return
     */
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught

    /**
     * Assigns itemClickListener to mClickListener, allows clicks events to be caught
     * @param itemClickListener
     */
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events

    /**
     * Interface for implementing onItemClick
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }






}