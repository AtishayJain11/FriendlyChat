package project.beryl.com.newfirebaseapplication.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import project.beryl.com.newfirebaseapplication.R;
import project.beryl.com.newfirebaseapplication.adapter.UserListAdapter;
import project.beryl.com.newfirebaseapplication.model.FriendlyMessage;
import project.beryl.com.newfirebaseapplication.model.UserModel;
import project.beryl.com.newfirebaseapplication.utils.ParameterConstants;

public class UserListActivity extends AppCompatActivity {
    String TAG = "tag";
    private String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    private UserListAdapter mUserListAdapter;
    private ListView mUserListView;
    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    List<UserModel> userList;
    ValueEventListener firstValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mUserListView = (ListView) findViewById(R.id.listView);

        // Initialize firebase components
        // Initialize firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child(ParameterConstants.USER);
        //  mUserDatabaseReference.addValueEventListener(firstValueListener);
        // Initialize message ListView and its adapter
        userList = new ArrayList<>();
       /* UserModel userModel = new UserModel("PC Patidar", "pcpatidar.4488@gmail.com", "9685830848",null);
        userList.add(userModel);
        userList.add(userModel);
        userList.add(userModel);*/
        mUserListAdapter = new UserListAdapter(this, R.layout.row_user_list, userList);
        mUserListView.setAdapter(mUserListAdapter);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //User signed in
                    // Toast.makeText(MainActivity.this, "You are now signed in, welcom to friendly chat!!!", Toast.LENGTH_SHORT).show();
                    onSignedInInitiliaze(user.getDisplayName());
                } else {
                    //User signed out
                    // Choose authentication providers
                    onSignedOutCleanup();
                    createSignInIntent();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //Signed out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitiliaze(String userName) {
        mUsername = userName;
        attacheDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mUserListAdapter.clear();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mUserDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attacheDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    if (!userModel.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        mUserListAdapter.add(userModel);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mUserDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            // Successfully signed in
            if (requestCode == RC_SIGN_IN) {
                Toast.makeText(this, "SignIn Successfully.", Toast.LENGTH_SHORT).show();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
               // UserModel userModel = new UserModel(user.getDisplayName(), user.getUid(), user.getEmail(), user.getPhoneNumber(), user.getPhotoUrl().toString());
              //  mUserDatabaseReference.push().setValue(userModel);
                getAllRecord(user);
                // mUserDatabaseReference.addValueEventListener(firstValueListener);
                //  DatabaseReference reference = database.getReference("Stats").child("Matematik");
               /* final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("user");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // dataSnapshot value will be Matematik, {ahmetozrahat=50, nihatkeklik=50}
                        // because it is indeed the value we need

                        // But you want key value pair to be added to your stats
                        // So we can just loop through the values

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                           // stats.put(childSnapshot.getKey(), childSnapshot.getValue().toString());
                            if (!user.getUid().equals(childSnapshot.getValue().toString())){
                                UserModel userModel = new UserModel(user.getDisplayName(),user.getUid(),user.getEmail(),user.getPhoneNumber(),user.getPhotoUrl().toString());
                                mUserDatabaseReference.push().setValue(userModel);
                            }
                        }
                       // StatsAdapter adapter = new StatsAdapter(stats);
                      //  listView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Toast.makeText(this, "Sign in failed.", Toast.LENGTH_SHORT).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    Boolean bool = false;

    public Boolean checkEmailPresent(final String userEmail) {
        mFirebaseAuth.fetchProvidersForEmail(userEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                bool = !task.getResult().getProviders().isEmpty();
            }
        });

        return bool;
    }

    void getAllRecord(final FirebaseUser user) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mUserDatabaseReference = database.getReference(ParameterConstants.USER);
        mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;
                int flag = 0;
                if (dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    long count = dataSnapshot.getChildrenCount();
                    Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                    while (iterator.hasNext()) {
                        i++;
                        DataSnapshot snapshot = iterator.next();
                        final UserModel userModel = (UserModel) snapshot.getValue(UserModel.class);
                        if (userModel.getUserId().equals(user.getUid())) {
                            flag = 1;
                            break;
                        }
                        if (i == count) {
                            break;
                        }
                    }
                    if (flag != 1) {
                        System.out.println("yes");
                         UserModel userModel = new UserModel(user.getDisplayName(),user.getUid(),user.getEmail(),user.getPhoneNumber(),user.getPhotoUrl().toString());
                         mUserDatabaseReference.push().setValue(userModel);
                    }
                } else {
                    System.out.println("yes");
                     UserModel userModel = new UserModel(user.getDisplayName(),user.getUid(),user.getEmail(),user.getPhoneNumber(),user.getPhotoUrl().toString());
                     mUserDatabaseReference.push().setValue(userModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
