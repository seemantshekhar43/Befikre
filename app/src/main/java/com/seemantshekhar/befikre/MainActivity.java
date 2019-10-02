package com.seemantshekhar.befikre;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.seemantshekhar.befikre.Adapter.PostAdapter;
import com.seemantshekhar.befikre.Fragment.HomeFragment;
import com.seemantshekhar.befikre.Fragment.ProfileFragment;
import com.seemantshekhar.befikre.Fragment.SearchFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity  {
    public static final String PROFILE_ID = "profileID";
    private static final String TAG = "MainActivity";
    private boolean isHome = true;

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        if(getIntent().getExtras() != null){
            String profileID = getIntent().getExtras().getString("profileID");
            ProfileFragment profileFragment = new ProfileFragment();
            Bundle data = new Bundle();
            data.putString(PROFILE_ID, profileID);
            profileFragment.setArguments(data);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    profileFragment).disallowAddToBackStack().commit();
            bottomNavigationView.getMenu().findItem(R.id.nav_profile).setChecked(true);

        } else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            isHome = false;
                            break;

                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            isHome = false;
                            break;

                        case R.id.nav_chat:
                            selectedFragment = null;
                            startActivity(new Intent(MainActivity.this, ChatActivity.class));
                            isHome = false;
                            break;

                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            isHome = false;
                            break;
                    }

                    if(selectedFragment != null){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedFragment).commit();
                    }

                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        if (!isHome) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
            isHome = true;
        } else {
            super.onBackPressed();
        }

    }
}
