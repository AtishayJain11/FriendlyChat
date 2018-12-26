package project.beryl.com.newfirebaseapplication.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import project.beryl.com.newfirebaseapplication.Activity.MainActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.FriendsRequestModel;

/**
 * reated by punamchand 12/12/2018alam on 10/21/2017.
 */

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.MyViewHolder> {

    private MainActivity mActivity;
    ArrayList<FriendsRequestModel> friendsModelsList;

    public FriendRequestAdapter(MainActivity mActivity, ArrayList<FriendsRequestModel> friendsModelsList) {

        this.mActivity = mActivity;
        this.friendsModelsList = friendsModelsList;

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_friend_accept_cancel, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        if (friendsModelsList.get(position).request_type.equals("received")) {
            holder.llAccRejContainer.setVisibility(View.VISIBLE);
            holder.tvCancel.setVisibility(View.GONE);
        } else {
            holder.llAccRejContainer.setVisibility(View.INVISIBLE);
            holder.tvCancel.setVisibility(View.VISIBLE);
        }
        holder.tvStatus.setText(friendsModelsList.get(position).status);
        holder.tvName.setText(friendsModelsList.get(position).name);
        holder.tvRequestDate.setText(friendsModelsList.get(position).request_date);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mActivity != null) {
                if (!friendsModelsList.get(position).thumb_image.equals("default")) {
                    Glide.with(mActivity).load(friendsModelsList.get(position).thumb_image).centerCrop().into(holder.ivProfilePic);
                }
            }
        }
        holder.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map updates = new HashMap();
                updates.put("/friend_req" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ friendsModelsList.get(position).user_id, null);
                updates.put("/friend_req" +"/"+ friendsModelsList.get(position).user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
                FirebaseDatabase.getInstance().getReference().updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }
        });


        holder.tvAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Map currentTime = new HashMap();
                currentTime.put("friend_since", ServerValue.TIMESTAMP);

                Map updates = new HashMap();
                updates.put("/friends" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ friendsModelsList.get(position).user_id, currentTime);
                updates.put("/friends" +"/"+ friendsModelsList.get(position).user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), currentTime);

                FirebaseDatabase.getInstance().getReference().updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Map updates = new HashMap();
                                updates.put("/friend_req" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ friendsModelsList.get(position).user_id, null);
                                updates.put("/friend_req" +"/"+ friendsModelsList.get(position).user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
                                FirebaseDatabase.getInstance().getReference().updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        });

            }
        });

       holder.tvReject.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Map updates = new HashMap();
               updates.put("/friend_req" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ friendsModelsList.get(position).user_id, null);
               updates.put("/friend_req" +"/"+ friendsModelsList.get(position).user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), null);
               FirebaseDatabase.getInstance().getReference().updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {

                   }
               });
           }
       });
    }

    @Override
    public int getItemCount() {
        return friendsModelsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvStatus, tvAccept, tvReject, tvCancel, tvRequestDate;
        private ImageView ivProfilePic;
        private RelativeLayout rootLayout;
        private LinearLayout llAccRejContainer;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvStatus = itemView.findViewById(R.id.tv_status);
            tvName = itemView.findViewById(R.id.tv_name);
            ivProfilePic = itemView.findViewById(R.id.civ_profile_pic);
            rootLayout = itemView.findViewById(R.id.root_layout);
            tvAccept = itemView.findViewById(R.id.accept);
            tvReject = itemView.findViewById(R.id.tv_reject_request);
            tvCancel = itemView.findViewById(R.id.tv_cancel_request);
            tvRequestDate = itemView.findViewById(R.id.tv_request_date);
            llAccRejContainer = itemView.findViewById(R.id.ll_accept_reject_container);


        }
    }
}
