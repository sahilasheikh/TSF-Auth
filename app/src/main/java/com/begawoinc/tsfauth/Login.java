package com.begawoinc.tsfauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class Login extends AppCompatActivity {

    TextView id, password;
    Button git;
    SignInButton google_signin;
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    FirebaseDatabase database;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            Login.this.finish();
        }
        else {
            Toast.makeText(getApplicationContext(),"Login To Continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        hiding status bar
        getSupportActionBar().hide();

//        assigning variables
        git = findViewById(R.id.git);
        google_signin = findViewById(R.id.google_signin);
        mAuth = FirebaseAuth.getInstance();
        id = findViewById(R.id.username);
        password = findViewById(R.id.password);


//      github login
        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(id.getText().toString())){
                    Toast.makeText(getApplication(), "Enter your email id", Toast.LENGTH_SHORT).show();
                }
                else {
                    SignInWithGithubProvider(
                            OAuthProvider.newBuilder("github.com")
                                    .addCustomParameter("login",id.getText().toString()).setScopes(
                                    new ArrayList<String>(){
                                        {
                                            add("user:email");
                                        }
                                    })

                                    .build()
                    );
                }
            }
        });

//      google sign in
//      creating request for google signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
//        build a GoogleSignInClient with the option specification by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        google_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signinIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signinIntent, RC_SIGN_IN);
            }
        });


    }

    private void SignInWithGithubProvider(OAuthProvider login) {
        Task<AuthResult> pendingAuthTask = mAuth.getPendingAuthResult();
        if (pendingAuthTask != null){
            pendingAuthTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(Login.this, "User Exist ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            mAuth.startActivityForSignInWithProvider(this,login).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(Login.this,"Login Successfull", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    String fullname =user.getDisplayName();
                    String email =user.getEmail();
                    String number =user.getPhoneNumber();

                    DataClass dataClass = new DataClass(
                            fullname,
                            email,
                            number
                    );


                    database.getReference("Data").child(user.getUid()).setValue(dataClass);

                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(Login.this, "Sign in with Github", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
//                            FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }


//    open register activity
    public void register(View view) {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
        this.finish();
    }


}