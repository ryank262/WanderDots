package com.example.wanderdots;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Intent;
import java.io.InputStream;

import wanderdots.Dot;
import wanderdots.server.post.DotPoster;
import wanderdots.Observer;

public class NewDotActivity extends AppCompatActivity
        implements View.OnClickListener, Observer {

    private ImageView imageView4;
    private DotPoster dotPoster;
    private static int result = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dot);
        Button createButton = findViewById(R.id.CreateButton) ;
        ImageButton imageButton = findViewById(R.id.imageButton);
        this.imageView4 = findViewById(R.id.imageView4);

        View.OnClickListener addImageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImages();
            }
        };
        imageButton.setOnClickListener(addImageListener);
        createButton.setOnClickListener(this);
        this.dotPoster = new DotPoster(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    private void addImages(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, result);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == result && resultCode == RESULT_OK && data != null) {
            try {
                final Uri imageUri = data.getData();
                if(imageUri != null) {
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageView4.setImageBitmap(selectedImage);
                    if (imageStream != null) {
                        imageStream.close();
                    }
                }
            } catch (Exception e) {
                Log.e("NewDotActivity", e.toString());
            }
        }
    }

    public void subscriberHasChanged(String message){
        if(this.dotPoster.getError() == null){
            finish() ; //should return back to home screen
        }else {
            Log.d("POST Dot Error:", this.dotPoster.getError()) ;
        }
    }

    //Runs when form is being submitted
    public void onClick(View v){

        Log.d("wanderdots", "About to create a Dot...") ;
        //create
        Dot dot = new Dot();
        final EditText name = findViewById(R.id.NameTextbox);
        dot.setName(name.getText().toString());
        dot.setCreator("Username");
        final EditText description = findViewById(R.id.DescriptionTextbox);
        dot.setDescription(description.getText().toString());
        //how to Get data from multi-select (shaheen)
        dot.addCategory("Filler");
        //what is the picture id? bitmap? file path? (alberto)
        //multiple image functionality (abby)
        dot.addPictureId("testPictureID");
        //how to make a map marker and Get lat/long from that - for now stub using current loc (abby)
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            dot.setLongitude(location.getLongitude());
            dot.setLatitude(location.getLatitude());
        }
        catch(SecurityException e){
            Log.e("NewDotActivity", e.toString());
        }

        this.dotPoster.postDot(dot) ;
    }
}
