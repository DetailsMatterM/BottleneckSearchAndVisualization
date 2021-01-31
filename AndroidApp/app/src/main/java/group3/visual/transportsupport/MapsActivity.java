package group3.visual.transportsupport;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; //for map view
    private MqttAndroidClient client;   //client for Mqtt Android
    private static ExecutorService threadPool = Executors.newFixedThreadPool(15);
    private TileOverlay mOverlay;   //for supply Load heat map
    private TileOverlay mOverlay2;  //for request Load heat map
    private LatLngBounds zoomExtent;    //for updating map camera zooming extent

    private final int cameraHeight = 12;    //default map camera height
    private CameraPosition currentCamHeight;    //updated map camera height

    private final double centerLat = 57.711482; //central goteborg's latitude
    private final double centerLon = 11.972338; //central goteborg's longitude
    private final LatLng goteborg = new LatLng(centerLat, centerLon); //central goteborg's location

    //for zoom extent coordinates (initially at the city center)
    private double lat1 = centerLat;
    private double lon1 = centerLon;
    private double lat2 = centerLat;
    private double lon2 = centerLon;

    // private String brokerPath = "tcp://farmer.cloudmqtt.com:12123";
    private String brokerPath = "tcp://group3distributed.ddns.net:3189";
    private final String B_SPOT_TOPIC = "blindspot";   //Subscription Topic
    private final String SUPPLY_LOAD_TOPIC = "bottleneck";    //Subscription Topic
    private final String REQUEST_LOAD_TOPIC = "requestLoad";    //Subscription Topic
    private String CUSTOM_TOPIC = "";    //Subscription Topic

    // set as global String since message arrival happens frequently
    private final String FORMAT_MSG = "Unsupported Message Format";

    private boolean updateNeeded = false;   //if there is new point plotted
    private boolean connected = false;  //if connected to Mqtt broker
    private boolean customTSubscribed = false;  //if connected to Mqtt broker
    private boolean autoMode = true;    //if viewing is auto depending on incoming data
    private final int ZERO = 0; //to represent 0
    private int msgCounter = 0;

    private ToggleButton autoViewButton;  //button to make auto mode on and off
    private ToggleButton blindSpotButton;  //button to affect blind Spot topic choice
    private ToggleButton supplyLoadButton;  //button to affect supply Load topic choice
    private ToggleButton requestLoadButton;  //button to affect request Load topic choice
    private ToggleButton customTopicButton;  //button to affect custom topic choice

    private AlertDialog dialog; //for setting popup
    private AlertDialog topicDialog; //for setting popup
    private AlertDialog brokerDialog; //for setting popup
    private TextInputEditText topicInput;   //for custom topic input
    private TextInputEditText brokerPathInput;  //for broker path input
    private RadioButton supplyLoadRBtn; //for supply Load View
    private RadioButton requestLoadRBtn; //for request load view
    private RadioButton heatMapButton; //for heat map view selection
    private FloatingActionButton fab;   //for setting button

    private ArrayList<LatLng> blindSpots;   //store blindSpots
    private ArrayList<LatLng> bottlenecks;  //store supplyLoads
    private ArrayList<LatLng> requestLoads; //store requestLoads
    private ArrayList<WeightedLatLng> wRequestLoads;    //weighted requestLoads
    private ArrayList<WeightedLatLng> wBottlenecks;     //weighted supplyLoads


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        initiateButtonsAndLists();
        initializeMqtt();   //connect to MQTT broker
        initiateSettingDialog();   //setting popup instantiation
        initiateCustomTopicDialog();   //setting topic instantiation
        initiateBrokerDialog();  //setting broker path instantiation

        fab = findViewById(R.id.fab);  //floating button for settings view
        fab.getBackground().setColorFilter(Color.parseColor("#708090"),
            PorterDuff.Mode.DARKEN);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings(); //to display the settings dialog
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!connected)
            connectClient();
    }

    // instantiate buttons on main view and global Lists
    private void initiateButtonsAndLists() {
        blindSpots = new ArrayList<>();
        bottlenecks = new ArrayList<>();
        wBottlenecks = new ArrayList<>();
        requestLoads = new ArrayList<>();
        wRequestLoads = new ArrayList<>();
        supplyLoadRBtn = (RadioButton) findViewById(R.id.supplyLoad);
        requestLoadRBtn = (RadioButton) findViewById(R.id.requestLoad);
    }


    //instantiate setting view add contents and create dialog
    private void initiateSettingDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setCancelable(true);
        View settingsView = getLayoutInflater().inflate(R.layout.activity_settings, null);

        heatMapButton = (RadioButton) settingsView.findViewById(R.id.heatMapButton);
        RadioButton markerButton = (RadioButton) settingsView.findViewById(R.id.markerButton);
        autoViewButton = (ToggleButton) settingsView.findViewById(R.id.toggleButton);
        blindSpotButton = (ToggleButton) settingsView.findViewById(R.id.blindSpotsBtn);
        supplyLoadButton = (ToggleButton) settingsView.findViewById(R.id.supplyLoadBtn);
        requestLoadButton = (ToggleButton) settingsView.findViewById(R.id.requestLoadBtn);
        customTopicButton = (ToggleButton) settingsView.findViewById(R.id.customTopicBtn);

        mBuilder.setView(settingsView);
        dialog = mBuilder.create();
    }

    //instantiate setting view add contents and create dialog
    private void initiateCustomTopicDialog() {
        AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(this);
        mBuilder2.setCancelable(true);
        View customTView = getLayoutInflater().inflate(R.layout.activity_cutom_topic, null);

        topicInput = (TextInputEditText) customTView.findViewById(R.id.topicInput);
        topicInput.setText(CUSTOM_TOPIC);

        mBuilder2.setView(customTView);
        topicDialog = mBuilder2.create();
    }

    //instantiate setting view add contents and create dialog
    private void initiateBrokerDialog() {
        AlertDialog.Builder mBuilder3 = new AlertDialog.Builder(this);
        mBuilder3.setCancelable(true);
        View brokerView = getLayoutInflater().inflate(R.layout.activity_broker_path, null);

        brokerPathInput = (TextInputEditText) brokerView.findViewById(R.id.brokerInput);
        brokerPathInput.setText(brokerPath);

        mBuilder3.setView(brokerView);
        brokerDialog = mBuilder3.create();
    }

    // start viewing the setting dialog
    private void openSettings() {
        dialog.show();
    }


    // onclick listener for the toggleButton
    public void toggleAutoView(View v) {

        if (autoViewButton.isChecked()) { //if On
            autoMode = true;
            automateZoom(); //start animation
        } else {
            autoMode = false;
        }
        closeSettDialog();
    }

    // onclick listener for the Blind Sport topic toggleButton
    public void selectBlindSpot(View v) {
        String status = blindSpotButton.getText().toString();
        if (connected) {
            if (blindSpotButton.isChecked()) { //if On
                setSubscription(B_SPOT_TOPIC);
            } else {
                unSubscribe(B_SPOT_TOPIC);
            }
        } else {
            toastShort("No Broker Connection");
            if (status.equals("ON"))
                blindSpotButton.setChecked(false);
            else
                blindSpotButton.setChecked(true);
        }
        refreshMap();
    }

    // onclick listener for the Supply Load topic toggleButton
    public void selectSupplyLoad(View v) {
        String status = supplyLoadButton.getText().toString();
        if (connected) {
            executeSupplyLoad();
        } else {
            toastShort("No Broker Connection");
            if (status.equals("ON"))
                supplyLoadButton.setChecked(false);
            else
                supplyLoadButton.setChecked(true);
        }
        refreshMap();
    }

    // subscription to supply load
    private void executeSupplyLoad() {
        if (supplyLoadButton.isChecked()) { //if On
            setSubscription(SUPPLY_LOAD_TOPIC);
        } else {
            unSubscribe(SUPPLY_LOAD_TOPIC);
        }
    }

    // clear blind spots only
    public void clearSpots(View v) {
        closeSettDialog();
        blindSpots.clear();
        refreshMap();
    }

    // onclick listener for the Request Load topic toggleButton
    public void selectRequestLoad(View v) {
        String status = requestLoadButton.getText().toString();
        if (connected) {
            executeRequestLoad();
        } else {
            toastShort("No Broker Connection");
            if (status.equals("ON"))
                requestLoadButton.setChecked(false);
            else
                requestLoadButton.setChecked(true);
        }
        refreshMap();
    }

    // subscription for request load
    private void executeRequestLoad() {
        if (requestLoadButton.isChecked()) { //if On
            setSubscription(REQUEST_LOAD_TOPIC);
        } else {
            unSubscribe(REQUEST_LOAD_TOPIC);
        }
    }

    // clear supply Loads only
    public void clearBtneck1(View v) {
        closeSettDialog();
        bottlenecks.clear();
        wBottlenecks.clear();
        refreshMap();
    }

    // clear supply Loads only
    public void clearBtneck2(View v) {
        closeSettDialog();
        requestLoads.clear();
        wRequestLoads.clear();
        refreshMap();
    }

    // clear custom topic only
    public void clearCustom(View v) {
        closeSettDialog();
        refreshMap();
    }

    // onclick listener for the Request Load topic toggleButton
    public void selectCustomTopic(View v) {
        String status = customTopicButton.getText().toString();
        if (connected) {
            if (customTopicButton.isChecked()) { //if On
                openCustomTopicView();
            } else {
                if (customTSubscribed) {
                    unSubscribe(CUSTOM_TOPIC);
                } else {
                    toastShort("Already not subscribed");
                }
                customTopicButton.setChecked(false);
                topicInput.setText("");
                CUSTOM_TOPIC = "";
                customTSubscribed = false;
            }
        } else {
            toastShort("No Broker Connection");
            if (status.equals("ON"))
                customTopicButton.setChecked(false);
            else
                customTopicButton.setChecked(true);
        }
        refreshMap();
    }

    // to check the custom topic input
    private void openCustomTopicView() {
        topicDialog.show();
    }

    //for broker path input
    public void openBrokerEdit(View v) {
        dialog.cancel();
        brokerDialog.show();
    }

    // to check the custom topic input when make ON
    public void saveTopic(View v) {
        String newString = topicInput.getEditableText().toString();
        if (!checkTopicFormat(newString)) { //if format is not acceptable
            topicInput.setError("Wrong Format");
            return;
        } else {
            topicInput.setError(null);
        }

        if (!newString.equals(CUSTOM_TOPIC)) {   //if new text
            if (!newString.equals("")) {  // if not empty
                if (customTSubscribed)
                    unSubscribe(CUSTOM_TOPIC);
                setSubscription(newString);
                customTSubscribed = true;
            } else if (customTSubscribed) {
                unSubscribe(CUSTOM_TOPIC);
                customTSubscribed = false;
            }
            CUSTOM_TOPIC = newString;
        } else {    //same text
            if (!CUSTOM_TOPIC.equals("")) {  //not empty
                setSubscription(CUSTOM_TOPIC);
                customTSubscribed = true;
            } else {
                customTopicButton.setChecked(false);
                customTSubscribed = false;
                toastShort("Input not updated");
            }
        }

        closeTopicDialog();
    }

    // save broker path and connect
    public void saveBrokerPath(View v) {
        String newTxt = brokerPathInput.getEditableText().toString();
        if (!checkPathFormat(newTxt)) { //if format is not acceptable
            brokerPathInput.setError("Wrong Format");
            return;
        } else {
            brokerPathInput.setError(null);
        }

        if (newTxt.equals(brokerPath)) {  //default
            if (!connected)
                initializeMqtt();
            else
                toastShort("Same Status");
        } else if (newTxt.equals("")) {
            if (connected) {
                disconnectClient();
                toastShort("Disconnected");
                connected = false;
            }
        } else {
            brokerPath = newTxt;
            initializeMqtt();
        }
        closeBrokerDialog();
    }

    // connect to broker
    public void connectToBroker(View v) {
        if (connected) {
            toastShort("Already Connected");
        } else
            connectClient();
        closeSettDialog();
    }

    // Connect and subscribe to the default topics
    private void connectClient() {
        // details for connecting to broker (IF NEEDED FOR CLOUD)
        String user = "losjdsrs";
        String password = "kzLYVkK7OAUF";

        String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(this.getApplicationContext(), brokerPath, clientId);

        MqttConnectOptions options;
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(user);
        options.setPassword(password.toCharArray());
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    toastLong("Connected");
                    fab.getBackground().setColorFilter(Color.parseColor("#D81830"),
                        PorterDuff.Mode.DARKEN);
                    connected = true;

                    //default subscriptions
                    if (blindSpotButton.isChecked())
                        setSubscription(B_SPOT_TOPIC);
                    if (supplyLoadButton.isChecked())
                        setSubscription(SUPPLY_LOAD_TOPIC);
                    if (requestLoadButton.isChecked())
                        setSubscription(REQUEST_LOAD_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    connected = false;
                    toastShort("Not Connected");
                    fab.getBackground().setColorFilter(Color.parseColor("#708090"),
                        PorterDuff.Mode.DARKEN);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                toastShort("Disconnected");
                fab.getBackground().setColorFilter(Color.parseColor("#708090"),
                    PorterDuff.Mode.DARKEN);
                connected = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                if (++msgCounter == 75) {
                    autoMode = false;
                    autoViewButton.setChecked(false);
                }

                String msg = message + "";
                continuePlotting(topic, msg);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //toastShort("Delivered");
            }
        });
    }

    // disconnect from broker
    public void disconnectFromBroker(View v) {
        disconnectClient();
    }

    //disconnect client from MQTT broker
    private void disconnectClient() {
        try {
            if (connected) {
                client.disconnect();
                connected = false;
                fab.getBackground().setColorFilter(Color.parseColor("#708090"),
                    PorterDuff.Mode.DARKEN);
                toastShort("Disconnected");
                closeSettDialog();
            } else {
                toastShort("Already not Connected");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // to cancel the view of a topic dialog
    public void dismissTopicView(View v) {
        topicDialog.cancel();
    }

    // cancel yopic dialog view
    private void closeTopicDialog() {
        topicDialog.cancel();
    }

    // to cancel the view of a dialog
    public void dismissView(View v) {
        dialog.cancel();
    }

    // close setting dialog view
    private void closeSettDialog() {
        dialog.cancel();
    }

    // to cancel the view of a broker path dialog
    public void dismissBrokerView(View v) {
        brokerDialog.cancel();
    }

    // cancel broker dialog view
    private void closeBrokerDialog() {
        brokerDialog.cancel();
    }

    // when radio buttons on the main view are selected
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        refreshBottlenecks();
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.supplyLoad:
                if (checked)
                    System.out.println("View changed to Supply Load");
                break;
            case R.id.requestLoad:
                if (checked)
                    System.out.println("View changed to Request load");
                break;
        }
    }

    // connecting and subscribing to a broker
    private void initializeMqtt() {
        toastShort("connecting...");

        threadPool.execute(new Runnable() {
            public void run() {
                connectClient();
            }
        });
    }

    // after receiving data from broker
    private void continuePlotting(String topic, String msg) {

        Gson gson = new GsonBuilder().create();

        switch (topic) {
            case B_SPOT_TOPIC:
                BlindSpot foundSpot = gson.fromJson(msg, BlindSpot.class);

                if (foundSpot.getLocation() != null) {
                    blindSpots.add(foundSpot.getLocation());
                    plotSpotMarker(foundSpot);
                } else {
                    toastLong(FORMAT_MSG);
                }
                break;
            case SUPPLY_LOAD_TOPIC:
                StationLoad foundLoad = gson.fromJson(msg, StationLoad.class);

                if (foundLoad.getLocation() != null) {
                    bottlenecks.add(foundLoad.getLocation());
                    plotStationLoad(foundLoad);
                } else {
                    toastLong(FORMAT_MSG);
                }
                break;
            case REQUEST_LOAD_TOPIC:
                //String date = "";
                //String vehicleName = "";
                JSONObject location = null;
                LatLng loc = null;
                //String direction = "";
                try {
                    JSONObject data = new JSONObject(msg);
                    //date = data.getString("dateTime");
                    //vehicleName = data.getString("vehicleName");
                    location = data.getJSONObject("location");
                    loc = new LatLng(location.getDouble("latitude"),
                        location.getDouble("longitude"));

                    requestLoads.add(loc);
                    WeightedLatLng weightedLoc = new WeightedLatLng(loc);
                    wRequestLoads.add(weightedLoc);

                    //direction = data.getString("direction");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //if (!date.equals("") && !vehicleName.equals("") && location != null &&
                // !direction.equals("")) {
                if (location != null) {
                    //String title = "@" + date + " " + vehicleName + " , to " + direction;
                    String title = "";
                    plotServiceLoad(loc, title);

                } else {
                    toastLong(FORMAT_MSG);
                }
                break;
        }


        if (topic.equals(CUSTOM_TOPIC)) {  //custom topic
            JSONObject location = null;
            LatLng loc = null;

            try {
                JSONObject data = new JSONObject(msg);

                location = data.getJSONObject("location");
                loc = new LatLng(location.getDouble("latitude"),
                    location.getDouble("longitude"));

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (location != null) {
                String title = "";
                plotCustomTopic(loc, title);
            } else {
                toastLong(FORMAT_MSG);
            }
        }
    }


    /**
     * subscribe for topic
     */
    private void setSubscription(String topic) {
        try {
            client.subscribe(topic, ZERO);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * un-subscribe from topic
     */
    private void unSubscribe(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Göteborg, Sweden.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //mMap.addMarker(new MarkerOptions().position(goteborg).title("Central Göteborg"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(goteborg, cameraHeight));

        initiateHeatMap();
        initiateRequestHeatMap();
        zoomExtent = new LatLngBounds(goteborg, goteborg);
    }


    /**
     * Check for the need of updating camera orientation
     */
    private void checkZoom(double pLat, double pLon) {
        final double buffer = 0.03;
        if (pLat < lat1) {
            lat1 = pLat - buffer;
            updateNeeded = true;
        }

        if (pLon < lon1) {
            lon1 = pLon - buffer;
            updateNeeded = true;
        }

        if (pLat > lat2) {
            lat2 = pLat + buffer;
            updateNeeded = true;
        }

        if (pLon > lon2) {
            lon2 = pLon + buffer;
            updateNeeded = true;
        }
    }


    // switch between choices of bottleneck displaying
    public void changeBNeckView(View v) {
        if (connected) {
            refreshBottlenecks();
            closeSettDialog();
        } else {
            toastShort("No Broker Connection");
        }
    }

    // used when switching with Supply and Request Loads
    private void refreshBottlenecks() {
        if (heatMapButton.isChecked()) {
            mMap.clear();
            if (supplyLoadRBtn.isChecked()) {
                plotSupplyHeatMap();
            } else {
                plotRequestHeat();
            }
        } else {
            mMap.clear();
            if (supplyLoadRBtn.isChecked()) {
                plotSupplyMarker();
            } else {
                plotRequestMarker();
            }

        }
    }


    // change zoom extent as needed in case of auto view mode
    private void automateZoom() {
        if (currentCamHeight != mMap.getCameraPosition()) { //if there is a manual change of view
            updateNeeded = true;
        }

        if (updateNeeded && autoMode) {
            if (lat1 == lat2) { // if the change view is done before any new point is plotted
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(goteborg, cameraHeight));
                return;
            }

            zoomExtent = new LatLngBounds(new LatLng(lat1, lon1), new LatLng(lat2, lon2));  //define zoom extent
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(zoomExtent, ZERO));  //animate
            currentCamHeight = mMap.getCameraPosition();    //register camera height as recent
            updateNeeded = false;   //until there is change
        }
    }


    // plot blind spot
    private void plotSpotMarker(BlindSpot input) {
        LatLng loc = input.getLocation();
        String title = "";
        float color = BitmapDescriptorFactory.HUE_YELLOW;

        plotMarker(loc, title, color);
        checkZoom(loc.latitude, loc.longitude);
        automateZoom();
    }


    // plot supply bottlenecks
    private void plotStationLoad(StationLoad message) {
        LatLng loc = message.getLocation();
        WeightedLatLng weightedLoc = new WeightedLatLng(loc);
        wBottlenecks.add(weightedLoc);

        if (supplyLoadRBtn.isChecked()) {
            if (heatMapButton.isChecked()) {
                plotSupplyHeatMap();
            } else {
                String title = message.getNVehicles() + "";
                float color = BitmapDescriptorFactory.HUE_GREEN;
                plotMarker(loc, title, color);
            }

            checkZoom(loc.latitude, loc.longitude);
            automateZoom();
        }
    }

    //plot request bottlenecks
    private void plotServiceLoad(LatLng loc, String title) {

        if (requestLoadRBtn.isChecked()) {
            if (heatMapButton.isChecked()) {
                plotRequestHeat();
            } else {
                float color = BitmapDescriptorFactory.HUE_VIOLET;

                plotMarker(loc, title, color);
            }

            checkZoom(loc.latitude, loc.longitude);
            automateZoom();
        }
    }

    // plot custom topic
    private void plotCustomTopic(LatLng loc, String title) {  //when there no near station

        float color = BitmapDescriptorFactory.HUE_BLUE;

        plotMarker(loc, title, color);
        checkZoom(loc.latitude, loc.longitude);
        automateZoom();
    }

    // marker plotter
    private void plotMarker(LatLng loc, String title, float color) {
        mMap.addMarker(new MarkerOptions()
            .position(loc)
            .title(title)
            .icon(BitmapDescriptorFactory.defaultMarker(color)));
    }


    // prepare Supply heat map details
    private void initiateHeatMap() {
        HeatmapTileProvider mProvider;
        // Create the gradient.
        int[] colors = {
            Color.GREEN,    // green(0-50)
            Color.YELLOW,    // yellow(51-100)
            Color.rgb(255, 165, 0), //Orange(101-150)
            Color.RED,              //red(151-200)
            Color.rgb(153, 50, 204), //dark orchid(201-300)
            Color.rgb(165, 42, 42) //brown(301-500)
        };

        float[] startPoints = {
            0.1F, 0.2F, 0.3F, 0.4F, 0.6F, 1.0F
        };

        Gradient gradient = new Gradient(colors, startPoints);

        if (wBottlenecks.size() > ZERO) {
            // Create a heat map tile provider
            mProvider = new HeatmapTileProvider.Builder()
                .weightedData(wBottlenecks)
                .gradient(gradient)
                .build();
            // Add a tile overlay to the map
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }


    // prepare Request heat map details
    private void initiateRequestHeatMap() {
        HeatmapTileProvider mProvider2;
        // Create the gradient.
        int[] colors = {
            Color.GREEN,    // green(0-50)
            Color.YELLOW,    // yellow(51-100)
            Color.rgb(255, 165, 0), //Orange(101-150)
            Color.RED,              //red(151-200)
            Color.rgb(153, 50, 204), //dark orchid(201-300)
            Color.rgb(165, 42, 42) //brown(301-500)
        };

        float[] startPoints = {
            0.1F, 0.2F, 0.3F, 0.4F, 0.6F, 1.0F
        };

        Gradient gradient = new Gradient(colors, startPoints);

        if (wRequestLoads.size() > ZERO) {
            // Create a heat map tile provider
            mProvider2 = new HeatmapTileProvider.Builder()
                .weightedData(wRequestLoads)
                .gradient(gradient)
                .build();
            // Add a tile overlay to the map
            mOverlay2 = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider2));
        }
    }


    // plot bottlenecks with heat map of many points
    private void plotSupplyHeatMap() {
        if (supplyLoadButton.isChecked()) {
            if (bottlenecks.isEmpty()) {
                toastShort("No Supply Load to display");
            } else {
                initiateHeatMap();
                mOverlay.clearTileCache();

                for (LatLng loc : bottlenecks) {
                    checkZoom(loc.latitude, loc.longitude);
                }
                automateZoom();
            }
        }
        refreshBlindSpots();
    }


    // plot bottlenecks with marker
    private void plotSupplyMarker() {
        if (supplyLoadButton.isChecked()) {
            if (bottlenecks.isEmpty()) {
                toastShort("No Supply Load to display");
            } else {
                for (LatLng loc : bottlenecks) {
                    mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    checkZoom(loc.latitude, loc.longitude);
                }
                automateZoom();
            }
        }
        refreshBlindSpots();
    }

    // plot bottlenecks with marker
    private void plotRequestMarker() {
        if (requestLoadButton.isChecked()) {
            if (requestLoads.isEmpty()) {
                toastShort("No Request Load to display");
            } else {
                for (LatLng loc : requestLoads) {
                    mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    checkZoom(loc.latitude, loc.longitude);
                }
                automateZoom();
            }
        }
        refreshBlindSpots();
    }

    // refresh map view
    private void refreshMap() {
        mMap.clear();
        refreshBlindSpots();
        refreshBottlenecks();
    }

    // used when blindSpot choice is on and off
    private void refreshBlindSpots() {
        if (!blindSpots.isEmpty() && blindSpotButton.isChecked()) {
            for (LatLng loc : blindSpots) {
                mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                checkZoom(loc.latitude, loc.longitude);
            }
            automateZoom();
        }
    }

    // plot bottlenecks with heat map of many points
    private void plotRequestHeat() {
        if (requestLoadButton.isChecked()) {
            if (requestLoads.isEmpty()) {
                toastShort("No Request Load to display");
            } else {
                initiateRequestHeatMap();
                mOverlay2.clearTileCache();

                for (LatLng loc : requestLoads) {
                    checkZoom(loc.latitude, loc.longitude);
                }
                automateZoom();
            }
        }
        refreshBlindSpots();
    }

    // a toast message for short span of time
    private void toastShort(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // a toast message for long span of time
    private void toastLong(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * checks if topic input is acceptable format
     */
    private boolean checkTopicFormat(String text) {
        text = replaceBasic(text);
        int fina = text.length();
        return (fina == ZERO);
    }

    // replacing basic characters with empty
    private String replaceBasic(String text) {
        text = text.replaceAll("[a-z]", "");    //"[^a-z]"
        text = text.replaceAll("[A-Z]", "");
        text = text.replaceAll("[0-9]", "");
        text = text.replaceAll("[/]", "");
        return text;
    }

    /**
     * checks if broker path input is acceptable format
     */
    private boolean checkPathFormat(String text) {
        text = replaceBasic(text);
        text = text.replaceAll("[:]", "");
        text = text.replaceAll("[.]", "");
        int fina = text.length();
        return (fina == ZERO);
    }

    // reset map and data
    public void clearSavedData(View v) {
        mMap.clear();
        initiateButtonsAndLists();
        toastShort("Success");
        closeSettDialog();
    }
}