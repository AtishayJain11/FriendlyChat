package project.beryl.com.newfirebaseapplication.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.AddGroupListFriendAdapter;
import project.beryl.com.newfirebaseapplication.adapter.AddedFriendsAdapter;
import project.beryl.com.newfirebaseapplication.model.FriendsModel;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvTitle;
    private ImageView ivBack;
    private RecyclerView rvFriends,rvAddFriend;
    private AddGroupListFriendAdapter friendsListAdapter;
    private ArrayList<String> friendsKeyList = new ArrayList<>();
    private ArrayList<FriendsModel> friendsList = new ArrayList<>();
    private ArrayList<FriendsModel> addedFriendList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add_in_group);
        initView();
        initialPageSetup();
        ivBack.setOnClickListener(this);
    }

    private void initialPageSetup() {
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        friendsListAdapter=new AddGroupListFriendAdapter(CreateGroupActivity.this, friendsList);
        rvFriends.setAdapter(friendsListAdapter);

    }

    private void initView() {
        tvTitle= (TextView) findViewById(R.id.tv_title);
        ivBack= (ImageView) findViewById(R.id.iv_back);
        rvFriends= (RecyclerView) findViewById(R.id.rv_friend_list);
        rvAddFriend= (RecyclerView) findViewById(R.id.rv_add_list);
        hitApi();

    }

    private void hitApi() {
        FirebaseDatabase.getInstance().getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(dataSnapshot.getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String uid = dataSnapshot.child("user_id").getValue().toString();
                                String image = dataSnapshot.child("image").getValue().toString();
                                String thumb_image = dataSnapshot.child("image").getValue().toString();
                                String name = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                friendsList.add(new FriendsModel(uid,name,status, image, thumb_image));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                friendsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (String friendKey: friendsKeyList) {
                    if(dataSnapshot.getKey().equals(friendKey)) {
                        friendsKeyList.remove(friendKey);
                    }
                }
                friendsListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_back:
                finish();
                break;
        }
    }

    /**
     *
     */
    public void setupAddedFriendAdapter() {
        ArrayList<FriendsModel> addedFriendList = new ArrayList<>();
        for (int i = 0; i < friendsList.size(); i++) {
            if(friendsList.get(i).selected) {
                addedFriendList.add(friendsList.get(i));
            }
        }
        if (addedFriendList.size()>0){
            rvAddFriend.setVisibility(View.VISIBLE);
        }else {
            rvAddFriend.setVisibility(View.GONE);
        }
        AddedFriendsAdapter addedFriendsAdapter = new AddedFriendsAdapter(this, addedFriendList);
        rvAddFriend.setLayoutManager(new LinearLayoutManager(this));
        rvAddFriend.setAdapter(addedFriendsAdapter);
    }

    public void setAddGroupFriendsAdapter(String user_id) {
        for (int i = 0; i < friendsList.size(); i++) {
            if(friendsList.get(i).user_id.equals(user_id)) {
                friendsList.get(i).selected = false;
            }
        }
        friendsListAdapter.notifyDataSetChanged();
    }
}
