package com.example.demo.config;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/5/22 18:39
 **/

public class ImpalaUtil {
    private static final String IMPALAD_HOST = "192.168.1.43";

    private static final String IMPALAD_JDBC_PORT = "21050";

    private static final String CONNECTION_URL = "jdbc:hive2://" + IMPALAD_HOST + ':' + IMPALAD_JDBC_PORT + "/myhive;auth=noSasl";

    private static final String JDBC_DRIVER_NAME = "org.apache.hive.jdbc.HiveDriver";

    private Connection conn = null;

    public <T> List<T> search(String sql, List<String> conditions, Class<T> clazz) {

        List list = new ArrayList();
        try {
            Class.forName(JDBC_DRIVER_NAME);
            conn = DriverManager.getConnection(CONNECTION_URL, "impala", "");
            PreparedStatement ps = conn.prepareStatement(sql);
            if (null != conditions && conditions.size() != 0) {
                int size = conditions.size();
                for (int i = 1; i <= 2; i++) {
                    ps.setString(i, conditions.get(i - 1));
                }
            }
            ResultSet rs = ps.executeQuery();
            System.out.println("\n== Begin Query Results ======================");
            list = mapRersultSetToObject(rs, clazz);
            System.out.println("== End Query Results =======================\n\n");
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                // swallow
            }

        }
        return list;
    }


    /**
     * 映射result为java的结果类
     *
     * @param rs
     * @param outputClass
     * @param <T>
     * @return
     */
    public <T> List<T> mapRersultSetToObject(ResultSet rs, Class outputClass) {
        List<T> outputList = null;
        try {
            // make sure resultset is not null
            if (rs != null) {
                ResultSetMetaData rsmd = rs.getMetaData();
                Field[] fields = outputClass.getDeclaredFields();
                while (rs.next()) {
                    T bean = (T) outputClass.newInstance();

                    for (Field field : fields) {
                        Object object = rs.getString(field.getName());
                        BeanUtils.setProperty(bean, field.getName(), object);
                    }
                    if (outputList == null) {
                        outputList = new ArrayList<T>();
                    }
                    outputList.add(bean);
                }

            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputList;
    }
}
