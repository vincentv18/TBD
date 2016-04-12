package com.example.vincent.tbd;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Vincent on 4/4/16.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    // Constructor
    private List<Item> ItemList;
    private Context mContext; //instance variable
    public ItemAdapter(Context context, List<Item> ItemList) {
        this.ItemList = ItemList;
        this.mContext = context;
    }

    // Holder initializes the views that belong to the items of our RecyclerView
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView imageView;
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
        }
    }

    // Method is called when the custom ViewHolder needs to be initialized
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ItemViewHolder viewHolder = new ItemViewHolder(view);
        return viewHolder;
    }

    // BindViewHolder used to specify the contents of each item of the RecyclerView
    @Override
    public void onBindViewHolder(ItemViewHolder itemViewHolder, final int i) {
        itemViewHolder.textView.setText(ItemList.get(i).title);
        itemViewHolder.imageView.setImageResource(ItemList.get(i).thumbnail);

        itemViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handles when the CardView is clicked
                GifImageView gifImageView = (GifImageView) ((Activity)mContext).findViewById(R.id.gifView);
                try {
                    // Sets GifView using file path
                    GifDrawable gifFromPath = new GifDrawable(ItemList.get(i).path);
                    gifImageView.setImageDrawable(gifFromPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "File could not be found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return ItemList.size();
    }
}
