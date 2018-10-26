package com.abinbev.dsa.bus.event;

import com.abinbev.dsa.model.Negotiation_Item__c;
import com.abinbev.dsa.ui.view.negotiation.Material__c;

import java.util.List;

/**
 * Created by wandersonblough on 12/15/15.
 */
public class NegotiationEvent {

    private NegotiationEvent(){
        super();
    }

    public static class AddItem {
        Material__c material__c;

        public AddItem(Material__c material__c) {
            this.material__c = material__c;
        }

        public Material__c getMaterial__c() {
            return material__c;
        }
    }

    public static class RemoveItem {
        Material__c material__c;

        public RemoveItem(Material__c material__c) {
            this.material__c = material__c;
        }

        public Material__c getMaterial__c() {
            return material__c;
        }
    }

    public static class UpdateItems {
        List<Negotiation_Item__c> items;

        public UpdateItems(List<Negotiation_Item__c> items) {
            this.items = items;
        }

        public List<Negotiation_Item__c> getItems() {
            return items;
        }
    }

    public static class UpdateStartDate {
        String startDate;

        public UpdateStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getStartDate() {
            return startDate;
        }
    }

    public static class UpdateEndDate {
        String endDate;

        public UpdateEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }

    public static class UpdateQuantity {

        String id;
        int quantity;

        public UpdateQuantity(String id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }

        public String getId() {
            return id;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class Classification {

        String value;

        public Classification(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class UpdatePromotionCategory {
        String category;

        public UpdatePromotionCategory(String cat) {
            this.category = cat;
        }

        public String getCategory() {
            return category;
        }
    }

    public static class UpdatePromotionType {
        String type;

        public UpdatePromotionType(String type) {
            this.type = type;
        }

        public String getPromotionType() {
            return type;
        }
    }

    public static class UpdatePromotionDescription {
        String description;

        public UpdatePromotionDescription(String description) {
            this.description = description;
        }

        public String getPromotionDescription() {
            return description;
        }
    }

    public static class UpdatePromotionProductId {
        String productId;

        public UpdatePromotionProductId(String productId) {
            this.productId = productId;
        }

        public String getPromotionProductId() {
            return productId;
        }
    }

}
