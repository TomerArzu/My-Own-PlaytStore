package com.example.engineercandidatetask.adpters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.engineercandidatetask.Listeners.StoreItemClickListener;
import com.example.engineercandidatetask.Model.StoreItem;
import com.example.engineercandidatetask.R;

import java.util.List;

public class StoreItemAdapter extends RecyclerView.Adapter<StoreItemAdapter.ItemViewHolder> {

    private List<StoreItem> storeItemList;
    private StoreItemClickListener storeItemClickListener;
    private View itemsView;


    public StoreItemAdapter(List<StoreItem> storeItemList) {
        this.storeItemList = storeItemList;
    }

    @NonNull
    @Override
    // to inflate the item layout and create the holder
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        this.itemsView = inflater.inflate(R.layout.item_layout, parent, false);
//        itemsView.setOnClickListener(storeItemClickListener);

        ItemViewHolder itemViewHolder = new ItemViewHolder(itemsView);
        return itemViewHolder;
    }

    @Override
    // to set the view attributes based on the data
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {
        StoreItem item = storeItemList.get(position);
        ImageView iconImageView = holder.iconImageView;
        iconImageView.setImageBitmap(item.getIconBitmap());
        TextView versionTextView = holder.versionTextView;
        versionTextView.setText(item.getVersion());
        versionTextView.setText(item.getVersion());
        TextView nameTextView = holder.nameTextView;
        nameTextView.setText(item.getAppName());
        TextView descriptionTextView = holder.descriptionTextView;
        descriptionTextView.setText(item.getDescription());
        holder.item =item;
    }

    @Override
    // to determine the number of items
    public int getItemCount() {
        return storeItemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconImageView;
        public TextView versionTextView;
        public TextView nameTextView;
        public TextView descriptionTextView;
        public Button downloadBtn;
        public StoreItem item;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = (ImageView) itemView.findViewById(R.id.icon);
            versionTextView = (TextView) itemView.findViewById(R.id.version);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description);
            downloadBtn = (Button) itemView.findViewById(R.id.downloadBtn);
            itemView.findViewById(R.id.downloadBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    storeItemClickListener.onStoreItemClick(item);
                }
            });
        }

    }

    public void setStoreItemClickListener(StoreItemClickListener storeItemClickListener) {
        this.storeItemClickListener = storeItemClickListener;
    }
}
