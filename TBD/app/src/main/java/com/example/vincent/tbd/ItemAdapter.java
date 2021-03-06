package com.example.vincent.tbd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

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
        itemViewHolder.imageView.setImageBitmap(ItemList.get(i).thumbnail);

        itemViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handles when the CardView is clicked
                GifImageView gifImageView = (GifImageView) ((Activity) mContext).findViewById(R.id.gifView);
                try {
                    if (ItemList.get(i).path.equals("_NONE_")) {
                        if (ItemList.get(i).title.equals("Cleat Hitch")) {
                            GifDrawable gifFromResource = new GifDrawable(mContext.getResources(), R.drawable.cleat);
                            gifImageView.setImageDrawable(gifFromResource);
                            ((HomeActivity) mContext).sendMsg(ItemList.get(i).title);
                        } else if (ItemList.get(i).title.equals("Sit Down")) {
                            GifDrawable gifFromResource = new GifDrawable(mContext.getResources(), R.drawable.sit);
                            gifImageView.setImageDrawable(gifFromResource);
                            ((HomeActivity) mContext).sendMsg(ItemList.get(i).title);
                        } else if (ItemList.get(i).title.equals("Stop Engine")) {
                            GifDrawable gifFromResource = new GifDrawable(mContext.getResources(), R.drawable.engine);
                            gifImageView.setImageDrawable(gifFromResource);
                            ((HomeActivity) mContext).sendMsg(ItemList.get(i).title);
                        }
                    } else {
                        // Sets GifView using file path
                        GifDrawable gifFromPath = new GifDrawable(ItemList.get(i).path);
                        gifImageView.setImageDrawable(gifFromPath);

                        // Send to client
                        ((HomeActivity) mContext).sendMsg(ItemList.get(i).title);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(), "File could not be found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        itemViewHolder.cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Storage storage = SimpleStorage.getExternalStorage();
                                String content = storage.readTextFile("GifHub", "animation_list.txt");
                                String[] info = content.split("\\r?\\n");
                                storage.deleteFile("GifHub", "animation_list.txt");
                                storage.createFile("GifHub", "animation_list.txt", "");

                                for(int j = 0; j < info.length; ++j) {
                                    String[] files = info[j].split(":");
                                    if(j != i) {
                                        Log.d("Adapter", "add to file: " + files[0]);
                                        storage.appendFile("GifHub", "animation_list.txt", files[0] + ":" + files[1] + ":");
                                    }
                                }
                                ItemList.remove(i);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .show();
                return true;
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
