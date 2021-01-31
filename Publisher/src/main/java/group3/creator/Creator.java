package group3.creator;

import java.util.UUID;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.*;

import javax.swing.*;


public class Creator {
    private static String TOPIC = "";
    public static String BROKER = "tcp://group3distributed.ddns.net:3189";
    //private final static String BROKER = "tcp://localhost:1883";
    public static String USER_ID = UUID.randomUUID() + "";

    public static IMqttClient middleware;
    static String jsonObject = "";
    public Creator() throws MqttException {
        create();
    }
    //emitter function

    public void emitter(JSONObject data, String topic) {
        TOPIC = topic;
        jsonObject = data.toString();
        sendMessage();
    }

    // create message and publish on the broker
    private void sendMessage() {
        PipeManager.counter++;
        MqttMessage message = new MqttMessage();
        message.setQos(1);
        message.setPayload(jsonObject.getBytes());
        try {
            middleware.publish(TOPIC, message);
        } catch (MqttException e) {
            JOptionPane.showMessageDialog(new JFrame("ERROR"), e.toString());
        }

    }

    public void create() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            middleware = new MqttClient(BROKER, USER_ID, persistence);
            middleware.connect();
        } catch (MqttException e) {
            JOptionPane.showMessageDialog(new JFrame("ERROR"),
                "Provide correct Information!");
        }
    }

    public void close() {
        try {
            if (middleware.isConnected()) {
                middleware.disconnect();
                middleware.close();
                JOptionPane.showMessageDialog(new JFrame("Connection"),
                    "Publisher Disconnected");
            }
        } catch (MqttException e) {
            JOptionPane.showMessageDialog(new JFrame("ERROR"), e.toString());
        }
    }

}
