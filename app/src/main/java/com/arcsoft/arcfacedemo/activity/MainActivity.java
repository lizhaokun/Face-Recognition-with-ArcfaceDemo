package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends Activity {

    private Button btn_get_data;
    private TextView tv_data;
    private Button btn_alter_data;
    private TextView tv_data2;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0x11:
                    String s = (String) msg.obj;
                    tv_data.setText(s);
                    break;
                case 0x12:
                    String ss = (String) msg.obj;
                    tv_data.setText(ss);
                    break;
            }

        }
    };


    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case 0x11:
                    String s = (String) msg.obj;
                    tv_data2.setText(s);
                    break;
                case 0x12:
                    String ss = (String) msg.obj;
                    tv_data2.setText(ss);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 控件的初始化
        btn_get_data = findViewById(R.id.btn_get_data);
        tv_data = findViewById(R.id.tv_data);
        btn_alter_data = findViewById(R.id.btn_alter_data);
        tv_data2 = findViewById(R.id.tv_data2);

        setListener();
    }

    /**
     * 设置监听
     */
    private void setListener() {

        // 按钮点击事件
        btn_get_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建一个线程来连接数据库并获取数据库中对应表的数据
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 调用数据库工具类DBUtils的getInfoByName方法获取数据库表中数据
                        HashMap<String, Object> map = DBUtils.getInfoByName("1");
                        Message message = handler.obtainMessage();
                        if(map != null){
                            String s = "";
                            for (String key : map.keySet()){
                                s += key + ":" + map.get(key) + "\n";
                            }
                            message.what = 0x12;
                            message.obj = s;
                        }else {
                            message.what = 0x11;
                            message.obj = "查询结果为空";
                        }
                        // 发消息通知主线程更新UI
                        handler.sendMessage(message);
                    }
                }).start();

            }
        });


        btn_alter_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 创建一个线程来连接数据库并获取数据库中对应表的数据
                // 在Android4.0之后，不允许在主线程中进行比较耗时的操作（连接数据库就属于比较耗时的操作），
                // 需要开一个新的线程来处理这种耗时的操作，
                // 没新线程时，一直就是程序直接退出，开了一个新线程处理直接，就没问题了。
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 调用数据库工具类DBUtils的getInfoByName方法获取数据库表中数据
                        int res = DBUtils.alterInfoByName("李兆坤");
                        Message message = handler2.obtainMessage();
                        if(res != -1){
                            message.what = 0x12;
                            message.obj = "信息已更新";
                        }else {
                            message.what = 0x11;
                            message.obj = "信息未更新";
                        }
                        // 发消息通知主线程更新UI
                        handler2.sendMessage(message);
                    }
                }).start();

            }
        });

    }
}

