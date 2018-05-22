package com.example.demo.service;

import com.example.demo.entity.Medicine;
import com.example.demo.repository.read.MedicineReadRepository;
import com.example.demo.repository.write.MedicineWriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.Predicate;

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

    /**
     * 这是适用于在查询字段或有或无的情况下使用的方法
     *
     * @param name
     * @return
     */
    public Page findByName(String name, String age,Pageable pageable) {

        Specification querySpecifi = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("state"), "0");
            if (null != name) {
                //root.get("userName")表达式，userName为实体中的字段名称。
                //如果用户名传空字串的话，%+""+%表示查询全部。
                //如果用户名传带空格的空字串的话，%+" "+%表示用户的意愿是查询带"空格"的全部。
                predicate = cb.and(cb.like(root.get("userName"), "%" + name + "%"));
            }

            if (StringUtils.isNotBlank(age)) {
                predicate = cb.and(cb.equal(root.get("age"), age));
            }
            query.orderBy(cb.desc(root.get("id").as(Integer.class)));
            return predicate;
        };
        //返回SpringJPA里的page类型
        Page page = medicineReadRepository.findAll(querySpecifi, pageable);

        return page;
    }

    @Override
    public Medicine add(Medicine medicine) {
        return medicineWriteRepository.save(medicine);
    }
}
