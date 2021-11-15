package com.spi.mockcardreaderservice;

import static com.spi.mockcardreaderservice.util.CardUtil.DISABLE_PLAYBACK;
import static com.spi.mockcardreaderservice.util.CardUtil.DISABLE_PLAYBACK_FOR_CURRENT_TXN;
import static com.spi.mockcardreaderservice.util.CardUtil.ENABLE_PLAYBACK;
import static com.spi.mockcardreaderservice.util.CardUtil.PLAYBACK_CARD_TYPE;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 */
public class MockCardReaderIntentService extends IntentService {
    private static final String TAG = "CardReaderIntentService";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_EMU = "action.emu";

    private static final String EXTRA_PARAM1 = "param.command";
    private static final String EXTRA_PARAM2 = "param.data";

    SharedPreferences mPrefs;

    public MockCardReaderIntentService() {
        super("MockCardReaderIntentService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EMU.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else {
                Log.i(TAG, "Action not support: " + action);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        if (ENABLE_PLAYBACK.equals(param1)) {
            // enable playback for all transactions
            SharedPreferences.Editor editPrefs = mPrefs.edit();
            editPrefs.putBoolean(ENABLE_PLAYBACK, true);
            editPrefs.commit();
        } else if (DISABLE_PLAYBACK.equals(param1)) {
            if (("00").equals(param2)) {
                // disable enable_playback for all transactions
                SharedPreferences.Editor editPrefs = mPrefs.edit();
                editPrefs.putBoolean(ENABLE_PLAYBACK, false);
                editPrefs.commit();
            } else if ("01".equals(param2)) {
                SharedPreferences.Editor editPrefs = mPrefs.edit();
                editPrefs.putBoolean(DISABLE_PLAYBACK_FOR_CURRENT_TXN, true);
                editPrefs.commit();
            }
        } else if (PLAYBACK_CARD_TYPE.equals(param1)) {
            SharedPreferences.Editor editPrefs = mPrefs.edit();
            editPrefs.putString(PLAYBACK_CARD_TYPE, param2);
            editPrefs.commit();
        }
    }
}