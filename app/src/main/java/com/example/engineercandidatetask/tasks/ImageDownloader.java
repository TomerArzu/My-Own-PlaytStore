package com.example.engineercandidatetask.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... strings) {
        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            String source = strings[0];
            if (source == null || source.equals("")) {
                source = "https://icons.iconarchive.com/icons/guillendesign/variations-3/256/Android-Store-icon.png";
            }
            URL url = new URL(source);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }

            } catch (IOException e) {

            }
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
    }
}
