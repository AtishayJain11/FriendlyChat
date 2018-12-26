package project.beryl.com.newfirebaseapplication.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;
import project.beryl.com.newfirebaseapplication.utils.MyTextWatcher;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etMail;
    private TextView tvSubmit,tvTitle;
    private ImageView ivBack;
    private FirebaseAuth mauth;
    private RelativeLayout relativeLayout;
    private TextInputLayout ettextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
        initialpageSetup();
        ivBack.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
    }

    private void initialpageSetup() {
        etMail.addTextChangedListener(new MyTextWatcher(etMail));
        mauth=FirebaseAuth.getInstance();
        setHinttitle();
    }

    private void setHinttitle() {

    }

    private void init() {
        ivBack= (ImageView) findViewById(R.id.iv_back);
        etMail= (EditText) findViewById(R.id.et_usermail);
        tvSubmit= (TextView) findViewById(R.id.submit);
        tvTitle= (TextView) findViewById(R.id.tv_title);
        ettextInputLayout= (TextInputLayout) findViewById(R.id.usernameWrapper);
        relativeLayout= (RelativeLayout) findViewById(R.id.rootLayout);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.submit:
                if(!validate()){

                    return;

                }else {

                    mauth.sendPasswordResetEmail(etMail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ForgotPasswordActivity.this, "Check email to reset your password!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText( ForgotPasswordActivity.this,"Fail to send reset password email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
        }

    }
    private boolean validate() {
        boolean valid=true;
        if(etMail.getText().toString().isEmpty()||!android.util.Patterns.EMAIL_ADDRESS.matcher(etMail.getText().toString()).matches()){
          AppUtils.getInstance().showSnackBar("please enter valid email",relativeLayout);
            valid=false;
        }
        return valid;
    }
}
