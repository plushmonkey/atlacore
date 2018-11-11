package com.plushnode.atlacore.game.attribute;

public class AttributeModifier {
    private String attribute;
    private ModifierOperation type;
    private double amount;

    public AttributeModifier(String attribute, ModifierOperation type, double amount) {
        this.attribute = attribute;
        this.type = type;
        this.amount = amount;
    }

    public String getAttribute() {
        return attribute;
    }

    public ModifierOperation getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }
}
