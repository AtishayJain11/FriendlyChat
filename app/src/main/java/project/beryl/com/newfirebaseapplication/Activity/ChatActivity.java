package project.beryl.com.newfirebaseapplication.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.Person;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import project.beryl.com.newfirebaseapplication.notification.GlobalNotificationBuilder;
import project.beryl.com.newfirebaseapplication.notification.MessagingIntentService;
import project.beryl.com.newfirebaseapplication.notification.MockDatabase;
import project.beryl.com.newfirebaseapplication.notification.NotificationUtil;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;
import project.beryl.com.newfirebaseapplication.utils.GetTimeAgo;

import static project.beryl.com.newfirebaseapplication.Activity.MainActivity.NOTIFICATION_ID;


public class ChatActivity extends BaseActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_FROM_FILE = 1;
    private ImageView ivBack, ivAddFiles, ivUserChatImage, ivSend;
    private TextView tvChatUserName, tvLastSeen, tvTyping;
    private EditText etTextMessage;
    private RelativeLayout root_layout;
    private DatabaseReference mDataBaseReferense;
    public static String other_user_id;
    private RecyclerView rvMessages;
    private ArrayList<MessageModel> messageList;
    private MessagesAdapter messagesAdapter;
    private String otherUserImage = "";
    private String lastMessage = "";
    private long unReadMessagesCount = 0 ;
    private boolean typingStarted;
    private Uri imageUri;
    private String message_push_id;
    public static Map map;
    private NotificationManagerCompat mNotificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_chat);
        initViews();
        initialPageSetUp();
        mNotificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
    }

    /**
     * method for initializing views
     */
    private void initViews() {
        root_layout = (RelativeLayout)findViewById(R.id.root_layout);
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

    private  void updateChatHistory(Map currentUserChatMap, final Map otherUserChatMap, Map message, String message_push_id) {

        Map updateMessageAndList = new HashMap();
        updateMessageAndList.put("/messages" +"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid() +"/"+ other_user_id + "/" + message_push_id, message);
        updateMessageAndList.put("/messages" +"/"+ other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + message_push_id, message);

        updateMessageAndList.put("/chat" + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + other_user_id, currentUserChatMap);
        updateMessageAndList.put("/chat" + "/" + other_user_id + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid(), otherUserChatMap);
        mDataBaseReferense.updateChildren(updateMessageAndList).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
              //  Toast.makeText(ChatActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                generateMessagingStyleNotification(otherUserChatMap);
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

    public static Map fetchMessage() {
        map = new HashMap();
        map.put("name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        map.put("last_message",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
       /* if (other_user_id!=null){
            DatabaseReference mDataBaseReferense;
            mDataBaseReferense = FirebaseDatabase.getInstance().getReference();
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

                    map.put("name",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    map.put("last_message",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
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
            });*/
       // }


        return map;
    }



    private void generateMessagingStyleNotification(Map otherUserChatMap) {

     //   Log.d(TAG, "generateMessagingStyleNotification()");

        MockDatabase.MessagingStyleCommsAppData messagingStyleCommsAppData = MockDatabase.getMessagingStyleData(getApplicationContext(),otherUserChatMap);

        String notificationChannelId = NotificationUtil.createNotificationChannel(this, messagingStyleCommsAppData);

        // 2. Build the NotificationCompat.Style (MESSAGING_STYLE).
        String contentTitle = messagingStyleCommsAppData.getContentTitle();

        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle(messagingStyleCommsAppData.getMe())
                        .setConversationTitle(contentTitle);

        // Adds all Messages.
        // Note: Messages include the text, timestamp, and sender.
        for (NotificationCompat.MessagingStyle.Message message : messagingStyleCommsAppData.getMessages()) {
            messagingStyle.addMessage(message);
        }

        messagingStyle.setGroupConversation(messagingStyleCommsAppData.isGroupConversation());

        // 3. Set up main Intent for notification.
        Intent notifyIntent = new Intent(this, ChatActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(ChatActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(notifyIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent mainPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.

        String replyLabel = getString(R.string.reply_label);
        RemoteInput remoteInput = new RemoteInput.Builder(MessagingIntentService.EXTRA_REPLY)
                .setLabel(replyLabel)
                // Use machine learning to create responses based on previous messages.
                .setChoices(messagingStyleCommsAppData.getReplyChoicesBasedOnLastMessage())
                .build();

        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge.
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver.
        PendingIntent replyActionPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(this, MessagingIntentService.class);
            intent.setAction(MessagingIntentService.ACTION_REPLY);
            replyActionPendingIntent = PendingIntent.getService(this, 0, intent, 0);

        } else {
            replyActionPendingIntent = mainPendingIntent;
        }

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_reply_white_18dp,
                        replyLabel,
                        replyActionPendingIntent)
                        .addRemoteInput(remoteInput)
                        // Informs system we aren't bringing up our own custom UI for a reply
                        // action.
                        .setShowsUserInterface(false)
                        // Allows system to generate replies by context of conversation.
                        .setAllowGeneratedReplies(true)
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .build();


        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating current notification), we
        // create a new Builder. Later, we update this same notification, so we need to save this
        // Builder globally (as outlined earlier).

        NotificationCompat.Builder notificationCompatBuilder =
                new NotificationCompat.Builder(getApplicationContext(), notificationChannelId);

        GlobalNotificationBuilder.setNotificationCompatBuilderInstance(notificationCompatBuilder);

        notificationCompatBuilder
                // MESSAGING_STYLE sets title and content for API 16 and above devices.
                .setStyle(messagingStyle)
                // Title for API < 16 devices.
                .setContentTitle(contentTitle)
                // Content for API < 16 devices.
                .setContentText(messagingStyleCommsAppData.getContentText())
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.ic_launcher))
                .setContentIntent(mainPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                // Set primary color (important for Wear 2.0 Notifications).
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))

                // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. If you have more than one notification and
                // you prefer a different summary notification, set a group key and create a
                // summary notification via
                // .setGroupSummary(true)
                // .setGroup(GROUP_KEY_YOUR_NAME_HERE)

                // Number of new notifications for API <24 (M and below) devices.
                .setSubText(Integer.toString(messagingStyleCommsAppData.getNumberOfNewMessages()))

                .addAction(replyAction)
                .setCategory(Notification.CATEGORY_MESSAGE)

                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                .setPriority(messagingStyleCommsAppData.getPriority())

                // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
                // visibility is set in the NotificationChannel.
                .setVisibility(messagingStyleCommsAppData.getChannelLockscreenVisibility());

        // If the phone is in "Do not disturb" mode, the user may still be notified if the
        // sender(s) are in a group allowed through "Do not disturb" by the user.
        for (Person name : messagingStyleCommsAppData.getParticipants()) {
            notificationCompatBuilder.addPerson(name.getUri());
        }

        Notification notification = notificationCompatBuilder.build();
        mNotificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }


}
