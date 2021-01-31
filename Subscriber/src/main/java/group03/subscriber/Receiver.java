package group03.subscriber;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Receiver implements MqttCallback {

	private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

	private final static String TOPIC = "external";
	private final static String BROKER = "tcp://group3distributed.ddns.net:3189";
	//private final static String BROKER = "tcp://localhost:1883";

	private final static String USER_ID = "test-subscriber";

	private final IMqttClient middleware;
	MemoryPersistence persistence = new MemoryPersistence();

	public Receiver() throws MqttException {
		middleware = new MqttClient(BROKER, USER_ID,persistence);
		middleware.connect();
		middleware.setCallback(this);
	}

	public static void main(String[] args) throws MqttException, InterruptedException {
		Receiver s = new Receiver();
		s.subscribeToMessages();
	}

	private void subscribeToMessages() {
		THREAD_POOL.submit(() -> {
			try {
				middleware.subscribe(TOPIC);
				middleware.subscribe("bottleneck");
			} catch (MqttSecurityException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void connectionLost(Throwable throwable) {
		System.out.println("Connection lost!");
		try {
			middleware.disconnect();
			middleware.close();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		// Try to reestablish? Plan B?
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		message.setQos(0);
	   System.out.println("topic '" + topic + "': " + message);
	}
}