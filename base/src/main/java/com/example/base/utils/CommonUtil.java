package com.example.base.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {


    /**
     * 字符串转时间
     *
     * @param strDate 日期字符串
     * @param format  转换格式  yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
     * @return
     * @throws ParseException
     */
    public static Date toDate(String strDate, String format) throws ParseException {

        if (!"".equals(strDate) && !"".equals(format)) {

            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date date = formatter.parse(strDate);
            return date;
        }
        return null;
    }

    /**
     * 字符串转明天日期
     *
     * @param date 日期字符串
     * @param format  转换格式  yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd
     * @return
     * @throws ParseException
     */
    public static Date toNextDate(String date, String format) throws ParseException {

        if (!"".equals(date) && !"".equals(format)) {

            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date currDate = formatter.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(currDate);
            c.add(Calendar.DAY_OF_MONTH, 1);// 今天+1天
            Date nextDate = c.getTime();

            return nextDate;
        }
        return null;
    }

    /**
     * 反射获取属性描述器
     * @param clazz
     * @param propertyName
     * @return
     */
    private static PropertyDescriptor getPropertyDescriptor(Class clazz,
                                                            String propertyName) {
        StringBuffer sb = new StringBuffer(); // 构建一个可变字符串用来构建方法名称
        Method setMethod = null;//set方法
        Method getMethod = null;//get方法
        PropertyDescriptor pd = null;//属性描述
        try {
            Field f = clazz.getDeclaredField(propertyName); // 根据字段名来获取字段
            if (f != null) {
                // 构建方法的后缀
                String methodEnd = propertyName.substring(0, 1).toUpperCase()
                        + propertyName.substring(1);

                // 构建set方法
                sb.append("set" + methodEnd);
                setMethod = clazz.getDeclaredMethod(sb.toString(), new Class[]{f.getType()});
                sb.delete(0, sb.length()); // 清空整个可变字符串

                // 构建get方法
                sb.append("get" + methodEnd);
                getMethod = clazz.getDeclaredMethod(sb.toString(), new Class[]{});

                // 构建一个属性描述器 把对应属性 propertyName 的 get 和 set 方法保存到属性描述器中
                pd = new PropertyDescriptor(propertyName, getMethod, setMethod);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pd;
    }

    /**
     * 反射使用set方法
     *
     * @param obj          调用者
     * @param propertyName 调用属性
     * @param value        要设置的值
     */
    public static void setProperty(Object obj, String propertyName, Object value) {
        Class clazz = obj.getClass();// 获取对象的类型
        PropertyDescriptor pd = getPropertyDescriptor(clazz, propertyName);
        Method setMethod = pd.getWriteMethod();// 从属性描述器中获取 set 方法
        try {
            setMethod.invoke(obj, new Object[]{value});// 调用 set
            // 方法将传入的value值保存属性中去
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射使用get方法
     *
     * @param obj          调用者
     * @param propertyName 属性名
     * @return 属性值
     */
    public static Object getProperty(Object obj, String propertyName) {
        Class clazz = obj.getClass();// 获取对象的类型
        PropertyDescriptor pd = getPropertyDescriptor(clazz, propertyName);
        Method getMethod = pd.getReadMethod();// 从属性描述器中获取 get 方法
        Object value = null;
        try {
            value = getMethod.invoke(obj, new Object[]{});// 调用方法获取方法的返回值
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;// 返回值
    }

    /**
     * 数组转换字符串
     *
     * @param arrStr eg:["1","2","3"]
     * @return "1,2,3"
     */
    public static String arr2Str(Object[] arrStr) {
        if(null==arrStr)
            return "";
        if (arrStr.length != 0) {
            String s = Arrays.asList(arrStr).toString();
            s = s.replaceAll(" ","");
            return s.substring(1, s.length() - 1);
        }
        return "";
    }

    /**
     * 分离组合的ID
     *
     * @param ids 传入的id字串数组eg:"ORG_1_1,ORG_2_3"类型标志_菜单ID_权限ID
     * @return map[菜单Id[权限id List[long]]]
     */
    public static Map<String, List<Long>> splitId2Map(String ids) {
        String[] split = ids.split(",");
        Map<String, List<Long>> map = new HashMap<>();
        HashSet<String> set = new HashSet<>();//存储
        for (int i = 0; i < split.length; i++) {
            set.add(split[i].split("_")[1]);
        }
        for (String menuId : set) {
            List<Long> list = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                String[] arr = split[i].split("_");
                if (menuId.equals(arr[1]))
                    list.add(Long.parseLong(arr[2]));
            }
            map.put(menuId, list);
        }
        return map;
    }

    /**
     * 分离组合的ID
     *
     * @param ids 传入的id字串数组eg:"ORG_1_1,ORG_2_3"类型标志_菜单ID_权限ID
     * @return map[菜单Id[权限id List[String]]]
     */
    public static Map<String, List<String>> splitId2StrMap(String ids) {
        String[] split = ids.split(",");
        Map<String, List<String>> map = new HashMap<>();
        HashSet<String> set = new HashSet<>();//存储
        for (int i = 0; i < split.length; i++) {
            set.add(split[i].split("_")[1]);
        }
        for (String menuId : set) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                String[] arr = split[i].split("_");
                if (menuId.equals(arr[1]))
                    list.add(arr[2]);
            }
            map.put(menuId, list);
        }
        return map;
    }

    /**
     * 字符串数组转换为long数组
     * @param strArr
     * @return
     */
    public static Long[] strArr2LongArr(String[] strArr) {
        if (null == strArr) return null;
        Long[] longArr = new Long[strArr.length];
        if (strArr.length > 0) {
            for (int i = 0; i < strArr.length; i++) {
                longArr[i] = Long.parseLong(strArr[i]);
            }
        }
        return longArr;
    }


    public static void main(String[] args) {

        String d = "2017-11-11 11:22:22";

        try {
            Date date = toDate(d, "yyyy-MM-dd HH:mm:ss");
            System.out.print(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
