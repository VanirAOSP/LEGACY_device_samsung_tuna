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

import android.os.Bundle;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference;

import com.cyanogenmod.settings.device.R;

public class DevicePreferenceActivity extends PreferenceFragment {

    public static final String SHARED_PREFERENCES_BASENAME = "com.cyanogenmod.settings.device";
    public static final String ACTION_UPDATE_PREFERENCES = "com.cyanogenmod.settings.device.UPDATE";
    public static final String KEY_OVERRIDE_INITD = "override_initd";
    public static final String KEY_COLOR_TUNING = "color_tuning";
    public static final String KEY_GAMMA_TUNING = "gamma_tuning";
    public static final String KEY_COLORGAMMA_PRESETS = "colorgamma_presets";
    public static final String KEY_VIBRATOR_TUNING = "vibrator_tuning";
    public static final String KEY_CATEGORY_RADIO = "category_radio";
    public static final String KEY_HSPA = "hspa";
    public static final String KEY_GPU_OVERCLOCK = "gpu_overclock";

    private CheckBoxPreference mOverrideInitd;
    private ColorTuningPreference mColorTuning;
    private GammaTuningPreference mGammaTuning;
    private ColorHackPresets mColorHackPresets;
    private VibratorTuningPreference mVibratorTuning;
    private ListPreference mGpuOverclock;

    private boolean mOverride = true;

    private void enabler()
    {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean("override_initd_colors", mOverride);
        editor.commit();
        mColorTuning.setEnabled(mOverride && ColorTuningPreference.isSupported());
        mGammaTuning.setEnabled(mOverride && GammaTuningPreference.isSupported());
        mColorHackPresets.setEnabled(mOverride && ColorHackPresets.isSupported());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mOverride = sharedPrefs.getBoolean("override_initd_colors", true);

        mOverrideInitd = (CheckBoxPreference) findPreference(KEY_OVERRIDE_INITD);
        mOverrideInitd.setChecked(mOverride);

        mColorTuning = (ColorTuningPreference) findPreference(KEY_COLOR_TUNING);
        mGammaTuning = (GammaTuningPreference) findPreference(KEY_GAMMA_TUNING);
        mColorHackPresets = (ColorHackPresets) findPreference(KEY_COLORGAMMA_PRESETS);

        enabler();

        mVibratorTuning = (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);
        mVibratorTuning.setEnabled(VibratorTuningPreference.isSupported());

        mGpuOverclock = (ListPreference) findPreference(KEY_GPU_OVERCLOCK);
        mGpuOverclock.setEnabled(GpuOverclock.isSupported());
        mGpuOverclock.setOnPreferenceChangeListener(new GpuOverclock());
        GpuOverclock.updateSummary(mGpuOverclock, Integer.parseInt(mGpuOverclock.getValue()));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference != mOverrideInitd)            
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        mOverride = mOverrideInitd.isChecked();
        enabler();
        return true;
    }
}
