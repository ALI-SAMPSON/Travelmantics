package com.google.alc.travelmantics.app.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.alc.travelmantics.app.model.TravelDeal;
import com.google.alc.travelmantics.app.ui.TravelDealListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.alc.travelmantics.app.constant.AppConstants.ADMINISTRATORS_REF;
import static com.google.alc.travelmantics.app.constant.AppConstants.STORAGE_REF;

public class FirebaseUtil {

    private static final int RC_SIGN_IN = 123;
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtil mFirebaseUtil;
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageRef;
    public static ArrayList<TravelDeal> mDeals;
    public static TravelDealListActivity thisActivity;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    public static boolean isAdmin;

    public FirebaseUtil() {}

    public static void openFirebaseRef(String myRef,final TravelDealListActivity callerActivity)
    {
        if(mFirebaseUtil == null){
            mFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            thisActivity = callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() == null){
                        FirebaseUtil.signIn();
                    }
                    else{
                        String uid = firebaseAuth.getUid();

                        checkIfIsAdmin(uid);
                    }
                }
            };

            connectFBStorage();

        }
        mDeals = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(myRef);
    }

    private static void connectFBStorage() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReference().child(STORAGE_REF);
    }

    // sign in user
    private static void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        thisActivity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    // check if current user is an admin
    private static void checkIfIsAdmin(String uid) {

        FirebaseUtil.isAdmin = false;
        DatabaseReference dbRef = mFirebaseDatabase.getReference().child(ADMINISTRATORS_REF)
                .child(uid);
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                // show menu
                thisActivity.showMenu();
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

        dbRef.addChildEventListener(eventListener);

    }

    // attach auth listener
    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    // remove auth listener
    public static void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

}
