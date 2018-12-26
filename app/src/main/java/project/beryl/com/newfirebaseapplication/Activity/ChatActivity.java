package project.beryl.com.newfirebaseapplication.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.MessagesAdapter;
import project.beryl.com.newfirebaseapplication.model.MessageModel;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;
import project.beryl.com.newfirebaseapplication.utils.GetTimeAgo;

public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_FROM_FILE = 1;
    private ImageView ivBack, ivAddFiles, ivUserChatImage, ivSend;
    private TextView tvChatUserName, tvLastSeen, tvTyping;
    private EditText etTextMessage;
    private DatabaseReference mDataBaseReferense;
    private String other_user_id;
    private RecyclerView rvMessages;
    private ArrayList<MessageModel> messageList;
    private MessagesAdapter messagesAdapter;
    private String otherUserImage = "";
    private String lastMessage = "";
    private long unReadMessagesCount = 0 ;
    private boolean typingStarted;
    private Uri imageUri;
    private String message_push_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_chat);
        initViews();
        initialPageSetUp();
    }

    /**
     * method for initializing views
     */
    private void initViews() {
        ivBack = (ImageView)findViewById(R.id.iv_back);
        ivUserChatImage = (ImageView)findViewById(R.id.iv_chat_user_pic);
        tvChatUserName = (TextView)findViewById(R.id.tv_chat_user_name);
        tvLastSeen = (TextView) findViewById(R.id.tv_last_seen);
        ivSend = (ImageView) findViewById(R.id.iv_send);
        etTextMessage = (EditText) findViewById(R.id.et_message_text);
        rvMessages = (RecyclerView) findViewById(R.id.recycler_view_chat);
        tvTyping = (TextView) findViewById(R.id.tv_typing);
        ivAddFiles = (ImageView) findViewById(R.id.iv_add_files);
        ivAddFiles.setOnClickListener(this);
        ivSend.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    private void initialPageSetUp() {
        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messageList);
        rvMessages.setAdapter(messagesAdapter);

        if(getIntent().hasExtra("from")) {
            other_user_id = getIntent().getStringExtra("chat_user_id");
            tvChatUserName.setText(getIntent().getStringExtra("chat_user_name"));
            if (getIntent().getStringExtra("chat_user_image")!=null){
                if (!getIntent().getStringExtra("chat_user_image").equals("default"))
                    otherUserImage = getIntent().getStringExtra("chat_user_image");
                Glide.with(getApplicationContext()).load(getIntent().getStringExtra("chat_user_image")).centerCrop().into(ivUserChatImage);
            }

            mDataBaseReferense = FirebaseDatabase.getInstance().getReference();

            //-------  below code is for setting online status of other user on toolbar---------------
            mDataBaseReferense.child("Users").child(getIntent().getStringExtra("chat_user_id")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("online").getValue().equals("true")){
                        tvLastSeen.setText("Online");
                    }else {
                        tvLastSeen.setText(GetTimeAgo.getTimeAgo(Long.valueOf(dataSnapshot.child("online").getValue().toString()), ChatActivity.this));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mDataBaseReferense.child("chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(other_user_id).child("seen").setValue("true");
        mDataBaseReferense.child("chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(other_user_id).child("count").setValue(0);

        mDataBaseReferense.child("messages").child(other_user_id)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByChild("seen").equalTo("false").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    mDataBaseReferense.child("messages").child(other_user_id)
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(child.getKey()).child("seen").setValue("true");
                }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fetchMessagesAndUpdate();


        etTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable.toString()) && editable.toString().trim().length() == 1) {
                    typingStarted = true;
                    mDataBaseReferense.child("chat").child(other_user_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("typing").setValue("true");
                } else if (editable.toString().trim().length() == 0 && typingStarted) {
                    typingStarted = false;
                    mDataBaseReferense.child("chat").child(other_user_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("typing").setValue("false");
                }
            }
        });

        mDataBaseReferense.child("chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(other_user_id).child("typing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null && dataSnapshot.getValue().equals("true")) {
                       tvTyping.setVisibility(View.VISIBLE);
                       tvLastSeen.setVisibility(View.GONE);
                       tvTyping.setText("Typing....");
                }else {
                    tvTyping.setVisibility(View.GONE);
                    tvLastSeen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * -------- fetching messages  and realtime updating ------------
     */
    private void fetchMessagesAndUpdate() {

        mDataBaseReferense.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(other_user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String messageTexts = null;
                if (dataSnapshot.child("message_text").getValue()!=null){
                    messageTexts = dataSnapshot.child("message_text").getValue().toString();
                }
                String seen = dataSnapshot.child("seen").getValue().toString();
                String time = String.valueOf(dataSnapshot.child("time").getValue());
                String type = dataSnapshot.child("type").getValue().toString();
                String from = dataSnapshot.child("from").getValue().toString();

                Date date=new Date(Long.valueOf(time));
                Format formatter = new SimpleDateFormat("EEE, MMM d, yyyy");
                final String messageTime = formatter.format(date);


                MessageModel message = new MessageModel(messageTexts, seen, messageTime, type, from);
                messageList.add(message);
                messagesAdapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messageList.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
                finish();
                break;
            case R.id.iv_send:
                sendMessage("text");
                break;
            case R.id.iv_add_files:

                if (Build.VERSION.SDK_INT < 23) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.image_action)), PICK_FROM_FILE);
                } else {
                    if (checkAndRequestPermissions()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.image_action)), PICK_FROM_FILE);
                    }
                }
                break;
        }

    }

    private boolean checkAndRequestPermissions() {
       // int permissionCAMERA = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
       /* if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }*/
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    /**
     * method for handling callbacks of requested permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.image_action)), PICK_FROM_FILE);

                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FROM_FILE:
                if(resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    sendMessage("image");
                    uploadImageToServer();
                }
                break;

           /* case PICK_FROM_CAMERA:
                startCrop(data.getData());

                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    ivProfilePic.setImageURI(resultUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;*/
        }
    }


    /**
     * method for sending message from current users end
     */
    private void sendMessage(String type) {

        if(!TextUtils.isEmpty(etTextMessage.getText().toString())|| type.equals("image")) {
            Map message = new HashMap();
            message.put("message_text", etTextMessage.getText().toString());
            message.put("seen", "false");
            message.put("type", type);
            message.put("time", ServerValue.TIMESTAMP);
            message.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());

            lastMessage = etTextMessage.getText().toString();
            etTextMessage.setText("");
            message_push_id = mDataBaseReferense.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(other_user_id).push().getKey();
            updateUserChatList(message, message_push_id, type);
        }
    }


    /**
     * ----------------------------method for realtime updating chat list-----------------
     */
    private void updateUserChatList(final Map message, final String message_push_id, String type) {
        final Map currentUserChatMap = new HashMap();
        currentUserChatMap.put("name", getIntent().getStringExtra("chat_user_name"));
        if (getIntent().getStringExtra("chat_user_image")!=null){
            currentUserChatMap.put("thumb_image", getIntent().getStringExtra("chat_user_image"));
        }
        currentUserChatMap.put("count",0);
        currentUserChatMap.put("last_message", lastMessage);
        currentUserChatMap.put("type", type);
        currentUserChatMap.put("time", ServerValue.TIMESTAMP);
        currentUserChatMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDataBaseReferense.child("chat").child(other_user_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("seen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if(dataSnapshot.getValue().equals("false")) {
                        currentUserChatMap.put("seen", "false");
                    }else {
                        currentUserChatMap.put("seen", "true");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        final Map otherUserChatMap = new HashMap();
        otherUserChatMap.put("name", AppSharedPreferences.getString(this, AppSharedPreferences.PREF_KEY.FULL_NAME));
        otherUserChatMap.put("thumb_image", AppSharedPreferences.getString(this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE));
        otherUserChatMap.put("last_message", lastMessage);
        otherUserChatMap.put("type", type);
        otherUserChatMap.put("time", ServerValue.TIMESTAMP);
        otherUserChatMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDataBaseReferense.child("chat").child(other_user_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("seen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()== null){
                    otherUserChatMap.put("seen", "false");
                    otherUserChatMap.put("count", 1);
                    updateChatHistory(currentUserChatMap, otherUserChatMap, message, message_push_id);
                }else {
                    if (dataSnapshot.getValue().equals("true")) {
                        otherUserChatMap.put("seen", "true");
                        otherUserChatMap.put("count", 0);
                        updateChatHistory(currentUserChatMap, otherUserChatMap, message, message_push_id);
                    } else {
                        otherUserChatMap.put("seen", "false");
                        mDataBaseReferense.child("chat").child(other_user_id).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("count").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                               unReadMessagesCount =  (Long)dataSnapshot.getValue();
                                otherUserChatMap.put("count", unReadMessagesCount + 1);
                                updateChatHistory(currentUserChatMap, otherUserChatMap, message, message_push_id);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private  void updateChatHistory(Map currentUserChatMap, Map otherUserChatMap, Map message, String message_push_id) {

        Map updateMessageAndList = new HashMap();
        updateMessageAndList.put("/messages" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ other_user_id + "/" + message_push_id, message);
        updateMessageAndList.put("/messages" +"/"+ other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + message_push_id, message);

        updateMessageAndList.put("/chat" + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + other_user_id, currentUserChatMap);
        updateMessageAndList.put("/chat" + "/" + other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), otherUserChatMap);
        mDataBaseReferense.updateChildren(updateMessageAndList).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
              //  Toast.makeText(ChatActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        etTextMessage.setText(""); //-------- the callback in textwatcher will set typing status to false
        mDataBaseReferense.child("chat").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(other_user_id).child("seen").setValue("false");

    }









    String thumbImgUrl;
    private void uploadImageToServer() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference childThumbRef = storageRef.child("images").child("chat");


           /* byte[] byteArray = {};
            try {
                File thumb_filePath = new File(imageUri.getPath());
                Bitmap compressedImageBitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(50)
                        .compressToBitmap(thumb_filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byteArray = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final byte[] finalByteArray = byteArray;*/
            final StorageReference thumb_path = childThumbRef.child(System.currentTimeMillis() + ".jpg");
            //uploading the image
//            UploadTask uploadTask = thumb_path.putBytes(finalByteArray);
            UploadTask uploadTask = thumb_path.putFile(imageUri);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                      //  final String thumbImgUrl = task.getResult().getDownloadUrl().toString();
                        thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                thumbImgUrl = uri.toString();
                            }
                        });
                        Map updateMessageAndList = new HashMap();
                        updateMessageAndList.put("/messages" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ other_user_id + "/" +  message_push_id + "/" + "message_text", thumbImgUrl);
                        updateMessageAndList.put("/messages" +"/"+ other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/"+  message_push_id + "/"  + "message_text", thumbImgUrl);

                        updateMessageAndList.put("/chat" + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + other_user_id + "/" + "last_message", thumbImgUrl);
                        updateMessageAndList.put("/chat" + "/" + other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + "last_message", thumbImgUrl);
                        mDataBaseReferense.updateChildren(updateMessageAndList).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                messagesAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }
    }
}
