package com.example.wanderdots.find.state;

import android.content.Context;
import android.util.Log;

import wanderdots.Adventure;

/*
 * Attaches itself as an "observer" of Adventure.
 * When something has changed in Adventure, specifically that it has loaded
 * the adventures, then this class goes in there and retrieves them.
 */

public class AdventureState extends State<Adventure> {

    public AdventureState(Context context){
        super(context) ;
        Adventure.addObserver(this);
    }

    public void subscriberHasChanged(String message){
        String tag = "arodr";
        Log.d(tag, "adventure data has changed" + message) ;
        if(Adventure.hasError()){
            Log.d(tag, "error occurred while loading adventures") ;
            Log.d(tag, "error: " + Adventure.getError()) ;
        }else {
            setData(Adventure.getData()) ;
        }
    }
}
