package com.google.alc.travelmantics.app.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.alc.travelmantics.app.R;
import com.google.alc.travelmantics.app.adapter.TravelDealAdapter;
import com.google.alc.travelmantics.app.model.TravelDeal;
import com.google.alc.travelmantics.app.util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.google.alc.travelmantics.app.constant.AppConstants.TRAVEL_DEALS_REF;

public class TravelDealListActivity extends AppCompatActivity {

    private TravelDealAdapter mDealAdapter;

    private ArrayList<TravelDeal> mDeals;

    private RecyclerView mRecyclerView;

    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mDatabaseReference;

    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_deal_list);

        initViews();

    }

    // method call to initialize views
    private void initViews(){


    }

    // method to display deals
    private void displayDeals(){

        // display progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    TravelDeal travelDeal = snapshot.getValue(TravelDeal.class);
                    assert travelDeal != null;
                    travelDeal.setId(snapshot.getKey());
                    mDeals.add(travelDeal);
                }

                // notify listener that item has been inserted
                mDealAdapter.notifyDataSetChanged();
                // dismiss progress bar
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // dismiss progress bar
                mProgressBar.setVisibility(View.GONE);
                // error message
                Toast.makeText(TravelDealListActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mDatabaseReference.addValueEventListener(eventListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_deal_list,menu);
        // getting reference to new deal menu item
        MenuItem newDealMenu = menu.findItem(R.id.menu_new_deal);
        if(FirebaseUtil.isAdmin){
            newDealMenu.setVisible(true);
        }
        else{
            newDealMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new_deal:
                // open deal activity
                Intent intent = new Intent(this, TravelDealActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_signout:
                // sign out user
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(TravelDealListActivity.this, getString(R.string.signout_msg), Toast.LENGTH_SHORT).show();
                            FirebaseUtil.attachListener();
                        }
                        else {
                            Toast.makeText(TravelDealListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // detach listener after sign is successful
                FirebaseUtil.detachListener();
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFirebaseRef(TRAVEL_DEALS_REF,this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDeals = FirebaseUtil.mDeals;

        // binding views
        mProgressBar = findViewById(R.id.progressBar);
        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));

        mDealAdapter = new TravelDealAdapter(this,mDeals);
        mRecyclerView.setAdapter(mDealAdapter);

        displayDeals();
        // attaching listener
        FirebaseUtil.attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // detaching listener
        FirebaseUtil.detachListener();
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }
}
