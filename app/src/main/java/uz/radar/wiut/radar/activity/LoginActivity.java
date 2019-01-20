package uz.radar.wiut.radar.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import uz.radar.wiut.radar.MainActivity;
import uz.radar.wiut.radar.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    LinearLayout ll_number;
    EditText et_email;
    EditText et_password;
    Button btn_send_code;
    LinearLayout ll_code;
    EditText et_email_register;
    EditText et_password_register;
    Button btn_send_code_register;
    TextView et_resiter;
    TextView et_login;

    String mMerificationId = new String();
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Log.d("[###]", String.valueOf(mAuth.getCurrentUser()));
            successLogin();
        }


        ll_number = findViewById(R.id.ll_number);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_send_code = findViewById(R.id.btn_send_code);
        ll_code = findViewById(R.id.ll_code);
        et_email_register = findViewById(R.id.et_email_register);
        et_password_register = findViewById(R.id.et_password_register);
        btn_send_code_register = findViewById(R.id.btn_send_code_register);
        et_resiter = findViewById(R.id.et_resiter);
        et_login = findViewById(R.id.et_login);

        et_resiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_number.setVisibility(View.GONE);
                ll_code.setVisibility(View.VISIBLE);
            }
        });
        et_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll_number.setVisibility(View.VISIBLE);
                ll_code.setVisibility(View.GONE);
            }
        });
    }

    public void register(View view) {
        mAuth.createUserWithEmailAndPassword(et_email_register.getText().toString(), et_password_register.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("[###]: Login >> ", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            successLogin();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("[###]: Login >> ", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void login(View view) {
        mAuth.signInWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("[###]: Login >> ", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Succes login.",
                                    Toast.LENGTH_SHORT).show();
                            successLogin();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("[###]: Login >> ", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void successLogin() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }


}
