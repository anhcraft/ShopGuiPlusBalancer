package dev.anhcraft.shopguiplusbalancer;

import com.google.common.collect.Lists;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.shop.ShopsNotLoadedException;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.stream.Collectors;

public class Runner {

    /**
     * Nguyên tắc: giá trị sản phẩm cao hơn giá trị thành phần
     * 1. Gía mua sản phẩm phải cao hơn giá mua nguyên liệu
     * 2. Gía bán sản phẩm phải cao hơn giá bán nguyên liệu
     */

    private final CommandSender commandSender;
    private HashMap<ItemStack, Product> shopItems;

    public Runner(CommandSender commandSender) {
        this.commandSender = commandSender;
    }

    public void run() throws ShopsNotLoadedException {
        List<ShopItem> allItems = ShopGuiPlusApi.getPlugin().getShopManager().getShops().stream()
                .flatMap(shop -> shop.getShopItems().stream())
                .filter(shopItem -> shopItem.getType() == ShopItemType.ITEM).collect(Collectors.toList());

        commandSender.sendMessage("Đã tải " + allItems.size() + " items");

        checkDupe(allItems);
    }

    private void checkDupe(List<ShopItem> allItems) {
        shopItems = new HashMap<>();
        int i = 0;

        for (ShopItem shopItem : allItems) {
            Product found = shopItems.get(shopItem.getItem());
            if (found != null) {
                commandSender.sendMessage(ChatColor.RED + String.format(
                        "Phát hiện trùng lặp: slot %d shop %s >< slot %d shop %s",
                        shopItem.getSlot(),
                        shopItem.getShop().getId(),
                        found.getSlot(),
                        found.getShop()
                ));
                i++;
            } else {
                ItemStack unit = shopItem.getItem().asOne();
                shopItems.put(unit, new Product(
                        unit,
                        shopItem.getBuyPriceForAmount(1),
                        shopItem.getSellPriceForAmount(1),
                        shopItem.getSlot(),
                        shopItem.getShop().getId()
                ));
            }
        }

        if (i > 0) {
            commandSender.sendMessage("Vui lòng xử lý các lỗi trên rồi chạy /sgpb để tiếp tục.");
        } else {
            checkRecipe();
        }
    }

    private void checkRecipe() {
        int foundErrors = 0;
        for (Product product : shopItems.values()) {
            for (Recipe recipe : Bukkit.getRecipesFor(product.getItemStack())) {
                if (foundErrors > 10) {
                    commandSender.sendMessage("Vui lòng xử lý các lỗi trên rồi chạy /sgpb để tiếp tục.");
                    return;
                }

                if (product.getBuyPrice() > 0 && product.getSellPrice() > 0 && product.getSellPrice() - product.getBuyPrice() > 0.1) {
                    commandSender.sendMessage(ChatColor.RED + String.format(
                            "[Lỗi] Phát hiện lỗi shop giá mua thấp hơn giá bán (slot %d shop %s)",
                            product.getSlot(),
                            product.getShop()
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x1.5 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 1.5
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x2 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 2
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x5 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 5
                    ));
                }

                if (product.getBuyPrice() > 0 && product.getSellPrice() > 0 && product.getBuyPrice() / product.getSellPrice() > 7) {
                    commandSender.sendMessage(ChatColor.RED + String.format(
                            "[Lỗi] Phát hiện lỗi shop giá mua quá cao, gấp >=7 lần so với giá bán (slot %d shop %s)",
                            product.getSlot(),
                            product.getShop()
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x1.5 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 1.5
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x2 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 2
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: chỉnh giá mua (buy price) %s là $%.2f/cái (x5 lần giá bán)",
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice() * 5
                    ));
                }

                Collection<ItemStack> ingredients;
                String action;

                if (recipe instanceof CookingRecipe) {
                    ingredients = Collections.singleton(((CookingRecipe<?>) recipe).getInput());
                    action = "nung";
                } else if (recipe instanceof ShapedRecipe) {
                    ingredients = ((ShapedRecipe) recipe).getIngredientMap().values();
                    action = "chế tạo";
                } else if (recipe instanceof ShapelessRecipe) {
                    ingredients = ((ShapelessRecipe) recipe).getIngredientList();
                    action = "chế tạo";
                } else if (recipe instanceof SmithingRecipe) {
                    ingredients = Lists.newArrayList(
                            ((SmithingRecipe) recipe).getBase().getItemStack(),
                            ((SmithingRecipe) recipe).getAddition().getItemStack()
                    );
                    action = "smith";
                } else if (recipe instanceof StonecuttingRecipe) {
                    ingredients = Collections.singleton(((StonecuttingRecipe) recipe).getInput());
                    action = "cắt đá";
                } else if (recipe instanceof MerchantRecipe) {
                    ingredients = ((MerchantRecipe) recipe).getIngredients();
                    action = "trade";
                } else {
                    continue;
                }

                IngredientCost ic = calculateIngredientCost(ingredients);

                // buy check
                if (product.getBuyPrice() > 0 && ic.getBuyCost() > 0 && ic.getBuyCost()/(product.getBuyPrice() * recipe.getResult().getAmount()) > 2) {
                    foundErrors++;
                    commandSender.sendMessage(ChatColor.RED + String.format(
                            "[Lỗi] Phát hiện lỗi shop giá mua (buy) sản phẩm thấp hơn 50%% so với giá mua các nguyên liệu (slot %d shop %s)",
                            product.getSlot(),
                            product.getShop()
                    ));
                    commandSender.sendMessage(String.format(
                            "Công thức %s gồm có:",
                            action
                    ));
                    for (Map.Entry<Product, Integer> e : ic.getBuyableIngredients().entrySet()) {
                        commandSender.sendMessage(String.format(
                                "- x%d %s giá $%.2f/cái, tổng $%.2f",
                                e.getValue(),
                                e.getKey().getItemStack().getI18NDisplayName(),
                                e.getKey().getBuyPrice(),
                                e.getKey().getBuyPrice() * e.getValue()
                        ));
                    }
                    commandSender.sendMessage(String.format(
                            "(Tổng giá nguyên liệu là $%.2f)",
                            ic.getBuyCost()
                    ));
                    commandSender.sendMessage(String.format(
                            "=> Tạo ra x%d %s giá $%.2f/cái, tổng $%.2f",
                            recipe.getResult().getAmount(),
                            product.getItemStack().getI18NDisplayName(),
                            product.getBuyPrice(),
                            product.getBuyPrice() * recipe.getResult().getAmount()
                    ));
                    commandSender.sendMessage(String.format(
                            "* Như vậy, mua thẳng sản phẩm sẽ lời $%.2f so với mua các nguyên liệu",
                            ic.getBuyCost() - (product.getBuyPrice() * recipe.getResult().getAmount())
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: nâng giá mua (buy price) %s lên ít nhất $%.2f/cái",
                            product.getItemStack().getI18NDisplayName(),
                            ic.getBuyCost()/recipe.getResult().getAmount()
                    ));
                }

                // sell check
                else if (product.getSellPrice() > 0 && ic.getSellProfit() > 0 && ic.getSellProfit() / (product.getSellPrice() * recipe.getResult().getAmount()) > 2) {
                    foundErrors++;
                    commandSender.sendMessage(ChatColor.RED + String.format(
                            "[Lỗi] Phát hiện lỗi shop giá bán (sell) sản phẩm thấp hơn 50%% so với bán các nguyên liệu (slot %d shop %s)",
                            product.getSlot(),
                            product.getShop()
                    ));
                    commandSender.sendMessage(String.format(
                            "Công thức %s gồm có:",
                            action
                    ));
                    for (Map.Entry<Product, Integer> e : ic.getSellableIngredients().entrySet()) {
                        commandSender.sendMessage(String.format(
                                "- x%d %s giá $%.2f/cái, tổng $%.2f",
                                e.getValue(),
                                e.getKey().getItemStack().getI18NDisplayName(),
                                e.getKey().getSellPrice(),
                                e.getKey().getSellPrice() * e.getValue()
                        ));
                    }
                    commandSender.sendMessage(String.format(
                            "(Tổng giá nguyên liệu là $%.2f)",
                            ic.getSellProfit()
                    ));
                    commandSender.sendMessage(String.format(
                            "=> Tạo ra x%d %s giá $%.2f/cái, tổng $%.2f",
                            recipe.getResult().getAmount(),
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice(),
                            product.getSellPrice() * recipe.getResult().getAmount()
                    ));
                    commandSender.sendMessage(String.format(
                            "* Như vậy, bán thẳng sản phẩm sẽ lỗ $%.2f so với bán lần lượt các nguyên liệu",
                            ic.getSellProfit() - product.getSellPrice() * recipe.getResult().getAmount()
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: nâng giá bán (sell price) %s lên ít nhất $%.2f/cái",
                            product.getItemStack().getI18NDisplayName(),
                            ic.getSellProfit()/recipe.getResult().getAmount()
                    ));
                }


                // recipe check
                // warn: phải mua hết nguyên liệu, ko chừa item nào
                else if (product.getSellPrice() > 0 && ic.getBuyCost() > 0 && product.getSellPrice() - ic.getBuyCost() > 0.1 && ic.getRemainBuy() == 0) {
                    foundErrors++;
                    commandSender.sendMessage(ChatColor.RED + String.format(
                            "[Lỗi] Phát hiện có thể mua nguyên liệu giá rẻ và %s sản phẩm bán kiếm lời (slot %d shop %s)",
                            action,
                            product.getSlot(),
                            product.getShop()
                    ));
                    commandSender.sendMessage(String.format(
                            "Công thức %s gồm có:",
                            action
                    ));
                    for (Map.Entry<Product, Integer> e : ic.getBuyableIngredients().entrySet()) {
                        commandSender.sendMessage(String.format(
                                "- x%d %s giá $%.2f/cái, tổng $%.2f",
                                e.getValue(),
                                e.getKey().getItemStack().getI18NDisplayName(),
                                e.getKey().getBuyPrice(),
                                e.getKey().getBuyPrice() * e.getValue()
                        ));
                    }
                    commandSender.sendMessage(String.format(
                            "(Tổng giá nguyên liệu là $%.2f)",
                            ic.getBuyCost()
                    ));
                    commandSender.sendMessage(String.format(
                            "=> Tạo ra x%d %s, có thể bán giá $%.2f/cái, thu về $%.2f",
                            recipe.getResult().getAmount(),
                            product.getItemStack().getI18NDisplayName(),
                            product.getSellPrice(),
                            product.getSellPrice() * recipe.getResult().getAmount()
                    ));
                    commandSender.sendMessage(String.format(
                            "* Như vậy, thu lời $%.2f",
                            ic.getBuyCost() - (product.getSellPrice() * recipe.getResult().getAmount())
                    ));
                    commandSender.sendMessage(ChatColor.GOLD + String.format(
                            "* Gợi ý: hạ giá bán (sell price) %s xuống $%.2f/cái",
                            product.getItemStack().getI18NDisplayName(),
                            ic.getBuyCost()/recipe.getResult().getAmount()
                    ));
                }
            }
        }
    }

    private IngredientCost calculateIngredientCost(Collection<ItemStack> input) {
        double buyCost = 0;
        double sellCost = 0;
        int failedToBuy = 0;
        int failedToSell = 0;
        Map<Product, Integer> buyableIngredients = new HashMap<>();
        Map<Product, Integer> sellableIngredients = new HashMap<>();
        for (ItemStack item : input) {
            Product p = shopItems.get(item);
            if (p == null) {
                failedToBuy++;
                failedToSell++;
            } else {
                if (p.getBuyPrice() == 0) failedToBuy++;
                else if (p.getSellPrice() == 0) failedToSell++;
                else {
                    buyCost += p.getBuyPrice() * item.getAmount();
                    if (buyableIngredients.containsKey(p)) {
                        buyableIngredients.put(p, buyableIngredients.get(p) + item.getAmount());
                    } else {
                        buyableIngredients.put(p, item.getAmount());
                    }
                    sellCost += p.getSellPrice() * item.getAmount();
                    if (sellableIngredients.containsKey(p)) {
                        sellableIngredients.put(p, sellableIngredients.get(p) + item.getAmount());
                    } else {
                        sellableIngredients.put(p, item.getAmount());
                    }
                }
            }
        }
        return new IngredientCost(buyCost, sellCost, failedToBuy, failedToSell, buyableIngredients, sellableIngredients);
    }
}
