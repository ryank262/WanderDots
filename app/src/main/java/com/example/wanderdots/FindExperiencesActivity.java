package com.example.wanderdots;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import WanderDots.Adventure;
import WanderDots.Dot;
import WanderDots.Observer ;
import WanderDots.Server.Get.GetAdventures;
import WanderDots.Server.Get.GetDots;

public class FindExperiencesActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, Observer, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    private static final float DEFAULT_ZOOM = 11f;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FloatingActionButton newDotButton;
    private RecyclerView listContainer;
    private GoogleMap mMap;

    private ArrayList<Dot> dotList ;
    private ExperienceListAdapter<Dot> dotListAdapter;
    private ArrayList<Adventure> adventureList ;
    private ExperienceListAdapter<Adventure> adventureListAdapter ;
    private ExperienceListAdapter mainListAdapter ;

    private Boolean mLocationPermissionGranted = false ; // Whether user has permitted us to access the devices location
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GetDots getDots ;
    private GetAdventures getAdventures ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_view);
        getLocationPermission();

        //SET CREATEBUTTON HANDLER
        this.newDotButton = (FloatingActionButton) findViewById(R.id.new_dot_btn);
        this.newDotButton.setOnClickListener(this) ;

        //SET TOGGLE BUTTON HANDLER
        ToggleButton toggle = (ToggleButton) findViewById(R.id.listToggleButton) ;
        toggle.setOnCheckedChangeListener(this)  ;

        //DOT LIST ADAPTER
        this.dotList = new ArrayList<>() ;
        this.dotListAdapter = new ExperienceListAdapter(this.dotList, this);
        this.dotListAdapter.notifyDataSetChanged();

        //ADVENTURE LIST ADAPTER
        this.adventureList = new ArrayList<>() ;
        this.adventureListAdapter = new ExperienceListAdapter(this.adventureList, this) ;
        this.adventureListAdapter.notifyDataSetChanged();

        //MAIN LIST ADAPTER
        this.mainListAdapter = dotListAdapter ;
        this.listContainer = (RecyclerView) findViewById(R.id.main_recycler_view);
        this.listContainer.setHasFixedSize(true);
        this.listContainer.setLayoutManager(new LinearLayoutManager(this));
        setAdapterOnFocus(mainListAdapter);

        //SERVER CONNECTION
        this.getDots = new GetDots(this.getApplicationContext(), this) ;
        this.getAdventures = new GetAdventures(this.getApplicationContext(), this) ;
        this.getDots.loadDots();
        this.getAdventures.loadAdventures();

    }

    //Runs whenever either list has loaded, message contains name of list that changed
    public void dataHasChanged(String message){

        if(message.equals("dots")){
            updateList(this.dotList, getDots.getDots()) ;
            this.dotListAdapter.notifyDataSetChanged();
            this.addDotsToMap();
        } else {
            updateList(this.adventureList, getAdventures.getAdventures()) ;
            this.adventureListAdapter.notifyDataSetChanged();
            this.addDotsToMap();
        }
    }

    //Runs whenever SwitchButton changes state and switches the list in view
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            setAdapterOnFocus(dotListAdapter);
            this.mainListAdapter = dotListAdapter ;
        } else {
            setAdapterOnFocus(adventureListAdapter);
            this.mainListAdapter = adventureListAdapter ;
        }
    }

    //Runs whenever the NEWDOTBUTTON has been clicked, begins NewDotActivity
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.getApplicationContext(), NewDotActivity.class);
        startActivity(intent);
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionResult: permissions failed");
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionResult: permissions granted");
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapRead: Map is ready");
        this.mMap = googleMap;
        this.mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        if (mLocationPermissionGranted){
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            this.mMap.setMyLocationEnabled(true);
        }
        addDotsToMap();
    }

    private void addDotsToMap(){
        if(this.mMap == null)
            return ; //in case maps loads after dot list
        for(Dot dot : dotList){ //TODO: generalize main list so dots are added based on selection
            MarkerOptions options = new MarkerOptions()
                    .title(dot.getName())
                    .position(new LatLng(dot.getLatitude(), dot.getLongitude()));
            mMap.addMarker(options);
        }
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void initMap(){
        Log.d(TAG, "intMap: intializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.the_map);
        mapFragment.getMapAsync(this);
    }

   //Replaces elements in oldList with those in newList, keeps reference to old list
    private <T> void updateList(ArrayList<T> oldList, ArrayList<T> newList){
        for(T thing : oldList)
            oldList.remove(thing) ;
        for(T newThing: newList)
            oldList.add(newThing) ;
    }

    //Given adapter becomes the list that is displayed in listContainer
    private void setAdapterOnFocus(RecyclerView.Adapter<ExperienceListAdapter.ViewHolder> newAdapter){
        listContainer.setAdapter(newAdapter);
        newAdapter.notifyDataSetChanged();
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting devices location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                Task task = location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            mainListAdapter.setLocation(currentLocation);
                            mainListAdapter.notifyDataSetChanged();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else{
                            Log.d(TAG, "onComplete: current location is null");
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latlng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }
}
