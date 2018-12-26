package project.beryl.com.newfirebaseapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.Activity.ChatActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.ChatModel;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;

/**
 * Created by punamchand on 12/12/2018.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyViewHolder> {

    private Context mActivity;
    ArrayList<ChatModel> chatlist;
    private FirebaseDatabase mFirebaseDatabase;

    public ChatListAdapter(Context mActivity, ArrayList<ChatModel> chatlist) {

        this.mActivity = mActivity;
        this.mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.chatlist = chatlist;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_history_current_user, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.tvName.setText(chatlist.get(position).getName());
        holder.tvTime.setText(AppUtils.getInstance().getFormattedDateFromTimestamp(Long.valueOf(chatlist.get(position).getTime())));
        if (chatlist.get(position).getImage()!=null){
            if(!chatlist.get(position).getImage().equals("default")) {
                Glide.with(mActivity).load(chatlist.get(position).getImage()).centerCrop().into(holder.ivProfilePic);
            }
        }
        if(chatlist.get(position).getCount()!=0) {
            holder.tvTime.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
            holder.tvCount.setText(String.valueOf(chatlist.get(position).getCount()));
            holder.tvCount.setVisibility(View.VISIBLE);
        }else {
            holder.tvTime.setTextColor(ContextCompat.getColor(mActivity, R.color.grey_300));
            holder.tvMessage.setTextColor(ContextCompat.getColor(mActivity, R.color.grey_300));
            holder.tvCount.setVisibility(View.GONE);
        }

        if(chatlist.get(position).getTyping().equals("true")) {
            holder.tvMessage.setText(chatlist.get(position).getName() + " " + "is typing....");
            holder.tvMessage.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));

        }else {
            if(chatlist.get(position).getType().equals("text")) {
                holder.tvMessage.setText(chatlist.get(position).getLastMessage());
            }else {
                holder.tvMessage.setText("Image File");
            }
            holder.tvMessage.setTextColor(ContextCompat.getColor(mActivity, R.color.grey_300));
        }

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivityIntent = new Intent(mActivity, ChatActivity.class);
                chatActivityIntent.putExtra("from", "FriendsFragment");
                chatActivityIntent.putExtra("chat_user_name", chatlist.get(position).getName());
                chatActivityIntent.putExtra("chat_user_image", chatlist.get(position).getImage());
                chatActivityIntent.putExtra("chat_user_id", chatlist.get(position).getOtherUserId());
                mActivity.startActivity(chatActivityIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvMessage, tvCount, tvTime;
        private ImageView ivProfilePic;
        private RelativeLayout rootLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvName = itemView.findViewById(R.id.tv_name);
            ivProfilePic = itemView.findViewById(R.id.civ_profile_pic);
            rootLayout=itemView.findViewById(R.id.root_layout);
            tvCount = itemView.findViewById(R.id.tv_count);
            tvTime = itemView.findViewById(R.id.tv_time);


        }
    }
}
