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
import android.widget.Button;
import android.util.Log;



/**
 * Special preference type that allows to set a pre-configuration of Color and Gamma
 * Value on Nexus Devices.
 */
public class ColorHackPresets extends DialogPreference implements OnClickListener {

    private static final String TAG = "PRESETS...";

    private static final String FILE_PATH_GAMMA = "/sys/class/misc/colorcontrol/v1_offset";

    private static final String GAMMA_PATH = "/sys/devices/platform/omapdss/manager0/gamma";

    private static final String FILE_PATH_MULTI = "/sys/class/misc/colorcontrol/multiplier";

    private static final UpdatePostpwner gammapwnage = new UpdatePostpwner(FILE_PATH_GAMMA);
    private static final UpdatePostpwner multipwnage = new UpdatePostpwner(FILE_PATH_MULTI);;

    // Align MAX_VALUE with Voodoo Control settings
    private static final int MAX_VALUE = 2000000000;
    

    public ColorHackPresets(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_colorgamma_presets);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SetupButtonClickListeners(view);
    }

    private void SetupButtonClickListeners(View view) {
        Button[] mPresets = new Button[6];

        mPresets[0] = (Button)view.findViewById(R.id.btnPreset1);
        mPresets[1] = (Button)view.findViewById(R.id.btnPreset2);
        mPresets[2] = (Button)view.findViewById(R.id.btnPreset3);
        mPresets[3] = (Button)view.findViewById(R.id.btnPreset4);
        mPresets[4] = (Button)view.findViewById(R.id.btnPreset5);
        mPresets[5] = (Button)view.findViewById(R.id.btnPreset6);
        for (int i = 0; i < 6; i++) {
            mPresets[i].setOnClickListener(this);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

    }

    
    /**
     * Check whether the running kernel supports color/gamma tuning or not.
     * 
     * @return Whether color/gamma tuning is supported or not
     */
    public static boolean isSupported() {
        return Utils.fileExists(FILE_PATH_GAMMA) && Utils.fileExists(FILE_PATH_MULTI) && Utils.fileExists(GAMMA_PATH);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnPreset1:
                    Preset1();
                    break;
            case R.id.btnPreset2:
                    Preset2();
                    break;
            case R.id.btnPreset3:
                    Preset3();
                    break;
            case R.id.btnPreset4:
                    Preset4();
                    break;
            case R.id.btnPreset5:
                    Preset5();
                    break;
            case R.id.btnPreset6:
                    Preset6();
                    break;                 
        }
    }


    private void WriteMultiplier(Double fValue , int iPos) {
        int iValue = (int) ((double) MAX_VALUE * fValue);
        int iValue2;
        Colors c = I2Color.Lookup[iPos];
        multipwnage.writeValue(c, ""+iValue);
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        iValue2 = sharedPrefs.getInt(multipwnage.fakepaths[c.ordinal()], iValue);
        //storing to SharedPreferences because of reloaded @ startup
        Editor editor = getEditor();
        editor.putInt(multipwnage.fakepaths[c.ordinal()], iValue);
        editor.commit();
        Log.d(TAG, "Changing ColorMultiplier " +iPos+ " from:" +iValue2+ " to: " +iValue);
    }


    private void WriteGamma(int iValue , int iPos) {
        int iValue2;
        String path = GAMMA_PATH;
        if (iPos == 3) {
            Utils.writeValue(path, String.valueOf((long) iValue));
        } else {
            Colors c = I2Color.Lookup[iPos];
            path = gammapwnage.fakepaths[c.ordinal()];
            gammapwnage.writeValue(c, String.valueOf((long) iValue));
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        iValue2 = sharedPrefs.getInt(path, iValue);
        //storing to SharedPreferences because of reloaded @ startup
        Editor editor = getEditor();
        editor.putInt(path, iValue);
        editor.commit();
        Log.d(TAG, "Changing GammaValue " +iPos+ " from:" +iValue2+ " to: " +iValue);
    }
    

    private void Preset1() {
        WriteMultiplier(0.5, 0);
        WriteMultiplier(0.5, 1);
        WriteMultiplier(0.5, 2);
        WriteGamma(0, 0);
        WriteGamma(0, 1);
        WriteGamma(0, 2);
        WriteGamma(0, 3);
    }

    private void Preset2() {
        WriteMultiplier(1.0, 0);
        WriteMultiplier(1.0, 1);
        WriteMultiplier(1.0, 2);
        WriteGamma(0, 0);
        WriteGamma(0, 1);
        WriteGamma(0, 2);
        WriteGamma(0, 3);
    }

    private void Preset3() {
        WriteMultiplier(0.35, 0);
        WriteMultiplier(0.38, 1);
        WriteMultiplier(0.5, 2);
        WriteGamma(0, 0);
        WriteGamma(0, 1);
        WriteGamma(0, 2);
        WriteGamma(0, 3);
    }

    private void Preset4() {
        WriteMultiplier(1.0, 0);
        WriteMultiplier(1.0, 1);
        WriteMultiplier(1.0, 2);
        WriteGamma(-6, 0);
        WriteGamma(0, 1);
        WriteGamma(7, 2);
        WriteGamma(0, 3);
    }

    private void Preset5() {
        WriteMultiplier(1.1021, 0);
        WriteMultiplier(1.0021, 1);
        WriteMultiplier(1.3271, 2);
        WriteGamma(-6, 0);
        WriteGamma(0, 1);
        WriteGamma(8, 2);
        WriteGamma(6, 3);
    }

    private void Preset6() {
        WriteMultiplier(0.45, 0);
        WriteMultiplier(0.48, 1);
        WriteMultiplier(0.5, 2);
        WriteGamma(-4, 0);
        WriteGamma(0, 1);
        WriteGamma(5, 2);
        WriteGamma(0, 3);
    }

}
