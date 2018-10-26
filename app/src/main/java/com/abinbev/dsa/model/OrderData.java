package com.abinbev.dsa.model;

import java.util.List;
import java.util.SortedSet;

public class OrderData {
    private List<Order__c> ordersList;
    private SortedSet<String> uniqueOrderBrand;
    private SortedSet<String> uniqueOrderSku;
    private SortedSet<String> orderSourceList;

    public OrderData(List<Order__c> ordersList, SortedSet<String> uniqueOrderBrand, SortedSet<String> uniqueOrderSku, SortedSet<String> orderSourceList) {
        this.ordersList = ordersList;
        this.uniqueOrderBrand = uniqueOrderBrand;
        this.uniqueOrderSku = uniqueOrderSku;
        this.orderSourceList = orderSourceList;
    }

    public List<Order__c> getOrdersList() {
        return ordersList;
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
