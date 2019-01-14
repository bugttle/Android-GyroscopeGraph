package com.blogspot.bugttle.androidgyroscopegraph;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mGyro;
    private TextView mTextView;
    private LineChart mChart;

    final String[] names = new String[]{ "x-values", "y-values", "z-values" };
    final int[] colors = new int[]{ Color.RED, Color.GREEN, Color.BLUE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mTextView = findViewById(R.id.gyroTextView);
        mChart = findViewById(R.id.chart);

        mChart.setData(new LineData());
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

            final LineData data = mChart.getLineData();
            if (data != null) {
                for (int i = 0; i < 3; ++i) {
                    ILineDataSet set = data.getDataSetByIndex(i);
                    if (set == null) {
                        set = createSet(names[i], colors[i]);
                        data.addDataSet(set);
                    }

                    data.addEntry(new Entry(set.getEntryCount(), sensorEvent.values[i]), i);
                    data.notifyDataChanged();
                }

                mChart.notifyDataSetChanged();
                mChart.setVisibleXRangeMaximum(50);
                mChart.moveViewToX(data.getEntryCount());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f); // 線の幅を指定
        set.setColor(color); // 線の色を指定
        set.setDrawCircles(false); // ポイントごとの円を表示しない
        set.setDrawValues(false); // 値を表示しない

        return set;
    }
}
