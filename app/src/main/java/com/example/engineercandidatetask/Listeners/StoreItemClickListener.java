package com.example.engineercandidatetask.Listeners;

import android.view.View;

import com.example.engineercandidatetask.Model.StoreItem;

public interface StoreItemClickListener extends View.OnClickListener {
    void onStoreItemClick(StoreItem item);
}
