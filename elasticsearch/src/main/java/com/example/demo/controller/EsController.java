package com.example.demo.controller;

import com.example.base.common.CommonException;
import com.example.demo.service.ESServiceImpl;
import com.example.demo.vo.Medicine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/6/10 16:16
 **/

@RestController
@RequestMapping("/es")
public class EsController {

    @Autowired
    private ESServiceImpl esService;

    @PostMapping("/index")
    public String index(@RequestBody Medicine medicine) {

        boolean index = esService.index(medicine);
        return index ? "成功" : "失败";
    }

    @GetMapping("/q1")
    public String q1() {
        System.out.println("111");
        try {
            List list = esService.q1();
            return list.toString();
        } catch (CommonException e) {
            e.printStackTrace();
            return "错误";
        }

    }
}
