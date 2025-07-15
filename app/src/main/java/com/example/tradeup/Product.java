package com.example.tradeup;

import java.io.Serializable;
import java.util.List;

/**
 * Model đại diện cho một sản phẩm được lưu trữ trong Firestore.
 */
public class Product implements Serializable {
    private String id;              // ID của sản phẩm (Firestore document ID)
    private String name;            // Tên sản phẩm
    private int quantity;           // Số lượng tồn kho
    private String manufacturer;    // Tên hãng sản xuất (brand)
    private int year;               // Năm sản xuất
    private double price;           // Giá sản phẩm
    private List<String> images;    // Danh sách ảnh (base64 hoặc URL)
    private String ownerId;         // ID người đăng sản phẩm
    private String ownerName;       // Tên người đăng (hiển thị)
    private String description;     // Mô tả sản phẩm
    private int views = 0;          // Lượt xem sản phẩm
    private long createdAt = 0L;    // Thời gian đăng (timestamp milliseconds)

    // Constructor mặc định - BẮT BUỘC cho Firestore
    public Product() {}

    // ==================== GETTERS & SETTERS ====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    // Alias để tiện gọi là brand
    public String getBrand() { return manufacturer; }
    public void setBrand(String brand) { this.manufacturer = brand; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // Aliases nếu bạn dùng tên cũ
    public List<String> getImagesBase64() { return images; }
    public void setImagesBase64(List<String> imagesBase64) { this.images = imagesBase64; }

    public List<String> getImageUrls() { return images; }
    public void setImageUrls(List<String> imageUrls) { this.images = imageUrls; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}