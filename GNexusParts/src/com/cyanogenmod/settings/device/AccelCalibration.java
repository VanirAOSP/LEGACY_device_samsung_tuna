/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.os.Vibrator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

/**
 * Allows calibration of accelerometer
 */
public class AccelCalibration extends DialogPreference implements OnClickListener, SensorEventListener {

    private static final String TAG = "CALIBRATION...";
    private static final String FILE_PATH = "/data/accelcalib";
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mValues = new float[] {0, 0, 0};
    private int mReadings = 0;
    
    private Button mResetButton;

    public AccelCalibration(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_accel_calibration);
        setPositiveButtonText(R.string.accel_calibration_calibrate_title);
        setNegativeButtonText(R.string.accel_calibration_close_title);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mResetButton = (Button)view.findViewById(R.id.btnaccelCalibReset);
        mResetButton.setOnClickListener(this);
        if (!Utils.fileExists(FILE_PATH))
            mResetButton.setEnabled(false);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mSensorManager.unregisterListener(this);
  
        if (positiveResult) {
            doCalibrate();
            Toast.makeText(getContext(), R.string.accel_calibration_success, Toast.LENGTH_LONG).show();
        }
    }

    public void onClick(View v) {
        doReset();
        mResetButton.setEnabled(false);
        Toast.makeText(getContext(), R.string.accel_calibration_reset_success, Toast.LENGTH_LONG).show();
    }
    
    public void doReset() {
        if (Utils.fileExists(FILE_PATH))
            Utils.deleteFile(FILE_PATH);
    }
    
    public void doCalibrate() {
        if (mSensor == null) {
            Log.e(TAG, "no accelerometer found");
            return;
        }

        ByteBuffer b = ByteBuffer.allocate(3 * (Float.SIZE / 8));
        b.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 3; i++) {
            mValues[i] /= -mReadings;
            if (i == 2)
                mValues[i] += 9.81;
            b.putFloat(mValues[i]);
            //Log.d(TAG, "calib value " + i + ": " + mValues[i]);
        }
        Utils.writeValue(FILE_PATH, b.array());
    }
    
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (mReadings > 12) {
            for (int i = 0; i < 3; i++)
                    mValues[i] = (mValues[i] / mReadings) + event.values[i];
            mReadings = 2;
        } else {
            for (int i = 0; i < 3; i++)
                    mValues[i] += event.values[i];
            mReadings++;
        }
    }
}

