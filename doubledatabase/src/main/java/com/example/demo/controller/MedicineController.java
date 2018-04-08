package com.example.demo.controller;

import com.example.demo.entity.Medicine;
import com.example.demo.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lingbao08 on 2018/4/8.
 * 本项目只做整理
 */
@RequestMapping("/medicine")
@RestController
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @RequestMapping("findById/{id}")
    public Medicine findById(@PathVariable Long id){
        return medicineService.findById(id);
    }
}
