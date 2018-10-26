package com.abinbev.dsa.model;

import java.util.List;
import java.util.SortedSet;

public class SalesVolumeData {
    private List<SalesVolume> salesVolume;
    private SortedSet<String> uniqueOrderBrand;
    private SortedSet<String> uniqueOrderSku;
    private SortedSet<String> orderSourceList;

    public SalesVolumeData(List<SalesVolume> salesVolume, SortedSet<String> uniqueOrderBrand, SortedSet<String> uniqueOrderSku, SortedSet<String> orderSourceList) {
        this.salesVolume = salesVolume;
        this.uniqueOrderBrand = uniqueOrderBrand;
        this.uniqueOrderSku = uniqueOrderSku;
        this.orderSourceList = orderSourceList;
    }

    public SalesVolumeData() {
    }

    public List<SalesVolume> getSalesVolume() {
        return salesVolume;
    }

    public SortedSet<String> getUniqueOrderBrand() {
        return uniqueOrderBrand;
    }

    public SortedSet<String> getUniqueOrderSku() {
        return uniqueOrderSku;
    }

    public SortedSet<String> getOrderSourceList() {
        return orderSourceList;
    }
}
