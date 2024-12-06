/*
 * Copyright (C) 2024 The LineageOS Project
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

package org.lineageos.settings.thermal;

import android.app.ActivityTaskManager;
import android.app.ActivityTaskManager.RootTaskInfo;
import android.app.IActivityTaskManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import org.lineageos.settings.R;

import java.util.List;

public class ThermalTileService extends TileService {

    private ThermalUtils mThermalUtils;
    private Tile tile;

    private String foregroundApp;

    protected boolean isAppLaunchable(String packageName) {
        PackageManager mPackageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);

        List<ResolveInfo> activities = mPackageManager.queryIntentActivities(intent, 0);

        return activities != null && !activities.isEmpty();
    }

    private int getNewState(int state) {
        switch (state) {
            case ThermalUtils.STATE_DEFAULT:
                return ThermalUtils.STATE_BENCHMARK;
            case ThermalUtils.STATE_BENCHMARK:
                return ThermalUtils.STATE_BROWSER;
            case ThermalUtils.STATE_BROWSER:
                return ThermalUtils.STATE_CAMERA;
            case ThermalUtils.STATE_CAMERA:
                return ThermalUtils.STATE_DIALER;
            case ThermalUtils.STATE_DIALER:
                return ThermalUtils.STATE_GAMING;
            case ThermalUtils.STATE_GAMING:
                return ThermalUtils.STATE_STREAMING;
            case ThermalUtils.STATE_STREAMING:
            default:
                return ThermalUtils.STATE_DEFAULT;
        }
    }

    protected String getStateString(int state) {
        switch (state) {
            case ThermalUtils.STATE_BENCHMARK:
                return getString(R.string.thermal_benchmark);
            case ThermalUtils.STATE_BROWSER:
                return getString(R.string.thermal_browser);
            case ThermalUtils.STATE_CAMERA:
                return getString(R.string.thermal_camera);
            case ThermalUtils.STATE_DIALER:
                return getString(R.string.thermal_dialer);
            case ThermalUtils.STATE_GAMING:
                return getString(R.string.thermal_gaming);
            case ThermalUtils.STATE_STREAMING:
                return getString(R.string.thermal_streaming);
            case ThermalUtils.STATE_DEFAULT:
            default:
                return getString(R.string.thermal_default);
        }
    }

    private void updateTileView() {
        int state = mThermalUtils.getStateForPackage(foregroundApp);

        String displayText = getStateString(state);

        tile.setContentDescription(displayText);
        tile.setSubtitle(displayText);

        if (isAppLaunchable(foregroundApp)) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        try {
            IActivityTaskManager mActivityTaskManager = ActivityTaskManager.getService();
            final RootTaskInfo info = mActivityTaskManager.getFocusedRootTaskInfo();
            if (info == null || info.topActivity == null) return;
            foregroundApp = info.topActivity.getPackageName();
        } catch (RemoteException e) {
            // Do nothing
        }

        mThermalUtils = new ThermalUtils(this);
        tile = getQsTile();
        updateTileView();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (tile == null || foregroundApp == null) return;

        if (tile.getState() == Tile.STATE_ACTIVE) {
            mThermalUtils.writePackage(foregroundApp,
                getNewState(mThermalUtils.getStateForPackage(foregroundApp)));
            mThermalUtils.setThermalProfile(foregroundApp);
        }
        updateTileView();
    }
}