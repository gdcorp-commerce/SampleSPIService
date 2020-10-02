package com.sample.spi;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

import co.poynt.os.model.EmvAidConfiguration;
import co.poynt.os.model.EmvCAPublicKey;
import co.poynt.os.model.EmvExceptionList;
import co.poynt.os.model.EmvRevocationList;
import co.poynt.os.model.Intents;
import co.poynt.os.services.v1.IPoyntConfigurationReadListener;
import co.poynt.os.services.v1.IPoyntConfigurationService;
import co.poynt.os.services.v2.IPoyntEmvApplicationReadListener;
import co.poynt.os.services.v2.IPoyntEmvCAPublicKeyReadListener;
import co.poynt.os.services.v2.IPoyntEmvExceptionListener;
import co.poynt.os.services.v2.IPoyntEmvRevocationListener;

public class EmvConfigsUpdateReceiver extends BroadcastReceiver {
    public static final String TAG = EmvConfigsUpdateReceiver.class.getSimpleName();
    private IPoyntConfigurationService poyntConfigurationService;
    private Intent mIntent;
    private Context mContext;
    private Handler loadingHandler;
    private AlertDialog alertDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        // when a new event is received - first try to establish connection with Poynt Configuration
        // Service if it hasn't already been made
        if (poyntConfigurationService == null) {
            this.mIntent = intent;
            context.bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_CONFIGURATION_SERVICE),
                    serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // if we already have the service connection - go ahead and process the event
            handleIntent(context, intent);
        }
    }

    /**
     * Handle the received event.
     * IMPORTANT: As you can note - there are several events that are broadcasted by the
     * Poynt Configuration Service - which events need to be handled really depends on your
     * implementation. The most important event is "poynt.intent.action.EMV_CONFIGS_UPDATED"
     * event which is broadcasted every time any of the EMV configs are updated. We recommend you
     * start with this event.
     *
     * @param context
     * @param intent
     */
    private void handleIntent(Context context, Intent intent) {
        if(poyntConfigurationService == null){
            Log.e(TAG, "Not connected to Poynt Configuration Service!");
        } else {
            Log.i(TAG, "Received intent: "+intent.getAction());
            if (loadingHandler == null) {
                loadingHandler = new Handler(Looper.getMainLooper());
            }
            if(Intents.ACTION_EMV_CONFIGS_UPDATED.equalsIgnoreCase(intent.getAction())){
                // always display an model dialog to prevent the merchant from interrupting the
                // config loading process
                displayConfigLoaderProgress(context);

                // For each card reader interface (CT/CL/MSR) fetch the Terminal and AID configs
                // and load them into your card reader using your existing SDK/services
                /**
                 * For CT interface
                 */
                // Terminal Config
                setTerminalConfiguration((byte)0x04, (byte)0x01);
                // AIDs for CT
                try {
                    poyntConfigurationService.getAIDConfigurationList((byte)0x04, new IPoyntEmvApplicationReadListener.Stub() {
                        @Override
                        public void onSuccess(List<EmvAidConfiguration> data) throws RemoteException {
                            Log.i(TAG, "Received AID List: (%d) (%s)" +data);
                            for(EmvAidConfiguration aidConfiguration: data){
                                Log.d(TAG, data.toString());
                                setAidConfiguration((byte)0x04, aidConfiguration);
                            }
                        }

                        @Override
                        public void onFailure() throws RemoteException {
                            Log.e(TAG, "Failed to retrieve AID Configuration for Contact");
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                /**
                 * CL interface
                 */
                // Terminal Config
                setTerminalConfiguration((byte)0x02, (byte)0x01);
                // AIDs for CL
                try {
                    poyntConfigurationService.getAIDConfigurationList((byte)0x02, new IPoyntEmvApplicationReadListener.Stub() {
                        @Override
                        public void onSuccess(List<EmvAidConfiguration> data) throws RemoteException {
                            Log.i(TAG, "Received AID List: (%d) (%s)" +data);
                            for(EmvAidConfiguration aidConfiguration: data){
                                setAidConfiguration((byte)0x02, aidConfiguration);
                            }
                        }

                        @Override
                        public void onFailure() throws RemoteException {
                            Log.e(TAG, "Failed to retrieve AID Configuration for CL");
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // EMV CA PUBLIC Keys - should be 03 since we use the same keys for both CT and CL
                setEMVCAPublicKeys((byte)0x03);
                // EMV CA Revocation List
                // EMV Exception list
                /**
                 * MSR
                 */
                setTerminalConfiguration((byte)0x01, (byte)0x01);
                // close the load progress dialog
                dismissConfigLoaderProgress();
            } else if (Intents.ACTION_DELETE_ALL_CONFIG.equalsIgnoreCase(intent.getAction())) {
                // TODO : Delete all local EMV configuration parameters from your card reader
            }
        }
    }

    /**
     * Fetches the latest Terminal configuration from Poynt Configuration service and loads into
     * your card reader.
     * @param cardInterface
     * @param mode
     */
    private void setTerminalConfiguration(byte cardInterface, byte mode) {
        try {
            // fetch latest terminal configuration from Poynt Configuration Service
            poyntConfigurationService.getTerminalConfiguration(cardInterface, new IPoyntConfigurationReadListener.Stub() {
                @Override
                public void onSuccess(byte[] data) throws RemoteException {
                    Log.v(TAG, "Received terminal configs.. "+new String(data));
                    // TODO: Load the fetched config data (TLVs) into your Card Reader
                }

                @Override
                public void onFailure() throws RemoteException {
                    Log.v(TAG, "Failed to get terminal config");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to load fetched AID configuration into your card reader
     * @param cardInterface
     * @param aidConfiguration
     */
    private void setAidConfiguration(byte cardInterface, EmvAidConfiguration aidConfiguration) {
        Log.i(TAG, "setAidConfiguration(%s) "+aidConfiguration.toString());
        // TODO: Load the fetched config data (TLVs) into your Card Reader
    }


    /**
     * Fetches the latest EMV CA Public keys for the given interface and loads into your Card Reader
     * @param cardInterface
     */
    private void setEMVCAPublicKeys(final byte cardInterface) {
        try {
            Log.d(TAG, "Fetching EMVCAPublicKeyList: cardInterface "+ cardInterface);
            // fetch latest EMV CA Public keys
            poyntConfigurationService.getEMVCAPublicKeyList(cardInterface, new IPoyntEmvCAPublicKeyReadListener.Stub() {
                @Override
                public void onSuccess(List<EmvCAPublicKey> keyList) throws RemoteException {
                    if (keyList != null && keyList.size() > 0) {
                        Log.i(TAG, "Retrieved EmvCAPublicKeys for cardInterface: " + cardInterface);
                        for (EmvCAPublicKey emvCAPublicKey : keyList) {
                            Log.v(TAG, "Received emvCAPublicKey.. "+emvCAPublicKey.toString());
                            // TODO : Load the emvCAPublic key into your card reader
                        }
                    }
                }

                @Override
                public void onFailure() throws RemoteException {
                    Log.i(TAG, "Failed to get updated ca public key");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to display load in progress dialog
     * @param context
     */
    private void displayConfigLoaderProgress(final Context context) {
        loadingHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                // TODO : Replace with any custom layout preferred
                View dialogView = inflater.inflate(R.layout.dialog_config_loader, null);
                dialogBuilder.setView(dialogView);
                alertDialog = dialogBuilder.create();
                alertDialog.setInverseBackgroundForced(true);
                alertDialog.setCancelable(false);
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                alertDialog.show();
            }
        });
    }

    /**
     * Method to close the config loader dialog
     */
    private void dismissConfigLoaderProgress() {
        loadingHandler.post(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
    }
    /**
     * Service Connection for Poynt Configuration Service
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            poyntConfigurationService = IPoyntConfigurationService.Stub.asInterface(iBinder);
            handleIntent(mContext, mIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.v(TAG, "Configuration service disconnected");
        }
    };
}
