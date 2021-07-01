package me.realized.duels.data;

import lombok.Getter;
import lombok.Setter;

public class AttributeData {

    @Getter
    private String name;
    @Getter
    private String attrName;
    @Getter
    private int operation;
    @Getter
    private double amount;
    @Getter
    @Setter
    private String slot;

    // for Gson
    private AttributeData() {
    }

    public AttributeData(String name, String attrName, int operation, double amount) {
        this.name = name;
        this.attrName = attrName;
        this.operation = operation;
        this.amount = amount;
    }
}
