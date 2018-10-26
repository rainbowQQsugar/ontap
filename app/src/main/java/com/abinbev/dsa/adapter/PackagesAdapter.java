package com.abinbev.dsa.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.abinbev.dsa.model.Paquetes_por_segmento__c;
import com.abinbev.dsa.ui.view.negotiation.PackagePageView;
import com.abinbev.dsa.ui.view.negotiation.PackagesPager;

import java.util.List;

/**
 * Created by wandersonblough on 12/11/15.
 */
public class PackagesAdapter extends PagerAdapter implements PackagesPager.ChildHelper, AdapterParameters {

    List<Paquetes_por_segmento__c> packages;
    String customerName;
    PackagePageView currentChild;

    public PackagesAdapter(List<Paquetes_por_segmento__c> packages, String customerName) {
        super();
        this.packages = packages;
        this.customerName = customerName;
    }

    public String getName(int position){
        return packages.get(position).getPaqueteName();
    }

    @Override
    public PackagePageView getCurrentChild() {
        return currentChild;
    }

    @Override
    public Paquetes_por_segmento__c getChildAt(int position) {
        return packages != null ? packages.get(position) : null;
    }

    @Override
    public int getCount() {
        return packages == null ? 0 : packages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PackagePageView pageView = new PackagePageView(container.getContext());
        pageView.setCustomerName(customerName);
        pageView.setPaquete(packages.get(position));
        container.addView(pageView);
        return pageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        this.currentChild = (PackagePageView) object;
    }
}
