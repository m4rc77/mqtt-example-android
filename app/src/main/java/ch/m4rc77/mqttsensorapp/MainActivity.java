package ch.m4rc77.mqttsensorapp;

import java.util.Arrays;
import java.util.LinkedList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener, Output {

    private SensorManager sensorManager;

    private Sensor sensor;

    private TextView textOutputView;

    private MqttHandler mqttHandler;

    private LinkedList<Float> lastValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // just fill last value list with some dummy values ...
        this.lastValues = new LinkedList<>(Arrays.asList(-99.9f, 42.0f));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        textOutputView = (TextView) findViewById(R.id.text_output);

        mqttHandler = new MqttHandler(this, getApplicationContext());

        final TextView additionalInfoView = (TextView) findViewById(R.id.text_additional_info);
        additionalInfoView.setText(new StringBuilder("")
                .append("Selected sensor is: ").append(sensor.getName()).append("\n")
                .append("Using MQTT broker ").append(mqttHandler.getBrokerUrl()).append("\n")
                .append("Publishing to ").append(mqttHandler.getTopic())
        );

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textOutputView.setText("");
            }
        });
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        addOutput("Sensor accuracy changed: " + accuracy);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor only returns one value ... just output and publish it ...
        Float lux = event.values[0];

        if (lastValues.contains(lux)) {
            //addOutput("Message '" + lux + "' skipped");
        } else {
            lastValues.add(lux);
            lastValues.removeFirst();

            String msg = "Sensor value: " + lux;
            addOutput(msg);
            mqttHandler.publishMessage("" + lux);
        }
    }

    @Override
    public void addOutput(String msg) {
        textOutputView.setText(msg +"\r\n" + textOutputView.getText());
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}
