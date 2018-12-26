package project.beryl.com.newfirebaseapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(AppSharedPreferences.getBoolean(SplashActivity.this, AppSharedPreferences.PREF_KEY.ISLOGIN)){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(SplashActivity.this,LoginSignUpActivity.class));
                    finish();
                }
            }
        }, 2*1000);
    }
}
