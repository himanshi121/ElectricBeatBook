package com.example.ElectricBeatBook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private ImageView editprofile;

    private CardView camera1;
    private CardView gallery1;
    private CardView upload;
    private CardView account;
    private CircleImageView img_user;
    private  TextView txt_username,txt_email;

    public static FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private StorageReference storage;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        img_user = findViewById(R.id.user_img);
        camera1 = findViewById(R.id.camdb);
        gallery1 = findViewById(R.id.galdb);
        upload = findViewById(R.id.uploaddb);
        account = findViewById(R.id.accountdb);
        txt_username = findViewById(R.id.txt_username);
        txt_email = findViewById(R.id.email);

editprofile=findViewById(R.id.user_img);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Internet Unavailable or User not Signed-In", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            storage = FirebaseStorage.getInstance().getReference();
            db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    String str = documentSnapshot.getString("email");
                    txt_email.setText(str);

                    if (documentSnapshot.getString("firstName") != null) {
                        txt_username.setText("Hello "+documentSnapshot.getString("firstName")+" !");
                    } else {
                        String[] arrOfStr = str.split("@", 2);

                        for (String a : arrOfStr)
                            txt_username.setText("Hello "+arrOfStr[0]+" !");

                    }
                    final StorageReference ref = storage.child("profiles/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide
                                        .with(getApplicationContext())
                                        .load(task.getResult())
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_account)
                                        .into(img_user);
//                            Toast.makeText(EditAccountActivity.this, "set", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
            });

            img_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Intent intent = new Intent(MainActivity.this, EditAccountActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });



            camera1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent2 = new Intent(MainActivity.this, CameraVideoActivity.class);
                    startActivity(intent2);
                }
            });

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent4 = new Intent(MainActivity.this, FirebaseUploadActivity.class);
                    startActivity(intent4);
                }
            });
            account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent5 = new Intent(MainActivity.this, EditAccountActivity.class);
                    startActivity(intent5);
                }
            });
            gallery1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent6 = new Intent(MainActivity.this, GalleryActivity.class);
                    startActivity(intent6);
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser!=null){
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.getString("firstName") != null) {
                        txt_username.setText("Hello "+documentSnapshot.getString("firstName")+" !");
                    } else {
                        String str = documentSnapshot.getString("email");
                        String[] arrOfStr = str.split("@", 2);

                        for (String a : arrOfStr)
                            txt_username.setText("Hello "+arrOfStr[0]+" !");

                    }
                    final StorageReference ref = storage.child("profiles/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Glide
                                        .with(getApplicationContext())
                                        .load(task.getResult())
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_account)
                                        .into(img_user);
//                            Toast.makeText(EditAccountActivity.this, "set", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
            });
        }


        }

}
