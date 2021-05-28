package com.example.engineercandidatetask.Model;

import android.graphics.Bitmap;

import com.example.engineercandidatetask.tasks.ImageDownloader;

import java.util.concurrent.ExecutionException;

public class StoreItem {
    private String appName;
    private String downloadUrl;
    private String version;
    private String description;
    private String iconUrl;
    private Bitmap iconBitmap;
    private String packageName;
    private boolean isDownloaded;
    private String pathToApk;

    public StoreItem(String appName, String downloadUrl, String version, String description, String iconUrl, String packageName, ImageDownloader imageDownloader) throws ExecutionException, InterruptedException {
        this.appName = appName;
        this.downloadUrl = downloadUrl;
        this.version = version;
        this.description = description;
        this.iconUrl = iconUrl;
        this.packageName = packageName;
        this.iconBitmap = imageDownloader.execute(this.iconUrl).get();
        isDownloaded = false;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getPathToApk() {
        return pathToApk;
    }

    public void setPathToApk(String pathToApk) {
        this.setDownloaded(true);
        this.pathToApk = pathToApk;
    }
}