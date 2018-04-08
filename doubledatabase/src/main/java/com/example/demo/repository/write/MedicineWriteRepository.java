package com.example.demo.repository.write;

import com.example.demo.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by lingbao08 on 2018/4/8.
 */
public interface MedicineWriteRepository extends JpaRepository<Medicine, Long> {


    @Modifying
    @Query("update Medicine m set m.name=:name where m.id=:id")
    void updateNameById(@Param("name") String name, @Param("id") Long id);


}
