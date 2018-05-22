package com.example.base.utils;

import org.dozer.DozerBeanMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Dozer转换实体类 示例： VineUser vineUser=(VineUser)DozerUtils.dozerObject(vineUserVo,new VineUser());
 */

public class DozerUtil {

    private static DozerBeanMapper mapper = new DozerBeanMapper();

    /**
     *
     * @param formObject 要转换的实体类
     * @param tClass  目标实体类.class
     * @param <T> 返回类型（和目标实体类一样）
     * @return
     */
    public static <T> T dozerObject(Object formObject,Class<T> tClass){

        T map=mapper.map(formObject, tClass);

        return map;
    }

    public static <T> List<T> dozerList(Collection c, Class<T> tClass){
        ArrayList<T> tList = new ArrayList<>();
        for (Object o:c) {
            tList.add(dozerObject(o, tClass));
        }
        return tList;
    }


}
