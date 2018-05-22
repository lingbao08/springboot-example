package com.example.demo.config;

import java.sql.*;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 18:40
 **/

public class HiveServer2Client {

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        try {
            Connection conn = DriverManager.getConnection("jdbc:hive2://192.168.0.42:10000/myhive", "hadoop", "");
            String sql = "select * from hivetest where seq = 1";
//            String sql1 = "drop table hivetest";

            PreparedStatement sta = conn.prepareStatement(sql);
            ResultSet result = sta.executeQuery();
            while (result.next()) {
                System.out.print(result.getString(1));
                System.out.print("\t"+result.getString(2));
                System.out.print("\t"+result.getString(3));
                System.out.print("\t"+result.getString(4));
                System.out.println("\t"+result.getString(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
