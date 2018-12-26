package project.beryl.com.newfirebaseapplication.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.Activity.MainActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.ChatListAdapter;
import project.beryl.com.newfirebaseapplication.model.ChatModel;


public class ChatFragment extends Fragment implements View.OnClickListener {

    private MainActivity mActivity;
    private RecyclerView rvChatList;
    private ChatListAdapter chatListAdapter;
    private ArrayList<ChatModel> chatList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_chat_fragment_1, viewGroup, false);
        mActivity = (MainActivity) getActivity();
        init(view);
        initialPageSetup();
        return view;
    }

    private void init(View view) {
        rvChatList = view.findViewById(R.id.recycler_view_chat);

    }


    private void initialPageSetup() {

        chatListAdapter = new ChatListAdapter(mActivity, chatList);
        rvChatList.setAdapter(chatListAdapter);


        //-----------below code is for listing and real time updating chat history(Inbox)
        FirebaseDatabase.getInstance().getReference().child("chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String seen = "", type = "", time = "", image = "", name = "", from = "", typing = "";
                long count = 0 ;
                String otherUserId = dataSnapshot.getKey();
                if (dataSnapshot.child("last_message").getValue() != null) { //---- last message null represents no message has been exchanged yet
                    String lastMessage = dataSnapshot.child("last_message").getValue().toString();
                    if (dataSnapshot.child("seen").getValue() != null) {
                        seen = dataSnapshot.child("seen").getValue().toString();
                    }
                    if (dataSnapshot.child("type").getValue() != null) {
                        type = dataSnapshot.child("type").getValue().toString();
                    }
                    if (dataSnapshot.child("time").getValue() != null) {
                        time = String.valueOf(dataSnapshot.child("time").getValue().toString());
                    }
                    if (dataSnapshot.child("thumb_image").getValue() != null) {
                        image = dataSnapshot.child("thumb_image").getValue().toString();
                    }
                    if (dataSnapshot.child("name").getValue() != null) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }
                    if(dataSnapshot.child("count").getValue()!=null){
                        count  = (Long)dataSnapshot.child("count").getValue();
                    }
                    if(dataSnapshot.child("from").getValue()!=null){
                        from  = dataSnapshot.child("from").getValue().toString();
                    }
                    if(dataSnapshot.child("typing").getValue()!=null){
                        typing  = dataSnapshot.child("typing").getValue().toString();
                    }
                    ChatModel chatModel = new ChatModel(otherUserId, lastMessage, seen, type, time, image, name, count, from, typing);
                    chatList.add(chatModel);
                }
                chatListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                for (int i = 0; i < chatList.size(); i++) {
                    long count = 0;
                    String typing = "";
                    if (dataSnapshot.getKey().equals(chatList.get(i).getOtherUserId())) {
                        chatList.remove(i);
                        String otherUserId = dataSnapshot.getKey();
                        String lastMessage = null;
                        if (dataSnapshot.child("last_message").getValue()!=null){
                            if (!dataSnapshot.child("last_message").getValue().equals("")){
                                lastMessage = dataSnapshot.child("last_message").getValue().toString();
                            }
                        }
                        String seen = dataSnapshot.child("seen").getValue().toString();
                        String type = dataSnapshot.child("type").getValue().toString();
                        String time = String.valueOf(dataSnapshot.child("time").getValue().toString());
                        String image = null;
                        if (dataSnapshot.child("thumb_image").getValue()!=null){
                            image = dataSnapshot.child("thumb_image").getValue().toString();
                        }

                        String name = dataSnapshot.child("name").getValue().toString();
                        if(dataSnapshot.child("count").getValue() != null) {
                            count = (Long) dataSnapshot.child("count").getValue();
                        }
                        String from = dataSnapshot.child("from").getValue().toString();
                        if(dataSnapshot.child("typing").getValue()!=null) {
                            typing = dataSnapshot.child("typing").getValue().toString();
                        }
                        ChatModel chatModel = new ChatModel(otherUserId, lastMessage, seen, type, time, image, name, count, from, typing);
                        chatList.add(i, chatModel);
                    }
                }
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

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
        switch (view.getId()) {
            case R.id.iv_back:

                break;
        }
    }
}