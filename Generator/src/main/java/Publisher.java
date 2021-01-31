import org.eclipse.paho.client.mqttv3.*;
import org.json.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class publishes request data with different scenario's.
 */
public class Publisher extends JFrame implements ActionListener {
    private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
    private static String topic = "external";
    private static String brokerAddress = "";

    private Generator generator;
    private int reqCounter;
    private HashMap<String, Long> request;
    private String publisherId;
    private IMqttClient middleware;
    private JButton startButton, stopButton;
    private JTextField brokerIPTextField;
    private JTextField portTextField;
    private JTextField topicTextField;
    private JTextField requestNumber;
    private JTextField delayTextField;
    private JRadioButton scenario1RadioBtn, scenario2RadioBtn, randomScenarioRadioBtn;
    private int patternSelection;

    public Publisher() {
        JFrame frame = new JFrame("Publisher");
        frame.setBounds(50, 50, 320, 320);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                if (middleware != null && middleware.isConnected()) {
                    try {
                        close();
                    } catch (MqttException error) {
                        error.printStackTrace();
                    }
                } else {
                    System.out.println("bye");
                }
            }
        });

        scenario1RadioBtn = new JRadioButton();
        scenario2RadioBtn =  new JRadioButton();
        randomScenarioRadioBtn = new JRadioButton();
        ButtonGroup groupRadio = new ButtonGroup();
        groupRadio.add(scenario1RadioBtn);
        groupRadio.add(scenario2RadioBtn);
        groupRadio.add(randomScenarioRadioBtn);
        scenario1RadioBtn.addActionListener(this);
        scenario2RadioBtn.addActionListener(this);
        randomScenarioRadioBtn.addActionListener(this);
        randomScenarioRadioBtn.setSelected(true);
        patternSelection = 3;

        brokerIPTextField = new JTextField("group3distributed.ddns.net");
        portTextField = new JTextField("3189");
        topicTextField = new JTextField("external");
        requestNumber = new JTextField("50");
        delayTextField = new JTextField("200");

        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        JPanel northPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel southPanel = new JPanel();

        centerPanel.setLayout(new GridLayout(5, 2, 2, 2));
        southPanel.setLayout(new GridLayout(4, 2, 2, 2));

        northPanel.add(new Label("Request Generator"));

        centerPanel.add(new JLabel("Broker ", JLabel.CENTER));
        centerPanel.add(brokerIPTextField);
        centerPanel.add(new JLabel("Port", JLabel.CENTER));
        centerPanel.add(portTextField);
        centerPanel.add(new JLabel("Topic", JLabel.CENTER));
        centerPanel.add(topicTextField);
        centerPanel.add(new JLabel("No# of Req.", JLabel.CENTER));
        centerPanel.add(requestNumber);
        centerPanel.add(new JLabel("Delay (ms)", JLabel.CENTER));
        centerPanel.add(delayTextField);

        southPanel.add(new JLabel("Ullevi Scenario", JLabel.CENTER));
        southPanel.add(scenario1RadioBtn);
        southPanel.add(new JLabel("Next Rush Hour Scenario", JLabel.CENTER));
        southPanel.add(scenario2RadioBtn);
        southPanel.add(new JLabel("Random Generation", JLabel.CENTER));
        southPanel.add(randomScenarioRadioBtn);
        southPanel.add(startButton);
        southPanel.add(stopButton);

        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);

        reqCounter = 0;

        // instantiation of generator should be at the end since
        // patternSelection is not instantiated in the beginning
        generator = new Generator();
        request = new HashMap<>();
        publisherId = String.valueOf(generator.getDeviceId());
        frame.setVisible(true);
    }

    /**
     * Returns data which is passed as an argument, but modifying it
     * by adding a 'deviceId' key-value.
     *
     * @param data json data.
     * @return modified data.
     */
    protected JSONObject getRequestID(JSONObject data) {
        // deviceId from generated data
        String deviceID = String.valueOf(data.getLong("deviceId"));
        // initial variable for request
        Long nReq = 1L;

        // if the device is new, put 1 as first request
        if (request.get(deviceID) != null) {
            nReq = request.get(deviceID);
            nReq = nReq + 1L;
        }

        request.put(deviceID, nReq);
        data.put("requestId", nReq);

        return data;
    }

    /**
     * Sleeps the thread by given number of milli-seconds.
     *
     * @param ms milli-seconds
     */
    public static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Invoked when an action occurs.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) sendData();
        if (e.getSource() == stopButton) reqCounter = 0;

        if (e.getSource() == scenario1RadioBtn) {
            if (scenario1RadioBtn.isSelected()) patternSelection = 1;
        }
        if (e.getSource() == scenario2RadioBtn) {
            if (scenario2RadioBtn.isSelected()) patternSelection = 2;
        }
        if (e.getSource() == randomScenarioRadioBtn) {
            if (randomScenarioRadioBtn.isSelected()) patternSelection = 3;
        }
    }

    /**
     * Sends data over MQTT server with given 'brokerAddress' and topic.
     */
    private void sendData() {
        try {
            reqCounter = Integer.parseInt(requestNumber.getText());
            int timeMs = Integer.parseInt(delayTextField.getText());

            if (brokerIPTextField.getText().equals("") || portTextField.getText().equals("")
                || topicTextField.getText().equals("") ||reqCounter < 0)
            {
                JOptionPane.showMessageDialog(new JFrame("ERROR"), "Please Input All Required Fields Correctly");
            }
            else {
                brokerAddress = "tcp://" + brokerIPTextField.getText() + ":" + portTextField.getText();
                middleware = new MqttClient(brokerAddress, publisherId);
                middleware.connect();
                topic = topicTextField.getText();
            }

            THREAD_POOL.submit(() -> {
                while (reqCounter > 0) {
                    try {
                        sendMessage();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    reqCounter--;
                    wait(timeMs);
                }

                try {
                    close();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishes a message in bytes over the MQTT middleware.
     *
     * @throws MqttException Thrown if an error occurs communicating with the server.
     */
    private void sendMessage() throws MqttException {
        JSONObject object = generator.generateData(patternSelection);
        object = getRequestID(object);

        System.out.println(object.toString());

        MqttMessage message = new MqttMessage();
        String mess = object.toString();
        message.setPayload(mess.getBytes());

        topic = topicTextField.getText();
        middleware.publish(topic, message);
    }

    /**
     * Closes the middleware by disconnecting it from the MQTT server.
     *
     * @throws MqttException Thrown if an error occurs communicating with the server.
     */
    private void close() throws MqttException {
        middleware.disconnect();
        middleware.close();
    }

    // main method
    public static void main(String[] args) {
        new Publisher();
    }
}