package com.abinbev.dsa.model;

import android.text.TextUtils;

import com.salesforce.dsa.data.model.SFBaseObject;

import java.util.Date;

/**
 * Created by Diana Błaszczyk on 27/10/17.
 */

public class SalesVolume extends SFBaseObject {

    public static final String TAG = SalesVolume.class.getSimpleName();

    private String productBrand;
    private String productSKU;
    private String volumeCase;
    private Date orderStartDate;
    private Date orderEndDate;
    private String orderSource;
    private String orderStatus;

    public SalesVolume(String brand, String sku, String volCase, String start, String end) {
        super("SalesVolume");
        this.productBrand = brand;
        this.productSKU = sku;
        this.volumeCase = volCase.isEmpty() ? "0" : volCase;
        if (!TextUtils.isEmpty(start) && !start.equals("null"))
            this.orderStartDate = getDateValueByString(start);
        if (!TextUtils.isEmpty(end) && !end.equals("null"))
            this.orderEndDate = getDateValueByString(end);

    }

    public SalesVolume(SalesVolume s) {
        super("SalesVolume");
        this.productBrand = s.getProductBrand();
        this.productSKU = s.getProductSKU();
        this.volumeCase = s.getVolumeCase();
        this.orderStartDate = s.getStartDate();
        this.orderEndDate = s.getEndDate();
        this.orderStatus = s.getOrderStatus();
        this.orderSource = s.getOrderSource();
    }


    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductSKU() {
        return productSKU;
    }

    public void setProductSKU(String productSKU) {
        this.productSKU = productSKU;
    }

    public String getVolumeCase() {
        return volumeCase;
    }

    public void setVolumeCase(String volumeCase) {
        this.volumeCase = volumeCase;
    }

    public Date getStartDate() {
        return orderStartDate;
    }

    public Date getEndDate() {
        return orderEndDate;
    }

    public void setStartDate(Date d) {
        this.orderStartDate = d;
    }

    public void setEndDate(Date d) {
        this.orderEndDate = d;
    }

    @Override
    public boolean equals(Object obj){
        SalesVolume other = (SalesVolume) obj;
        return this.productBrand.equals(other.getProductBrand()) &&
        this.productSKU.equals(other.getProductSKU()) &&
        this.volumeCase.equals(other.getVolumeCase()) &&
               ((this.orderStartDate != null && other.getStartDate() != null && this.orderStartDate.equals(other.getStartDate()))
                       || (this.orderStartDate == null && other.getStartDate() == null)) &&
               ((this.orderEndDate != null && other.getEndDate() != null && this.orderEndDate.equals(other.getEndDate()))
                       || (this.orderEndDate == null && other.getEndDate() == null));
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
}
