package com.example.sara;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmailAddress;
    private Button btnReset;
    private TextView tvSignIn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getSupportActionBar().hide();

        btnReset = findViewById(R.id.btnReset);
        tvSignIn = findViewById(R.id.tvSignIn);
        etEmailAddress = findViewById(R.id.etKcaEmail);
        progressBar = findViewById(R.id.progressBar);

        btnReset.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnReset:
                resetPassword();
                break;

            case R.id.tvSignIn:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
    private void resetPassword() {
        String email = etEmailAddress.getText().toString().trim();

        if (email.isEmpty()){
            etEmailAddress.setError("Email is required!");
            etEmailAddress.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailAddress.setError("Provide valid a valid email address!");
            etEmailAddress.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
    }
}