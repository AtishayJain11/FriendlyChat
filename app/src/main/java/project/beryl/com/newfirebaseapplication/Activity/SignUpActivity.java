package project.beryl.com.newfirebaseapplication.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etUsername, etUsermail, etPassword, etConfirmPassword;
    private TextView tvSignup, tvTitle, tvLogin;
    private ImageView ivBack, ivVisibilityPassword, ivVisibilityConfirmPassword;
    FirebaseStorage storage;
    StorageReference storageRef;
    private FirebaseAuth auth;
    ProgressDialog pd;
    private LinearLayout rootLayout;
    private boolean visibilityPassword = false;
    private boolean visibilityConfirmPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up);
        init();
        initialpageSetup();
    }

    private void initialpageSetup() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        pd = new ProgressDialog(this);
        pd.setMessage("Please wait....");

        SpannableString styledString = new SpannableString(tvLogin.getText().toString());   // index 103 - 112
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        styledString.setSpan(new StyleSpan(Typeface.BOLD), tvLogin.getText().toString().length() - 5, tvLogin.getText().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);  // make text bold
        styledString.setSpan(clickableSpan, tvLogin.getText().toString().length() - 5, tvLogin.getText().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        tvLogin.setMovementMethod(LinkMovementMethod.getInstance());//--makes styled string clickable --
        styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), tvLogin.getText().toString().length() - 5, tvLogin.getText().toString().length(), 0);
        tvLogin.setText(styledString);

    }


    private void init() {
        etUsername = (EditText) findViewById(R.id.et_username);
        etUsermail = (EditText) findViewById(R.id.et_mail);
        etPassword = (EditText) findViewById(R.id.et_password);
        etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
        ivVisibilityPassword = (ImageView) findViewById(R.id.iv_visibility);
        ivVisibilityConfirmPassword = (ImageView) findViewById(R.id.iv_visibility_confirm);
        tvSignup = (TextView) findViewById(R.id.signuptv);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        rootLayout = (LinearLayout) findViewById(R.id.llRootlayout);
        tvLogin = (TextView) findViewById(R.id.tv_login);

        //------registering listeners
        tvSignup.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        ivVisibilityPassword.setOnClickListener(this);
        ivVisibilityConfirmPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.signuptv:
                if (validate()) {
                    if (AppUtils.getInstance().isNetworkAvailable(SignUpActivity.this)) {
                        signUpHit();
                    } else {
                        AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_no_internet), rootLayout);
                    }
                }
                break;

            case R.id.iv_visibility:
                if (!visibilityPassword) {
                    etPassword.setTransformationMethod(null);
                    ivVisibilityPassword.setImageResource(R.drawable.ic_visibility_black_24dp);
                    etPassword.setSelection(etPassword.getText().toString().length());
                    visibilityPassword = true;
                } else {
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    ivVisibilityPassword.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    etPassword.setSelection(etPassword.getText().toString().length());
                    visibilityPassword = false;
                }
                break;

            case R.id.iv_visibility_confirm:
                if (!visibilityConfirmPassword) {
                    etConfirmPassword.setTransformationMethod(null);
                    ivVisibilityPassword.setImageResource(R.drawable.ic_visibility_black_24dp);
                    etConfirmPassword.setSelection(etConfirmPassword.getText().toString().length());
                    visibilityConfirmPassword = true;
                } else {
                    etConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    ivVisibilityPassword.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    etConfirmPassword.setSelection(etConfirmPassword.getText().toString().length());
                    visibilityConfirmPassword = false;
                }
                break;
        }
    }

    private void signUpHit() {
        pd.show();
        auth.createUserWithEmailAndPassword(etUsermail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException(), Toast.LENGTH_SHORT).show();
                            System.out.println("Authentication"+task.getException());

                        } else {
                            AppUtils.getInstance().hideProgessDialog();
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            addUsertoDataBase(SignUpActivity.this, firebaseUser);
                        }
                    }
                });

    }

    private void addUsertoDataBase(SignUpActivity signUpActivity, final FirebaseUser firebaseUser) {

        Map<String, String> params = new HashMap<>();
        params.put("user_id", firebaseUser.getUid());
        params.put("name", etUsername.getText().toString());
        params.put("email_id", firebaseUser.getEmail());
        params.put("image", "default");
        params.put("status", "Hi there i am using TheMessenger App");
        params.put("thumb_image", "default");
        params.put("online", "true");
        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(firebaseUser.getUid())
                .setValue(params)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            AppUtils.getInstance().showSnackBar("SignUp Failed", rootLayout);
                            pd.dismiss();
                        } else {
                            pd.dismiss();
                            AppSharedPreferences.putString(SignUpActivity.this, AppSharedPreferences.PREF_KEY.USER_ID, firebaseUser.getUid());
                            AppSharedPreferences.putString(SignUpActivity.this, AppSharedPreferences.PREF_KEY.FULL_NAME, etUsername.getText().toString());
                            AppSharedPreferences.putString(SignUpActivity.this, AppSharedPreferences.PREF_KEY.EMAIL_ID, firebaseUser.getEmail());
                            AppSharedPreferences.putString(SignUpActivity.this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE, "default");
                            AppSharedPreferences.putBoolean(SignUpActivity.this, AppSharedPreferences.PREF_KEY.ISLOGIN, true);
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });

    }


    private boolean validate() {
        if (etUsername.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_please_enter_name), rootLayout);
            etUsername.requestFocus();
            return false;
        } else if (etUsername.getText().toString().trim().length() < 2) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_name_limit), rootLayout);
            etUsername.requestFocus();
            return false;
        } else if (etUsermail.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_please_enter_email), rootLayout);
            etUsermail.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etUsermail.getText().toString().trim()).matches()) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_please_enter_valid_email), rootLayout);
            etUsermail.requestFocus();
            return false;
        } else if (etPassword.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_password_can_not_be_empty), rootLayout);
            etPassword.requestFocus();
            return false;
        } else if (etPassword.getText().toString().trim().length() < 8 || etPassword.getText().toString().trim().length() > 20) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_password_length), rootLayout);
            etPassword.requestFocus();
            return false;
        } else if (etConfirmPassword.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_confirm_password_can_not_be_empty), rootLayout);
            etConfirmPassword.requestFocus();
            return false;
        } else if (!etConfirmPassword.getText().toString().trim().equals(etPassword.getText().toString().trim())) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_password_length), rootLayout);
            etPassword.setText("");
            etConfirmPassword.setText("");
            etPassword.requestFocus();
            return false;
        } else {
            return true;
        }

    }

}
