package com.example.engineercandidatetask.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.engineercandidatetask.Listeners.InstallationDoneListener;
import com.example.engineercandidatetask.Model.StoreItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ApkDownloaderInstaller extends AsyncTask<String, Integer, String> {
    public static final String TAG = ApkDownloaderInstaller.class.getName();
    private StoreItem itemInProcess;
    private Context context;
    private InstallationDoneListener installationDoneListener;
    private ProgressDialog downloadingProgressDialog;

    public ApkDownloaderInstaller(StoreItem item, ProgressDialog downloadingProgressDialog, Context context) {
        this.itemInProcess = item;
        this.downloadingProgressDialog = downloadingProgressDialog;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream apkInputStream = null;
        FileOutputStream apkOutputStream = null;
        HttpURLConnection connection = null;
        try {
            URL apkUrl = new URL(strings[0]);
            connection = (HttpURLConnection) apkUrl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            String externalStorage = this.context.getFilesDir().getAbsolutePath();
            String appName = this.itemInProcess.getAppName().substring(0, (this.itemInProcess.getAppName().length() - ".apk".length() - 1));
            File apkDir = new File(externalStorage, appName);
            if (!apkDir.exists()) {
                apkDir.mkdir();
            }
            File apkFile = new File(apkDir, this.itemInProcess.getAppName());
            int apkFileLength = connection.getContentLength();
            apkInputStream = connection.getInputStream();
            apkOutputStream = new FileOutputStream(apkFile);
            byte[] downloadStreamData = new byte[4096];
            int total=0;
            int count;
            while ((count = apkInputStream.read(downloadStreamData)) != -1) {
                total += count;
                if(apkFileLength>0){
                    publishProgress((int) (total * 100 / apkFileLength));
                }
                apkOutputStream.write(downloadStreamData, 0, count);
            }
            installApk(apkFile.getAbsolutePath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (apkInputStream != null) {
                    apkInputStream.close();
                }
                if (apkOutputStream != null) {
                    apkOutputStream.close();
                }

            } catch (IOException e) {

            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private void installApk(String apkFileLocation) {
        try {
            File file = new File(apkFileLocation);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri downloadedApkUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(downloadedApkUri, "application/vnd.android.package-archive");
                List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resolveInfoList) {
                    context.grantUriPermission(context.getApplicationContext().getPackageName() + ".provider", downloadedApkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Downloading " + this.itemInProcess.getAppName() + ", Please Wait...", Toast.LENGTH_SHORT).show();
        this.downloadingProgressDialog.setMessage("Downloading " + itemInProcess.getAppName());
        this.downloadingProgressDialog.show();

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        this.downloadingProgressDialog.setIndeterminate(false);
        this.downloadingProgressDialog.setMax(100);
        this.downloadingProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        this.downloadingProgressDialog.dismiss();
        if (result != null) {
            Toast.makeText(context, "Download and install error: " + result, Toast.LENGTH_LONG).show();
            Log.d(TAG, "Download error: " + result);
        } else {
            Toast.makeText(context, "Congrats! Installation completed successfully, Enjoy you new application.", Toast.LENGTH_SHORT).show();
            installationDoneListener.onInstallationDone(itemInProcess);
        }
    }

    public void setInstallationDoneListener(InstallationDoneListener installationDoneListener) {
        this.installationDoneListener = installationDoneListener;
    }
}
