package com.example.vincent.tbd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Vincent on 4/17/16.
 */
public class CastActivity extends AppCompatActivity {

    private static GifImageView mGifImageView;

    //--------------------------------------------------------------------------------------
    // Initialize
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Casting");

        // Init view
        setContentView(R.layout.activity_cast);
        mGifImageView = (GifImageView) findViewById(R.id.castView);

    }

    public static void display(String fileName, Storage storage) {
        String content = storage.readTextFile("ShowMeHow", "animation_list.txt");
        String[] info = content.split("\\r?\\n");
        if (!content.equals("")) {
            boolean found = false;
            for(int i = 0; i < info.length; ++i) {
                String[] files = info[i].split(":");
                if(files[0].equals(fileName)) {
                    try {
                        // Sets GifView using file path
                        GifDrawable gifFromPath = new GifDrawable(files[1]);
                        mGifImageView.setImageDrawable(gifFromPath);
                        found = true;
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mGifImageView.getContext(), "File could not be found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if(!found) { Toast.makeText(mGifImageView.getContext(), "File could not be found", Toast.LENGTH_SHORT).show(); }
        }
    }

    public static void stop() {
        mGifImageView.setImageResource(R.drawable.black);
    }

}
