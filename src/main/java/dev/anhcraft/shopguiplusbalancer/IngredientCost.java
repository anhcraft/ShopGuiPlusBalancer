package dev.anhcraft.shopguiplusbalancer;

import java.util.Map;

public class IngredientCost {
    private double buyCost;
    private double sellProfit;
    private int remainBuy;
    private int remainSell;
    private Map<Product, Integer> buyableIngredients;
    private Map<Product, Integer> sellableIngredients;

    public IngredientCost(double buyCost, double sellProfit, int remainBuy, int remainSell, Map<Product, Integer> buyableIngredients, Map<Product, Integer> sellableIngredients) {
        this.buyCost = buyCost;
        this.sellProfit = sellProfit;
        this.remainBuy = remainBuy;
        this.remainSell = remainSell;
        this.buyableIngredients = buyableIngredients;
        this.sellableIngredients = sellableIngredients;
    }

    public double getBuyCost() {
        return buyCost;
    }

    public double getSellProfit() {
        return sellProfit;
    }

    public int getRemainBuy() {
        return remainBuy;
    }

    public int getRemainSell() {
        return remainSell;
    }

    public String getBuyDesc() {
        StringBuilder sb = new StringBuilder("Mua hết nguyên liệu với giá $" + buyCost + " gồm ");
        for (Product p : buyableIngredients.keySet()) {
            sb.append(p.getItemStack().getI18NDisplayName())
                    .append(" x")
                    .append(buyableIngredients.get(p))
                    .append(" ($").append(p.getBuyPrice() * buyableIngredients.get(p))
                    .append(") ");
        }
        return sb + "; còn dư " + remainBuy + " item không thể mua";
    }

    public String getSellDesc() {
        StringBuilder sb = new StringBuilder("Bán hết nguyên liệu thu về $" + buyCost + " gồm ");
        for (Product p : sellableIngredients.keySet()) {
            sb.append(p.getItemStack().getI18NDisplayName())
                    .append(" x")
                    .append(sellableIngredients.get(p))
                    .append(" ($").append(p.getBuyPrice() * sellableIngredients.get(p))
                    .append(") ");
        }
        return sb + "; còn dư " + remainSell + " item không thể bán";
    }

    public Map<Product, Integer> getBuyableIngredients() {
        return buyableIngredients;
    }

    public Map<Product, Integer> getSellableIngredients() {
        return sellableIngredients;
    }
}
