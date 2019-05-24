package wanderDots.Server.Get;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import wanderDots.Experience;
import wanderDots.Observer;
import wanderDots.Server.MyRequestQueue;

/* Returns All the Dots contained in the database
 * This methods expects the user to implements "Listener" methods throw the Volley.Response.Listener
 * This class implements a version of Listener, one for strings, but users will need one for ArrayList<Dot> (using generics).
 */
public class Get<T extends Experience> implements ErrorListener, Listener<String> {

    private Observer observer ;
    private String url ;
    private ArrayList<T> data ;
    private String error ;
    private JSONObject response ;
    private String getDot = "http://10.0.2.2:5000/api/get/dots" ;
    private String getAdventures = "http://10.0.2.2:5000/api/get/adventures";

    private MyRequestQueue queue ;

    public Get(Context context, Observer observer, boolean isDot){
        this.queue = MyRequestQueue.getInstance(context);
        this.observer = observer ;
        this.url = isDot ? getDot : getAdventures ;
        this.data = null ;
        this.error = null ;
    }

    public void loadData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.url, this, this) ;
        queue.addToRequestQueue(stringRequest);
    }

    public void onResponse(String response) {
        try {
            this.response = new JSONObject(response);
            observer.subscriberHasChanged("update");
        }catch(JSONException e){
            Log.d("arodr:Get","JSON Error" + e.toString()) ;
            this.observer.subscriberHasChanged("error");
        }
    }

    public void onErrorResponse(VolleyError error) {
        Log.d("arodr:Get", "an error has occurred creating a request") ;
        this.error = error.toString() ;
        this.observer.subscriberHasChanged(this.url);
    }

    public boolean hasError(){
        return this.error != null ;
    }

    public String getError(){
        return this.error ;
    }

    public JSONObject getResponse(){
        return this.response ;
    }
}