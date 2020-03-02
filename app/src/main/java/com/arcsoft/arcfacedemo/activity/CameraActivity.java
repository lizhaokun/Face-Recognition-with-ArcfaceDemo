package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ImageUtil;
import com.arcsoft.arcfacedemo.widget.ProgressDialog;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import com.arcsoft.arcfacedemo.activity.FaceManageActivity.*;

public class CameraActivity extends AppCompatActivity{
    private Button btn_start;//登录按钮
    private Button btn_camera;//登录按钮
    private Button btn_register;
    private Toast toast = null;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private TextView tvNotificationRegisterResult;

    //注册图所在的目录
    private static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "arcfacedemo";
    private static final String REGISTER_DIR = ROOT_DIR + File.separator + "register";
    private static final String REGISTER_FAILED_DIR = ROOT_DIR + File.separator + "failed";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String shouldRegister;

    ProgressDialog progressDialog = null;
    public HashMap<String, String> name_map = new HashMap<>();
    public static CameraActivity cameraAct;
    public HashMap<String, String> getCameraHashMap(){
        return name_map;
    }
    public void setCameraHashMap(HashMap<String, String> newHashMap){
        this.name_map = newHashMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        progressDialog = new ProgressDialog(this);
        FaceServer.getInstance().init(this);
        cameraAct = this;
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        tvNotificationRegisterResult = findViewById(R.id.notification_register_result);
        init();
    }

    @Override
    protected void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        FaceServer.getInstance().unInit();
        super.onDestroy();
    }

    private void setshouldRegister(String s){
        this.shouldRegister = s;
    }

    private String getshouldRegister(){
        return this.shouldRegister;
    }
    //获取界面控件
    private void init() {

        btn_camera=findViewById(R.id.btn_camera);
        btn_register=findViewById(R.id.btn_register);
        final Intent btn_camera_intent = new Intent(CameraActivity.this, RegisterAndRecognizeActivity.class);
        //打卡键的点击事件
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeEngine(null);
                startActivity(btn_camera_intent);
            }
        });
        //注册键的点击事件
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arcfacedemo_path = Environment.getExternalStoragePublicDirectory("").getAbsolutePath()+"/arcfacedemo/register/";

                File file = new File(arcfacedemo_path);
                if (!file.exists()){
                    file.mkdirs();
                }

                deleteAllFiles(new File(arcfacedemo_path));
                getPhotoPathRequest();
                clearFaces(v);
            }
        });
        ConfigUtil.setFtOrient(CameraActivity.this, FaceEngine.ASF_OP_0_HIGHER_EXT);
    }

    public void activeEngine(final View view) {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(CameraActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast("激活成功");
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast("已经激活");
                        } else {
                            showToast("激活失败");
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    private boolean checkPermissionss(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine(null);
                doRegister();
            } else {
                showToast("permission_denied");
            }
        }
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

    public void getPhotoPathRequest() {
        //请求地址
        String url = "http://114.213.210.211:8080/api/worker/list";    //注①
        String tag = "getWorkerListJSON";    //注②
//        final ArrayList<String> PathList = new ArrayList<String>();
        final DownloadUtils downloadUtils = new DownloadUtils(CameraActivity.this);

        //取得请求队列
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);
        //创建JsonObjectRequest，定义字符串请求的请求方式为GET(省略第一个参数会默认为GET方式)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
//                    ArrayList<String> list = new ArrayList<>();
                    JSONArray workerList = response.getJSONArray("workerList");
                    for (int i = 0; i < workerList.length(); i++) {
                        JSONObject jsonObject = workerList.getJSONObject(i);
                        String ImageUrl = "http://114.213.210.211:8080/api/download/" + jsonObject.getString("id");
                        String ImageName = jsonObject.getString("photoName");
//
//                        String filename = checkFileName(list, ImageName, 0);
                        downloadUtils.downloadImage(ImageUrl, ImageName);
                        System.out.println("xiazaichenggong");
                    }
                    showToast("数据下载成功！");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("666");
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JsonObjectRequest", error.getMessage());
                showToast("数据库连接失败！");
            }
        });

        jsonObjectRequest.setTag(tag);
        requestQueue.add(jsonObjectRequest);
    }

    public void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                        showToast("文件夹存在，但删除失败！");
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                            showToast("删除失败！");
                        }
                    }
                }
            }
        } else{
            showToast("本地数据库为空！");
        }
    }

    public void batchRegister(View view) {
        if (checkPermissionss(NEEDED_PERMISSIONS)) {
            doRegister();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    private void doRegister() {
        File dir = new File(REGISTER_DIR);
        if (!dir.exists()) {
            Toast.makeText(this, "path \n" + REGISTER_DIR + "\n is not exists", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dir.isDirectory()) {
            Toast.makeText(this, "path \n" + REGISTER_DIR + "\n is not a directory", Toast.LENGTH_SHORT).show();
            return;
        }
        final File[] jpgFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FaceServer.IMG_SUFFIX);
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int totalCount = jpgFiles.length;

                int successCount = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMaxProgress(totalCount);
                        progressDialog.show();
                        tvNotificationRegisterResult.setText("");
                        tvNotificationRegisterResult.append("开始处理,请等待\n\n");
                    }
                });
                for (int i = 0; i < totalCount; i++) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) {
                                progressDialog.refreshProgress(finalI);
                            }
                        }
                    });
                    final File jpgFile = jpgFiles[i];
                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    bitmap = ImageUtil.alignBitmapForNv21(bitmap);
                    if (bitmap == null) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    byte[] nv21 = ImageUtil.bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
                    boolean success = FaceServer.getInstance().register(CameraActivity.this, nv21, bitmap.getWidth(), bitmap.getHeight(),
                            jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
                    if (!success) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                    } else {
                        successCount++;
                    }
                }
                final int finalSuccessCount = successCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        tvNotificationRegisterResult.append("处理完成!\n总的数量 = " + totalCount + "\n成功数量 = " + finalSuccessCount + "\n失败数量 = " + (totalCount - finalSuccessCount)
                                + "\n注册失败的图片在以下目录 '" + REGISTER_FAILED_DIR + "'");
                    }
                });
                Log.i(FaceManageActivity.class.getSimpleName(), "run: " + executorService.isShutdown());
            }
        });
    }

    public void clearFaces(View view) {
        int faceNum = FaceServer.getInstance().getFaceNumber(this);
        if (faceNum == 0){
//            Toast.makeText(this, R.string.no_face_need_to_delete, Toast.LENGTH_SHORT).show();
        }else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.notification)
                    .setMessage(getString(R.string.confirm_delete,faceNum)  )
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int deleteCount = FaceServer.getInstance().clearAllFaces(CameraActivity.this);
                            Toast.makeText(CameraActivity.this, deleteCount + " faces cleared!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel,null)
                    .create();
//            dialog.show();
        }
    }

}
