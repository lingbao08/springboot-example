package com.example.demo.service;

import com.example.demo.entity.Medicine;
import com.example.demo.repository.read.MedicineReadRepository;
import com.example.demo.repository.write.MedicineWriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lingbao08 on 2018/4/8.
 */
@Service
public class MedicineServiceImpl implements MedicineService {

    @Autowired
    private MedicineReadRepository medicineReadRepository;
    @Autowired
    private MedicineWriteRepository medicineWriteRepository;

    @Override
    public Medicine findById(Long id) {
        Medicine medicine = medicineReadRepository.findById(id).orElse(null);
        return medicine;
    }
}
