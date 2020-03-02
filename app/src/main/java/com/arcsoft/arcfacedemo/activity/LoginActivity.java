package com.arcsoft.arcfacedemo.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.arcsoft.arcfacedemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.arcsoft.arcfacedemo.activity.FaceManageActivity.*;


public class LoginActivity extends AppCompatActivity {
    private TextView tv1;//标题
    private TextView tv2;//返回键
    private Button btn_login, btn_cancel;//登录按钮
    private String userName, psw, spPsw;//获取的用户名，密码，加密密码
    private EditText et_user_name, et_psw;//编辑框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

    //获取界面控件
    private void init() {
        //从main_title_bar中获取的id
//        tv_main_title = findViewById(R.id.tv_main_title);
//        tv_main_title.setText("登录");
//        tv_back = findViewById(R.id.tv_back);
        //从activity_login.xml中获取的
        btn_login = findViewById(R.id.btn_login);
        btn_cancel = findViewById(R.id.download_p);
        et_user_name = findViewById(R.id.et_user_name);
        et_psw = findViewById(R.id.et_psw);

        //返回键的点击事件
//        tv_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //登录界面销毁
//                LoginActivity.this.finish();
//            }
//        });
        //登录键的点击事件
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String AccountNumber = et_user_name.getText().toString().trim();
                final String Password = et_psw.getText().toString().trim();
                if(AccountNumber.equals("test") && Password.equals("test")) {
                    startActivity(new Intent(LoginActivity.this, CameraActivity.class));
//                LoginRequest(AccountNumber, Password);
                }
                else{
                    Util.showToast(LoginActivity.this, "用户名或密码错误");
//                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllFiles(new File(Environment.getExternalStorageDirectory()+"/arcfacedemo/register/"));
                getPhotoPathRequest();
//                Toast.makeText(LoginActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void LoginRequest(final String AccountNumber, final String Password) {
        //请求地址
        String url = "http://47.100.222.46:8080/MyFirstWebAPP/LoginServlet";    //注①
        String tag = "Login";    //注②

        //取得请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);

        //创建StringRequest，定义字符串请求的请求方式为POST(省略第一个参数会默认为GET方式)
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");  //注③
                            String result = jsonObject.getString("Result");  //注④
                            if (result.equals("success")) {  //注⑤
                                //做自己的登录成功操作，如页面跳转
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//                                deleteAllFiles(new File(Environment.getExternalStorageDirectory()+"/arcfacedemo/register/"));
                                getPhotoPathRequest();
//                                Thread thread = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        getPhotoPathRequest();
//                                    }
//                                });
//                                thread.start();
                                startActivity(new Intent(LoginActivity.this, CameraActivity.class));

                                return;
                            } else {
                                //做自己的登录失败操作，如Toast提示
                                Toast.makeText(LoginActivity.this, "输入的用户名和密码不一致", Toast.LENGTH_SHORT).show();
                                return;

                            }
                        } catch (JSONException e) {
                            //做自己的请求异常操作，如Toast提示（“无网络连接”等）
                            Log.e("TAG", e.getMessage(), e);
                            Toast.makeText(LoginActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
                Log.e("TAG", error.getMessage(), error);
                Toast.makeText(LoginActivity.this, "请稍后重试", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
//                params.put("user_img", user_img);
                params.put("AccountNumber", AccountNumber);
                params.put("Password", Password);
                return params;
            }
        };

        //设置Tag标签
        request.setTag(tag);

        //将请求添加到队列中
        requestQueue.add(request);
    }


    public void getPhotoPathRequest() {
        //请求地址
        String url = "http://114.213.210.211:8080/api/worker/list";    //注①
        String tag = "getWorkerListJSON";    //注②
//        final ArrayList<String> PathList = new ArrayList<String>();
        final DownloadUtils downloadUtils = new DownloadUtils(LoginActivity.this);

        //取得请求队列
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //防止重复请求，所以先取消tag标识的请求队列
        requestQueue.cancelAll(tag);
        //创建JsonObjectRequest，定义字符串请求的请求方式为GET(省略第一个参数会默认为GET方式)
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
//                    ArrayList<String> list = new ArrayList<>();
                    JSONArray workerList = response.getJSONArray("workerList");
                    for(int i = 0; i < workerList.length(); i++){
                        JSONObject jsonObject = workerList.getJSONObject(i);
                        String ImageUrl = "http://114.213.210.211:8080/api/download/" + jsonObject.getString("id");
                        String ImageName = jsonObject.getString("photoName");
//
//                        String filename = checkFileName(list, ImageName, 0);
                        downloadUtils.downloadImage(ImageUrl, ImageName);
                        System.out.println("xiazaichenggong");
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
                System.out.println("666");
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JsonObjectRequest", error.getMessage());
            }
        });


        jsonObjectRequest.setTag(tag);
        requestQueue.add(jsonObjectRequest);

//        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        int length = response.length();
//                        for (int i = 0; i < length; i++) {
//                            try {
//                                JSONObject jsonObject = response.getJSONObject(i);
//                                String ImageUrl = "http://114.213.210.211:8080/api/download/" + jsonObject.getString("id");
//                                String ImageName = jsonObject.getString("photoName");
//                                downloadUtils.downloadImage(ImageUrl, ImageName);
//                                System.out.println("xiazaichenggong");
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        startActivity(new Intent(LoginActivity.this, CameraActivity.class));
//                        System.out.println("sljflaskjflskjflskjlfjsal;kfj;lsjflksdjl");
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                //做自己的响应错误操作，如Toast提示（“请稍后重试”等）
//                Toast.makeText(LoginActivity.this, "请稍后再试", Toast.LENGTH_SHORT).show();
//                Log.e("TAG", error.getMessage(), error);
//            }
//        });
//
//        //设置Tag标签
//        request.setTag(tag);

        //将请求添加到队列中
//        requestQueue.add(request);
    }


    public void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }


    /**
     *
     * @param path 需要遍历的路径
     * @return 路径下文件的名称集合
     */
    private static ArrayList<String> getFile(String path, int deep){
        // 获得指定文件对象
        File file = new File(path);
        // 获得该文件夹内的所有文件
        File[] array = file.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        int n = 0;
        for(int i=0;i<array.length;i++)
        {
            if(array[i].isFile())//如果是文件
            {
                for (int j = 0; j < deep; j++)//输出前置空格
                    System.out.print(" ");
                // 只输出文件名字
                list.add(array[i].getName());
            }
        }
        return list;
    }

    /**
     *
     * @param names 文件下文件名的集合
     * @param name 存入的文件名
     * @param index 索引的开始位置
     * @return 符合要求的文件名
     */
    private static String checkFileName(ArrayList<String> names,String name,int index) {
        if(names.contains(name.substring(0,name.indexOf("."))+index+name.substring(name.indexOf("."),name.length()))) {
            name = checkFileName(names,name,index+1);
        } else {
            return name.substring(0,name.indexOf("."))+index+name.substring(name.indexOf("."),name.length());
        }
        return name;
    }

    public boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }


}
