package com.example.sara;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etFullName, etEmailAddress, etPhoneNumber, etPassword, etRegNumber;
    private ProgressBar progressBar;
    private Button btnRegister;
    private TextView tvSignIn;
    private FirebaseAuth fAuth;
    FirebaseFirestore db;
    String documentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }

        etFullName = findViewById(R.id.etFullName);
        etEmailAddress = findViewById(R.id.etKcaEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etRegNumber = findViewById(R.id.etRegNumber);
        etPassword = findViewById(R.id.etPassword);

        progressBar = findViewById(R.id.progressBar);

        btnRegister = findViewById(R.id.btnRegister);

        tvSignIn = findViewById(R.id.tv_Login);

        btnRegister.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRegister:
                registerUser();
                break;

            case R.id.tv_Login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString();
        String email = etEmailAddress.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString();
        String regNumber = etRegNumber.getText().toString();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(RegisterActivity.this, "Please enter your full name!", Toast.LENGTH_LONG).show();
            etFullName.setError("Full name is required!");
            etFullName.requestFocus();
        }else if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please enter your email!", Toast.LENGTH_LONG).show();
            etEmailAddress.setError("Email is required!");
            etEmailAddress.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email address!",Toast.LENGTH_LONG).show();
            etEmailAddress.setError("Valid email address is required!");
            etEmailAddress.requestFocus();
        }else if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(RegisterActivity.this, "Please enter your phone number!",Toast.LENGTH_LONG).show();
            etPhoneNumber.setError("Phone number is required!");
            etPhoneNumber.requestFocus();
        }else if (phoneNumber.length() != 10){
            Toast.makeText(RegisterActivity.this, "Please check your phone number!",Toast.LENGTH_LONG).show();
            etPhoneNumber.setError("Phone number should be 10 digits!");
            etPhoneNumber.requestFocus();
        }else if (TextUtils.isEmpty(regNumber)) {
            Toast.makeText(RegisterActivity.this, "Please enter your staff number!", Toast.LENGTH_LONG).show();
            etRegNumber.setError("Staff number is required!");
            etRegNumber.requestFocus();
        }else if (regNumber.length() < 8 || regNumber.length() > 8){
            Toast.makeText(RegisterActivity.this, "Please check your staff number!", Toast.LENGTH_LONG).show();
            etRegNumber.setError("Staff number should be between 3 - 5 characters!");
            etRegNumber.requestFocus();
        }else if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please enter your password!",Toast.LENGTH_LONG).show();
            etPassword.setError("Password is required!");
            etPassword.requestFocus();

        }else if(password.length() < 6 ){
            Toast.makeText(RegisterActivity.this, "Password is too weak!",Toast.LENGTH_LONG).show();
            etPassword.setError("Password should be at least 6 characters or digits!");
            etPassword.requestFocus();
        }
        progressBar.setVisibility(View.VISIBLE);

        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this,"Registration Successful.", Toast.LENGTH_LONG).show();

                documentID = fAuth.getCurrentUser().getEmail();

                Map<String,Object> student = new HashMap<>();
                student.put("Name",fullName);
                student.put("Email Address",email);
                student.put("Phone Number",phoneNumber);
                student.put("Registration Number",regNumber);

                db.collection("student").document(documentID).set(student)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(RegisterActivity.this,"Data captured.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this,"Data capture error!", Toast.LENGTH_SHORT).show();
                            }
                        });
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
            else {
                Toast.makeText(RegisterActivity.this, "Registration not Successful." + " " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}