package com.example.firebaseauthapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int SIGNIN_GOOGLE = 1000;

    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;

    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button signinBtn;
    private Button signupBtn;
    private TextView showCurrentUserText;
    private SignInButton signinGoogleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        setupFirebaseAuth();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        updateUI(currentUser);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null){
        firebaseAuth.signOut();
        }

        if(googleApiClient != null){
            googleApiClient.connect();
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if(status.isSuccess()){
                        Log.d(TAG, "로그아웃 성공");
                        setResult(1);
                    }else {
                        Log.d(TAG, "로그아웃 실패");
                        setResult(0);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNIN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if(account == null){
                    return;
                }
                signInWithGoogle(account);
            } else {
                //구글 로그인 실패
                Log.d(TAG, "onActivityResult() - 로그인 실패");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, String.format("onConnectionFailed() - %s", connectionResult.getErrorMessage()));
    }

    // view 초기화, 버튼 리스너 구현
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_signup:
                createUserWithEmailAndPassword();
                break;
            case R.id.btn_signin:
                signInWithEmailAndPassword();
                break;
            case R.id.btn_signin_google:
                startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), SIGNIN_GOOGLE);
                break;
        }
    }

    private void initView() {
        usernameEdit = findViewById(R.id.edit_username);
        passwordEdit = findViewById(R.id.edit_password);
        signinBtn = findViewById(R.id.btn_signin);
        signinBtn.setOnClickListener(this);
        signupBtn = findViewById(R.id.btn_signup);
        signupBtn.setOnClickListener(this);
        showCurrentUserText = findViewById(R.id.text_show_current_user);
        signinGoogleBtn = findViewById(R.id.btn_signin_google);
        signinGoogleBtn.setOnClickListener(this);
    }

    private void setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            Log.d(TAG, firebaseUser.getEmail());
            showCurrentUserText.setText(firebaseUser.getEmail());
        }
    }

    private void createUserWithEmailAndPassword() {
        final String email = usernameEdit.getText().toString();
        final String password = passwordEdit.getText().toString();
        if (email.length() == 0 || password.length() == 0) {
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signInWithEmailAndPassword() {
        final String email = usernameEdit.getText().toString();
        final String password = passwordEdit.getText().toString();
        if (email.length() == 0 || password.length() == 0) {
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signInWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
}
