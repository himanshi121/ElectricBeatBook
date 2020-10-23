package com.example.ElectricBeatBook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.provider.MediaStore.Video.VideoColumns.LATITUDE;
import static android.provider.MediaStore.Video.VideoColumns.LONGITUDE;

public class FirebaseUploadActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView mButtonChooseImage;
    private TextView mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageRef;

    private StorageTask mUploadTask;
    StorageReference storage;
    private FirebaseAuth firebaseAuth;

    float Latitude, Longitude;
    String textUri;
    Uri targetUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_upload);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.WHITE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp);
        upArrow.setColorFilter(getResources().getColor(R.color.black_overlay2), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        ////mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        storage = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(FirebaseUploadActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    Dexter.withActivity(FirebaseUploadActivity.this)
                            .withPermissions(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                uploadFile();
                            } else {
                                Toast.makeText(FirebaseUploadActivity.this, "Please allow Permissions!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        }
                    }).check();
                }
            }
        });
        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesActivity();
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            targetUri = mImageUri;
//            textUri=mImageUri.toString();
            Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    String showExif(Uri photoUri){
        String exif="";
        if(photoUri != null){

//            String photoPath = getRealPathFromURI(photoUri);

            ParcelFileDescriptor parcelFileDescriptor = null;

            /*
            How to convert the Uri to FileDescriptor, refer to the example in the document:
            https://developer.android.com/guide/topics/providers/document-provider.html
             */

            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(photoUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();


                /*
                ExifInterface (String filename) added in API level 5
                 */
                ExifInterface exifInterface = new ExifInterface(fileDescriptor);

                String LATITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String LATITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String LONGITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                String LONGITUDE_REF = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
// your Final lat Long Values
                if((LATITUDE !=null)
                        && (LATITUDE_REF !=null)
                        && (LONGITUDE != null)
                        && (LONGITUDE_REF !=null))
                {
                    if(LATITUDE_REF.equals("N")){
                        Latitude = convertToDegree(LATITUDE);
                    }
                    else{
                        Latitude = 0 - convertToDegree(LATITUDE);
                    }
                    if(LONGITUDE_REF.equals("E")){
                        Longitude = convertToDegree(LONGITUDE);
                    }
                    else{
                        Longitude = 0 - convertToDegree(LONGITUDE);
                    }
                }

                exif="Exif: ";
                exif += "\n DATETIME: " + exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                exif += "\n MODEL: " + exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                exif += exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                exif += "\n GPS LOCATION:";
                exif += "\n LATITUDE: " +String.valueOf(Latitude);
                exif += "\n LONGITUDE: " + String.valueOf(Longitude);

                parcelFileDescriptor.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Something wrong:\n" + e.toString(),
                        Toast.LENGTH_LONG).show();
            }

            String strPhotoPath = photoUri.getPath();
        }else{
            Toast.makeText(getApplicationContext(),
                    "photoUri == null",
                    Toast.LENGTH_LONG).show();
        }
        return exif;
    }

    /*
    This method getRealPathFromURI() is not coded by me,
    but I forgot where I copy it from.
     */
    /*private String getRealPathFromURI(Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getApplicationContext().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }*/

    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;


    }

    private void uploadFile() {

        if (mImageUri != null) {
            //////////
            final StorageReference fileReference = storage.child("uploads/" + firebaseAuth.getCurrentUser().getUid() + "/"+System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);
//                            Toast.makeText(FirebaseUploadActivity.this, "Upload successful", Toast.LENGTH_LONG).show();

                            Task<Uri>urlTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!urlTask.isSuccessful());
                            Uri downloadUrl=urlTask.getResult();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> map = new HashMap<>();
                            map.put("imageUrl", downloadUrl.toString());
                            map.put("name", mEditTextFileName.getText().toString().trim());
                            map.put("details",showExif(targetUri));
                            //map.put("timestamp",22-10-2020);
                          map.put("timestamp",new SimpleDateFormat("dd-MM-yy",Locale.getDefault()).format(new Date()));

                            db.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection("UserData")
                                    .add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(FirebaseUploadActivity.this, "Successfully Updated !", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        String error=task.getException().getMessage();
                                        Toast.makeText(FirebaseUploadActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FirebaseUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void openImagesActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
