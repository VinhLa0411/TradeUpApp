package com.example.tradeup.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tradeup.LoginActivity;
import com.example.tradeup.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 123;
    private ImageView imgAvatar;
    private TextView tvName, tvDob, tvPhone, tvHomeTown, tvEmail;
    private EditText etName, etDob, etPhone, etHomeTown;
    private Button btnLogout, btnEditOrSave, btnChooseAvatar, btnDeleteAccount;
    private LinearLayout viewInfoLayout, editInfoLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private String avatarBase64 = "";
    private String email = "";
    private Uri selectedImageUri = null;
    private static final String USER_COLLECTION = "users";

    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvName = view.findViewById(R.id.tvName);
        tvDob = view.findViewById(R.id.tvDob);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvHomeTown = view.findViewById(R.id.tvHomeTown);
        tvEmail = view.findViewById(R.id.tvEmail);

        etName = view.findViewById(R.id.etName);
        etDob = view.findViewById(R.id.etDob);
        etPhone = view.findViewById(R.id.etPhone);
        etHomeTown = view.findViewById(R.id.etHomeTown);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditOrSave = view.findViewById(R.id.btnEditOrSave);
        btnChooseAvatar = view.findViewById(R.id.btnChooseAvatar);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        viewInfoLayout = view.findViewById(R.id.viewInfoLayout);
        editInfoLayout = view.findViewById(R.id.editInfoLayout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            email = currentUser.getEmail();
            tvEmail.setText(email);
            loadProfile();
        }

        btnLogout.setOnClickListener(v -> logout());

        btnEditOrSave.setOnClickListener(v -> {
            if (!isEditMode) {
                switchToEditMode();
            } else {
                saveProfile();
            }
        });

        btnChooseAvatar.setOnClickListener(v -> chooseImage());

        imgAvatar.setOnClickListener(v -> {
            if (isEditMode) chooseImage();
        });

        // Ngăn bàn phím bật lên, chỉ chọn qua dialog
        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> {
            if (isEditMode) showStepByStepDatePicker();
        });

        // Xử lý nút Hủy tài khoản
        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        return view;
    }

    private void loadProfile() {
        db.collection(USER_COLLECTION).document(currentUser.getUid())
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String dob = snapshot.getString("dob");
                        String phone = snapshot.getString("phone");
                        String homeTown = snapshot.getString("homeTown");
                        avatarBase64 = snapshot.getString("photoBase64");

                        tvName.setText(name != null && !name.isEmpty() ? name : "Chưa đặt tên");
                        tvDob.setText("Ngày sinh: " + (dob != null ? dob : ""));
                        tvPhone.setText("Số điện thoại: " + (phone != null ? phone : ""));
                        tvHomeTown.setText("Quê quán: " + (homeTown != null ? homeTown : ""));

                        etName.setText(name != null ? name : "");
                        etDob.setText(dob != null ? dob : "");
                        etPhone.setText(phone != null ? phone : "");
                        etHomeTown.setText(homeTown != null ? homeTown : "");

                        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                            byte[] imageBytes = Base64.decode(avatarBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imgAvatar.setImageBitmap(bitmap);
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_user);
                        }
                    }
                    switchToViewMode();
                });
    }

    private void switchToEditMode() {
        isEditMode = true;
        viewInfoLayout.setVisibility(View.GONE);
        editInfoLayout.setVisibility(View.VISIBLE);
        btnEditOrSave.setText("Lưu thông tin");
    }

    private void switchToViewMode() {
        isEditMode = false;
        viewInfoLayout.setVisibility(View.VISIBLE);
        editInfoLayout.setVisibility(View.GONE);
        btnEditOrSave.setText("Chỉnh sửa thông tin");
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgAvatar.setImageURI(selectedImageUri);
            avatarBase64 = imageUriToBase64(selectedImageUri);
        }
    }

    private String imageUriToBase64(Uri uri) {
        try {
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String homeTown = etHomeTown.getText().toString().trim();

        if (name.isEmpty()) { etName.setError("Không được để trống tên!"); return; }
        if (dob.isEmpty()) { etDob.setError("Chọn ngày sinh!"); return; }
        if (phone.isEmpty()) { etPhone.setError("Nhập số điện thoại!"); return; }
        if (homeTown.isEmpty()) { etHomeTown.setError("Nhập quê quán!"); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("name", name);
        update.put("dob", dob);
        update.put("phone", phone);
        update.put("homeTown", homeTown);
        update.put("photoBase64", avatarBase64 != null ? avatarBase64 : "");

        db.collection(USER_COLLECTION).document(currentUser.getUid())
                .update(update)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                    loadProfile(); // Load lại và chuyển về view mode
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // Step-by-step DatePicker: Năm -> Tháng -> Ngày
    private void showStepByStepDatePicker() {
        String[] parts = etDob.getText().toString().split("/");
        final int[] selectedDay = {1};
        final int[] selectedMonth = {1};
        final int[] selectedYear = {2000};
        if (parts.length == 3) {
            selectedDay[0] = Integer.parseInt(parts[0]);
            selectedMonth[0] = Integer.parseInt(parts[1]);
            selectedYear[0] = Integer.parseInt(parts[2]);
        }

        final Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        NumberPicker yearPicker = new NumberPicker(getContext());
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(selectedYear[0]);

        AlertDialog.Builder yearDialog = new AlertDialog.Builder(getContext());
        yearDialog.setTitle("Chọn năm sinh");
        yearDialog.setView(yearPicker);
        yearDialog.setPositiveButton("Tiếp tục", (dialog, which) -> {
            selectedYear[0] = yearPicker.getValue();

            NumberPicker monthPicker = new NumberPicker(getContext());
            monthPicker.setMinValue(1);
            monthPicker.setMaxValue(12);
            monthPicker.setValue(selectedMonth[0]);

            AlertDialog.Builder monthDialog = new AlertDialog.Builder(getContext());
            monthDialog.setTitle("Chọn tháng sinh");
            monthDialog.setView(monthPicker);
            monthDialog.setPositiveButton("Tiếp tục", (dialog2, which2) -> {
                selectedMonth[0] = monthPicker.getValue();

                Calendar tmp = Calendar.getInstance();
                tmp.set(Calendar.YEAR, selectedYear[0]);
                tmp.set(Calendar.MONTH, selectedMonth[0] - 1);
                int maxDay = tmp.getActualMaximum(Calendar.DAY_OF_MONTH);

                NumberPicker dayPicker = new NumberPicker(getContext());
                dayPicker.setMinValue(1);
                dayPicker.setMaxValue(maxDay);
                dayPicker.setValue(selectedDay[0]);

                AlertDialog.Builder dayDialog = new AlertDialog.Builder(getContext());
                dayDialog.setTitle("Chọn ngày sinh");
                dayDialog.setView(dayPicker);
                dayDialog.setPositiveButton("OK", (dialog3, which3) -> {
                    selectedDay[0] = dayPicker.getValue();
                    String dob = String.format("%02d/%02d/%04d", selectedDay[0], selectedMonth[0], selectedYear[0]);
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

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy tài khoản")
                .setMessage("Bạn chắc chắn muốn xóa vĩnh viễn tài khoản? Dữ liệu không thể khôi phục.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAccount() {
        String uid = currentUser.getUid();
        db.collection(USER_COLLECTION).document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa luôn tài khoản Auth
                    currentUser.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Đã xóa tài khoản!", Toast.LENGTH_SHORT).show();
                                logout();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi xóa tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        mAuth.signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        googleSignInClient.signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
