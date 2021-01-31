package group3.creator;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PipeManager implements MqttCallback, ActionListener {
    // threadPools
    private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
    private static ExecutorService threadPool1 = Executors.newFixedThreadPool(80);
    public static TrafficInfo traffic;
    public static String token;
    public static int counter = 0;
    public static Creator emitter;
    private static String brokerAddress = "tcp://group3distributed.ddns.net:3189";
    // private static String brokerAddress = "tcp://localhost:1883";
    private static String userId = UUID.randomUUID().toString();
    private static String topic = "external";

    private HashMap<String, Long> passengersIntents;
    private HashMap<String, Long> supplyLimits;
    private HashMap<String, Long> blindLocations;
    private Pipe pipe;
    private int requestCounter = 0;
    private IMqttClient middleware;
    private MemoryPersistence persistence;

    private JTextField brokerIPSub, portSub, topicSub, userIDSub;
    private JTextField brokerIPPub, portPub, userIDPub;
    private JButton startS, stopS, startP, stopP;

    public PipeManager() throws MqttException {
        traffic = new TrafficInfo();
        token = Oauth2Traffic.getClientCredentials();
        passengersIntents = new HashMap<>();
        supplyLimits = new HashMap<>();
        blindLocations = new HashMap<>();

        JFrame frame = new JFrame("Backend");
        frame.setBounds(50, 250, 600, 250);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    if (middleware.isConnected()) {
                        middleware.disconnect();
                        middleware.close();
                    }
                } catch (MqttException ex) {
                    ex.printStackTrace();
                }
            }
        });

        brokerIPSub = new JTextField("group3distributed.ddns.net");
        portSub = new JTextField("3189");
        topicSub = new JTextField("external");
        userIDSub = new JTextField(userId);
        brokerIPPub = new JTextField("group3distributed.ddns.net");
        portPub = new JTextField("3189");
        userIDPub = new JTextField("creator");

        topicSub.addActionListener(this);

        // panels and layouts
        JPanel northP = new JPanel();
        JPanel centerP = new JPanel();
        JPanel southP = new JPanel();

        northP.setLayout(new GridLayout(1, 3, 3, 3));
        centerP.setLayout(new GridLayout(5, 3, 3, 3));
        southP.setLayout(new GridLayout(2, 3, 3, 3));

        // elements on central Panel
        centerP.add(new JLabel(""));
        centerP.add(new JLabel("Subscriber", JLabel.CENTER));
        centerP.add(new JLabel("Publisher", JLabel.CENTER));
        centerP.add(new JLabel("Host ", JLabel.CENTER));
        centerP.add(brokerIPSub);
        centerP.add(brokerIPPub);
        centerP.add(new JLabel("Port ", JLabel.CENTER));
        centerP.add(portSub);
        centerP.add(portPub);
        centerP.add(new JLabel("User ID ", JLabel.CENTER));
        centerP.add(userIDSub);
        centerP.add(userIDPub);
        centerP.add(new JLabel("Topic ", JLabel.CENTER));
        centerP.add(topicSub);
        centerP.add(new JLabel(""));

        // south panel elements
        startS = new JButton("Start Connection");
        stopS = new JButton("Stop Connection");
        startP = new JButton("Start Connection");
        stopP = new JButton("Stop Connection");
        startS.addActionListener(this);
        stopS.addActionListener(this);
        startP.addActionListener(this);
        stopP.addActionListener(this);

        southP.add(new JLabel(""));
        southP.add(startS);
        southP.add(startP);
        southP.add(new JLabel(""));
        southP.add(stopS);
        southP.add(stopP);

        frame.add(northP, BorderLayout.NORTH);
        frame.add(centerP, BorderLayout.CENTER);
        frame.add(southP, BorderLayout.SOUTH);

        pipe = new Pipe();
        emitter = new Creator();
        persistence = new MemoryPersistence();
        middleware = new MqttClient(brokerAddress, userId, persistence);
        createConnection();
        frame.setVisible(true);
    }

    private void createConnection() throws MqttException {
        middleware.connect();
        middleware.setCallback(this);

        subscribeToMessages();
    }

    private void subscribeToMessages() {
        THREAD_POOL.submit(() -> {
            try {
                middleware.subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost!");

        try {
            middleware.disconnect();
            middleware.close();
        } catch (MqttException e) {
            try {
                createConnection();
            } catch (MqttException ex) {
                ex.printStackTrace();
            }

            subscribeToMessages();
            System.out.println("Re-connected! ");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        requestCounter++;

        if (requestCounter % 100 == 0) System.out.println("Message arrived: " + requestCounter);

        threadPool1.execute(() -> {
            JSONObject origin;
            JSONObject destination;
            long originId;
            long destinationId;
            String trDate;
            String time1;
            String time2;

            JSONObject mData = new JSONObject(String.valueOf(message));
            try{
                 origin = mData.getJSONObject("origin");
                destination = mData.getJSONObject("destination");
                String dateStr = mData.getString("timeOfDeparture");

                int divider = dateStr.indexOf(" ");
                trDate = dateStr.substring(0, divider);
                String trTime = dateStr.substring(divider + 1);
                time1 = trTime.substring(0, 2);
                time2 = trTime.substring(3);

                message.setQos(1);

                originId = traffic.getStationID(origin.get("latitude").toString(),
                    origin.get("longitude").toString());
                destinationId = traffic.getStationID(destination.get("latitude").toString(),
                    destination.get("longitude").toString());
            } catch (Exception e){
                if(e.toString().contains("org.json.JSONException: JSONObject"))
                    System.out.println("Data format not acceptable");
                else
                    e.printStackTrace();
                return;
            }

            JSONObject orgSpot = new JSONObject();
            boolean startSpotRepeated = checkIfRepeated(origin);

            if (!startSpotRepeated) {
                if (originId == 1L) {
                    System.out.println("Blind Origin: " + origin);
                    orgSpot.put("location", origin);
                    pipe.transfer(orgSpot, "pipeManagerToEmitter", "blindspot");
                } else {
                    operationBlindSpot("Origin", origin);
                    operationBottleneck(origin);
                }
            }

            JSONObject desSpot = new JSONObject();
            boolean endSpotRepeated = checkIfRepeated(destination);
            if (!endSpotRepeated) {
                if (destinationId == 1L) {
                    System.out.println("Blind Destination:  " + destination);
                    desSpot.put("location", destination);
                    pipe.transfer(desSpot, "pipeManagerToEmitter", "blindspot");
                } else {
                    operationBlindSpot("Destination", destination);
                    operationBottleneck(destination);
                }
            }

            // for Request Load
            if (originId > 1L && destinationId > 1L) {
                System.out.print("");
                JSONArray tripDetail = traffic.getTripDetail2(originId, destinationId, trDate, time1, time2);
                if (tripDetail != null) {
                    checkRouteDetail(tripDetail);
                }
            }
        });
    }

    private void operationBlindSpot(String location, JSONObject loc) {
        JSONObject obj = pipe.transfer(loc, "pipeManagerToBlindspot", "blindspot");

        if (obj != null) {
            if (location.equals("Origin")) {
                System.out.println("Blind Origin. :  " + obj.toString());
            } else {
                System.out.println("Blind Destination. :  " + obj.toString());
            }

            pipe.transfer(obj, "pipeManagerToEmitter", "blindspot");
        }
    }

    // prepare each station used as start of transport including transits
    private void checkRouteDetail(JSONArray arr) {
        JSONObject trip;

        for (int x = 0; x < arr.length(); x++) {
            String content = arr.get(x).toString();
            trip = new JSONObject(content);

            if (trip.toString().contains("location")) {
                JSONObject loc = trip.getJSONObject("location");
                String dateTime = trip.getString("dateTime");
                String vehicleName = trip.getString("vehicleName");
                String direction = trip.getString("direction");

                String tripText = loc + dateTime + vehicleName + direction;
                // prepare for forwarding for checking
                JSONObject temp = new JSONObject();
                temp.put("location", loc);
                checkBottlenecks(temp, tripText);
            }
        }
    }

    // to check the repetition on request of service
    private void checkBottlenecks(JSONObject loc, String requestKey) {
        Long nReq = 1L;

        if (passengersIntents.get(requestKey) != null) {
            nReq = passengersIntents.get(requestKey);

            if (nReq <= 5) {
                nReq = nReq + 1L;
                System.out.print("");
                passengersIntents.put(requestKey, nReq);

                if (nReq >= 2) {
                    pipe.transfer(loc, "pipeManagerToEmitter", "requestLoad");
                    System.out.println("Request load:  " + loc.toString());
                }
            }
        } else {
            passengersIntents.put(requestKey, nReq);
        }
    }

    private void operationBottleneck(JSONObject loc) {
        JSONObject obj = pipe.transfer(loc, "pipeManagerToBottleneck", "bottleneck");

        if (obj != null) {
            int numbVehicles = Integer.parseInt(obj.get("nVehicles").toString());
            if (numbVehicles > 5 && checkSupplyReport(obj)) {
                System.out.println("Bottleneck station :  " + obj.toString());
                pipe.transfer(obj, "pipeManagerToEmitter", "bottleneck");
            }
        }
    }

    // check if blindSpot was already registered
    private boolean checkIfRepeated(JSONObject loc) {
        String lat;
        String lon;

        try {
            // for minimum of 60 meters radius
            lat = loc.get("latitude").toString().substring(0, 6); // ##.###
            lon = loc.get("longitude").toString().substring(0, 6); // ##.###
        } catch (Exception e) {
            return true;
        }

        String key = lat + lon;
        if (blindLocations.get(key) != null) {
            return true;
        } else {
            blindLocations.put(key, 1L);
            return false;
        }
    }

    // to check if a station has already been reported before
    private boolean checkSupplyReport(JSONObject loc) {
        String key = String.valueOf(loc);
        Long nReg = 1L;

        if (supplyLimits.get(key) != null) {
            nReg = supplyLimits.get(key);

            if (nReg <= 2) {
                nReg = nReg + 1L;
                supplyLimits.put(key, nReg);
                return true;
            } else {
                return false;
            }
        } else {
            supplyLimits.put(key, nReg);
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // start button actions for subscriber
        if (e.getSource() == startS) {
            try {
                brokerAddress = "tcp://" + brokerIPSub.getText() + ":" + portSub.getText();
                userId = userIDSub.getText();

                if (!middleware.isConnected()) {
                    middleware = new MqttClient(brokerAddress, userId, persistence);
                    createConnection();
                    JOptionPane.showMessageDialog(new JFrame("Connection SuccessFul"),
                        "Subscriber Connected!");
                } else {
                    JOptionPane.showMessageDialog(new JFrame("Connection"),
                        "Subscriber already Connected!");
                }
            } catch (MqttException ex) {
                JOptionPane.showMessageDialog(new JFrame("Error"), ex);
            }
        }

        // stop button actions for subscriber
        if (e.getSource() == stopS) {
            try {
                if (middleware.isConnected()) {
                    middleware.disconnect();
                    middleware.close();
                    JOptionPane.showMessageDialog(new JFrame("Connection"),
                        "Subscriber Disconnected!");
                } else {
                    JOptionPane.showMessageDialog(new JFrame("Connection"),
                        "Subscriber already disconnected!");
                }
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }

        // start button actions for Publisher
        if (e.getSource() == startP) {
            pipe = new Pipe();
            Creator.BROKER = "tcp://" + brokerIPPub.getText() + ":" + portPub.getText();
            Creator.USER_ID = userIDPub.getText();

            if (!Creator.middleware.isConnected()) {
                emitter.create();
                JOptionPane.showMessageDialog(new JFrame("Connection SuccessFul"),
                    "Connected!");
            }
        }

        // stop button actions for Publisher
        if (e.getSource() == stopP) emitter.close();

        // on change of subscriber topic
        if (e.getSource() == topicSub) {
            try {
                middleware.unsubscribe(topic);
            } catch (MqttException ex) {
                ex.printStackTrace();
            }

            topic = topicSub.getText();
            subscribeToMessages();
        }
    }

    public static void main(String[] args) {
        String web1 = "chalmers.se";
        String web2 = "google.com";
        String web3 = "bbc.co.uk";
        try {
            if (connectionExists(web1) || connectionExists(web2) || connectionExists(web3))
                new PipeManager();
            else {
                System.out.println("Info: Check internet connectivity first!");
            }
        } catch (Exception e) {
            if(e.toString().contains("org.json.JSONException: JSONObject"))
                System.out.println("Data Format not acceptable");
            else
                e.printStackTrace();
        }
    }

    private static boolean connectionExists(String testSite) {
        boolean connected = true;
        Socket sock = new Socket();
        InetSocketAddress socketAddress = new InetSocketAddress(testSite, 80);

        try {
            sock.connect(socketAddress, 3000);
            connected = true;
        } catch (IOException e) {
            connected = false;
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
                if (!connected)
                    System.out.println("Warning: Connectivity!");
            }
        }
        return connected;
    }
}