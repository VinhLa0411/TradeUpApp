package com.example.tradeup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.tradeup.fragment.AccountFragment;
import com.example.tradeup.fragment.AddFragment;
import com.example.tradeup.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        CartManager.getInstance().init(this);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        // Load Fragment đầu tiên là Home
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selected = new HomeFragment();
            }
             else if (id == R.id.nav_add) {
                selected = new AddFragment();
            } else if (id == R.id.nav_chat) {
                selected = new ChatFragment();
            } else if (id == R.id.nav_account) {
                selected = new AccountFragment();
            }
            if (selected != null) loadFragment(selected);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
