package com.example.demo.service;

import com.example.demo.entity.Medicine;

/**
 * Created by lingbao08 on 2018/4/8.
 */
public interface MedicineService {

    Medicine findById(Long id);

    Medicine add(Medicine medicine);
}
