package com.example.ElectricBeatBook.UserSession;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.ElectricBeatBook.R;

public class UserSessionActivity extends AppCompatActivity {
    private static final String TAG_FRAGMENT = "tag";
    private FrameLayout frameLayout;
    public static boolean registerFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_session);
      //  getSupportActionBar().hide();
        frameLayout =findViewById(R.id.framelayout);
//        setFragment(new LoginFragment());
//        RegisterFragment =getIntent().getBooleanExtra("Register",false);
        if(registerFrag){
            setFragment(new RegisterFragment());
            registerFrag=false;

        }else{
            setFragment(new LoginFragment());
            FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack(null);
//            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }

    public void updateStatusBarColor(String color){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(color));
//        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryRegister));
    }

    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction= getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out);

        if(fragment instanceof ForgotPasswordFragment || fragment instanceof OTPFragment ){
            fragmentTransaction.addToBackStack(null);
        }

        else if (fragment instanceof RegisterFragment){
//            final LoginFragment fragment = new LoginFragment();
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.framelayout, fragment, TAG_FRAGMENT);
            if (!registerFrag) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }

        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}
