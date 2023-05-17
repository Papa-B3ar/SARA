package com.example.sara;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecordAttendanceFragment extends Fragment {
    EditText unitName,lecturerName;
    Button btnSubmit;
    FirebaseFirestore fStore;
    private Location lastKnownLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_attendance, container, false);

        fStore = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        unitName = view.findViewById(R.id.etUnitName);
        lecturerName = view.findViewById(R.id.etLecturerName);
        btnSubmit = view.findViewById(R.id.btnSubmitAttendance);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        String locationStr = String.format(Locale.getDefault(), "%f, %f", location.getLatitude(), location.getLongitude());
                                        lastKnownLocation = location;
                                        submitAttendanceRecord();
                                    } else {
                                        Toast.makeText(getContext(), "Could not get location", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });
        return view;
    }
    private void submitAttendanceRecord() {
        String unit = unitName.getText().toString().trim();
        String lecturer = lecturerName.getText().toString().trim();

        unit = capitalizeFirstLetters(unit);
        lecturer = capitalizeFirstLetters(lecturer);

        String documentName = lecturer + " - " + unit + " - " + getCurrentDate();
        fStore.collection("attendance sheets").document(documentName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            GeoPoint geofenceCenter = documentSnapshot.getGeoPoint("Center");
                            double geofenceRadius = documentSnapshot.getDouble("Radius");
                            if (lastKnownLocation != null) {
                                double distance = distance(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                                        geofenceCenter.getLatitude(), geofenceCenter.getLongitude());
                                if (distance <= geofenceRadius) {
                                    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
                                    FirebaseAuth fAuth = FirebaseAuth.getInstance();

                                    String studentEmail = fAuth.getCurrentUser().getEmail();
                                    fStore.collection("student").document(studentEmail)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        String studentName = documentSnapshot.getString("Name");
                                                        String studentRegNo = documentSnapshot.getString("Registration Number");

                                                        String unit = unitName.getText().toString().trim();
                                                        String lecturer = lecturerName.getText().toString().trim();
                                                        unit = capitalizeFirstLetters(unit);
                                                        lecturer = capitalizeFirstLetters(lecturer);

                                                        String documentName = lecturer + " - " + unit + " - " + getCurrentDate();

                                                        Map<String, Object> studentData = new HashMap<>();
                                                        studentData.put("Name", studentName);
                                                        studentData.put("Registration Number", studentRegNo);

                                                        fStore.collection("attendance sheets").document(documentName)
                                                                .update("Students", FieldValue.arrayUnion(studentData))
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(getContext(), "Attendance record added successfully", Toast.LENGTH_LONG).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getContext(), "Error adding attendance record", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });

                                                    } else {
                                                        Toast.makeText(getContext(), "Student record not found!", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(getContext(), "You must be in class to record attendance!", Toast.LENGTH_LONG).show();
                                    Toast.makeText(getContext(), "Restart your phone's location service (if you are currently in class) and try again!", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(getContext(),"Sign Sheet not found",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private String capitalizeFirstLetters(String text) {
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1).toLowerCase());
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for calculating distance between two points on a sphere
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}