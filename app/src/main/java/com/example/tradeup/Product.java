package com.example.tradeup;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    private String id;
    private String name;
    private int quantity;
    private String manufacturer;   // Lưu brand (hãng)
    private int year;
    private double price;
    private List<String> images;   // Danh sách base64 hoặc URL ảnh (tùy bạn lưu Firestore)
    private String ownerId;        // ID Firebase người đăng
    private String ownerName;      // Tên người đăng (hiện lên trang chủ)
    private String description;    // Mô tả sản phẩm (tùy chọn)

    public Product() {} // BẮT BUỘC cho Firestore

    private int views = 0;
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    // ---- GETTERS & SETTERS ----
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    // Alias cho brand
    public String getBrand() { return manufacturer; }
    public void setBrand(String brand) { this.manufacturer = brand; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Ảnh (base64 hoặc URL)
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // Alias cho tương thích các adapter cũ/new
    public List<String> getImagesBase64() { return images; }
    public void setImagesBase64(List<String> imagesBase64) { this.images = imagesBase64; }

    public List<String> getImageUrls() { return images; }
    public void setImageUrls(List<String> imageUrls) { this.images = imageUrls; }

    // Người đăng
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    // Mô tả sản phẩm
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
