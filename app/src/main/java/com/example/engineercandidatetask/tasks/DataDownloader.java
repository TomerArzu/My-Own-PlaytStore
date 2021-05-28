package com.example.engineercandidatetask.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.engineercandidatetask.Listeners.DownloadCompleteListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataDownloader extends AsyncTask<String, Integer, String> {
    private static final String TAG = DataDownloader.class.getName();
    private String sourceUrl;
    private Context context;
    private ProgressDialog activityProgressDialog;
    private PowerManager.WakeLock mWakeLock;
    private File downloadedDataFile;
    private DownloadCompleteListener notifyDownloadCompleteListener;


    public DataDownloader(String sourceUrl, ProgressDialog activityProgressDialog, Context context) {
        this.sourceUrl = sourceUrl;
        this.context = context;
        this.activityProgressDialog = activityProgressDialog;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        this.downloadedDataFile = new File(context.getFilesDir(), strings[0]);
        try {
            URL url = new URL(this.sourceUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect 200 status code
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "doInBackground: cannot download json file from source");
                return "Cant download the data file " + connection.getResponseCode() + ":" + connection.getResponseMessage();
            }
            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(downloadedDataFile);

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (fileLength > 0)
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: exception: " + e.toString());
            return e.toString();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }

            } catch (IOException e) {

            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        activityProgressDialog.setMessage("Initializing Application Data...");
        activityProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        activityProgressDialog.setIndeterminate(false);
        activityProgressDialog.setMax(100);
        activityProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        activityProgressDialog.dismiss();
        if (result != null) {
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Download error: " + result);
        } else {
            Toast.makeText(context, "Application Ready to go!", Toast.LENGTH_SHORT).show();
            this.notifyDownloadCompleteListener.onDownloadDone(this.downloadedDataFile);
        }
    }

    public void setNotifyDownloadCompleteListener(DownloadCompleteListener notifyDownloadCompleteListener) {
        this.notifyDownloadCompleteListener = notifyDownloadCompleteListener;
    }
}
