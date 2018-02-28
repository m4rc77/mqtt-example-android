package ch.m4rc77.mqttsensorapp;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;

/**
 * see https://github.com/eclipse/paho.mqtt.android/blob/master/paho.mqtt.android.example/src/main/java/paho/mqtt/java/example/PahoExampleActivity.java
 */
public class MqttHandler {

    private final Output output;

    private MqttAndroidClient mqttAndroidClient;

    public MqttHandler(Output o, Context context) {
        output = o;

        String clientId = "MqttSensorApp" + System.currentTimeMillis();
        mqttAndroidClient = new  MqttAndroidClient(context, getBrokerUrl(), clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    output.addOutput("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    output.addOutput("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                output.addOutput("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                output.addOutput("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            output.addOutput("Connecting to " + getBrokerUrl());
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    output.addOutput("Failed to connect to: " + getBrokerUrl() + " " + exception);
                }
            });
        } catch (MqttException ex){
            output.addOutput("Error connecting " + getBrokerUrl() + " " + ex);
        }
    }
    
    private void subscribeToTopic() {
        // no topic subscriptions in this example ...
    }

    public void publishMessage(String msg){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            if(mqttAndroidClient.isConnected()) {
                mqttAndroidClient.publish(getTopic(), message);
                output.addOutput("Message '" + msg + "' published");
            } else {
                output.addOutput("Message '" + msg + "' NOT published (unconnected)");
            }
        } catch (MqttException e) {
            output.addOutput("Error Publishing: " + e);
        }
    }

    public String getBrokerUrl() {
        return Config.BROKER;
    }

    public String getTopic() {
        return Config.TOPIC;
    }
}
