package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 数据库工具类：连接数据库用、获取数据库数据用
 * 相关操作数据库的方法均可写在该类
 */
public class DBUtils {

    //private static String driver = "com.mysql.jdbc.Driver";// MySql驱动

//    private static String url = "jdbc:mysql://localhost:3306/map_designer_test_db";

    private static String user = "*****";// 用户名

    private static String password = "*******";// 密码
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm:ss");
    private static Date date = null;
    private static int num = 0;
    private static String hour = null;
    private static int h = 0;
    private static String minute = null;
    private static int m = 0;
    private static int count1 = 0;
    private static int count2 = 0;
    private static int count3 = 0;
    private static int count4 = 0;
    private static int count5 = 0;

    static{
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("加载JDBC驱动成功");
        } catch (ClassNotFoundException e) {
            System.out.println("加载JDBC驱动失败");
        }
    }

    private static Connection getConn(String dbName){

        Connection connection = null;


        try{
//            Class.forName(driver);// 动态加载类
            String ip = "192.168.1.2";// 写成本机地址，不能写成localhost，同时手机和电脑连接的网络必须是同一个

            // 尝试建立到给定数据库URL的连接
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":3306/" + dbName,
                    user, password);

        }catch (Exception e){
            e.printStackTrace();
        }

        return connection;
    }

    private static Connection connection = getConn("emp");

    public static HashMap<String, Object> getInfoByName(String name){

        HashMap<String, Object> map = new HashMap<>();
        // 根据数据库名称，建立连接
//        Connection connection = getConn("emp");

        try {
            // mysql简单的查询语句。这里是根据emp表的name字段来查询某条记录
            String sql = "select * from book where bookid = ?";
//            String sql = "select * from MD_CHARGER";
            if (connection != null){// connection不为null表示与数据库建立了连接
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null){
                    // 设置上面的sql语句中的？的值为name
                    ps.setString(1, name);
                    // 执行sql查询语句并返回结果集
                    ResultSet rs = ps.executeQuery();
                    if (rs != null){
                        int count = rs.getMetaData().getColumnCount();
                        Log.e("DBUtils","列总数：" + count);
                        while (rs.next()){
                            // 注意：下标是从1开始的
                            for (int i = 1;i <= count;i++){
                                String field = rs.getMetaData().getColumnName(i);
                                map.put(field, rs.getString(field));
                            }
                        }
                        connection.close();
                        ps.close();
                        rs.close();
                        return  map;
                    }else {
                        return null;
                    }
                }else {
                    return  null;
                }
            }else {
                return  null;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("DBUtils","异常：" + e.getMessage());
            return null;
        }

    }

    public static int alterInfoByName(String name){

//        HashMap<String, Object> map = new HashMap<>();
        // 根据数据库名称，建立连接
//        Connection connection = getConn("emp");
        // 获取时间
        date = new Date();
        String s_date = simpleDateFormat.format(date);
        System.out.println(s_date);

        //获取星期几
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        num = calendar.get(Calendar.DAY_OF_WEEK);
        System.out.println(num);

        hour = s_date.substring(0, 2);
        minute = s_date.substring(3, 5);
        if (hour.charAt(0) == '0'){
            hour = hour.substring(1,2);
            h = Integer.valueOf(hour).intValue();
        }else{
            h = Integer.valueOf(hour).intValue();
        }

        if (minute.charAt(0) == '0'){
            minute = minute.substring(1,2);
            m = Integer.valueOf(minute).intValue();
        }else{
            m = Integer.valueOf(minute).intValue();
        }


        try {
            // mysql简单的查询语句。这里是根据emp表的name字段来查询某条记录

            String sql;
            if (num == 2 && count1 == 0){
                sql = "update lmc set Mon_come = '" + s_date + "' where name = ?";
//                if (h >= 9 && m > 0){
//
//                }
                count1++;
            }else if(num == 2 && count1 == 1){
                sql = "update lmc set Mon_leave = '" + s_date + "' where name = ?";
            }else if(num == 3 && count1 == 0){
                sql = "update lmc set Tue_come = '" + s_date + "' where name = ?";
                count2++;
            }else if(num == 3 && count2 == 1){
                sql = "update lmc set Tue_leave = '" + s_date + "' where name = ?";
            }else if(num == 4 && count3 == 0){
                sql = "update lmc set Wed_come = '" + s_date + "' where name = ?";
                count3++;
            }else if(num == 4 && count3 == 1){
                sql = "update lmc set Wed_leave = '" + s_date + "' where name = ?";
            }else if(num == 5 && count4 == 0){
                sql = "update lmc set Thu_come = '" + s_date + "' where name = ?";
                count4++;
            }else if(num == 5 && count4 == 1){
                sql = "update lmc set Thu_leave = '" + s_date + "' where name = ?";
            }else if(num == 6 && count5 == 0){
                sql = "update lmc set Fri_come = '" + s_date + "' where name = ?";
                count5++;
            }else if(num == 6 && count5 == 1){
                sql = "update lmc set Fri_leave = '" + s_date + "' where name = ?";
            }else{
                sql = null;
            }


//            sql = "select * from MD_CHARGER";
            try{
                if (connection != null){// connection不为null表示与数据库建立了连接
                    PreparedStatement ps = connection.prepareStatement(sql);
                    if (ps != null){
                        int rs = -1;
                        // 设置上面的sql语句中的？的值为name
                        ps.setString(1, name);
                        // 执行sql查询语句并返回结果集
                        rs = ps.executeUpdate();
                        connection.close();
                        ps.close();
                        return rs;
                    }else {
                        return -1;
                    }
                }else {
                    return -1;
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.e("DBUtils","异常：" + e.getMessage());
            return -1;
        }
    return -1;
    }

}


