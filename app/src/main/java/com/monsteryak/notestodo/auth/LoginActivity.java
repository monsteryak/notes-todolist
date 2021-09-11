package com.monsteryak.notestodo.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.monsteryak.notestodo.Home;
import com.monsteryak.notestodo.MainActivity;
import com.monsteryak.notestodo.R;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    public static final int GOOGLE_SIGN_IN_CODE = 10005;

    EditText lEmail,lPass;
    Button uLogin;
    SignInButton signInButton;
    TextView forgetPass, createAcc;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser fUser;
    GoogleSignInOptions gso;
    GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login to Notes&ToDo");

        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        lEmail = findViewById(R.id.email);
        lPass = findViewById(R.id.lPassword);
        uLogin = findViewById(R.id.loginBtn);
        createAcc = findViewById(R.id.createAccount);
        forgetPass = findViewById(R.id.forgotPassword);
        signInButton = findViewById(R.id.googleLogin);
        spinner =findViewById(R.id.progressBar3);

        showWarning();

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });



        uLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = lEmail.getText().toString();
                String mPass = lPass.getText().toString();

                if (mEmail.isEmpty() || mPass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Fields are Required", Toast.LENGTH_SHORT).show();
                    return;
                }

                spinner.setVisibility(View.VISIBLE);

                //delete notes
                deleteAnonymousUser();

                fAuth.signInWithEmailAndPassword(mEmail,mPass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(LoginActivity.this, "Success!!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),Home.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Login Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                    }
                });
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("255639997584-3laam4hmn4dr8umc1ke3dml4i8rkao87.apps.googleusercontent.com")
                .requestEmail()
                .build();

        //Getting the GoogleSignIn Client with Google SignIn Options
        signInClient = GoogleSignIn.getClient(this,gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                deleteAnonymousUser();
                Intent sign = signInClient.getSignInIntent();
                startActivityForResult(sign, GOOGLE_SIGN_IN_CODE);
            }
        });


    }

    private void deleteAnonymousUser() {

        if (fAuth.getCurrentUser().isAnonymous()) {

            //FirebaseUser fUser = fAuth.getCurrentUser();

            fStore.collection("Users").document(fUser.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {


                    Toast.makeText(LoginActivity.this, "All temp Notes are Deleted", Toast.LENGTH_SHORT).show();
                }
            });



            //delete temp user
            fUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(LoginActivity.this, "Temporary User Deleted", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure ?")
                .setMessage("Syncing Existing Account will delete all temporary Notes. Create New Account To Save")
                .setPositiveButton("Save Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                    }
                }).setNegativeButton("It's Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        fUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                //Nothing
//                            }
//                        });
                    }
                });
        warning.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_CODE){
            Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount signInAcc = signInTask.getResult(ApiException.class);

                Log.d("log1", "firebaseAuthWithGoogle:" + signInAcc.getId());
                firebaseAuthWithGoogle(signInAcc.getIdToken());

            } catch (ApiException e) {
                e.printStackTrace();
            }

        }
    }

    //For getting/inserting the data of Google SignIn Into the Foribase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("successlogin", "signInWithCredential:success");
                    //Showing a text confirmation message to the user for login
                    Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(),Home.class));
                            finish();
                        }
                    }, 500);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("failledlogin", "signInWithCredential:failure", task.getException());
                    Snackbar.make(Objects.requireNonNull(getCurrentFocus()), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, Home.class));
        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
        return super.onOptionsItemSelected(item);
    }
}