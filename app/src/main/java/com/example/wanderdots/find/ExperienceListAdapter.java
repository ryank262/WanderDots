package com.example.wanderdots.find;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.example.wanderdots.R;

import java.util.ArrayList;
import java.util.List;

import wanderdots.Experience;

public class ExperienceListAdapter<T extends Experience> extends RecyclerView.Adapter<ListItem> {

    private static float metersToMiles = 0.000621371f;

    private List<T> dotList;
    private Context ctx;
    private Location currentPosition;

    public ExperienceListAdapter(ArrayList<T> dotList, Context ctx){
        this.dotList = dotList;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ListItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dot_list_item, viewGroup, false);
        return new ListItem(view, ctx);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItem viewHolder, int i) {
        //Displays the i'th element in the List
        viewHolder.title.setText(dotList.get(i).getName());
        if(currentPosition == null)
            viewHolder.distance.setText("0 mi");
        else
            viewHolder.distance.setText(calculateDistance(dotList.get(i), currentPosition));

        viewHolder.rating.setText("5 Stars"); //TODO: Remove hardcoded rating
    }

    @Override
    public int getItemCount() {
        return dotList.size();
    }

    public void setLocation(Location loc){
        currentPosition = loc;
    }

    //Calculates distance between Experience like object and location
    //Returns the String representing the distance followed by the units
    private String calculateDistance(T object, Location loc){
        float []result = new float[1];
        Location.distanceBetween(object.getLatitude(), object.getLongitude(), loc.getLatitude(), loc.getLongitude(), result);
        result[0] = result[0] * metersToMiles; // Convert meters to miles
        String dis = String.format("%.1f", result[0]); // Format String to one decimal
        return dis + " mi";
    }
}