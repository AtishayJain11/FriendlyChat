package project.beryl.com.newfirebaseapplication.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.FriendlyMessage;

public class SplashActivity extends Activity {

    String s = "FRIENDLY CHATTING ";
    int i;
   private TextView mTextView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        mTextView = findViewById(R.id.text1);

        handler(s.charAt(i));
    }

    void handler(final char c) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (i < s.length() - 1) {
                    mTextView.append("" + c);
                    i = i + 1;
                    handler(s.charAt(i));
                } else {
                    startActivity(new Intent(getApplicationContext(), UserListActivity.class));
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    finish();
                }
            }
        }, 100);
    }
}
