package group3.creator;

import org.json.JSONObject;

public class BlindspotFilter {

    private JSONObject data;

    public BlindspotFilter(JSONObject data){
        this.data = data;
        //filterBlindSpot();
        newFilterBlindSpot();
    }

    public void newFilterBlindSpot (){
        //Checking for origin
        int numStops = PipeManager.traffic.nearbyStops(data.get("latitude").toString(),data.get("longitude")
            .toString());

        if (numStops>1){
            data = null;
        }else {
            JSONObject temp = new JSONObject();
            temp.put("location",data);
            data = temp;
        }
    }


    public JSONObject getData() {
        return data;
    }

}
