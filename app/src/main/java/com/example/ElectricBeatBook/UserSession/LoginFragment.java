package com.example.ElectricBeatBook.UserSession;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ElectricBeatBook.MainActivity;
import com.example.ElectricBeatBook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = "usernname";

    public LoginFragment() {
        // Required empty public constructor
    }
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private EditText username_or_phone,password;
    private TextView forgot_password_txt,sign_up__txt;
    private Button login;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressbar;
    private ImageView skipLoginBtn;
    private  TextView appName;
    public static boolean disableCloseBtn=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((UserSessionActivity)getActivity()).updateStatusBarColor("#20000000");

        View view= inflater.inflate(R.layout.fragment_login, container, false);
        skipLoginBtn=view.findViewById(R.id.skipLoginImg);

        appName=view.findViewById(R.id.appNametxt);
        TextPaint paint =appName.getPaint();
        float width = paint.measureText("INVESTICAM");


        Shader textShader = new LinearGradient(0, 0, width, appName.getTextSize(),
                new int[]{
                        Color.parseColor("#7986CB"),
                        Color.parseColor("#70C3E7"),
                }, null, Shader.TileMode.CLAMP);
        appName.setTextColor(Color.parseColor("#7986CB"));
        appName.getPaint().setShader(textShader);




        if (disableCloseBtn){
            skipLoginBtn.setVisibility(View.GONE);
        }else {
            skipLoginBtn.setVisibility(View.VISIBLE);
        }
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        sign_up__txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((UserSessionActivity) getActivity()).setFragment(new RegisterFragment());
            }
        });

        forgot_password_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UserSessionActivity) getActivity()).setFragment(new ForgotPasswordFragment());
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username_or_phone.setError(null);
                password.setError(null);


                if (username_or_phone.getText().toString().isEmpty()) {
                    username_or_phone.setError("Required!");
                    return;

                }

                if (password.getText().toString().isEmpty()) {
                    password.setError("Required!");
                    return;
                }

                if (VALID_EMAIL_ADDRESS_REGEX.matcher(username_or_phone.getText().toString()).find()) {
                    login(username_or_phone.getText().toString());

                } else if (username_or_phone.getText().toString().matches("\\d{10}")) {
                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                    firebaseAuth=FirebaseAuth.getInstance();
                    firebaseFirestore.collection("Users").whereEqualTo("phone", username_or_phone.getText().toString())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                List<DocumentSnapshot> document =task.getResult().getDocuments();
                                if(document.isEmpty()){
                                    username_or_phone.setError("Phone number not found!");
                                    progressbar.setVisibility(View.VISIBLE);
                                    return;
                                }else{
                                    String email=document.get(0).get("email").toString();
                                    login(email);
                                }
                            }else {
                                String error =task.getException().getMessage();
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    username_or_phone.setError("Invalid Email or Phone");

                }
            }
        });

        skipLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getContext(),MainActivity.class);
//                intent.putExtra("Not Registered", true);
                startActivity(intent);
                disableCloseBtn=false;
                getActivity().finishAffinity();

            }
        });
    }


    private void init(View view){
        username_or_phone=view.findViewById(R.id.usernameOrPhone);
        password=view.findViewById(R.id.password);
        forgot_password_txt=view.findViewById(R.id.forgot_password_txt);
        sign_up__txt=view.findViewById(R.id.sign_up_txt);
        login=view.findViewById(R.id.btn_login);
        progressbar=view.findViewById(R.id.progressBar);


    }

    private void login(String username){
        progressbar.setVisibility(View.VISIBLE);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(username,password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    if (disableCloseBtn){
                        disableCloseBtn=false;
                    }else {
                        Intent mainIntent =new Intent(getContext(), MainActivity.class);
//                    mainIntent.putExtra("Not Registered", false);
                        startActivity(mainIntent);}
                    getActivity().finish();

                }
                else{ String error=task.getException().getMessage();
                    Toast.makeText(getContext(),error,  Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.INVISIBLE);

                }
            }
        });
    }

}
