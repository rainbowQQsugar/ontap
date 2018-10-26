package com.abinbev.dsa.ui.view.FlexibleData;

/**
 * Created by wandersonblough on 1/26/16.
 */
public enum Category {

    CERVEZAS("Cervezas"),
    WORTHMORE("Worthmore"),
    MAINSTREAM("Mainstream"),
    MALTA("Maltas"),
    JUGO("Jugo"),
    GASEOSAS("Gaseosas");

    String value;

    Category(String value) {
        this.value = value;
    }

    public static Category from(String value) {
        for (Category category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        return null;
    }
}
