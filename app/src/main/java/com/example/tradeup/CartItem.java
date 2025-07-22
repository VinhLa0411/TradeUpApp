package com.example.tradeup;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity > 0 ? quantity : 1;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        if (quantity > 1) {
            this.quantity--;
        }
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public int getTotalPrice() {
        return (int) (product.getPrice() * quantity);
    }
}
