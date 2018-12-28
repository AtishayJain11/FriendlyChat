package project.beryl.com.newfirebaseapplication.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Map;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;

public class SplashActivity extends AppCompatActivity {

    private ConstraintLayout root_layout;
    private NotificationManagerCompat mNotificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);
        root_layout = (ConstraintLayout)findViewById(R.id.root_layout);
        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        doPermission();

    }

    void doPermission(){
        boolean areNotificationsEnabled = mNotificationManagerCompat.areNotificationsEnabled();
        if (!areNotificationsEnabled) {
            // Because the user took an action to create a notification, we create a prompt to let
            // the user re-enable notifications for this application again.
            Snackbar snackbar = Snackbar
                    .make(root_layout, "You need to enable notifications for this app", Snackbar.LENGTH_LONG)
                    .setAction("ENABLE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Links to this app's notification settings
                            openNotificationSettingsForApp();
                        }
                    });
            snackbar.show();
            return;
        }else {
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

    private void openNotificationSettingsForApp() {
        // Links to this app's notification settings.
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);
        startActivity(intent);
    }
}
