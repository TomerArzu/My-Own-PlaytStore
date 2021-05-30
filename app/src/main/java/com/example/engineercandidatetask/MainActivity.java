package com.example.engineercandidatetask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.engineercandidatetask.Listeners.DownloadCompleteListener;
import com.example.engineercandidatetask.Listeners.InstallationDoneListener;
import com.example.engineercandidatetask.Listeners.StoreItemClickListener;
import com.example.engineercandidatetask.Model.StoreItem;
import com.example.engineercandidatetask.adpters.StoreItemAdapter;
import com.example.engineercandidatetask.tasks.ApkDownloaderInstaller;
import com.example.engineercandidatetask.tasks.DataDownloader;
import com.example.engineercandidatetask.tasks.ImageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements DownloadCompleteListener, StoreItemClickListener, InstallationDoneListener {
    private static final String TAG = MainActivity.class.getName();
    private DataDownloader dataDownloader;
    private String[] permsRequired = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    private String jsonSourceUrl = "http://fota.colmobil.co.il.s3-eu-west-1.amazonaws.com/FOTA/settings/update_apps_test.json";
    private ProgressDialog mProgressDialog;
    private File downloadedFile;
    private ArrayList<StoreItem> storeItems;
    private RecyclerView storeItemsRv;
    StoreItemAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        downloadBtn = findViewById(R.id.downloadBtn);
//        downloadBtn.setOnClickListener(this.onStoreItemClick());
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        dataDownloader = new DataDownloader(jsonSourceUrl, mProgressDialog, this);
        dataDownloader.setNotifyDownloadCompleteListener(this);
        if (isPermissionsGranted()) {
            startDownload();
        } else {
            getPermissions();
        }

    }

    private void setupRecycleView() {
        storeItemsRv= (RecyclerView)findViewById(R.id.storeItemsRv);

        adapter = new StoreItemAdapter(this.storeItems);
        adapter.setStoreItemClickListener(this);
        storeItemsRv.setAdapter(adapter);
        storeItemsRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    private void startDownload() {
        if (dataDownloader != null) {
            dataDownloader.execute("data.json");
        }
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(this.permsRequired, Constants.PERMISSIONS_REQ);
        }
    }

    private boolean isPermissionsGranted() {
        for (String perm : permsRequired) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSIONS_REQ) {
            if (isPermissionsGranted()) {
                startDownload();
            }
        }
    }

    @Override
    public void onDownloadDone(File downloadedFile) {
        if (isDataFile(downloadedFile)) {
            createStoreItemsFromData(downloadedFile);
        }
    }

    private boolean isDataFile(File file) {
        return file.getAbsolutePath().contains(".json");
    }

    private void createStoreItemsFromData(File dataFile) {
        String jsonStringContent = readDownloadedJson(dataFile);
        JSONObject jsonItem;
        JSONObject payload;
        StoreItem item;
        try {
            if(this.storeItems == null){
                storeItems = new ArrayList<>();
            }
            JSONObject data = new JSONObject (jsonStringContent);
            JSONArray responseItemArray = (JSONArray) data.optJSONArray("endpoint_response_items_array");
            for(int i=0;i<responseItemArray.length();i++){
                payload = responseItemArray.getJSONObject(i).getJSONObject("payload");
                item = new StoreItem(
                        payload.optString("app_name"),
                        payload.optString("download_url1"),
                        payload.optString("version"),
                        payload.optString("description"),
                        payload.optString("icon_url"),
                        payload.optString("package_name"),
                        new ImageDownloader());
                if(!isUpdated(item.getPackageName(), item.getVersion())) {
                    storeItems.add(item);
                }
            }
            setupRecycleView();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean isUpdated(String packageName, String packageVersion) {
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ArrayList<String> currentVersion = new ArrayList<>(Arrays.asList(info.versionName.split("\\.")));
            ArrayList<String> suggestVersion = new ArrayList<>(Arrays.asList(packageVersion.split("\\.")));
            if(currentVersion.size() > suggestVersion.size()){
                suggestVersion = versionEqualizer(currentVersion, suggestVersion);
            } else if (suggestVersion.size() > currentVersion.size()){
                currentVersion = versionEqualizer(suggestVersion, currentVersion);
            }
            for(int i=0;i<suggestVersion.size();i++){
                if(Integer.parseInt(suggestVersion.get(i)) > Integer.parseInt(currentVersion.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private ArrayList<String> versionEqualizer(ArrayList<String> longVersion, ArrayList<String> shortVersion) {
        while (shortVersion.size() != longVersion.size()){
            shortVersion.add("0");
        }
        return shortVersion;
    }

    private String readDownloadedJson(File dataFile) {
        String stringJson = "";
        InputStream inputStream = null;
        try {
            inputStream = openFileInput("data.json");
            if(inputStream!=null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while((receiveString = bufferedReader.readLine())!=null){
                    stringBuilder.append(receiveString);
                }
                stringJson = stringBuilder.toString();
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "readDownloadedJson: File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "readDownloadedJson: Can not read file");
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e){
                Log.d(TAG, "readDownloadedJson: IOException");
                e.printStackTrace();
            }
        }
        return stringJson;
    }


    @Override
    public void onStoreItemClick(final StoreItem item) {
        ApkDownloaderInstaller apkDownloaderInstaller = new ApkDownloaderInstaller(item, this.mProgressDialog,this);
        apkDownloaderInstaller.setInstallationDoneListener(this);
        apkDownloaderInstaller.execute(item.getDownloadUrl());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInstallationDone(StoreItem installedItem) {
        int installedItemPos = storeItems.indexOf(installedItem);
        storeItems.remove(installedItem);
        this.storeItemsRv.removeViewAt(installedItemPos);
        this.adapter.notifyItemRemoved(installedItemPos);
        this.adapter.notifyItemRangeChanged(installedItemPos, storeItems.size());
        this.adapter.notifyDataSetChanged();

    }
}

