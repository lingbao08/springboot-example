package com.example.demo.entity;

import javax.persistence.Entity;
import java.util.Date;

/**
 * 药品类
 * Created by lingbao08 on 2018/4/8.
 */
@Entity
public class Medicine {

    private Long id;

    //药品名称
    private String name;

    //成分
    private String component;

    //生产日期
    private Date productDate;

    //有效期
    private Date validate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Date getProductDate() {
        return productDate;
    }

    public void setProductDate(Date productDate) {
        this.productDate = productDate;
    }

    public Date getValidate() {
        return validate;
    }

    public void setValidate(Date validate) {
        this.validate = validate;
    }
}
