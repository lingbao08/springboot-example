package com.example.demo.service;

import com.example.base.common.CommonException;
import com.example.demo.utils.ESSearchDto;
import com.example.demo.utils.ESUtil;
import com.example.demo.vo.Medicine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lingbao08
 * @DESCRIPTION es的测试使用类
 * @create 2018/5/22 18:52
 **/
@Service
public class ESServiceImpl {

    @Autowired
    private ESUtil esUtil;

    public boolean index(Medicine medicine){
        try {
            boolean b = esUtil.indexOne("hospital", "medicine", medicine);
            return b;
        } catch (CommonException e) {
            e.printStackTrace();
            return false;
        }
    }


    public  List q1() throws CommonException {
        List<ESSearchDto> list1 = new ArrayList<>();
        //设置查询条件和查询字段，并塞入查询的list中
        ESSearchDto esSearchDto = new ESSearchDto();
        esSearchDto.setField("standardName");
        esSearchDto.setValue("银桥");
        list1.add(esSearchDto);

        List<Map<String, String>> list = esUtil.queryAllListByFields("hospital", list1, null);

        System.out.println(list);

        return list;
    }
}
