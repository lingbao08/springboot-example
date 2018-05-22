package com.example.demo.controller;

import com.example.demo.entity.Medicine;
import com.example.demo.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by lingbao08 on 2018/4/8.
 * 本项目只做整理
 */
@RequestMapping("/medicine")
@RestController
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("findById/{id}")
    public Medicine findById(@PathVariable Long id){
        return medicineService.findById(id);
    }


    @PostMapping("add")
    public Medicine add(@RequestBody Medicine medicine){
        medicine = medicineService.add(medicine);
        return medicine;
    }
}
