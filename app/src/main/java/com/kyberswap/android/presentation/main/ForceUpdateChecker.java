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
            if (appVersion.compareTo(currentVersion) < 0 && onUpdateNeededListener != null) {
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