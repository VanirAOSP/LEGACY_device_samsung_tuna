/*
 * Copyright (C) 2011 The CyanogenMod Project
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
import android.util.Log;
import android.widget.Button;

/**
 * Special preference type that allows configuration of Color settings on Nexus
 * Devices
 */
public class ColorTuningPreference extends DialogPreference implements OnClickListener {

    private static final String TAG = "COLOR...";

    private static final int[] SEEKBAR_ID = new int[] {
            R.id.color_red_seekbar, R.id.color_green_seekbar, R.id.color_blue_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
            R.id.color_red_value, R.id.color_green_value, R.id.color_blue_value
    };

    private static final String FILE_PATH = "/sys/class/misc/colorcontrol/multiplier";

    private static final UpdatePostpwner pwnage = new UpdatePostpwner(FILE_PATH);

    private ColorSeekBar mSeekBars[] = new ColorSeekBar[3];

    // Align MAX_VALUE with Voodoo Control settings
    private static final int MAX_VALUE = 2000000000;

    // Track instances to know when to restore original color
    // (when the orientation changes, a new dialog is created before the old one
    // is destroyed)
    private static int sInstances = 0;

    public ColorTuningPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        I2Color.Init();

        setDialogLayoutResource(R.layout.preference_dialog_color_tuning);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            mSeekBars[i] = new ColorSeekBar(seekBar, valueDisplay, pwnage, I2Color.Lookup[i]);
        }
        SetupButtonClickListeners(view);
    }

    private void SetupButtonClickListeners(View view) {
            Button mButton1 = (Button)view.findViewById(R.id.btnColor1);
            Button mButton2 = (Button)view.findViewById(R.id.btnColor2);
            Button mButton3 = (Button)view.findViewById(R.id.btnColor3);
            mButton1.setOnClickListener(this);
            mButton2.setOnClickListener(this);
            mButton3.setOnClickListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (ColorSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (ColorSeekBar csb : mSeekBars) {
                csb.reset();
            }
        }
    }

    /**
     * Restore color tuning from SharedPreferences. (Write to kernel.)
     * 
     * @param context The context to read the SharedPreferences from
     */
    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }
        
        int iValue;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPrefs.getBoolean("override_initd_colors", true))
        {
            Boolean bFirstTime = sharedPrefs.getBoolean("FirstTimeColor", true);

            String sDefaultValue = Utils.readOneLine(FILE_PATH);

            for(Colors c : Colors.values())
            {
                pwnage.InitializeMultiplier(c, MAX_VALUE);
                iValue = sharedPrefs.getInt(pwnage.fakepaths[c.ordinal()], Integer.valueOf(pwnage.getDefault(c)));
        
                if (bFirstTime) {
                    Log.d(TAG, "restore default value: " +MAX_VALUE+ " File: " + pwnage.fakepaths[c.ordinal()]);
                    pwnage.writeColor(c, MAX_VALUE);
                }
                else {
                    pwnage.writeColor(c,iValue);
                }
            }

            if (bFirstTime)
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("FirstTimeColor", false);
                editor.commit();
            }
        }
    }

    /**
     * Check whether the running kernel supports color tuning or not.
     * 
     * @return Whether color tuning is supported or not
     */
    public static boolean isSupported() {
        return Utils.fileExists(FILE_PATH);
    }

    class ColorSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;

        private int mOriginal;

        private SeekBar mSeekBar;

        private TextView mValueDisplay;

        private UpdatePostpwner pwnage;
        private Colors mColor;

        public ColorSeekBar(SeekBar seekBar, TextView valueDisplay, UpdatePostpwner pwn, Colors color) {
            mColor = color;
            pwnage = pwn;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;

            SharedPreferences sharedPreferences = getSharedPreferences();

            mOriginal = pwn.InitializeMultiplier(color, MAX_VALUE);

            mSeekBar.setMax(MAX_VALUE);
            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            mSeekBar.setProgress(mOriginal);
            updateValue(mOriginal);
        }

        public void save() {
            Editor editor = getEditor();
            editor.putInt(pwnage.fakepaths[mColor.ordinal()], mSeekBar.getProgress());
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            pwnage.writeColor(mColor, progress);
            updateValue(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        private void updateValue(int progress) {
            mValueDisplay.setText(String.format("%d", (int) progress / 5000000));
        }

        public void SetNewValue(int iValue) {
            mOriginal = iValue;
            reset();
        }

    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnColor1:
                    SetSettings1();
                    break;
            case R.id.btnColor2:
                    SetSettings2();
                    break;
            case R.id.btnColor3:
                    SetSettings3();
                    break;
        }
    }

    private void SetSettings1() {
        mSeekBars[0].SetNewValue(875000000);
        mSeekBars[1].SetNewValue(875000000);
        mSeekBars[2].SetNewValue(875000000);
    }


    private void SetSettings2() {
        mSeekBars[0].SetNewValue(1000000000);
        mSeekBars[1].SetNewValue(1000000000);
        mSeekBars[2].SetNewValue(1000000000);
    }

    private void SetSettings3() {
        mSeekBars[0].SetNewValue(900000000);
        mSeekBars[1].SetNewValue(960000000);
        mSeekBars[2].SetNewValue(1000000000);
    }
}
