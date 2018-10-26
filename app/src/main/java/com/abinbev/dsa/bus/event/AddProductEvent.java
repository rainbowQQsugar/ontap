package com.abinbev.dsa.bus.event;

import com.abinbev.dsa.adapter.MaterialGiveAdapter;
import com.abinbev.dsa.model.Product;

public class AddProductEvent {

    private AddProductEvent(){
        super();
    }

    public static class AddProduct {

        private final MaterialGiveAdapter.LineItem lineItem;

        private final Product product;

        public AddProduct(MaterialGiveAdapter.LineItem lineItem) {
            this.lineItem = lineItem;
            this.product = null;
        }

        public AddProduct(Product product) {
            this.product = product;
            this.lineItem = null;
        }

        public MaterialGiveAdapter.LineItem getLineItem() {
            return lineItem;
        }

        public Product getProduct() {
            return product;
        }
    }

}
