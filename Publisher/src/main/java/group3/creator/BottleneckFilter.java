package group3.creator;

import org.decimal4j.util.DoubleRounder;
import org.json.JSONObject;

import java.math.BigDecimal;

public class BottleneckFilter {

    private JSONObject data;

    public BottleneckFilter(JSONObject data){
        this.data = data;
        filterBottleNeck();
    }

    private void filterBottleNeck(){

        JSONObject nearByStation = PipeManager.traffic.getFirstNearbyStop(data.get("latitude").toString(),
            data.get("longitude").toString());
        if (nearByStation!=null){
            data = checkTransportInArea(nearByStation);
        }else{
            data =null;
        }
    }

    //Checks how many transportation are there in a certain location point ( Origin and destination)
    private JSONObject checkTransportInArea(JSONObject point){
        //bound difference for Live Map
        double diff = 0.009999;
        //predefined value used by vasttrafik api to create a virtual map
        double mapMultiplier = 1000000;

        //calculation of latitudes and longitudes multiplied by the mapMultiplier which is supported by the Api
        double lat =Double.parseDouble(point.get("lat").toString());
        double lon = Double.parseDouble(point.get("lon").toString());
        BigDecimal lonMin = new BigDecimal(DoubleRounder.round(lon-diff,6) * mapMultiplier);
        BigDecimal lonMax = new BigDecimal(DoubleRounder.round(lon+diff,6) * mapMultiplier);
        BigDecimal latMin = new BigDecimal (DoubleRounder.round(lat-diff,6)* mapMultiplier);
        BigDecimal latMax = new BigDecimal (DoubleRounder.round(lat+diff,6)* mapMultiplier);


        //call to Api by traffic class which returns how many transportation are found in the Area
        int transtportCounter = PipeManager.traffic.countTransportationInArea(lonMin+"",lonMax+"",
            latMin+"",latMax+"");
        JSONObject temp = new JSONObject();
        JSONObject location = new JSONObject();
        location.put("latitude",point.get("lat").toString());
        location.put("longitude",point.get("lon").toString());
        //creating a new field regarding bottleneck depending on the fact that if there are more
        // transportation than the limit in the zone

        temp.put("location",location);
        temp.put("nVehicles",transtportCounter);

        //return the object with a new field
        return temp;
    }

    public JSONObject getData() {
        return data;
    }
}
