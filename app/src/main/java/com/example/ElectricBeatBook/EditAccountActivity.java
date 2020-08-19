package com.example.ElectricBeatBook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ElectricBeatBook.UserSession.UserSessionActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends AppCompatActivity {
    private CircleImageView profileImg;

    private Uri photoUri;
    private String url="";
    private TextInputEditText fullName,email,contact_no,notes;
    private TextView saveChanges,logout,profileusername;
    StorageReference storage;
    private FirebaseAuth firebaseAuth;
    AlertDialog.Builder builder;
    private ImageView imgBack,imgLogout,removeCircle,removeCross;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        profileImg = findViewById(R.id.profile_image);
        removeCircle = findViewById(R.id.imageViewCross);
        removeCross = findViewById(R.id.imageViewColorCross);
        imgBack=findViewById(R.id.img_back);
        imgLogout=findViewById(R.id.img_logout);
        fullName = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        contact_no = findViewById(R.id.contactno);
        notes = findViewById(R.id.notes);
        saveChanges = findViewById(R.id.saveChanges_btn);
        logout = findViewById(R.id.logout_btn);
        profileusername = findViewById(R.id.profileusername);
        builder = new AlertDialog.Builder(this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();



        db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
////////UpdateProfileTitle

                if (documentSnapshot.getString("fullName" ) != null ) {
                    String fullNameString=documentSnapshot.getString("fullName");
                  //  String lastNameString=documentSnapshot.getString("lastName");
                   // String fullNameString= firstNameString + " " + lastNameString;
                    profileusername.setText("Hey "+fullNameString+" !");

                } else {
                    String str = documentSnapshot.getString("email");
                    String[] arrOfStr = str.split("@", 2);

                    for (String a : arrOfStr)
                        profileusername.setText("Hey "+arrOfStr[0]+" !");

                }
                contact_no.setText(documentSnapshot.getString("phone"));
                email.setText(documentSnapshot.getString("email"));
////////UpdateProfileTitle

////////UpdatePersonalDetails
                fullName.setText(documentSnapshot.getString("fullName"));
                notes.setText(documentSnapshot.getString("notes"));
////////UpdatePersonalDetails



////////SetProfilePicture

                final StorageReference ref = storage.child("profiles/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
                ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Glide
                                    .with(getApplicationContext())
                                    .load(task.getResult())
                                    .centerCrop()
                                    .placeholder(R.drawable.user_profile)
                                    .into(profileImg);
                            removeCircle.setVisibility(View.VISIBLE);
                            removeCross.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(EditAccountActivity.this, "Please Update Your Profile", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditAccountActivity.this, "Failed to Update Changes!", Toast.LENGTH_SHORT).show();


            }
        });




        /////////saveChangesPersonalDetails Button
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String fullNameTxt= fullName.getText().toString();
                profileusername.setText("Hey "+fullNameTxt+" !");

               // progress_layout.setVisibility(View.VISIBLE);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", fullNameTxt);
        //  map.put("lastName", LastNameTxt);
        map.put("notes", notes.getText().toString());

        db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditAccountActivity.this, "Successfully Updated !", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditAccountActivity.this, "error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
});
/////////saveChangesPersonalDetails Button


                ////////BackButton
        imgBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(EditAccountActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


                    }
                });
////////BackButton

////////LogoutButton&Image
        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(EditAccountActivity.this, UserSessionActivity.class);
                startActivity(intent);
                finishAffinity();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(EditAccountActivity.this, UserSessionActivity.class);
                startActivity(intent);
                finishAffinity();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });



/////////RemoveProfilePicture
        removeCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setMessage("                                                                                Do you want to delete profile picture?")
                        .setCancelable(false)
                        .setPositiveButton(Html.fromHtml("<font color='#FF7F27'>Yes</font>"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                StorageReference ref = storage.child("profiles/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
                                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        url = null;
                                        photoUri = null;
                                        profileImg.setImageResource(R.drawable.user_profile);
                                        Map<String, Object> map = new HashMap<>();
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        map.put("profile_url", url);
                                        db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update(map);
                                        Toast.makeText(EditAccountActivity.this, "Successfully Deleted", Toast.LENGTH_SHORT).show();
                                        removeCircle.setVisibility(View.INVISIBLE);
                                        removeCross.setVisibility(View.INVISIBLE);
                                        // File deleted successfully
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        if (photoUri == null) {
                                            Toast.makeText(EditAccountActivity.this, "Already Deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EditAccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            // Uh-oh, an error occurred!
                                        }
                                    }
                                });

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();

                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle(" Alert!");
                alert.show();
            }
        });
/////////RemoveProfilePicture

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
                Dexter.withActivity(EditAccountActivity.this)
                        .withPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            selectImage();

                        } else {
                            Toast.makeText(EditAccountActivity.this, "Please allow Permissions!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();
            }
        });

    }

    private void selectImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setActivityTitle("Profile Picture")
                .setAspectRatio(1,1)
                .setFixAspectRatio(true)
                .start(this);
    }


    private void updateChanges() {

        if (photoUri != null) {

            final StorageReference ref = storage.child("profiles/"+firebaseAuth.getCurrentUser().getUid() + ".jpg");
            UploadTask uploadTask = ref.putFile(photoUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<
                        UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        String error = task.getException().getMessage();
                        Toast.makeText(EditAccountActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url = uri.toString();
                            Map<String, Object> map = new HashMap<>();
                            map.put("profile_url", url);
//                            Toast.makeText(EditAccountActivity.this, url, Toast.LENGTH_SHORT).show();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(firebaseAuth.getCurrentUser().getUid()).update(map);



                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                       // progress_layout.setVisibility(View.GONE);

//uplDetails()
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(EditAccountActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                photoUri = result.getUri();
                updateChanges();
                Glide
                        .with(this)
                        .load(photoUri)
                        .centerCrop()
                        .placeholder(R.drawable.user_profile)
                        .into(profileImg);
                removeCircle.setVisibility(View.VISIBLE);
                removeCross.setVisibility(View.VISIBLE);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
              //  progress_layout.setVisibility(View.GONE);
                Exception error = result.getError();
                Toast.makeText(EditAccountActivity.this, error.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }
    }


}