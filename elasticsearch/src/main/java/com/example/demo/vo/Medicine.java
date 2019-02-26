package com.example.demo.vo;

import java.util.Date;

/**
 * @author lingbao08
 * @DESCRIPTION
 * @create 2018/6/10 16:18
 **/

public class Medicine {

    private Long medicineId;

    //商业名称
    private String businessName;

    //标准名称
    private String standardName;

    //成分
    private String component;

    //生产日期
    private Date productDate;

    //有效期
    private Date validate;

    private Double price;

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
