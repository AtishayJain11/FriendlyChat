package project.beryl.com.newfirebaseapplication.Activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import de.hdodenhof.circleimageview.CircleImageView;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.PagerAdapter;
import project.beryl.com.newfirebaseapplication.background_service.ServiceNoDelay;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivMenu;
    private CircleImageView ivProfilePic;
    private TabLayout tabLayout;
    private ProgressDialog pd;
    private DatabaseReference mDatabaseReference;
    public static final int NOTIFICATION_ID = 888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
       // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // Background Service
        ServiceNoDelay mSensorService = new ServiceNoDelay(getApplicationContext());
        Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
            startService(mServiceIntent);
        }

        initViews();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("Chats"));
        tabLayout.addTab(tabLayout.newTab().setText("Requests"));


        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        // tabLayout.setTabGravity(TableLayout.TEXT_ALIGNMENT_GRAVITY);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }


        });
        //  setupTabIcons();
    }

    private void initViews() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ivMenu = (ImageView) findViewById(R.id.iv_menu);
        ivProfilePic = (CircleImageView) findViewById(R.id.iv_profile_pic);

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait....");
        //------registering listeners

        ivMenu.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_menu:
                openDialog();
        }
    }

    /**
     * open dialog
     */
    private void openDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_menu_home);
        TextView tvLogout = dialog.findViewById(R.id.tv_logout);
        TextView tvUsers = dialog.findViewById(R.id.tv_all_users);
        TextView tvAccountSettings = dialog.findViewById(R.id.tv_account_settings);
        dialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                mDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);
                auth.signOut();
                auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                        if (firebaseAuth.getCurrentUser() == null) {
                            pd.dismiss();
                            AppSharedPreferences.clearAllPrefs(MainActivity.this);
                            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            dialog.dismiss();
                        } else {

                        }
                    }
                });
            }
        });


        tvUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, AllUsersActivity.class));
            }
        });


        tvAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseReference.child("online").setValue("true");

        if ((AppSharedPreferences.getString(this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE)) != null) {
            if (!AppSharedPreferences.getString(this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE).equals("default"))
                Glide.with(this).load(AppSharedPreferences.getString(this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE)).centerCrop().into(ivProfilePic);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseReference.child("online").setValue(ServerValue.TIMESTAMP);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}