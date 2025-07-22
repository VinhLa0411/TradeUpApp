package com.example.tradeup;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class CartManager {
    private static CartManager instance;
    private final Map<String, CartItem> cartMap = new HashMap<>();
    private SharedPreferences prefs;
    private final String PREF_NAME = "cart_prefs";
    private final String KEY_CART = "cart_data";

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void init(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadCartFromPrefs();
    }

    public void addToCart(Product product) {
        if (cartMap.containsKey(product.getId())) {
            increaseQuantity(product);
        } else {
            if (product.getQuantity() > 0) {
                cartMap.put(product.getId(), new CartItem(product, 1));
            }
        }
        saveCartToPrefs();
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : cartMap.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    public void removeFromCart(Product product) {
        cartMap.remove(product.getId());
        saveCartToPrefs();
    }

    public void increaseQuantity(Product product) {
        CartItem item = cartMap.get(product.getId());
        if (item != null) {
            int current = item.getQuantity();
            int max = product.getQuantity();
            if (current < max) {
                item.setQuantity(current + 1);
                saveCartToPrefs();
            }
        }
    }

    public void decreaseQuantity(Product product) {
        if (cartMap.containsKey(product.getId())) {
            CartItem item = cartMap.get(product.getId());
            item.setQuantity(item.getQuantity() - 1);
            if (item.getQuantity() <= 0) {
                cartMap.remove(product.getId());
            }
            saveCartToPrefs();
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartMap.values());
    }

    public void clearCart() {
        cartMap.clear();
        saveCartToPrefs();
    }

    private void saveCartToPrefs() {
        if (prefs == null) return;
        Gson gson = new Gson();
        String json = gson.toJson(getCartItems());
        prefs.edit().putString(KEY_CART, json).apply();
    }

    private void loadCartFromPrefs() {
        if (prefs == null) return;
        String json = prefs.getString(KEY_CART, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CartItem>>(){}.getType();
            List<CartItem> saved = gson.fromJson(json, type);
            for (CartItem item : saved) {
                cartMap.put(item.getProduct().getId(), item);
            }
        }
    }
}
