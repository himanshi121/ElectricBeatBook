package com.example.ElectricBeatBook.UserSession;


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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ElectricBeatBook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForgotPasswordFragment extends Fragment {

    private TextView forgot_pass_heading;
    private EditText reg_mail_or_phone;
    private ProgressBar progressbar;
    private Button reset_btn;
    private FirebaseAuth firebaseAuth;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$");
    public ForgotPasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((UserSessionActivity)getActivity()).updateStatusBarColor("#20000000");

        init(view);

        TextPaint paint = forgot_pass_heading.getPaint();
        float width = paint.measureText("Verification");

        Shader textShader = new LinearGradient(0, 0, width, forgot_pass_heading.getTextSize(),
                new int[]{
                        Color.parseColor("#7986CB"),
                        Color.parseColor("#70C3E7"),
                }, null, Shader.TileMode.CLAMP);
        forgot_pass_heading.setTextColor(Color.parseColor("#7986CB"));
        forgot_pass_heading.getPaint().setShader(textShader);

        firebaseAuth=FirebaseAuth.getInstance();


        reset_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reg_mail_or_phone.setError(null);
                if (VALID_EMAIL_ADDRESS_REGEX.matcher(reg_mail_or_phone.getText().toString()).find()) {
                    progressbar.setVisibility(View.VISIBLE);
                    reset_btn.setEnabled(false);
                    firebaseAuth.sendPasswordResetEmail(reg_mail_or_phone.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressbar.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), "E-mail sent to reset password successfully", Toast.LENGTH_LONG).show();
                                getActivity().onBackPressed();
                            }
                            else {
                                String error = task.getException().getMessage();
                                Toast.makeText(getContext(),error, Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.INVISIBLE);
                            }
                            reset_btn.setEnabled(true);
                        }

                    });
                }else{
                    reg_mail_or_phone.setError("Invalid E-mail");
                }
            }
        });

    }

    private void init(View view){
        forgot_pass_heading=view.findViewById(R.id.forgot_pass_heading);
        reg_mail_or_phone=view.findViewById(R.id.registered_mail_or_phone);
        progressbar=view.findViewById(R.id.progressBar);
        reset_btn=view.findViewById(R.id.reset_pass_btn);


    }

}
