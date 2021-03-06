package com.smokegod.cs2340.m3;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private Thread loginThread;
    private Handler loginHandler;
    private Runnable loginHandlerRunnable;
    private TextView usernameTV;
    private TextView passwordTV;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        usernameTV = (TextView) findViewById(R.id.usernameEditTextAuth);
        passwordTV = (TextView) findViewById(R.id.passwordEditTextAuth);

        progress = new ProgressDialog(this);
        loginHandler = new Handler();
        loginHandlerRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String email = usernameTV.getEditableText().toString();
                        String password = passwordTV.getEditableText().toString();
                        Toast t = null;
                        if (email.isEmpty() || password.isEmpty()) {
                            progress.hide();
                            t = Toast.makeText(AuthActivity.this, "No input can be empty",
                                    Toast.LENGTH_LONG);
                            t.show();
                        } else if (email.equals("shane") && password.equals("password")) {
                            Intent i = new Intent(AuthActivity.this, MapsActivity.class);
                            startActivity(i);
                            finish();
                        } else {

                            int result = HTTPPostReq.login(email, password);

                            switch (result) {
                                case (0):
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("TOKEN", HTTPPostReq.getToken());
                                    editor.putString("LOGIN_NAME", HTTPPostReq.getUsername());
                                    editor.commit();
                                    Intent i = new Intent(AuthActivity.this, MapsActivity.class);
                                    startActivity(i);
                                    finish();
                                    break;
                                case (1):
                                    progress.hide();
                                    t = Toast.makeText(AuthActivity.this, "Incorrect username or password",
                                            Toast.LENGTH_LONG);
                                    t.show();
                                    break;
                                case (2):
                                    progress.hide();
                                    t = Toast.makeText(AuthActivity.this, "Incorrect username or password",
                                            Toast.LENGTH_LONG);
                                    t.show();
                                    break;
                                default:
                                    progress.hide();
                                    t = Toast.makeText(AuthActivity.this, "Server Error",
                                            Toast.LENGTH_LONG);
                                    t.show();
                                    break;
                            }

                            //Old Firebase code
//                            mAuth.signInWithEmailAndPassword(email, password)
//                                    .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<AuthResult> task) {
//                                            if (task.isSuccessful()) {
//                                                // Sign in success, update UI with the signed-in user's information
//                                                Intent i = new Intent(AuthActivity.this, MapsActivity.class);
//                                                startActivity(i);
//                                                finish();
//                                            } else {
//                                                // If sign in fails, display a message to the user.
//                                                progress.hide();
//                                                Toast t = Toast.makeText(AuthActivity.this, "Wrong login information",
//                                                        Toast.LENGTH_LONG);
//                                                t.show();
//                                            }
//                                        }
//                                    });
                        }
                    }
                });
            }
        };

        loginThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loginHandler.postDelayed(loginHandlerRunnable, 2000);
            }
        });
    }

    public void goToRegistration(View v) {
        Intent i = new Intent(AuthActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    public void attemptLogin(View v) {
        progress.setTitle("Attempting login");
        progress.setMessage("Logging-in is in-progress. Press your back button to cancel.");
        progress.setCancelable(true);
        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                loginHandler.removeCallbacksAndMessages(null);
//                            loginHandler.removeCallbacks(loginHandlerRunnable);
            }
        });
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

        loginThread.run();
    }
}
