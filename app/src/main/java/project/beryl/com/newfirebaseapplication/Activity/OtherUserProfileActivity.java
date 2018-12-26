package project.beryl.com.newfirebaseapplication.Activity;

import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import project.beryl.com.newfirebaseapplication.R;

public class OtherUserProfileActivity extends BaseActivity implements View.OnClickListener {
    private ImageView ivBack, IVProfile;
    private TextView tvName, tvDeclinerequest, tvSendRequest, tvStatus;
    private String other_user_id, current_user_id;
    private DatabaseReference other_user_ref;
    private  DatabaseReference current_user_ref;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_other_user_profile);
        init();
        initialpageSetup();
        ivBack.setOnClickListener(this);
        tvSendRequest.setOnClickListener(this);
        tvDeclinerequest.setOnClickListener(this);
    }

    private void initialpageSetup() {
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        other_user_id = getIntent().getStringExtra("other_user_id");
        tvName.setText(getIntent().getStringExtra("other_user_name"));
        tvStatus.setText(getIntent().getStringExtra("other_status"));
        Glide.with(OtherUserProfileActivity.this).load(getIntent().getStringExtra("other_image")).centerCrop().into(IVProfile);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        current_user_ref = FirebaseDatabase.getInstance().getReference().child("friend_req").child(current_user_id).child(other_user_id);
        other_user_ref = FirebaseDatabase.getInstance().getReference().child("friend_req").child(other_user_id).child(current_user_id);
        getFriendRequestStatus();
    }

    private void getFriendRequestStatus() {
        FirebaseDatabase.getInstance().getReference().child("friend_req").child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(other_user_id)) {

                    if(dataSnapshot.child(other_user_id).child("request_type").getValue().equals("send")) {
                        tvSendRequest.setText("Cancel Friend Request");
                        tvDeclinerequest.setVisibility(View.INVISIBLE);
                    }else {
                        tvSendRequest.setText("Accept Friend Request");
                        tvDeclinerequest.setVisibility(View.VISIBLE);
                    }
                } else {

                    FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(other_user_id)) {

                                tvSendRequest.setText("Unfriend this person");
                                tvDeclinerequest.setVisibility(View.INVISIBLE);

                            }else {
                                tvSendRequest.setText("Send Friend Request");
                                tvDeclinerequest.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void init() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        IVProfile = (ImageView) findViewById(R.id.profile_pic);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvSendRequest = (TextView) findViewById(R.id.tv_send_request);
        tvDeclinerequest = (TextView) findViewById(R.id.tv_decline_request);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;

            case R.id.tv_send_request:
                handleFriendRequestFeature();
                break;

            case R.id.tv_decline_request :
                declineRequestHandling();
                break;
        }
    }

    private void declineRequestHandling() {

        current_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                other_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        tvSendRequest.setText("Send Friend Request");
                        tvDeclinerequest.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    /**
     * method for sending friend request to another user
     */
    private void handleFriendRequestFeature() {

        if(tvSendRequest.getText().equals("Send Friend Request")) {
            Map map = new HashMap<>();
            map.put("request_type", "send");
            map.put("request_time", ServerValue.TIMESTAMP);
            current_user_ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Map map = new HashMap<>();
                        map.put("request_type", "received");
                        map.put("request_time", ServerValue.TIMESTAMP);
                        other_user_ref.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    tvSendRequest.setText("Cancel Friend Request");
                                    tvDeclinerequest.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                }
            });
        }else if(tvSendRequest.getText().equals("Cancel Friend Request"))  {
            current_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    other_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            tvSendRequest.setText("Send Friend Request");
                            tvDeclinerequest.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }else if(tvSendRequest.getText().equals("Accept Friend Request")) {
            String current = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                 current = DateFormat.getDateTimeInstance().format(new Date());
            }
            final String finalCurrent = current;
            FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id).child(other_user_id).child("friend_since").setValue(current).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child("friends").child(other_user_id).child(current_user_id).child("friend_since").setValue(finalCurrent).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            current_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    other_user_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            tvSendRequest.setText("Unfriend this person");
                                            tvDeclinerequest.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });

        }else if(tvSendRequest.getText().equals("Unfriend this person")) {

            FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id).child(other_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child("friends").child(other_user_id).child(current_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            tvSendRequest.setText("Send Friend Request");
                            tvDeclinerequest.setVisibility(View.INVISIBLE);

                        }
                    });
                }
            });

        }
    }
}
