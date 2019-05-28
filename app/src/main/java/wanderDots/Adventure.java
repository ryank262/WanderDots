package wanderDots;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Adventure extends Experience {

    private static ArrayList<Observer> observers = new ArrayList<>() ;
    private static ArrayList<Adventure> data = null ;
    private static AdventureLoader loader = new AdventureLoader() ;

    private ArrayList<String> dotsVisited ;
    private String[] requiredFields = {"dotsVisited"} ;
    private final String dotsVString = "dotsVisited";

    public Adventure(JSONObject adventure) throws org.json.JSONException{
        super(adventure) ;
        if(!containsRequiredFields(adventure, requiredFields))
            throw new RuntimeException("Adventure Validation Error: Missing Field: " + getMissingField(adventure, requiredFields)) ;
        instantiateFromJSON(adventure);
    }

    @Override
    public void instantiateFromJSON(JSONObject adventure) throws org.json.JSONException {
        super.instantiateFromJSON(adventure);
        JSONArray dotsVisitedArray = adventure.getJSONArray(dotsVString) ;
        this.dotsVisited = createStringList(adventure.getJSONArray(dotsVString)) ;
    }

    public String toString(){
        return this.toJSON().toString() ;
    }

    public JSONObject toJSON(){
        try {
            JSONObject data = super.toJSON() ;
            data.put(dotsVString, (Object) dotsVisited) ;
            return data ;
        } catch(JSONException e){
            Log.d("arodr:Adventure:toJSON", e.toString()) ;
            return null ;
        }
    }

    public HashMap<String, String> toHashMap(){
        HashMap<String, String> adventure = super.toHashMap() ;
        adventure.put(dotsVString, jsonifyArray(dotsVisited)) ;
        return adventure ;
    }

    public static void addObserver(Observer observer){
        observers.add(observer) ;
    }

    public static ArrayList<Adventure> getData(){
        return data ;
    }

    public static void dataFinishedLoading(){
        if(loader.hasError())
            setError(loader.getError()) ;
        else
            data = loader.getData() ;
        notifyObservers();
    }

    public static void notifyObservers(){
        for(Observer observer : observers)
            observer.subscriberHasChanged("update");
    }

    public static void reload(){
        loader.reload();
    }
}
