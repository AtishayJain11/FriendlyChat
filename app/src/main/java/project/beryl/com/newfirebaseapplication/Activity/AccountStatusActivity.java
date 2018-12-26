package project.beryl.com.newfirebaseapplication.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;

public class AccountStatusActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llRootLayout;
    private ImageView ivBack;
    private TextView tvTitle, tvSaveChanges;
    private EditText etStatus;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_account_status);
        initViews();
        initialPageSetUp();
    }

    /**
     * Method for initial page setup
     */
    private void initialPageSetUp() {
        tvTitle.setText(getResources().getString(R.string.s_account_status));
        if (getIntent().getStringExtra("status") != null)
            etStatus.setText(getIntent().getStringExtra("status"));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    /**
     * method for initializing views
     */
    private void initViews() {
        llRootLayout = (LinearLayout) findViewById(R.id.ll_rootlayout);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        etStatus = (EditText) findViewById(R.id.et_status);
        tvSaveChanges = (TextView) findViewById(R.id.tv_save_changes);

        ivBack.setOnClickListener(this);
        tvSaveChanges.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_save_changes:
                pd = new ProgressDialog(this);
                pd.setTitle("Saving Changes");
                pd.setMessage("Please wait....");
                pd.show();
                mDatabaseReference.child("status").setValue(etStatus.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {

                        } else {
                            AppUtils.getInstance().showSnackBar(getResources().getString(R.string.s_try_again), llRootLayout);
                        }
                    }
                });
                break;

            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }
}
