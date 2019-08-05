package com.google.alc.travelmantics.app.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.alc.travelmantics.app.R;
import com.google.alc.travelmantics.app.model.TravelDeal;
import com.google.alc.travelmantics.app.util.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class TravelDealActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int IMAGE_REQUEST_CODE = 35;
    private static final int GALLERY_PERMISSION_CODE = 5;

    EditText editTextTitle,editTextPrice,editTextDescription;

    Button btnUpload;

    ImageView imageDeal;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mDatabaseReference;

    TravelDeal deal;

    ProgressDialog progressDialog;

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_deal);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDescription = findViewById(R.id.editTextDescription);
        btnUpload = findViewById(R.id.btnUpload);
        imageDeal = findViewById(R.id.imageDeal);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        mProgressBar = findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progress_title));
        progressDialog.setMessage(getString(R.string.progress_msg));
        progressDialog.setCanceledOnTouchOutside(false);

        btnUpload.setOnClickListener(this);

        TravelDeal deal = getIntent().getParcelableExtra(getString(R.string.intent_extra_deal));

        if(deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        editTextTitle.setText(deal.getTitle());
        editTextPrice.setText(deal.getPrice());
        editTextDescription.setText(deal.getDescription());
        // display image if user wants to update deal
        displayImage(deal.getImageUrl());

    }

    // display image of deal
    private void displayImage(String imageUrl) {

        if(imageUrl != null && !imageUrl.isEmpty()){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(imageUrl)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageDeal);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUpload:{
                // request permission to read storage
                requestPermission();
                break;
            }
        }
    }

    // request permission
    public void requestPermission(){

        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};

        // checks build version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

            if(ContextCompat.checkSelfPermission(TravelDealActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(TravelDealActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // display error message
                    Toast.makeText(this,
                            getString(R.string.text_permission_product_image), Toast.LENGTH_LONG).show();

                }
                else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(TravelDealActivity.this,
                            perms, GALLERY_PERMISSION_CODE);
                }

            }
            else{
                // method call to open gallery
                chooseImage();
            }
        }
        else{
            // method call to open gallery
            chooseImage();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case GALLERY_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    private void chooseImage() {
        Intent intentChoose = new Intent(Intent.ACTION_GET_CONTENT);
        intentChoose.setType("image/*");
        intentChoose.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        startActivityForResult(Intent.createChooser(intentChoose,"Select Deal Image"),IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            Uri imageUri = data.getData();

            // loading image into image view
            Picasso.get().load(imageUri).into(imageDeal);

            // display progress bar
            mProgressBar.setVisibility(View.VISIBLE);

            // storing image in database
            final StorageReference mUploadRef = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());

            mUploadRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    mUploadRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                           String downloadUrl = uri.toString();

                           String imageName = taskSnapshot.getStorage().getPath();

                            deal.setImageUrl(downloadUrl);
                            deal.setImageName(imageName);

                            // hide progress bar
                            mProgressBar.setVisibility(View.GONE);

                            Log.d("Url:", downloadUrl);
                            Log.d("Name",imageName);
                            displayImage(downloadUrl);


                        }
                    });
                }
            });

        }
        else{
            // hide progress bar
            mProgressBar.setVisibility(View.GONE);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save,menu);

        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.menu_save).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
            // enable button
            btnUpload.setEnabled(true);
            // enable edit Text field
            enableEditText(true);
        }
        else{
            menu.findItem(R.id.menu_save).setVisible(false);
            menu.findItem(R.id.menu_delete).setVisible(false);
            // disable button
            btnUpload.setEnabled(false);
            // disable edit Text field
            enableEditText(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                checkFields();
                return true;

            case R.id.menu_delete:
                deleteTravelDeal();
                backToDealList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void enableEditText(boolean isEnabled){
        editTextTitle.setEnabled(isEnabled);
        editTextPrice.setEnabled(isEnabled);
        editTextDescription.setEnabled(isEnabled);
    }

    private void deleteTravelDeal()
    {
        if(deal == null)
        {
            Toast.makeText(this, getString(R.string.error_delete_msg), Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   // display success message
                   Toast.makeText(TravelDealActivity.this, getString(R.string.msg_delete_success), Toast.LENGTH_SHORT).show();

               }
               else {
                   Toast.makeText(TravelDealActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
               }
            }
        });
        if(deal.getImageName() != null && !deal.getImageName().isEmpty()){
            StorageReference imageRef = FirebaseUtil.mFirebaseStorage.getReference().child(deal.getImageName());
            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    // display success message
                    Toast.makeText(TravelDealActivity.this, getString(R.string.msg_delete_success), Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TravelDealActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void backToDealList() {
        // open deal activity
        Intent intent = new Intent(this, TravelDealListActivity.class);
        startActivity(intent);
    }

    // validating fields
    private void checkFields() {

        String title = editTextTitle.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // check fields for nullability
        if(TextUtils.isEmpty(title))
        {
            editTextTitle.setError(getString(R.string.error_empty_title));
        }
        else if(TextUtils.isEmpty(price))
        {
            editTextTitle.setError(getString(R.string.error_empty_price));
        }
        else if(TextUtils.isEmpty(description))
        {
            editTextTitle.setError(getString(R.string.error_empty_description));
        }
        else{
            // method call to save deal
            saveDeal(title,price,description);
        }


    }

    private void saveDeal(String title, String price, String description)
    {
        // show dialog
        showDialog();

        deal.setTitle(title);
        deal.setPrice(price);
        deal.setDescription(description);

        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(TravelDealActivity.this, getString(R.string.msg_deal_saved), Toast.LENGTH_LONG).show();
                        // method call to clear input fields
                        clearFields();
                        // navigate to list activity
                        backToDealList();
                    }
                    else{
                        Toast.makeText(TravelDealActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // hide dialog
                    hideDialog();
                }
            });
        }
        else{
            mDatabaseReference.child(deal.getId()).setValue(deal)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(TravelDealActivity.this, getString(R.string.msg_deal_saved), Toast.LENGTH_LONG).show();
                        // clear input fields
                        clearFields();
                        // navigate to list activity
                        backToDealList();
                    }
                    else{
                        Toast.makeText(TravelDealActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    // hide dialog
                    hideDialog();
                }
            });
        }

    }

    // clear input fields
    private void clearFields(){
        editTextTitle.setText("");
        editTextPrice.setText("");
        editTextDescription.setText("");
        editTextTitle.requestFocus();
    }

    private void showDialog(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
    }

    private void hideDialog(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

}
