package project.beryl.com.newfirebaseapplication.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import project.beryl.com.newfirebaseapplication.Activity.CreateGroupActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.FriendsModel;

public class  AddedFriendsAdapter extends RecyclerView.Adapter<AddedFriendsAdapter.MyViewHolder> {

    CreateGroupActivity mActivity;
    ArrayList<FriendsModel> addedFriendList;
    public AddedFriendsAdapter(CreateGroupActivity mActivity, ArrayList<FriendsModel> addedFriendList) {
        this.mActivity = mActivity;
        this.addedFriendList = addedFriendList;
    }

    @Override
    public AddedFriendsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.row_add_group, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AddedFriendsAdapter.MyViewHolder holder, final int position) {
        holder.frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addedFriendList.get(position).selected) {
                    addedFriendList.get(position).selected = false;
                }else {
                    addedFriendList.get(position).selected = true;
                    holder.ivCancel.setVisibility(View.VISIBLE);

                }
                ((CreateGroupActivity)mActivity).setAddGroupFriendsAdapter(addedFriendList.get(position).user_id);
                addedFriendList.remove(position);
                notifyDataSetChanged();
            }
        });


    }

    @Override
    public int getItemCount() {
        return addedFriendList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView ivCancel;
        private FrameLayout frameLayout;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivCancel=itemView.findViewById(R.id.iv_cancel);
            frameLayout=itemView.findViewById(R.id.frame_root);
        }
    }
}