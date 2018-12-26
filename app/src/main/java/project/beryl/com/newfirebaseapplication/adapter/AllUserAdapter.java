package project.beryl.com.newfirebaseapplication.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import project.beryl.com.newfirebaseapplication.Activity.AllUsersActivity;
import project.beryl.com.newfirebaseapplication.Activity.OtherUserProfileActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.UserModel;

/**
 * Created by punamchand on 17/Dec/2018.
 */

public class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.MyViewHolder>{

    private AllUsersActivity allUsersActivity;
    ArrayList<UserModel> userList;

    public AllUserAdapter(AllUsersActivity allUsersActivity, ArrayList<UserModel> userList) {

        this.allUsersActivity = allUsersActivity;
        this.userList = userList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_all_users, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.tvName.setText(userList.get(position).name);
        holder.tvStatus.setText(userList.get(position).status);
        if (userList.get(position).thumb_image!=null){
            if(!userList.get(position).thumb_image.equals("default")) {
                Glide.with(allUsersActivity).load(userList.get(position).thumb_image).centerCrop().into(holder.ivProfilePic);
            }
        }

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(allUsersActivity, OtherUserProfileActivity.class);
                   intent.putExtra("other_user_id",userList.get(position).user_id);
                   intent.putExtra("other_user_name",userList.get(position).name);
                   intent.putExtra("other_status",userList.get(position).status);
                   intent.putExtra("other_image",userList.get(position).image);
                     allUsersActivity.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName, tvStatus;
        private ImageView ivProfilePic;
        private RelativeLayout rootLayout;
        public MyViewHolder(View itemView) {
            super(itemView);

            tvStatus = itemView.findViewById(R.id.tv_status);
            tvName = itemView.findViewById(R.id.tv_name);
            ivProfilePic = itemView.findViewById(R.id.civ_profile_pic);
            rootLayout=itemView.findViewById(R.id.root_layout);


        }
    }
}
