package com.mj1666.providers3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amazonaws.amplify.generated.graphql.CreateTodoMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTodoInput;

public class Form extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText name, phoneNo,address,pincode;
    Button submit;
    Location currentLocation;
    private AWSAppSyncClient mAWSAppSyncClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    String latitude,longitude,occ;
    Spinner occupation;

    private static final int REQUEST_CODE = 101;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);
        name = findViewById(R.id.name);
        phoneNo = findViewById(R.id.phoneno);
        address = findViewById(R.id.add);

        pincode = findViewById(R.id.pin);
        occupation = findViewById(R.id.occ);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.Occupation,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        occupation.setAdapter(adapter);
        occupation.setOnItemSelectedListener(this);
        submit = findViewById(R.id.button3);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(this)

                .awsConfiguration(new AWSConfiguration(this))
                .build();


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
String sid = AWSMobileClient.getInstance().getIdentityId();
                CreateTodoInput createTodoInput = CreateTodoInput.builder().
                        name(name.getText().toString()).
                        phoneNo(phoneNo.getText().toString()).occupation(occ).latitude(latitude).longitude(longitude).sid(sid).address(address.getText().toString()).
                        build();



                mAWSAppSyncClient.mutate(CreateTodoMutation.builder().input(createTodoInput).build())
                        .enqueue(mutationCallback);

                Intent intent = new Intent(Form.this, profile.class);
                startActivity(intent);

            }
        });



    }
    private GraphQLCall.Callback<CreateTodoMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateTodoMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTodoMutation.Data> response) {
            Log.i("Results", "Added Todo");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };
    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                   // Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                   latitude= String.valueOf(currentLocation.getLatitude());
                   longitude= String.valueOf(currentLocation.getLongitude());
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
         occ= parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
