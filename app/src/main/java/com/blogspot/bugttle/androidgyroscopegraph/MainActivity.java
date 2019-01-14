package com.blogspot.bugttle.androidgyroscopegraph;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGyro;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mTextView = findViewById(R.id.gyroTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGyro != null) {
            mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        } else {
            mTextView.setText("Not Supported");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            final float sensorX = sensorEvent.values[0];
            final float sensorY = sensorEvent.values[1];
            final float sensorZ = sensorEvent.values[2];
            final String text = String.format(Locale.US, "Gyroscope: x=%f y=%f z=%f", sensorX, sensorY, sensorZ);
            mTextView.setText(text);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
