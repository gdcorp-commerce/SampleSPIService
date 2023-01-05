package com.sample.diagnostics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.poynt.diagnostics.IDiagnosticsProviderService;
import com.poynt.diagnostics.IDiagnosticsServiceRunner;
import com.sample.spi.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import co.poynt.os.model.PoyntKeyInfo;
import co.poynt.os.model.PoyntKeyType;

public class SampleDiagnosticsService extends Service {

    private static final String TAG = "SampleDiagnostics";
    /** Contains the test configuration data. */
    private class TestConfig {
        public final String stepName;
        public final boolean successDependsOnUser;

        /**
         * @param stepName
         * @param successDependsOnUser
         */
        public TestConfig(String stepName, boolean successDependsOnUser) {
            this.stepName = stepName;
            this.successDependsOnUser = successDependsOnUser;
        }
    }
    private HashMap<String, TestConfig> mTests;

    private static final String CR = System.getProperty("line.separator");
    private static final String SEP = "----------------";

    private static final String DIAG_STEP_CARD_READER_CONNECTED = "card_reader_connected";
    private static final String DIAG_STEP_CARD_READER_KEYS = "card_reader_keys";
    private static final String DIAG_STEP_CARD_READER_SECURITY = "card_reader_security";
    private static final String DIAG_STEP_CARD_READER_CERTS = "card_reader_certs";
    private static final String DIAG_STEP_CARD_READER_INJECTION_SUMMARY =
            "card_reader_injection_summary";
    private static final String EXTRA_REPETITIONS = "param.repetitions";
    private static final String EXTRA_APDU_EXCHANGES = "param.apdu_exchanges";
    private static final String EXTRA_TIMEOUT = "param.timeout";

    private static final String DIAG_APP_BUZZER_TEST = "buzzer_test";
    private static final String DIAG_STEP_CARD_READER_RTC_TEST = "rtc_test";
    private static final String DIAG_APP_MSR_TEST = "msr_test";
    private static final String DIAG_APP_SMARTCARD_TEST = "emv_test";
    private static final String DIAG_APP_CONTACTLESS_TEST = "cl_test";
    private static final String DIAG_APP_OBJECT_SIGNED = "obj_signed_test";
    private static final byte TRACK_NOT_AVAILABLE = 0x00;
    private static final byte TRACK_OK = 0x01;
    private static final int COMBINED_TRACK_1_INDEX = 6;
    private static final int COMBINED_TRACK_2_INDEX = 7;
    private static final int COMBINED_TRACK_3_INDEX = 8;

    private Handler handler;
    private HandlerThread handlerThread;
    private String mRunningTest;
    private IDiagnosticsServiceRunner mRunner;

    public SampleDiagnosticsService() {
        mTests = new LinkedHashMap<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread = new HandlerThread("DiagnosticProviderThread");
        handlerThread.start();
        // never run handler on default main looper when using it for card reader
        handler = new Handler(handlerThread.getLooper());

        mTests.put(
                DIAG_STEP_CARD_READER_CONNECTED,
                new TestConfig(
                        getString(R.string.diagnostics_title_card_reader_connection), false));
        mTests.put(
                DIAG_STEP_CARD_READER_SECURITY,
                new TestConfig(
                        getString(R.string.diagnostics_title_card_reader_security), false));
        mTests.put(
                DIAG_STEP_CARD_READER_RTC_TEST,
                new TestConfig(getString(R.string.rtc_test_label), false));
        mTests.put(
                DIAG_STEP_CARD_READER_KEYS,
                new TestConfig(getString(R.string.diagnostics_title_card_reader_keys), false));
        mTests.put(
                DIAG_STEP_CARD_READER_INJECTION_SUMMARY,
                new TestConfig(getString(R.string.card_reader_injection_label), false));
        mTests.put(
                DIAG_APP_BUZZER_TEST,
                new TestConfig(getString(R.string.buzzer_test_label), true));
        mTests.put(
                DIAG_STEP_CARD_READER_CERTS,
                new TestConfig(getString(R.string.card_reader_certs_label), false));
        mTests.put(
                DIAG_APP_OBJECT_SIGNED,
                new TestConfig(getString(R.string.object_signing_test_label), false));
        mTests.put(
                DIAG_APP_MSR_TEST,
                new TestConfig(getString(R.string.magstripe_test_label), false));
        mTests.put(
                DIAG_APP_CONTACTLESS_TEST,
                new TestConfig(getString(R.string.contactless_test_label), false));
        mTests.put(
                DIAG_APP_SMARTCARD_TEST,
                new TestConfig(getString(R.string.emv_test_label), false));
        mRunningTest = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public final IDiagnosticsProviderService.Stub mBinder =
            new IDiagnosticsProviderService.Stub() {
                @Override
                public List<String> getDiagnosticStepIds() throws RemoteException {
                    List<String> stepIds = new ArrayList<>();
                    for (String id : mTests.keySet()) {
                        stepIds.add(id);
                    }
                    return stepIds;
                }

                @Override
                public String getDiagnosticStepName(String stepId) throws RemoteException {
                    TestConfig testConfig = mTests.get(stepId);
                    return (testConfig != null ? testConfig.stepName : null);
                }

                @Override
                public boolean setCallback(IDiagnosticsServiceRunner runner) throws RemoteException {
                    mRunner = runner;
                    return true;
                }

                @Override
                public boolean startStep(String stepId) throws RemoteException {
                    if (mRunner == null || stepId == null) {
                        return false;
                    }
                    switch (stepId) {
                        case DIAG_STEP_CARD_READER_CONNECTED:
                            return checkCardReaderConnection();
                        case DIAG_STEP_CARD_READER_SECURITY:
                            return checkCardReaderSecurity();
                        case DIAG_STEP_CARD_READER_KEYS:
                            return checkCardReaderKeys();
                        case DIAG_APP_BUZZER_TEST:
                            return checkBuzzer();
                        case DIAG_STEP_CARD_READER_RTC_TEST:
                            return checkRTC();
                        case DIAG_STEP_CARD_READER_CERTS:
                            return checkCertificates();
                        case DIAG_STEP_CARD_READER_INJECTION_SUMMARY:
                            return checkInjectionSummary();
                        case DIAG_APP_MSR_TEST:
                            return checkMsr();
                        case DIAG_APP_SMARTCARD_TEST:
                            return checkSmartCard();
                        case DIAG_APP_CONTACTLESS_TEST:
                            return checkContactless();
                        case DIAG_APP_OBJECT_SIGNED:
                            return checkObjectSigned();
                        default:
                            return false;
                    }
                }

                @Override
                public boolean cancelStep(String stepId) throws RemoteException {
                    return false;
                }

                @Override
                public boolean successDependsOnUser(String stepId) throws RemoteException {
                    TestConfig testConfig = mTests.get(stepId);
                    return (testConfig != null ? testConfig.successDependsOnUser : false);
                }

                private boolean checkObjectSigned() throws RemoteException {
                    Intent intent = new Intent();
                    mCountDownTimer.start();
                    mRunningTest = DIAG_APP_OBJECT_SIGNED;
                    intent.putExtra(EXTRA_TIMEOUT, "10");
                    // this is where you can check for the presense of device signing certificate
                    // sendAsyncCommand(Command.GET_DEVICE_SIGNING_CERT_SUMMARY, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_APP_OBJECT_SIGNED, getString(R.string.diagnostics_obj_sign_start));
                    return true;
                }

                private boolean checkContactless() throws RemoteException {
                    Intent intent = new Intent();
                    mCountDownTimer.start();
                    mRunningTest = DIAG_APP_CONTACTLESS_TEST;
                    intent.putExtra(EXTRA_TIMEOUT, "10");
                    // start CL test
                    // sendAsyncCommand(Command.CONTACTLESS_CARD_TEST, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_APP_CONTACTLESS_TEST,
                            getString(R.string.diagnostics_contactless_start_p5));
                    return true;
                }

                private boolean checkSmartCard() throws RemoteException {
                    Intent intent = new Intent();
                    mCountDownTimer.start();
                    mRunningTest = DIAG_APP_SMARTCARD_TEST;
                    intent.putExtra(EXTRA_TIMEOUT, "10");
                    // start CT test
                    // sendAsyncCommand(Command.SMARTCARD_TEST, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_APP_SMARTCARD_TEST, getString(R.string.diagnostics_emv_start));
                    return true;
                }

                private boolean checkMsr() throws RemoteException {
                    mCountDownTimer.start();
                    Intent intent = new Intent();
                    mRunningTest = DIAG_APP_MSR_TEST;
                    intent.putExtra(EXTRA_TIMEOUT, "3");
                    // start MSR test
                    // sendAsyncCommand(Command.MSR_TEST, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_APP_MSR_TEST, getString(R.string.diagnostics_msr_start));
                    return true;
                }

                private boolean checkCardReaderConnection() throws RemoteException {
                    // check for sdk instance
//                    if (sdkInstance) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_CONNECTED,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return true;
//                    }

                    // check if you can get hold of card reader instance
//                    PoyntReader reader = mCardReaderManager.getCardReader();
//                    if (reader == null || !reader.isReady()) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_CONNECTED,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return false;
//                    }

                    // when connected - return status along with FW version
                    String status =
                            getString(R.string.diagnostics_card_reader_connected)
                                    + System.lineSeparator()
                                    + getString(R.string.firmware_version)
                                    + getFirmwareBuildVersion();

                    mRunner.onDiagnosticStepSuccess(DIAG_STEP_CARD_READER_CONNECTED, status);
                    return true;
                }

                private String getFirmwareBuildVersion() {
                    return "return actual fw version";
                }

                private boolean checkCardReaderSecurity() throws RemoteException {
                    // always check for card reader instance and connection
//                    if (mCardReaderManager == null) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_SECURITY,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return true;
//                    }
//                    if (reader == null || !reader.isReady()) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_SECURITY,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return true;
//                    }
//
//                    boolean cardReaderSecure = true;
//                    StringBuilder outBuffer = new StringBuilder();
//                    // Check PCI security status
//                    Boolean pciSecurityEnabled = reader.isPCISecurityEnabled();
//                    if (pciSecurityEnabled == null) {
//                        cardReaderSecure = false;
//                        outBuffer.append(
//                                getString(
//                                        R.string
//                                                .diagnostics_card_reader_security_could_not_pci_security_status));
//                    } else if (!pciSecurityEnabled) {
//                        outBuffer.append(
//                                getString(
//                                        R.string
//                                                .diagnostics_card_reader_security_pci_security_not_enabled));
//                        cardReaderSecure = false;
//                    }
//
//                    // Check tamper status
//                    Boolean tamperDetected = reader.isTamperDetected();
//                    if (tamperDetected == null) {
//                        cardReaderSecure = false;
//                        outBuffer.append(
//                                getString(
//                                        R.string
//                                                .diagnostics_card_reader_security_could_not_get_tamper_status));
//                    } else if (tamperDetected) {
//                        cardReaderSecure = false;
//                        outBuffer.append(
//                                getString(
//                                        R.string.diagnostics_card_reader_security_tamper_detected));
//                    }
//
//                    // Check 24hr/boot check
//                    Boolean check24Flag = reader.has24CheckFailed();
//                    if (check24Flag == null) {
//                        cardReaderSecure = false;
//                        outBuffer.append(
//                                getString(
//                                        R.string
//                                                .diagnostics_card_reader_security_could_not_get_24hr_check_status));
//                    } else if (check24Flag) {
//                        cardReaderSecure = false;
//                        outBuffer.append(
//                                getString(
//                                        R.string
//                                                .diagnostics_card_reader_security_24hr_check_failed));
//                    }
//
//                    if (!cardReaderSecure) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_SECURITY, outBuffer.toString());
//                        return false;
//                    }
//
//                    outBuffer.append(
//                            getString(R.string.diagnostics_card_reader_security_no_tamper));
//                    outBuffer.append(CR);
//                    outBuffer.append(
//                            mCardReaderManager.getCardReader().getPciSecurityStatus().name());
//                    mRunner.onDiagnosticStepSuccess(
//                            DIAG_STEP_CARD_READER_SECURITY, outBuffer.toString());
                    return true;
                }

                private boolean checkCertificates() throws RemoteException {
                    mCountDownTimer.start();
                    mRunningTest = DIAG_STEP_CARD_READER_CERTS;
                    Intent intent = new Intent();
//                    sendAsyncCommand(Command.GET_TRUSTED_CERTS_SUMMARY, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_STEP_CARD_READER_CERTS,
                            getString(R.string.diagnostics_certs_start));

                    return true;
                }

                private boolean checkInjectionSummary() throws RemoteException {
                    mCountDownTimer.start();
                    mRunningTest = DIAG_STEP_CARD_READER_INJECTION_SUMMARY;
                    Intent intent = new Intent();
//                    sendAsyncCommand(Command.GET_KEY_INJECTION_SUMMARY, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_STEP_CARD_READER_INJECTION_SUMMARY,
                            getString(R.string.diagnostics_injection_start));

                    return true;
                }

                private boolean checkRTC() throws RemoteException {
                    mCountDownTimer.start();
                    mRunningTest = DIAG_STEP_CARD_READER_RTC_TEST;
                    Intent intent = new Intent();
//                    sendAsyncCommand(Command.GET_RTC, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_STEP_CARD_READER_RTC_TEST,
                            getString(R.string.diagnostics_rtc_start));
                    return true;
                }

                private boolean checkBuzzer() throws RemoteException {
                    mCountDownTimer.start();
                    mRunningTest = DIAG_APP_BUZZER_TEST;
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_REPETITIONS, "1");
//                    sendAsyncCommand(Command.BUZZER_TEST, intent);
                    mRunner.onDiagnosticsStepDetailsUpdated(
                            DIAG_APP_BUZZER_TEST, getString(R.string.diagnostics_buzzer_start));
                    return true;
                }
                private boolean checkCardReaderKeys() throws RemoteException {

//                    if (mCardReaderManager == null) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_KEYS,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return true;
//                    }
//                    PoyntReader reader = mCardReaderManager.getCardReader();
//                    if (reader == null || !reader.isReady()) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_KEYS,
//                                getString(R.string.diagnostics_card_reader_not_connected));
//                        return true;
//                    }
//                    Boolean keysInjected = reader.areKeysInjected();
//                    if (keysInjected == null) {
//                        mRunner.onDiagnosticStepFailed(
//                                DIAG_STEP_CARD_READER_KEYS,
//                                getString(R.string.diagnostics_could_not_get_key_status));
//                        return true;
//                    }
//                    List<PoyntKeyInfo> keyInfos = mCardReaderManager.getKeyInfo();
//                    String keyLostStatus =
//                            PoyntSystemProperties.get(
//                                    POYNT_SYSTEM_PROP_FIRMWARE_KEY_LOST,
//                                    POYNT_SYSTEM_PROP_FIRMWARE_KEY_LOST_DEFAULT);
//                    boolean hasLostKeys =
//                            !Strings.equals(
//                                    keyLostStatus, POYNT_SYSTEM_PROP_FIRMWARE_KEY_LOST_DEFAULT);
//                    String output =
//                            getKeyInfoStatusString(
//                                    DiagnosticsProviderService.this,
//                                    hasLostKeys,
//                                    keysInjected,
//                                    keyInfos);
//                    String keyNames = getKeyNames(mCardReaderManager);
//                    if (keyNames != null) {
//                        String s =
//                                SEP
//                                        + " "
//                                        + getString(R.string.diagnostics_key_names)
//                                        + " "
//                                        + SEP
//                                        + CR
//                                        + keyNames;
//                        output += s;
//                    }
//
//                    if (keysInjected) {
//                        mRunner.onDiagnosticStepSuccess(DIAG_STEP_CARD_READER_KEYS, output);
//                        return true;
//                    }
//                    if (hasLostKeys) {
//                        mRunner.onDiagnosticStepFailed(DIAG_STEP_CARD_READER_KEYS, output);
//                        return true;
//                    }
//
//                    mRunner.onDiagnosticStepWarning(DIAG_STEP_CARD_READER_KEYS, output);
                    return true;
                }
                private String getKeyInfoStatusString(
                        Context context,
                        boolean hasLostKeys,
                        boolean areKeysInject,
                        List<PoyntKeyInfo> keyInfos) {
                    String found_str = context.getResources().getString(R.string.found);
                    String not_found_str = context.getResources().getString(R.string.not_found);

                    StringBuilder sb = new StringBuilder();

                    if (hasLostKeys && !areKeysInject) {
                        String keyLostString = context.getResources().getString(R.string.keys_lost);
                        sb.append(keyLostString).append(CR);
                    }

                    sb.append(
                            (areKeysInject)
                                    ? context.getResources().getString(R.string.diagnostics_key_status_good)
                                    : context.getResources().getString(R.string.diagnostics_key_status_bad));

                    boolean tspkFlag = false;
                    if (keyInfos != null && keyInfos.size() > 0) {
                        ArrayList<Integer> keyslots = new ArrayList<>();
                        for (int i = 0; i < keyInfos.size(); i++) {
                            PoyntKeyInfo keyInfo = keyInfos.get(i);
                            if (keyInfo.isKeyPresent()) {
                                int slot = keyInfo.getSlotNumber();
                                if (keyInfo.getKeyType() == PoyntKeyType.TRUSTED_SRC_PUBLIC_KEY) {
                                    if (keyInfo.isKeyPresent()) {
                                        tspkFlag = true;
                                    }
                                } else {
                                    if (slot > 10) {
                                        slot -= 1;
                                    }
                                    keyslots.add(slot);
                                }
                            }
                        }

                        sb.append(" ").append(Arrays.toString(keyslots.toArray())).append(CR);
                        String tspkString =
                                context.getResources()
                                        .getString(
                                                R.string.key_tspk_found,
                                                (tspkFlag ? found_str : not_found_str));
                        sb.append(tspkString).append(CR);

                        for (PoyntKeyInfo info : keyInfos) {
                            int slot = info.getSlotNumber();
                            if (info.getKeyType() == PoyntKeyType.TRUSTED_SRC_PUBLIC_KEY) {
                                continue;
                            }
                            if (!info.isKeyPresent() && !info.isKEKPresent()) {
                                continue;
                            }
                            if (info.getSlotNumber() > 10) {
                                slot -= 1;
                            }
                            sb.append(SEP).append("  slot:").append(slot).append(" ").append(SEP).append(CR);
                            if (info.getKeyType() != null) {
                                sb.append("type:").append(info.getKeyType().getName()).append(CR);
                            } else {
                                sb.append("type: Unknown").append(CR);
                            }
                            sb.append("presence:")
                                    .append(info.isKeyPresent())
                                    .append(" end_of_life:")
                                    .append(info.isKeyEndOfLife())
                                    .append(CR);
                            sb.append("kek_presence:")
                                    .append(info.isKEKPresent())
                                    .append(" kek_disabled:")
                                    .append(info.isKEKDisabled())
                                    .append(CR);
                            sb.append(String.format("raw data:%x", info.getRawData())).append(CR);
                        }
                    }
                    return sb.toString();
                }
            };

    CountDownTimer mCountDownTimer =
            new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long l) {
                    Log.d(TAG, "diagnostics countdown timer: " + l);
                }

                @Override
                public void onFinish() {
                    final String testName = mRunningTest;
                    if (mRunner != null && testName != null) {
                        try {
                            mRunner.onDiagnosticStepWarning(
                                    testName, getString(R.string.diagnostic_step_timed_out));
                            handler.postDelayed(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            handleCountDownTimerExpirationFor(testName);
                                        }
                                    }, 1000);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        mRunningTest = null;
                    }
                }
            };

    /**
     * Handle the timer expiration for a particular test
     * This is where you can clean up any card reader interactions, etc.
     * @param mRunningTest test that expired
     */
    private void handleCountDownTimerExpirationFor(String mRunningTest) {

        switch (mRunningTest) {
            default:
                break;
        }
    }
}