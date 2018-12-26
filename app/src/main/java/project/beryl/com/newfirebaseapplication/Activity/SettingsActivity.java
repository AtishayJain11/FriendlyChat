package project.beryl.com.newfirebaseapplication.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.utils.AppSharedPreferences;
import project.beryl.com.newfirebaseapplication.utils.AppUtils;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private static final int PICK_FROM_CAMERA = 1, PICK_FROM_FILE = 3, PERMISSION_REQUEST_CODE = 4;
    private LinearLayout llRootLayout;
    private CircleImageView ivProfilePic;
    private ImageView ivBack;
    private TextView tvName, tvStatus, tvChangeImage, tvChangeStatus;
    private DatabaseReference mDatabaseReference;
    private Uri mImageCaptureUri, outputUri;
    private Uri filePath, resultUri;
    ProgressDialog pd;
    FirebaseStorage storage;
    StorageReference storageRef;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);
        initViews();
        initialPageSetUp();
    }

    /**
     * method for initializing variables
     */
    private void initViews() {

        ivBack = (ImageView) findViewById(R.id.iv_back);
        llRootLayout = (LinearLayout) findViewById(R.id.ll_rootlayout);
        ivProfilePic = (CircleImageView) findViewById(R.id.iv_profile_pic);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvChangeImage = (TextView) findViewById(R.id.tv_change_image);
        tvChangeStatus = (TextView) findViewById(R.id.tv_change_status);

        tvChangeImage.setOnClickListener(this);
        tvChangeStatus.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }


    private void initialPageSetUp() {

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvStatus.setText(dataSnapshot.child("status").getValue().toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(!isFinishing() && !isDestroyed()) {
                        if (dataSnapshot.child("image").getValue()!=null){
                            if (!dataSnapshot.child("image").getValue().equals("default")) {
                                imageUrl = dataSnapshot.child("image").getValue().toString();
                                Glide.with(SettingsActivity.this).load(imageUrl).centerCrop().into(ivProfilePic);
                            }
                        }
                    }
                }
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
            case R.id.tv_change_image:

                if (Build.VERSION.SDK_INT < 23) {
                    selectImage();
                } else {
                    if (checkAndRequestPermissions()) {
                        selectImage();
                    }
                }

                break;

            case R.id.tv_change_status:

                startActivity(new Intent(this, AccountStatusActivity.class).putExtra("status", tvStatus.getText().toString()));
                break;
        }
    }

    private boolean checkAndRequestPermissions() {
        int permissionCAMERA = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }



    /**
     * dialog opens providing photo upload options with gallery or camera
     */
    private void selectImage() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_images);
        TextView cameraTV = (TextView) dialog.findViewById(R.id.tv_camera);
        TextView galleryTV = (TextView) dialog.findViewById(R.id.tv_gallery);
        ImageView cameraIV = (ImageView) dialog.findViewById(R.id.iv_camera);
        ImageView galleryIV = (ImageView) dialog.findViewById(R.id.iv_gallery);
        ImageView cancelIV = (ImageView) dialog.findViewById(R.id.iv_cancel);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//----width math parent for dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//----makes default dialog background transparent

        dialog.show();
        cameraTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraSelect();
                dialog.dismiss();
            }
        });

        galleryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picImageFromGallery();
                dialog.dismiss();
            }
        });


        cameraIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraSelect();
                dialog.dismiss();
            }
        });

        galleryIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picImageFromGallery();
                dialog.dismiss();
            }
        });

        cancelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * pic image from gallery
     */
    private void picImageFromGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.image_action)), PICK_FROM_FILE);
    }

    /**
     * Selecting camera for taking images
     */
    private void cameraSelect() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //  mImageCaptureUri = Uri.fromFile(new File(getFilename()));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FROM_FILE:
                startCrop(data.getData());
                break;

            case PICK_FROM_CAMERA:
                startCrop(data.getData());

                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    resultUri = result.getUri();
                    ivProfilePic.setImageURI(resultUri);
                    uploadImageToServer();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }

    String imageUrl1;
    String thumbImgUrl;
    private void uploadImageToServer() {
        if (filePath != null) {
            pd = new ProgressDialog(this);
            pd.setMessage("Please wait ....");
            pd.show();
            StorageReference childRef = storageRef.child("images");
            StorageReference childThumbRef = storageRef.child("images").child("thumb_images");


            byte[] byteArray = {};
            try {
                File thumb_filePath = new File(resultUri.getPath());
                Bitmap compressedImageBitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byteArray = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            final byte[] finalByteArray = byteArray;



            final StorageReference imaepathRef = childRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            final StorageReference thumb_path = childThumbRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            //uploading the image
            UploadTask uploadTask = imaepathRef.putFile(filePath);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                   // final String imageUrl = taskSnapshot.getDownloadUrl().toString();
                    imaepathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl1 = uri.toString();
                            thumbImgUrl = uri.toString();
                        }
                    });
                    UploadTask uploadTask = thumb_path.putBytes(finalByteArray);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){
                                thumb_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                      //  thumbImgUrl = uri.toString();
                                    }
                                });
                               // final String thumbImgUrl = task.getResult().getDownloadUrl().toString();

                                Map update_HashMap = new HashMap();
                                update_HashMap.put("image", imageUrl1);
                                update_HashMap.put("thumb_image", thumbImgUrl);
                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance()
                                        .getCurrentUser().getUid()).updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        pd.dismiss();
                                        if(task.isSuccessful()) {
                                            Glide.with(SettingsActivity.this).load(imageUrl1).centerCrop().into(ivProfilePic);
                                            AppUtils.getInstance().showSnackBar(getString(R.string.s_successfully_uploaded), llRootLayout);
                                            AppSharedPreferences.putString(SettingsActivity.this, AppSharedPreferences.PREF_KEY.THUMB_IMAGE,thumbImgUrl);

                                        }else {
                                            AppUtils.getInstance().showSnackBar("Failed", llRootLayout);
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(SettingsActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void startCrop(Uri imageUri) {
        filePath = imageUri;
        CropImage.activity(imageUri).start(this);

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
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                } else if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                }
                break;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
      //  mDatabaseReference.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
     //   mDatabaseReference.child("online").setValue("false");

    }
}
