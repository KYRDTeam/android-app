package com.kyberswap.android.presentation.main;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import timber.log.Timber;

public class ForceUpdateChecker {

    public static final String KEY_UPDATE_REQUIRED = "force_update_required";
    public static final String KEY_CURRENT_VERSION = "force_update_base_version";
    public static final String KEY_UPDATE_URL = "force_update_store_url";
    public static final String KEY_UPDATE_TITLE = "force_update_title";
    public static final String KEY_UPDATE_MESSAGE = "force_update_message";
    private static final String TAG = ForceUpdateChecker.class.getSimpleName();
    private OnUpdateNeededListener onUpdateNeededListener;
    private Context context;

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public void check() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        if (remoteConfig.getBoolean(KEY_UPDATE_REQUIRED)) {
            String currentVersion = remoteConfig.getString(KEY_CURRENT_VERSION);
            String appVersion = getAppVersion(context);
            String updateUrl = remoteConfig.getString(KEY_UPDATE_URL);
            String title = remoteConfig.getString(KEY_UPDATE_TITLE);
            String message = remoteConfig.getString(KEY_UPDATE_MESSAGE);
            if (convertVersionNameToVersionCode(appVersion) < convertVersionNameToVersionCode(currentVersion) && onUpdateNeededListener != null) {
                onUpdateNeededListener.onUpdateNeeded(title, message, updateUrl);
            }
        }
    }

    private String getAppVersion(Context context) {
        String result = "";

        try {
            result = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Timber.e(e.getMessage());
        }

        return result;
    }

    private int convertVersionNameToVersionCode(String appVersion) {
        int versionMajor = 0;
        int versionMiner = 0;
        int versionPatch = 0;

        try {
            String[] results = appVersion.split("\\.");
            if (results[0] != null) {
                versionMajor = Integer.parseInt(results[0]);
            }
            if (results[1] != null) {
                versionMiner = Integer.parseInt(results[1]);
            }

            if (results[2] != null) {
                versionPatch = Integer.parseInt(results[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionMajor * 10000 + versionMiner * 100 + versionPatch;
    }

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String title, String message, String updateUrl);
    }

    public static class Builder {

        private Context context;
        private OnUpdateNeededListener onUpdateNeededListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();

            return forceUpdateChecker;
        }
    }
}