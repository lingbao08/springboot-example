package com.example.demo.service;

import com.example.base.common.CommonException;
import com.example.demo.utils.ESSearchDto;
import com.example.demo.utils.ESUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lingbao08
 * @DESCRIPTION es的测试使用类
 * @create 2018/5/22 18:52
 **/

public class ESServiceImpl {

    @Autowired
    private ESUtil esUtil;

    public  void main(String[] args) throws CommonException {
        List<ESSearchDto> list1 = new ArrayList<>();
        //设置查询条件和查询字段，并塞入查询的list中
        ESSearchDto esSearchDto = new ESSearchDto();
        esSearchDto.setField("codeProbe");
        esSearchDto.setValue("20180329");
        list1.add(esSearchDto);

        List<Map<String, String>> list = esUtil.queryAllListByFields("test01", list1, null);
    }
}
