package project.beryl.com.newfirebaseapplication.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.Activity.GroupActivity;
import project.beryl.com.newfirebaseapplication.Activity.MainActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.FriendsAdapter;
import project.beryl.com.newfirebaseapplication.model.FriendsModel;

public class FriendsFragment extends Fragment implements View.OnClickListener {

    private MainActivity mActivity;
    private TextView tvTitle;
    private RecyclerView rvFriends;
    private FriendsAdapter friendsAdapter;
    private ArrayList<String> friendsKeyList = new ArrayList<>();
    private ArrayList<FriendsModel> friendsList = new ArrayList<>();
    private FloatingActionButton floatingCreateGroup;

    private FirebaseDatabase mFirebaseDatabase;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, viewGroup, false);
        mActivity = (MainActivity) getActivity();
        initViews(view);
        initialPageSetUp();
        floatingCreateGroup.setOnClickListener(this);
        return view;
    }

    private void initViews(View view) {
         // tvTitle = (TextView) view.findViewById(R.id.tv_title);
            rvFriends = (RecyclerView) view.findViewById(R.id.rv_friends);
            floatingCreateGroup=view.findViewById(R.id.create_group);
    }

    private void initialPageSetUp() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        rvFriends.setLayoutManager(new LinearLayoutManager(mActivity));
        friendsAdapter = new FriendsAdapter(mActivity, friendsKeyList);
        rvFriends.setAdapter(friendsAdapter);

        //--------fetch all users data----
        hitAllFriendsApi();
    }

    private void hitAllFriendsApi() {

        mFirebaseDatabase.getReference().child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                friendsKeyList.add(0, dataSnapshot.getKey());
                friendsAdapter.notifyDataSetChanged();
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
                friendsAdapter.notifyDataSetChanged();
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
            case R.id.create_group:
                startActivity(new Intent(getActivity(), GroupActivity.class));
                break;
        }
    }
}