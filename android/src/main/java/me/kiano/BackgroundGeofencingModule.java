package me.kiano;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONException;

import me.kiano.interfaces.RNGeofenceHandler;
import me.kiano.models.RNGeofence;
import me.kiano.models.RNGeofenceWebhookConfiguration;
import me.kiano.models.RNNotification;

public class BackgroundGeofencingModule extends ReactContextBaseJavaModule {

    public BackgroundGeofencingModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "BackgroundGeofencing";
    }

    @ReactMethod
    public void add(ReadableMap geoFence, final Promise promise) {
        try {

            if (!RNGeofence.hasLocationPermission(getReactApplicationContext())) {
                promise.reject("permission_denied", "Access fine location is not permitted");
                return;
            }

            if (!RNGeofence.isLocationServicesEnabled(getReactApplicationContext())) {
                promise.reject("location_services_disabled", "Location services are disabled");
                return;
            }

            final RNGeofence rnGeofence = new RNGeofence(getReactApplicationContext(), geoFence);

            rnGeofence.start(rnGeofence.registerOnDeviceRestart, new RNGeofenceHandler() {
                @Override
                public void onSuccess(String geofenceId) {
                    promise.resolve(geofenceId);
                }
                @Override
                public void onError(String geofenceId, Exception e) {
                    promise.reject("geofence_exception", "Failed to start geofence service for id: " + rnGeofence.id, e);
                }
            });
        } catch (Exception e) {
            promise.reject("geofence_exception", "Failed to start geofence service for id: " + geoFence.getString("id"), e);
        }
    }

    @ReactMethod
    public void remove(String id) {
        RNGeofence.remove(getReactApplicationContext(), id);
    }

    @ReactMethod
    public void configureWebhook (ReadableMap configureWebhook, final Promise promise) {
        try {
            RNGeofenceWebhookConfiguration rnGeofenceWebhookConfiguration = new RNGeofenceWebhookConfiguration(configureWebhook);
            rnGeofenceWebhookConfiguration.save(getReactApplicationContext());
            promise.resolve(true);
        } catch (JSONException e) {
            promise.reject("geofence_exception", e.getMessage());
            e.printStackTrace();
        }

    }

    @ReactMethod
    public void configureNotification (ReadableMap configureNotification, final Promise promise) {
        RNNotification notification = new RNNotification(configureNotification);
        notification.save(getReactApplicationContext());
        promise.resolve(true);
    }

    @ReactMethod
    public void configure(ReadableMap configuration, final Promise promise) {
        try {
            if (configuration.hasKey("notification")) {
                ReadableMap notification = configuration.getMap("notification");
                RNNotification rnNotification = new RNNotification(notification);
                rnNotification.save(getReactApplicationContext());
            } else {
                RNNotification rnNotification = new RNNotification();
                rnNotification.save(getReactApplicationContext());
            }
            if (configuration.hasKey("webhook")) {
                ReadableMap webhook = configuration.getMap("webhook");
                RNGeofenceWebhookConfiguration rnGeofenceWebhookConfiguration = new RNGeofenceWebhookConfiguration(webhook);
                rnGeofenceWebhookConfiguration.save(getReactApplicationContext());
            }
            promise.resolve(true);
        } catch (JSONException e) {
            promise.reject("geofence_exception", e.getMessage());
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void hasLocationPermission(Promise promise) {
        promise.resolve(RNGeofence.hasLocationPermission(getReactApplicationContext()));
    }

    @ReactMethod
    public void isLocationServicesEnabled(Promise promise) {
        promise.resolve(RNGeofence.isLocationServicesEnabled(getReactApplicationContext()));
    }

    @ReactMethod
    public void reRegisterErroneousGeofences() {
        if (RNGeofence.hasLocationPermission(getReactApplicationContext()) && RNGeofence.isLocationServicesEnabled(getReactApplicationContext())) {
            RNGeofence.reRegisterErroneousGeofences(getReactApplicationContext());
        }
    }

}
