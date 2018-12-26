package project.beryl.com.newfirebaseapplication.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.AllUserAdapter;
import project.beryl.com.newfirebaseapplication.model.UserModel;

public class AllUsersActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBack, ivSearch;
    private TextView tvTitle;
    private RecyclerView rvAllUser;
    private AllUserAdapter allUserAdapter;
    private ArrayList<UserModel> userList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText etSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_all_users);

        initViews();
        initialPageSetUp();
    }


    private void initViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        rvAllUser = (RecyclerView) findViewById(R.id.rv_all_users);
        ivSearch = (ImageView) findViewById(R.id.iv_search);
        etSearch = (EditText) findViewById(R.id.et_search);
        ivBack.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
    }

    private void initialPageSetUp() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        tvTitle.setText(getString(R.string.all_user));
        ivSearch.setTag("Search");
        rvAllUser.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        allUserAdapter = new AllUserAdapter(this, userList);
        rvAllUser.setAdapter(allUserAdapter);

        //--------fetch all users data----
        hitAllUserApi();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
              /*  if(editable.toString().trim().length() != 0) {*/
                    mFirebaseDatabase.getReference().child("Users").orderByChild("name")
                            .startAt(etSearch.getText().toString())
                            .endAt(etSearch.getText().toString()+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    userList.clear();
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        if (!child.getRef().getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                            userList.add(child.getValue(UserModel.class));
                                    }

                                    allUserAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
               /* } else {
                    hitAllUserApi();
                }*/
            }
        });
    }

    private void hitAllUserApi() {

        if(!isFinishing()) {
            mFirebaseDatabase.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (!child.getRef().getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            userList.add(child.getValue(UserModel.class));
                    }

                    allUserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_back:
                onBackPressed();
                break;

            case R.id.iv_search:
                 if(ivSearch.getTag().equals("Search")){
                     tvTitle.setVisibility(View.INVISIBLE);
                     etSearch.setVisibility(View.VISIBLE);
                     ivSearch.setTag("Done");
                     ivSearch.setImageResource(R.drawable.ic_check_circle_white_24dp);
                 }else {
                     tvTitle.setVisibility(View.VISIBLE);
                     etSearch.setVisibility(View.INVISIBLE);
                     ivSearch.setTag("Search");
                     ivSearch.setImageResource(R.drawable.ic_search_white_24dp);

                 }
                break;
        }
    }
}
