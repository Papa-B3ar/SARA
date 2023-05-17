package com.example.sara;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private TextView tvFullName, tvRegNumber, tvEmail, tvPhoneNumber;
    private String userID;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvFullName = view.findViewById(R.id.tvFullName);
        tvRegNumber = view.findViewById(R.id.tvRegNumber);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getEmail();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        DocumentReference docRef = fStore.collection("student").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        String email = documentSnapshot.getString("Email Address");
                        String phone = documentSnapshot.getString("Phone Number");
                        String staffNumber = documentSnapshot.getString("Registration Number");

                        tvFullName.setText(name);
                        tvEmail.setText(email);
                        tvPhoneNumber.setText(phone);
                        tvRegNumber.setText(staffNumber);
                    } else {
                        Toast.makeText(getContext(),"Failed to retrieve profile!" +" "+ task.getException(),Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(),"Failed to retrieve profile!"+" "+ task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}