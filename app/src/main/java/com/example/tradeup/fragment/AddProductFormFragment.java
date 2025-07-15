package com.example.tradeup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tradeup.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.*;

public class AddProductFormFragment extends Fragment {
    private static final int PICK_IMAGES_REQUEST = 222;

    private EditText etName, etQuantity, etBrand, etYear, etPrice, etDescription;
    private Button btnPickImages, btnSubmitProduct;
    private LinearLayout layoutImages;

    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> selectedImageBase64s = new ArrayList<>();

    // Trạng thái chỉnh sửa (nếu có)
    private boolean isEditMode = false;
    private String editProductId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_form, container, false);

        etName = view.findViewById(R.id.etName);
        etQuantity = view.findViewById(R.id.etQuantity);
        etBrand = view.findViewById(R.id.etBrand);
        etYear = view.findViewById(R.id.etYear);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription); // EditText mô tả
        btnPickImages = view.findViewById(R.id.btnPickImages);
        btnSubmitProduct = view.findViewById(R.id.btnSubmitProduct);
        layoutImages = view.findViewById(R.id.layoutImages);

        // Chỉ cho nhập số vào các trường này (năm, số lượng)
        etYear.setFilters(new InputFilter[]{getOnlyNumberInputFilter()});
        etQuantity.setFilters(new InputFilter[]{getOnlyNumberInputFilter()});

        btnPickImages.setOnClickListener(v -> pickImages());

        // Kiểm tra nếu là chế độ EDIT
        Bundle args = getArguments();
        if (args != null && args.containsKey("productId")) {
            isEditMode = true;
            editProductId = args.getString("productId");
            bindProductDataFromBundle(args);
            btnSubmitProduct.setText("Cập nhật sản phẩm");
        }

        btnSubmitProduct.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateProductToFirestore();
                } else {
                    saveProductToFirestore();
                }
            }
        });

        return view;
    }

    // Hiển thị preview cho tất cả ảnh đã chọn, có nút chọn ảnh đại diện
    private void addAllThumbnails() {
        layoutImages.removeAllViews();
        for (int i = 0; i < selectedImageBase64s.size(); i++) {
            addThumbnail(i);
        }
    }

    private void addThumbnail(int position) {
        String base64 = selectedImageBase64s.get(position);

        // Layout cha
        LinearLayout thumbLayout = new LinearLayout(getContext());
        thumbLayout.setOrientation(LinearLayout.VERTICAL);
        thumbLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Ảnh
        ImageView imageView = new ImageView(getContext());
        int size = (int) (getResources().getDisplayMetrics().density * 80);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(6, 6, 6, 6);
        Bitmap bmp = base64ToBitmap(base64);
        if (bmp != null) imageView.setImageBitmap(bmp);

        // Nút chọn đại diện
        Button btnSetCover = new Button(getContext());
        btnSetCover.setTextSize(12f);
        btnSetCover.setPadding(0,0,0,0);
        btnSetCover.setAllCaps(false);

        if (position == 0) {
            btnSetCover.setText("Đại diện");
            btnSetCover.setEnabled(false);
            btnSetCover.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnSetCover.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            btnSetCover.setText("Đặt làm đại diện");
            btnSetCover.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            btnSetCover.setTextColor(getResources().getColor(android.R.color.white));
            btnSetCover.setOnClickListener(v -> {
                // Đưa ảnh này lên đầu danh sách
                String cover = selectedImageBase64s.remove(position);
                selectedImageBase64s.add(0, cover);
                addAllThumbnails();
            });
        }

        // Thêm vào layout cha
        thumbLayout.addView(imageView);
        thumbLayout.addView(btnSetCover);
        layoutImages.addView(thumbLayout);
    }

    // Dùng khi chọn ảnh mới hoặc khi bind data ở chế độ chỉnh sửa
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUris.clear();
            selectedImageBase64s.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (count > 4) count = 4;
                for (int i = 0; i < count; i++) {
                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(imgUri);
                    selectedImageBase64s.add(uriToBase64(imgUri));
                }
            } else if (data.getData() != null) {
                Uri imgUri = data.getData();
                selectedImageUris.add(imgUri);
                selectedImageBase64s.add(uriToBase64(imgUri));
            }
            addAllThumbnails();
        }
    }

    // Khi vào chế độ EDIT: load lại data + preview ảnh (giữ đúng thứ tự)
    private void bindProductDataFromBundle(Bundle args) {
        etName.setText(args.getString("name", ""));
        etQuantity.setText(String.valueOf(args.getInt("quantity", 1)));
        etBrand.setText(args.getString("brand", ""));
        etYear.setText(String.valueOf(args.getInt("year", 2020)));
        etPrice.setText(NumberFormat.getInstance(new Locale("vi", "VN")).format(args.getDouble("price", 0)));
        etDescription.setText(args.getString("description", ""));

        // Hiển thị ảnh
        ArrayList<String> imagesBase64 = args.getStringArrayList("images");
        if (imagesBase64 != null) {
            selectedImageBase64s = imagesBase64;
            addAllThumbnails();
        }
    }

    private InputFilter getOnlyNumberInputFilter() {
        return (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn tối đa 4 ảnh"), PICK_IMAGES_REQUEST);
    }

    private String uriToBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) { etName.setError("Nhập tên hàng hóa!"); return false; }
        if (etQuantity.getText().toString().trim().isEmpty()) { etQuantity.setError("Nhập số lượng!"); return false; }
        if (etBrand.getText().toString().trim().isEmpty()) { etBrand.setError("Nhập hãng sản xuất!"); return false; }
        if (etYear.getText().toString().trim().isEmpty()) { etYear.setError("Nhập năm sản xuất!"); return false; }
        if (etPrice.getText().toString().trim().isEmpty()) { etPrice.setError("Nhập giá sản phẩm!"); return false; }
        if (selectedImageBase64s.size() < 1) {
            Toast.makeText(getContext(), "Chọn ít nhất 1 ảnh!", Toast.LENGTH_SHORT).show(); return false;
        }
        if (selectedImageBase64s.size() > 4) {
            Toast.makeText(getContext(), "Chọn tối đa 4 ảnh!", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }

    // THÊM MỚI
    private void saveProductToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = etName.getText().toString().trim();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
        String brand = etBrand.getText().toString().trim();
        int year = Integer.parseInt(etYear.getText().toString().trim());
        String description = etDescription.getText().toString().trim();

        String priceStr = etPrice.getText().toString().replace(".", "").replace(",", "").trim();
        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            etPrice.setError("Giá không hợp lệ!"); return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("quantity", quantity);
        product.put("brand", brand);
        product.put("year", year);
        product.put("price", price);
        product.put("images", selectedImageBase64s); // Ảnh đầu tiên là ảnh đại diện!
        product.put("description", description);
        product.put("ownerId", uid);
        product.put("createdAt", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String ownerName = snapshot.getString("name");
                    product.put("ownerName", ownerName);

                    FirebaseFirestore.getInstance().collection("products")
                            .add(product)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(requireContext(), "Đăng sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Không lấy được tên người đăng!", Toast.LENGTH_SHORT).show();
                });
    }

    // CHỈNH SỬA (UPDATE)
    private void updateProductToFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = etName.getText().toString().trim();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());
        String brand = etBrand.getText().toString().trim();
        int year = Integer.parseInt(etYear.getText().toString().trim());
        String description = etDescription.getText().toString().trim();

        String priceStr = etPrice.getText().toString().replace(".", "").replace(",", "").trim();
        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            etPrice.setError("Giá không hợp lệ!"); return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("quantity", quantity);
        product.put("brand", brand);
        product.put("year", year);
        product.put("price", price);
        product.put("images", selectedImageBase64s); // Ảnh đầu tiên là ảnh đại diện!
        product.put("description", description);
        product.put("ownerId", uid);

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String ownerName = snapshot.getString("name");
                    product.put("ownerName", ownerName);

                    FirebaseFirestore.getInstance().collection("products")
                            .document(editProductId)
                            .update(product)
                            .addOnSuccessListener(doc -> {
                                Toast.makeText(requireContext(), "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Không lấy được tên người đăng!", Toast.LENGTH_SHORT).show();
                });
    }
}