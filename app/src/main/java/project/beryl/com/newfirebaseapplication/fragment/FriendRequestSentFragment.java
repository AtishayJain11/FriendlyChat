package project.beryl.com.newfirebaseapplication.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import project.beryl.com.newfirebaseapplication.Activity.MainActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.FriendRequestAdapter;
import project.beryl.com.newfirebaseapplication.model.FriendsRequestModel;

public class FriendRequestSentFragment extends Fragment {
    public RelativeLayout rootLayout;
    private MainActivity mActivity;
    private RecyclerView rvRequest;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference friendRequestReference;
    private ArrayList<FriendsRequestModel> friendsRequestList = new ArrayList<>();
    private FriendRequestAdapter friendRequestAdapter;
    private String current_user_id;
    private String name;
    private String status;
    private String image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request_received_fragment, viewGroup, false);
        mActivity = (MainActivity) getActivity();
        initViews(view);
        initialPageSetup();
        return view;
    }

    private void initViews(View view) {
        rvRequest = view.findViewById(R.id.rv_friend_request);
        rootLayout = view.findViewById(R.id.rootlayout);
    }

    private void initialPageSetup() {
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        friendRequestReference = mFirebaseDatabase.getReference().child("friend_req").child(current_user_id);
        rvRequest.setLayoutManager(new LinearLayoutManager(mActivity));
        friendRequestAdapter = new FriendRequestAdapter(mActivity, friendsRequestList);
        rvRequest.setAdapter(friendRequestAdapter);


        hitRequestList();
    }

    private void hitRequestList() {

        Query friendRequestReceived = friendRequestReference.orderByChild("request_type").equalTo("send");
        friendRequestReceived.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String userId = dataSnapshot.getKey();
                final String requestType = dataSnapshot.child("request_type").getValue().toString();
                final String requestTime = String.valueOf(dataSnapshot.child("request_time").getValue().toString());
                Date date=new Date(Long.valueOf(requestTime));
                Format formatter = new SimpleDateFormat("EEE, MMM d, yyyy");
                final String requestDate = formatter.format(date);

                FirebaseDatabase.getInstance().getReference().child("Users").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        name = dataSnapshot.child("name").getValue().toString();
                        status = dataSnapshot.child("status").getValue().toString();
                        if (dataSnapshot.child("thumb_image").getValue()!=null){
                            image = dataSnapshot.child("thumb_image").getValue().toString();
                        }
                        FriendsRequestModel friendsRequestModel = new FriendsRequestModel(userId, name, status, requestType, image, requestDate);
                        friendsRequestList.add(friendsRequestModel);
                        friendRequestAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i = 0 ; i < friendsRequestList.size(); i++) {
                    if(dataSnapshot.getKey().equals(friendsRequestList.get(i).user_id))
                        friendsRequestList.remove(i);
                }
                friendRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
