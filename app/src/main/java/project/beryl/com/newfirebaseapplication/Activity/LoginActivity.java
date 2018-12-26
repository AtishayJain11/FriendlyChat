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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppConstants;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;
import project.beryl.com.newfirebaseapplication.utils.MyTextWatcher;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView ivBack,ivHidePassword,ivShowPassword;
    private EditText etUsermail,etPassword;
    private TextView tvLogin,tvSignUp,tvForgotPassword;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private LinearLayout linearLayout;
    private boolean visibility = false;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        init();
        initialpageSetup();
        sethint();
        ivBack.setOnClickListener(this);
        ivHidePassword.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);

    }

    private void sethint() {
        etUsermail.addTextChangedListener(new MyTextWatcher(etUsermail));
        etPassword.addTextChangedListener(new MyTextWatcher(etPassword));

    }

    private void initialpageSetup() {
      //  etUsermail.setHint("Enter your mail");
       // etPassword.setHint("Enter your password");
        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();

        SpannableString styledString = new SpannableString(tvSignUp.getText().toString());   // index 103 - 112
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        styledString.setSpan(new StyleSpan(Typeface.BOLD), tvSignUp.getText().toString().length() - 6, tvSignUp.getText().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);  // make text bold
        styledString.setSpan(clickableSpan, tvSignUp.getText().toString().length() - 6, tvSignUp.getText().toString().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        tvSignUp.setMovementMethod(LinkMovementMethod.getInstance());//--makes styled string clickable --
        styledString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), tvSignUp.getText().toString().length() - 6, tvSignUp.getText().toString().length(), 0);
        tvSignUp.setText(styledString);
    }

    private void init() {
        ivBack= (ImageView) findViewById(R.id.iv_back);
        ivHidePassword= (ImageView) findViewById(R.id.off_visibility);
        etUsermail= (EditText) findViewById(R.id.et_username);
        etPassword= (EditText) findViewById(R.id.et_password);
        tvForgotPassword= (TextView) findViewById(R.id.forgotPassword);
        tvLogin= (TextView) findViewById(R.id.login);
        tvSignUp= (TextView) findViewById(R.id.signup);
        linearLayout= (LinearLayout) findViewById(R.id.rootLayout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.login:
                if(validate()) {
                    if(AppUtils.getInstance().isNetworkAvailable(this)){
                        loginApiCall();
                    }else {
                        AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_no_internet), linearLayout);
                    }
                }
                break;

            case R.id.signup:
               startActivity(new Intent(this,SignUpActivity.class));
                break;
            case R.id.off_visibility:
                if (!visibility) {
                    etPassword.setTransformationMethod(null);
                    ivHidePassword.setImageResource(R.drawable.ic_visibility_black_24dp);
                    etPassword.setSelection(etPassword.getText().toString().length());
                    visibility = true;
                } else {
                    etPassword.setTransformationMethod(new PasswordTransformationMethod());
                    ivHidePassword.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    etPassword.setSelection(etPassword.getText().toString().length());
                    visibility = false;
                }

                break;

            case R.id.forgotPassword:
                startActivity(new Intent(this,ForgotPasswordActivity.class));
                break;
        }
    }










    private boolean validate() {
        if (etUsermail.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_email_can_not_empty),linearLayout );
            etUsermail.requestFocus();
            return false;
        }else if (etPassword.getText().toString().trim().length() == 0) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_password_can_not_empty),linearLayout );
            etPassword.requestFocus();
            return false;
        }else if (etPassword.getText().toString().trim().length() < 6 || etPassword.getText().toString().trim().length() > 15) {
            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_password_length),linearLayout );
            etPassword.requestFocus();
            return false;
        }else return true;
    }

    private void loginApiCall(){
        AppUtils.getInstance().showProgessDialog(this);
        auth.signInWithEmailAndPassword(etUsermail.getText().toString(),etPassword.getText().toString())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            AppUtils.getInstance().showSnackBar("Login fail",linearLayout);
                        }else {
                            firebaseUser=auth.getCurrentUser();
                            fetchUserDataApiCall(firebaseUser.getUid());
                        }

                    }
                });

    }

    private void fetchUserDataApiCall(String uid) {

        firebaseDatabase.getReference().child(AppConstants.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AppSharedPreferences.putString(LoginActivity.this,AppSharedPreferences.PREF_KEY.USER_ID,dataSnapshot.child("user_id").getValue().toString());
                AppSharedPreferences.putString(LoginActivity.this, AppSharedPreferences.PREF_KEY.FULL_NAME, dataSnapshot.child("name").getValue().toString());
                AppSharedPreferences.putString(LoginActivity.this, AppSharedPreferences.PREF_KEY.EMAIL_ID, dataSnapshot.child("email_id").getValue().toString());
                if(dataSnapshot.child("thumb_image").getValue()!=null){
                    AppSharedPreferences.putString(LoginActivity.this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE, dataSnapshot.child("thumb_image").getValue().toString());
                }
                AppSharedPreferences.putBoolean(LoginActivity.this, AppSharedPreferences.PREF_KEY.ISLOGIN, true);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
  /*  private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }*/
   /* private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_mail:
                    validateEmail();
                    break;
                case R.id.et_password:
                    validateEmail();
                    break;

            }
        }
    }*/
}
