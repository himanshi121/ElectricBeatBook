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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ElectricBeatBook.MainActivity;
import com.example.ElectricBeatBook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class OTPFragment extends Fragment {
    private TextView verification_txt,otp_sent_txt;
    private EditText enter_otp ;
    private Button verify,resend;
    private ProgressBar progressBar;
    private Timer timer;
    private int count=60;
    String username,phone,password;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallback;
    private FirebaseAuth firebaseAuth;


    public OTPFragment() {
        // Required empty public constructor
    }
    public OTPFragment(String username, String phone, String password) {
        this.username = username;
        this.phone = phone;
        this.password = password;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ot, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((UserSessionActivity)getActivity()).updateStatusBarColor("#20000000");

        init(view);

        TextPaint paint = verification_txt.getPaint();
        float width = paint.measureText("Verification");

        Shader textShader = new LinearGradient(0, 0, width, verification_txt.getTextSize(),
                new int[]{
                        Color.parseColor("#7986CB"),
                        Color.parseColor("#70C3E7"),
                }, null, Shader.TileMode.CLAMP);
        verification_txt.setTextColor(Color.parseColor("#7986CB"));
        verification_txt.getPaint().setShader(textShader);


        otp_sent_txt.setText("OTP has been sent to +91"+phone);
        sendOTP();

        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (count==0){
                            resend.setText("Resend");
                            resend.setEnabled(true);
                            resend.setAlpha(1f);
                        }
                        else {
                            resend.setText("Resend In "+count);
                            count--;
                        }
                    }
                });

            }
        },0,1000);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter_otp.setError(null);
                if (enter_otp.getText()== null || enter_otp.getText().toString().isEmpty()){
                    enter_otp.setError("Required!");
                    return;
                }

                String code =enter_otp.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);

                progressBar.setVisibility(View.VISIBLE);


            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendotp();
                resend.setEnabled(false);
                resend.setAlpha(0.5f);
                count=60;

            }
        });

        firebaseAuth=FirebaseAuth.getInstance();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private void sendOTP(){
        mcallback =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
//                        Log.d(TAG, "onVerificationCompleted:" + credential);
//
//                        signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
//                        Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    enter_otp.setError(e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    enter_otp.setError(e.getMessage());
                }
                progressBar.setVisibility(View.INVISIBLE);

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
//                        Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mcallback);        // OnVerificationStateChangedCallbacks
    }


    private  void  resendotp(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mcallback,mResendToken);        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");

                            final FirebaseUser user = task.getResult().getUser();
                            AuthCredential credential= EmailAuthProvider.getCredential(username,password);
                            user.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("email",username);
                                        map.put("phone", phone);

                                        firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
                                                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Intent mainIntent = new Intent (getContext(), MainActivity.class);
                                                                startActivity(mainIntent);
                                                                getActivity().finish();
//
                                                   // Toast.makeText(getContext(), "Account Successfully Created!", Toast.LENGTH_SHORT).show();

//                                                    firebase      Firestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid())
//                                                            .collection("UserData").document("Wishlist")
//                                                            .set(documentFields).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                            if (task.isSuccessful()){
//                                                                Intent mainIntent = new Intent (getContext(), MainActivity.class);
//                                                                startActivity(mainIntent);
////                                                    disableCloseBtn=false;
//                                                                getActivity().finish();
//
//                                                            }else{
//                                                                String error = task.getException().getMessage();
//                                                                Toast.makeText(getContext(),error , Toast.LENGTH_SHORT).show();
//                                                                progressBar.setVisibility(View.INVISIBLE);
//
//                                                            }
//                                                        }
//                                                    });

                                                }else{
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(getContext(),error , Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.INVISIBLE);

                                                }
                                            }
                                        });

                                    }
                                    else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(getContext(),error , Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                enter_otp.setError("Invalid OTP ");// The verification code entered was invalid
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }



    private void init(View view){
        verification_txt=view.findViewById(R.id.verification_txt);
        otp_sent_txt=view.findViewById(R.id.otp_sent_txt);
        enter_otp=view.findViewById(R.id.enter_otp);
        verify=view.findViewById(R.id.verify_btn);
        resend=view.findViewById(R.id.resend_btn);
        progressBar=view.findViewById(R.id.progressBar);

    }

}
