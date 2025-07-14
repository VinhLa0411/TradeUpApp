package com.example.tradeup;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView imgAvatar;
    private EditText etName, etDob, etPhone, etHomeTown, etEmail;
    private Button btnSave;

    private String avatarBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        imgAvatar = findViewById(R.id.imgAvatar);
        etName = findViewById(R.id.etName);
        etDob = findViewById(R.id.etDob);
        etPhone = findViewById(R.id.etPhone);
        etHomeTown = findViewById(R.id.etHomeTown);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);

        // Set email từ FirebaseAuth (không cho sửa)
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        etEmail.setText(email);

        imgAvatar.setOnClickListener(v -> chooseImage());

        // Step-by-step DatePicker cho ngày sinh
        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> showStepByStepDatePicker());

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
            avatarBase64 = imageUriToBase64(imageUri);
        }
    }

    private String imageUriToBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Step-by-step chọn năm → tháng → ngày
    private void showStepByStepDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        NumberPicker yearPicker = new NumberPicker(this);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(2000);

        AlertDialog.Builder yearDialog = new AlertDialog.Builder(this);
        yearDialog.setTitle("Chọn năm sinh");
        yearDialog.setView(yearPicker);
        yearDialog.setPositiveButton("Tiếp tục", (dialog, which) -> {
            int selectedYear = yearPicker.getValue();

            NumberPicker monthPicker = new NumberPicker(this);
            monthPicker.setMinValue(1);
            monthPicker.setMaxValue(12);
            monthPicker.setValue(1);

            AlertDialog.Builder monthDialog = new AlertDialog.Builder(this);
            monthDialog.setTitle("Chọn tháng sinh");
            monthDialog.setView(monthPicker);
            monthDialog.setPositiveButton("Tiếp tục", (dialog2, which2) -> {
                int selectedMonth = monthPicker.getValue();

                Calendar tmp = Calendar.getInstance();
                tmp.set(Calendar.YEAR, selectedYear);
                tmp.set(Calendar.MONTH, selectedMonth - 1);
                int maxDay = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);

                NumberPicker dayPicker = new NumberPicker(this);
                dayPicker.setMinValue(1);
                dayPicker.setMaxValue(maxDay);
                dayPicker.setValue(1);

                AlertDialog.Builder dayDialog = new AlertDialog.Builder(this);
                dayDialog.setTitle("Chọn ngày sinh");
                dayDialog.setView(dayPicker);
                dayDialog.setPositiveButton("OK", (dialog3, which3) -> {
                    int selectedDay = dayPicker.getValue();
                    String dob = String.format("%02d/%02d/%04d", selectedDay, selectedMonth, selectedYear);
                    etDob.setText(dob);
                });
                dayDialog.setNegativeButton("Hủy", null);
                dayDialog.show();

            });
            monthDialog.setNegativeButton("Hủy", null);
            monthDialog.show();

        });
        yearDialog.setNegativeButton("Hủy", null);
        yearDialog.show();
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String homeTown = etHomeTown.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate cơ bản
        if (name.isEmpty()) { etName.setError("Nhập tên!"); return; }
        if (dob.isEmpty()) { etDob.setError("Chọn ngày sinh!"); return; }
        if (phone.isEmpty()) { etPhone.setError("Nhập số điện thoại!"); return; }
        if (homeTown.isEmpty()) { etHomeTown.setError("Nhập quê quán!"); return; }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("dob", dob);
        userProfile.put("phone", phone);
        userProfile.put("homeTown", homeTown);
        userProfile.put("email", email);
        userProfile.put("photoBase64", avatarBase64); // Ảnh base64, nếu chưa chọn là ""
        userProfile.put("bio", "");
        userProfile.put("contact", phone);
        userProfile.put("rating", 5.0);
        userProfile.put("reviewCount", 0);
        userProfile.put("isActive", true);

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .set(userProfile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
