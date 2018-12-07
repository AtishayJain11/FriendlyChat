package project.beryl.com.newfirebaseapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import project.beryl.com.newfirebaseapplication.Activity.MainActivity;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.model.FriendlyMessage;
import project.beryl.com.newfirebaseapplication.model.UserModel;

public class UserListAdapter extends ArrayAdapter<UserModel> {
    public UserListAdapter(Context context, int resource, List<UserModel> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.row_user_list, parent, false);
        }

        LinearLayout main_layout = (LinearLayout) convertView.findViewById(R.id.main_layout);
        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView emailTextView = (TextView) convertView.findViewById(R.id.emailTextView);
        TextView phoneTextView = (TextView) convertView.findViewById(R.id.phoneTextView);

        final UserModel user = getItem(position);

        boolean isPhoto = user.getPhotoUrl() != null;
        if (isPhoto) {
            Glide.with(photoImageView.getContext())
                    .load(user.getPhotoUrl())
                    .into(photoImageView);
        }
        nameTextView.setText(user.getName());
        emailTextView.setText(user.getPhone());
        phoneTextView.setText(user.getPhone());

        main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),MainActivity.class);
                intent.putExtra("user_id",user.getUserId());
                intent.putExtra("user_email",user.getEmail());
                ((Activity)getContext()).startActivity(intent);
               // ((Activity)getContext()).finish();
            }
        });

        return convertView;
    }
}
