package cordova.plugin.pip;

import android.content.Context;
import android.content.Intent;
import android.app.PictureInPictureParams;
import android.util.Rational;
import android.util.Log;
import android.os.Bundle;
import android.os.Build;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import android.content.res.Configuration;

import com.wave.dev.Wave2;
import com.wave.dev.Wave2Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PictureInPicture extends CordovaPlugin {

    private static final String TAG = "HostApp";
    private Wave2 wave2;
    private JSONObject encryptedAuthData;

    Wave2Callback wave2Callback = new Wave2Callback() {
        @Override
        public void onInitialiseSuccess(String version) {
            Log.d(TAG, "onInitialiseSuccess " + version);
        }

        @Override
        public void onInitialiseFailed(JSONObject error) {
            Log.d(TAG, "onInitialiseFailed Error: " + error.toString());
        }

        @Override
        public void onLaunchSuccess() {
            Log.d(TAG, "onLaunchSuccess");
        }

        @Override
        public void onLaunchFailed(JSONObject error) {
            Log.d(TAG, "onLaunchFailed Error: " + error.toString());
        }

        @Override
        public void onLoginSuccess(JSONObject userData) {
            Log.d(TAG, "onLoginSuccess userData: " + userData.toString());
        }

        @Override
        public void onLoginFailed(JSONObject error) {
            Log.d(TAG, "onLoginFailed Error: " + error.toString());
        }

        @Override
        public void onKycRequired(JSONObject userData) {
            Log.d(TAG, "onUserKycIsComplete userData: " + userData.toString());
        }

        @Override
        public void onOrderPlaced(JSONObject orderDetails) {
            Log.d(TAG, "onOrderPlaced orderDetails: " + orderDetails.toString());
        }

        @Override
        public void onOrderFailed(JSONObject orderDetails) {
            Log.d(TAG, "onOrderFailed orderDetails: " + orderDetails.toString());
        }

        @Override
        public void onAnalyticsEvent(JSONObject event) {
            Log.d(TAG, "onAnalyticsEvent event: " + event.toString());
        }

        @Override
        public void onSDKClosed() {
            Log.d(TAG, "onSDKClosed");
        }

        @Override
        public void onSDKExited() {
            Log.d(TAG, "onSDKExited");
        }

        @Override
        public void onAdditionalData(JSONObject data) {
            Log.d(TAG, "onAdditionalData data: " + data.toString());
        }

        @Override
        public void openAccountStatements(JSONObject userData) {
            Log.d(TAG, "onOrderFailed orderDetails: " + userData.toString());
        }

        @Override
        public void onError(JSONObject error) {
            Log.d(TAG, "onError Error: " + error.toString());
        }

        @Override
        public void onSessionPingReceived() {
            Log.d(TAG, "onSessionPingReceived");
        }

        @Override
        public void onExitTimeout(JSONObject error) {
            Log.d(TAG, "onExitTimeout Error: " + error.toString());
        }

        @Override
        public void onInactiveTimeout(JSONObject error) {
            Log.d(TAG, "onExitTimeout Error: " + error.toString());
        }
    };

    encryptedAuthData = new JSONObject();
        try {
        encryptedAuthData.put("token", "a9b84026-4c07-4d8b-892c-f74ab2b135d0");
        encryptedAuthData.put("guest_user", false);
    } catch (Exception e) {
        //
    }

    wave2 = Wave2.getInstance(this);

    wave2.initialiseWave2(Wave2.environment.UAT, WAVE2_PARTNER_ID, encryptedAuthData, false, Wave2.session.INTERVAL_NONE, 20 * 1000, 10 * 1000, wave2Callback);

    private final PictureInPictureParams.Builder pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
    private CallbackContext callback = null;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals("enter")){
            Double width = args.getDouble(0);
            Double height = args.getDouble(1);
            this.enterPip(width, height, callbackContext);
            return true;
        } else if(action.equals("isPip")){
            this.isPip(callbackContext);
            return true;
        } else if(action.equals("onPipModeChanged")){
            if(callback == null){
                callback = callbackContext; //save global callback for later callbacks
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT); //send no result to execute the callbacks later
                pluginResult.setKeepCallback(true); // Keep callback
            }
            return true;
        } else if(action.equals("isPipModeSupported")){
            this.isPipModeSupported(callbackContext);
            return true;
        } else if(action.equals("opneAARFile")) {
            this.opneAARFile(callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        if(callback != null){
            try{
                if(this.cordova.getActivity().isInPictureInPictureMode()){
                    this.callbackFunction(true, "true");
                } else {
                    this.callbackFunction(true, "false");
                }
            } catch(Exception e){
                String stackTrace = Log.getStackTraceString(e);
                this.callbackFunction(false, stackTrace);
            }
        }
    }

    public void callbackFunction(boolean op, String str){
        if(op){
            PluginResult result = new PluginResult(PluginResult.Status.OK, str);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, str);
            result.setKeepCallback(true);
            callback.sendPluginResult(result);
        }
    }

    private void enterPip(Double width, Double height, CallbackContext callbackContext) {
        try{
            if(width != null && width > 0 && height != null && height > 0){
                Rational aspectRatio = new Rational(Integer.valueOf(width.intValue()), Integer.valueOf(height.intValue()));
                pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
                this.cordova.getActivity().enterPictureInPictureMode(pictureInPictureParamsBuilder.build());

                callbackContext.success("Scaled picture-in-picture mode started.");
            } else {
                this.cordova.getActivity().enterPictureInPictureMode();

                callbackContext.success("Default picture-in-picture mode started.");
            }
        } catch(Exception e){
            String stackTrace = Log.getStackTraceString(e);
            callbackContext.error(stackTrace);
        }             
    }

    public void opneAARFile(CallbackContext callbackContext) {
        try {
            wave2.startWave2(null);
            callbackContext.success("OPEN AAR FILE");
        } catch(Exception e){
            String stackTrace = Log.getStackTraceString(e);
            this.callbackFunction(false, stackTrace);
        }
    }

    public void isPip(CallbackContext callbackContext) {
        try{
            if(this.cordova.getActivity().isInPictureInPictureMode()){
                callbackContext.success("true");
            } else {
                callbackContext.success("false");
            }
        } catch(Exception e){
            String stackTrace = Log.getStackTraceString(e);
            callbackContext.error(stackTrace);
        }
    }

    private void isPipModeSupported(CallbackContext callbackContext) {
        try{
            boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O; //>= SDK 26 //Oreo

            if(supported){
                callbackContext.success("true");
            } else {
                callbackContext.success("false");
            }
        } catch(Exception e){
            String stackTrace = Log.getStackTraceString(e);
            callbackContext.error(stackTrace);
        }
    }
}
