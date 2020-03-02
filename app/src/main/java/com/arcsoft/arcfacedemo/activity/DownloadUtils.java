package com.arcsoft.arcfacedemo.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;

public class DownloadUtils {
    //下载器
    private DownloadManager downloadManager;
    //上下文
    private Context mContext;
    //下载的ID
    private long downloadId;
    public  DownloadUtils(Context context){
        this.mContext = context;
    }

    //下载apk
    public void downloadImage(String url, String name) {

        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));//DownloadManager.Request是一个内部类,Uri代表资源的名称，url就是下载链接
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("图片");
        request.setDescription("Apk Downloading");
        request.setVisibleInDownloadsUi(true);

//        String arcfacedemo_path = Environment.getExternalStoragePublicDirectory("").getAbsolutePath()+"/arcfacedemo/register/";
        //设置下载的路径
        request.setDestinationInExternalPublicDir("arcfacedemo/register/", name);

        //获取DownloadManager
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        downloadId = downloadManager.enqueue(request);

        //注册广播接收者，监听下载状态
         mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));//IntentFilter是意图滤波器
    }

    //
    //下载apk
    public void downloadAPK(String url, String name) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("通知标题，随意修改");
        request.setDescription("新版***下载中...");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        request.setDestinationUri(Uri.fromFile(file));
        String pathstr = file.getAbsolutePath();
        //获取DownloadManager
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }

        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));  //根据downloadManager.enqueue(request)返回的下载ID，从数据库查询下载状态。
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    System.out.println("下载暂停");
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    System.out.println("下载延迟");
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    System.out.println("正在下载");
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成安装APK
                    System.out.println("下载成功");
//                    installAPK();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    System.out.println("下载失败");
                    Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        c.close();
    }

    //下载到本地后执行安装
    private void installAPK() {
        //获取下载文件的Uri
        Uri downloadFileUri = downloadManager.getUriForDownloadedFile(downloadId);
        if (downloadFileUri != null) {
            Intent intent= new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            mContext.unregisterReceiver(receiver);
        }
    }

}
