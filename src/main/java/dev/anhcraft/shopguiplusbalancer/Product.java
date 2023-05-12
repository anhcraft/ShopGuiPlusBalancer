package dev.anhcraft.shopguiplusbalancer;

import org.bukkit.inventory.ItemStack;

public class Product {
    private ItemStack itemStack;
    private double buyPrice;
    private double sellPrice;
    private int slot;
    private String shop;

    public Product(ItemStack itemStack, double buyPrice, double sellPrice, int slot, String shop) {
        this.itemStack = itemStack;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
        this.shop = shop;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getSlot() {
        return slot;
    }

    public String getShop() {
        return shop;
    }
}
