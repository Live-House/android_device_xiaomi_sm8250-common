/*
 * Copyright (C) 2018 The LineageOS Project
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

package org.lineageos.settings.display;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.FileUtils;

public class DisplaySettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    private SwitchPreferenceCompat mDcDimmingPreference;
    private static final String DC_DIMMING_ENABLE_KEY = "dc_dimming_enable";
    private static final String DC_DIMMING_NODE = "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/dimlayer_exposure";

    private SwitchPreferenceCompat mHBMPreference;
    private SwitchPreferenceCompat mAutoHBMPreference;
    private static final String HBM_ENABLE_KEY = "hbm";
    private static final String AUTO_HBM_ENABLE_KEY = "auto_hbm";
    private static final String HBM_NODE = "/sys/class/drm/card0/card0-DSI-1/disp_param";
    private static final String BACKLIGHT = "/sys/class/backlight/panel0-backlight/brightness";

    private SwitchPreferenceCompat mTouchSamplingPreference;
    private static final String HTSR_ENABLE_KEY = "htsr_enable";
    private static final String HTSR_FILE = "/sys/devices/virtual/touch/touch_dev/bump_sample_rate";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.advanced_display_settings);

        // High Touch Polling
        mTouchSamplingPreference = (SwitchPreferenceCompat) findPreference(HTSR_ENABLE_KEY);
        if (FileUtils.fileExists(HTSR_FILE)) {
            mTouchSamplingPreference.setEnabled(true);
            mTouchSamplingPreference.setOnPreferenceChangeListener(this);
        } else {
            mTouchSamplingPreference.setSummary(R.string.htsr_enable_summary_not_supported);
            mTouchSamplingPreference.setEnabled(false);
        }

        // DC Dimming
        mDcDimmingPreference = (SwitchPreferenceCompat) findPreference(DC_DIMMING_ENABLE_KEY);
        if (FileUtils.fileExists(DC_DIMMING_NODE)) {
            mDcDimmingPreference.setEnabled(true);
            mDcDimmingPreference.setOnPreferenceChangeListener(this);
        } else {
            mDcDimmingPreference.setSummary(R.string.dc_dimming_enable_summary_not_supported);
            mDcDimmingPreference.setEnabled(false);
        }

        // HBM
        mHBMPreference = (SwitchPreferenceCompat) findPreference(HBM_ENABLE_KEY);
        if (FileUtils.fileExists(HBM_NODE)) {
            mHBMPreference.setEnabled(true);
            mHBMPreference.setOnPreferenceChangeListener(this);
        } else {
            // mHBMPreference.setSummary(R.string.hbm_enable_summary_not_supported);
            mHBMPreference.setEnabled(false);
        }
        // AutoHBM
        mAutoHBMPreference = (SwitchPreferenceCompat) findPreference(AUTO_HBM_ENABLE_KEY);
        if (FileUtils.fileExists(HBM_NODE)) {
            mAutoHBMPreference.setEnabled(true);
            mAutoHBMPreference.setOnPreferenceChangeListener(this);
            mAutoHBMPreference.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(AUTO_HBM_ENABLE_KEY, false));
        } else {
            // mAutoHBMPreference.setSummary(R.string.hbm_enable_summary_not_supported);
            mAutoHBMPreference.setEnabled(false);
        }
    }

    // public static boolean isAUTOHBMEnabled(Context context) {
    //     return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AUTO_HBM_ENABLE_KEY, false);
    // }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;

        // High Touch Polling
        if (HTSR_ENABLE_KEY.equals(preference.getKey())) {
            FileUtils.writeLine(HTSR_FILE, enabled ? "1" : "0");
        }

        // DC Dimming
        if (DC_DIMMING_ENABLE_KEY.equals(preference.getKey())) {
            FileUtils.writeLine(DC_DIMMING_NODE, enabled ? "1" : "0");
        }

        // HBM
        if (HBM_ENABLE_KEY.equals(preference.getKey())) {
            FileUtils.writeLine(HBM_NODE, enabled ? "0x10000" : "0xF0000");
            if (enabled) FileUtils.writeLine(BACKLIGHT, "2047");
        }
        // AutoHBM
        if (AUTO_HBM_ENABLE_KEY.equals(preference.getKey())) {
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(AUTO_HBM_ENABLE_KEY, enabled).commit();
            FileUtils.enableService(getContext());
        }

        return true;
    }
}
