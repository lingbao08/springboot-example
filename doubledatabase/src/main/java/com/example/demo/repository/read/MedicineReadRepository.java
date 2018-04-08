package com.example.demo.repository.read;

import com.example.demo.entity.Medicine;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by lingbao08 on 2018/4/8.
 * 在读操作中多继承了一个Specification。是为了应对规则查询，即某些无法直接用JPA写出的查询
 */
public interface MedicineReadRepository extends JpaRepository<Medicine,Long>,Specification {
}
