package com.blogspot.bugttle.androidgyroscopegraph;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final int REQUEST_PERMISSION = 1;

    private boolean mFileWritablePermitted = false;
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

        if (mGyro != null) {
            // almost every 1 second
            mSensorManager.registerListener(this, mGyro, 1 * 1000 * 1000);
        } else {
            mTextView.setText("Not Supported");
        }

        // REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        } else {
            mFileWritablePermitted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Don't stop
        //if (mGyro != null) {
        //    mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        //} else {
        //    mTextView.setText("Not Supported");
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Keep to listen sensors
        //mSensorManager.unregisterListener(this);
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

            if (mFileWritablePermitted) {
                save(sensorX, sensorY, sensorZ);
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

    void save(final float x, final float y, final float z) {
        if (isExternalStorageWritable()) {
            String text;
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/gyroscope.log");

            try (FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(outputStreamWriter);
            ) {
                // ex.) 2016-06-01T06:59:45+09:00
                java.text.DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                text = String.format(Locale.US, "%s,%f,%f,%f", df.format(new Date()), x, y, z);
                bw.write(text);
                bw.newLine();
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            mFileWritablePermitted = true;
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mFileWritablePermitted = true;
            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
