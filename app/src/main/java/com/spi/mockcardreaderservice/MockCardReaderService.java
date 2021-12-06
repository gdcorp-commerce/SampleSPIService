package com.spi.mockcardreaderservice;

import static com.spi.mockcardreaderservice.util.CardUtil.DISABLE_PLAYBACK;
import static com.spi.mockcardreaderservice.util.CardUtil.DISABLE_PLAYBACK_FOR_CURRENT_TXN;
import static com.spi.mockcardreaderservice.util.CardUtil.ENABLE_PLAYBACK;
import static com.spi.mockcardreaderservice.util.CardUtil.PLAYBACK_CARD_TYPE;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.spi.mockcardreaderservice.builder.ApplicationListBuilder;
import com.spi.mockcardreaderservice.builder.CardReaderResponseBuilder;
import com.spi.mockcardreaderservice.util.CardUtil;
import com.spi.mockcardreaderservice.util.MockCardreaderHelper;
import com.spi.mockcardreaderservice.util.MockResponseData;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.poynt.cardreader.ApplicationItem;
import co.poynt.cardreader.CardInterfaceType;
import co.poynt.cardreader.CardScheme;
import co.poynt.os.model.APDUData;
import co.poynt.os.model.ConnectionOptions;
import co.poynt.os.model.KernelType;
import co.poynt.os.services.v1.IPoyntCardPresenceListener;
import co.poynt.os.services.v1.IPoyntConfigurationUpdateListener;
import co.poynt.os.services.v1.IPoyntConnectToCardListener;
import co.poynt.os.services.v1.IPoyntDisconnectFromCardListener;
import co.poynt.os.services.v1.IPoyntExchangeAPDUListListener;
import co.poynt.os.services.v1.IPoyntKernelVersionListener;
import co.poynt.os.util.ByteUtils;
import co.poynt.vendor.model.CardReaderEvent;
import co.poynt.vendor.model.CardReaderEventName;
import co.poynt.vendor.model.CardReaderRequest;
import co.poynt.vendor.model.CardReaderResponse;
import co.poynt.vendor.model.DataEntrySkipReason;
import co.poynt.vendor.model.HostResponse;
import co.poynt.vendor.model.InvalidPANContinueOptions;
import co.poynt.vendor.services.v1.IPoyntPaymentSPI;
import co.poynt.vendor.services.v1.IPoyntPaymentSPICancelListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIStatusListener;

// This currently mocks cardreader responses for CT/CL/MSR transactions
// with Visa, Amex, Mastercard and Discover brands
// Todo: Also mock cardreader notifications
public class MockCardReaderService extends Service {
    private static final String TIMEOUT = "TIMEOUT";
    private final String TAG = "MockCardReaderService";
    private static final String SUCCESS = "SUCCESS";
    private static final String ONLINE_AUTHZ_REQ = "ONLINE_AUTHZ_REQ";
    private static final String STOP_AFTER_READ_RECORDS = "STOP_AFTER_READ_RECORDS";
    private static final String APP_SELECTION_REQUIRED = "APP_SELECTION_REQUIRED";
    private static final String APP_SELECTION_RETRY_REQUIRED = "APP_SELECTION_RETRY_REQUIRED";

    int index = 0;
    long transactionAmount;

    public MockCardReaderService() {

    }

    MockCardreaderHelper mockCardreaderHelper = null;
    List<MockResponseData> mockResponseDataList;
    MockResponseData mockResponseData = null;
    List<MockResponseData.ResponseData> responseDataList = null;

    SharedPreferences mPrefs;

    @Override
    public IBinder onBind(Intent intent) {
        mockCardreaderHelper = new MockCardreaderHelper(getApplicationContext());
        mockResponseDataList = mockCardreaderHelper.loadResponseJsonFileFromAssets();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return mBinder;
    }

    private final IPoyntPaymentSPI.Stub mBinder = new IPoyntPaymentSPI.Stub() {

        @Override
        public void connectToCard(ConnectionOptions connectionOptions,
                                  IPoyntConnectToCardListener iPoyntConnectToCardListener) throws RemoteException {

        }

        @Override
        public void disconnectFromCard(ConnectionOptions connectionOptions,
                                       IPoyntDisconnectFromCardListener iPoyntDisconnectFromCardListener) throws RemoteException {

        }

        @Override
        public void checkCardPresence(ConnectionOptions connectionOptions,
                                      IPoyntCardPresenceListener iPoyntCardPresenceListener) throws RemoteException {

        }

        @Override
        public void exchangeAPDUList(List<APDUData> list,
                                     IPoyntExchangeAPDUListListener iPoyntExchangeAPDUListListener) throws RemoteException {

        }

        @Override
        public void isCardReaderConnected(IPoyntPaymentSPIStatusListener iPoyntPaymentSPIStatusListener) throws RemoteException {
            iPoyntPaymentSPIStatusListener.connected();
        }

        @Override
        public void startTransaction(CardReaderRequest cardReaderRequest,
                                     IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {
            // DO-6441 Enable/Disable playback mode check
            if (checkDisablePlaybackForCurrentTransaction()) {
                // Disable playback for this transaction only, by just returning the event and
                // not playing back the responses
                // Also enable the flag back here
                resetDisablePlaybackForCurrentTransaction();
                CardReaderEvent event = CardUtil.getEvent(cardReaderRequest.getCardInterfaceTypes());
                iPoyntPaymentSPIListener.onEvent(event);

                // Don't do anything for current transaction
                return;
            }

            if (!checkPlaybackEnabled()) {
                // Disabled playback until it is enabled again
                CardReaderEvent event = CardUtil.getEvent(cardReaderRequest.getCardInterfaceTypes());
                iPoyntPaymentSPIListener.onEvent(event);

                return;
            }

            // Save transaction amount for later. Needed to update the mock data with this amount
            // before we go online
            transactionAmount = cardReaderRequest.getAmount();

            int value = (int) (transactionAmount % 100);
            index = 0;

            // based on amount entered, get the respective scheme
            Log.i(TAG, "Transaction Amount entered" + Long.toString(cardReaderRequest.getAmount()));
            Log.i(TAG, "Minor decimal part of the transaction amount value" + Integer.toString(value));

            // DO-6472 Playback a specific card type
            String cardType = getPlaybackCardType();
            if (!("NONE".equals(cardType))) {
                // get response data for a particular card brand based on the entered value
                mockResponseData = mockCardreaderHelper.findMockResponseData(cardType);
            } else {
               // value = getValueBasedOnInterface(cardReaderRequest.getCardInterfaceTypes(), value);
                mockResponseData = mockCardreaderHelper.getMockResponseData(value);
            }

            // get all the response packets
            responseDataList = mockResponseData.getResponseData();

            MockResponseData.ResponseData data = null;
            // Todo: Improve iterating technique if possible
            // get the response packet that needs to be used for start transaction
            data = responseDataList.get(index);
            index++;

            Log.i(TAG, "response packet to mock  :" + data.getPacket());

            if (SUCCESS.equals(data.getStatus())) {
                // Replace mock transaction amount with the amount entered
                data = replaceMockAmount(data);
                Log.i(TAG, "Updated packet with transaction amount entered :" + data.getPacket());

                returnResponse(CardReaderResponse.Status.SUCCESS,
                               ByteUtils.hexStringToByteArray(data.getPacket()),
                               iPoyntPaymentSPIListener);
            } else if (APP_SELECTION_REQUIRED.equals(data.getStatus()) ||
                       APP_SELECTION_RETRY_REQUIRED.equals(data.getStatus())) {
                Log.i(TAG, "Status received: " + data.getStatus());
                ApplicationListBuilder applicationListBuilder = new ApplicationListBuilder();
                List<ApplicationItem> applicationItems =
                        applicationListBuilder.getApplicationList(ByteUtils.hexStringToByteArray(data.getPacket()));
                applicationListBuilder.setRetry(data.getStatus() == APP_SELECTION_RETRY_REQUIRED);
                iPoyntPaymentSPIListener.onAppSelectionRequired(applicationItems,
                        applicationListBuilder.isCardHolderConfirmationSupported(),
                        applicationListBuilder.isRetry(),
                        applicationListBuilder.isContactless(),
                        applicationListBuilder.isConsumerDefinedApplicationPreference());
            } else if (ONLINE_AUTHZ_REQ.equals(data.getStatus())) {
                // Replace mock transaction amount with the amount entered
                data = replaceMockAmount(data);
                Log.i(TAG, "Updated packet with transaction amount entered :" + data.getPacket());

                CardReaderResponseBuilder cardReaderResponseBuilder = new CardReaderResponseBuilder();
                cardReaderResponseBuilder.process(CardReaderResponse.Status.ONLINE_AUTH_REQUIRED,
                        ByteUtils.hexStringToByteArray(data.getPacket()));
                CardReaderResponse cardReaderResponse = cardReaderResponseBuilder.getCardReaderResponse();
                Log.i(TAG, "Calling online Auth required in startTransaction");
                iPoyntPaymentSPIListener.onOnlineAuthorizationRequired(cardReaderResponse);
            } else {
                Log.e(TAG, "Something went wrong in startTransaction");
            }
        }

        private void returnResponse(CardReaderResponse.Status status, byte[] data,
                                    IPoyntPaymentSPIListener callback) throws RemoteException {
            // Todo Support GET_MANUAL_CARD_DATA
            //if (isTransactionInProgress() || poyntReaderResponse.getCommand() == GET_MANUAL_CARD_DATA) {
                CardReaderResponseBuilder cardReaderResponseBuilder = new CardReaderResponseBuilder();
                cardReaderResponseBuilder.process(status, data);
                CardReaderResponse cardReaderResponse = cardReaderResponseBuilder.getCardReaderResponse();
                Log.d(TAG, "Returning cardReaderResponse ");
                callback.onComplete(cardReaderResponse);
            //}
        }

        @Override
        public void cancelTransaction(IPoyntPaymentSPICancelListener iPoyntPaymentSPICancelListener) throws RemoteException {

        }

        @Override
        public void completeTransaction(HostResponse hostResponse,
                                        IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {
            MockResponseData.ResponseData responseData = responseDataList.get(index);

            // Replace mock transaction amount with the amount entered
            responseData = replaceMockAmount(responseData);
            Log.i(TAG, "Updated packet with transaction amount entered :" +
                    responseData.getPacket());

            returnResponse(CardReaderResponse.Status.SUCCESS,
                           ByteUtils.hexStringToByteArray(responseData.getPacket()),
                           iPoyntPaymentSPIListener);

            //sendBroadcast(new Intent(CardReaderEventName.ACTION_CARD_REMOVED));

            //Log.d(TAG, "CARD_REMOVED Broadcast event sent");
        }

        @Override
        public void continueTransaction(CardReaderRequest cardReaderRequest,
                                        IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {
            MockResponseData.ResponseData data = responseDataList.get(index);
            index++;

            if (APP_SELECTION_REQUIRED.equals(data.getStatus()) ||
                                            APP_SELECTION_RETRY_REQUIRED.equals(data.getStatus())) {
                Log.i(TAG, "Status received: " + data.getStatus());
                ApplicationListBuilder applicationListBuilder = new ApplicationListBuilder();
                List<ApplicationItem> applicationItems =
                        applicationListBuilder.getApplicationList(ByteUtils.hexStringToByteArray(data.getPacket()));
                applicationListBuilder.setRetry(data.getStatus() == APP_SELECTION_RETRY_REQUIRED);
                iPoyntPaymentSPIListener.onAppSelectionRequired(applicationItems,
                        applicationListBuilder.isCardHolderConfirmationSupported(),
                        applicationListBuilder.isRetry(),
                        applicationListBuilder.isContactless(),
                        applicationListBuilder.isConsumerDefinedApplicationPreference());
            } else if (TIMEOUT.equals(data.getStatus()) ) {
                Log.d(TAG, "Timed out waiting for card!");
                iPoyntPaymentSPIListener.onCardEntryTimeoout();
            } else if (STOP_AFTER_READ_RECORDS.equals(data.getStatus())) {
                // Replace mock transaction amount with the amount entered, Fix for DO-6444
                transactionAmount = cardReaderRequest.getAmount();
                data = replaceMockAmount(data);
                CardReaderResponseBuilder cardReaderResponseBuilder = new CardReaderResponseBuilder();
                cardReaderResponseBuilder.process(CardReaderResponse.Status.STOP_AFTER_READ_RECORDS,
                                                  ByteUtils.hexStringToByteArray(data.getPacket()));
                CardReaderResponse cardReaderResponse = cardReaderResponseBuilder.getCardReaderResponse();
                Log.i(TAG, "Calling onCheckCard in continueTransaction");
                iPoyntPaymentSPIListener.onCheckCard(cardReaderResponse);
            } else if (ONLINE_AUTHZ_REQ.equals(data.getStatus())) {
                // Replace mock transaction amount with the amount entered, Fix for DO-6444
                transactionAmount = cardReaderRequest.getAmount();
                data = replaceMockAmount(data);
                Log.i(TAG, "Updated packet with transaction amount entered :" + data.getPacket());

                // Todo: Clean up getting cardReaderResponse to use a method
                CardReaderResponseBuilder cardReaderResponseBuilder = new CardReaderResponseBuilder();
                cardReaderResponseBuilder.process(CardReaderResponse.Status.ONLINE_AUTH_REQUIRED,
                        ByteUtils.hexStringToByteArray(data.getPacket()));
                CardReaderResponse cardReaderResponse = cardReaderResponseBuilder.getCardReaderResponse();
                Log.i(TAG, "Calling online Auth required");
                iPoyntPaymentSPIListener.onOnlineAuthorizationRequired(cardReaderResponse);
            } else if (SUCCESS.equals(data.getStatus())) {
                // Replace mock transaction amount with the amount entered, Fix for DO-6444
                transactionAmount = cardReaderRequest.getAmount();
                data = replaceMockAmount(data);
                Log.i(TAG, "Updated packet with transaction amount entered :" + data.getPacket());

                CardReaderResponseBuilder cardReaderResponseBuilder = new CardReaderResponseBuilder();
                cardReaderResponseBuilder.process(CardReaderResponse.Status.SUCCESS,
                                                    ByteUtils.hexStringToByteArray(data.getPacket()));
                CardReaderResponse cardReaderResponse = cardReaderResponseBuilder.getCardReaderResponse();
                Log.d(TAG, "Returning cardReaderResponse: (%s)" + cardReaderResponse);
                returnResponse(CardReaderResponse.Status.SUCCESS,
                               ByteUtils.hexStringToByteArray(data.getPacket()),
                               iPoyntPaymentSPIListener);
            } else {
                Log.e(TAG, "Something went wrong in continueTransaction");
            }
        }

        @Override
        public void sendNoCVVReason(DataEntrySkipReason dataEntrySkipReason) throws RemoteException {

        }

        @Override
        public void sendInvalidPANContinue(InvalidPANContinueOptions invalidPANContinueOptions) throws RemoteException {

        }

        @Override
        public void endTransaction() throws RemoteException {

        }

        @Override
        public void getKernelVersion(KernelType kernelType,
                                     IPoyntKernelVersionListener iPoyntKernelVersionListener) throws RemoteException {

        }

        @Override
        public boolean isDeviceTampered() throws RemoteException {
            return false;
        }

        @Override
        public boolean isDeviceObjectSigned() throws RemoteException {
            return false;
        }

        @Override
        public boolean isDevicePCIEnabled() throws RemoteException {
            return false;
        }

        @Override
        public boolean isKeyInjected() throws RemoteException {
            return false;
        }

        @Override
        public void setRTC(String s, String s1,
                           IPoyntConfigurationUpdateListener iPoyntConfigurationUpdateListener) throws RemoteException {

        }

        @Override
        public String getRTC() throws RemoteException {
            return null;
        }

        @Override
        public String getConfigurationVersion() throws RemoteException {
            return null;
        }

        @Override
        public String getFirmwareVersion() throws RemoteException {
            return null;
        }

        @Override
        public String getFirmwareComponentVersion() throws RemoteException {
            return null;
        }

        @Override
        public void collectManualEntryData(Bundle bundle,
                                           IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {

        }

        @Override
        public boolean playBuzzer() throws RemoteException {
            return false;
        }

        @Override
        public boolean setAPDUMode(int i) throws RemoteException {
            return false;
        }

        @Override
        public boolean setAPDUPlaybackValidationType(int i) throws RemoteException {
            return false;
        }

        @Override
        public int getAPDUMode() throws RemoteException {
            return 0;
        }

        @Override
        public int getPlaybackValidationType() throws RemoteException {
            return 0;
        }

        @Override
        public boolean setAPDUData(String s) throws RemoteException {
            return false;
        }

        @Override
        public String exportAPDUData() throws RemoteException {
            return null;
        }

        @Override
        public boolean setAPDUParams(Bundle bundle) throws RemoteException {
            return false;
        }

        @Override
        public boolean setAutoPin(String s) throws RemoteException {
            return false;
        }
    };

    private MockResponseData.ResponseData replaceMockAmount(MockResponseData.ResponseData responseData) {
        // Replace transaction amount tag 9F02 value with the amount entered
        int transactionAmountIndex = responseData.getPacket().indexOf("9F02");
        Log.i(TAG, "transactionAmountIndex :" + Integer.toString(transactionAmountIndex));

        if (transactionAmountIndex > 1) {
            String mockAmountStr = responseData.getPacket().substring(transactionAmountIndex + 6,
                    transactionAmountIndex + 18);
            String grossAmountStr = StringUtils.leftPad(String.valueOf(transactionAmount), 12, "0");
            String data = responseData.getPacket().replace(mockAmountStr, grossAmountStr);
            responseData.setPacket(data);

            Log.i(TAG, "mock amount :" + mockAmountStr + " replaced with :" + grossAmountStr);
            Log.i(TAG, "updated responseData packet" + responseData.getPacket());
        }

        return responseData;
    }

    String getStringFromFile(File file) {
        String result = null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                result = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean checkDisablePlaybackForCurrentTransaction() {
        return mPrefs.getBoolean(DISABLE_PLAYBACK_FOR_CURRENT_TXN, false);
    }

    private void resetDisablePlaybackForCurrentTransaction() {
        SharedPreferences.Editor editPrefs = mPrefs.edit();
        editPrefs.putBoolean(DISABLE_PLAYBACK_FOR_CURRENT_TXN, false);
        editPrefs.putBoolean(ENABLE_PLAYBACK, true);
        editPrefs.commit();
    }

    private boolean checkPlaybackEnabled() {
        // check ENABLE_PLAYBACK shared pref
        return mPrefs.getBoolean(ENABLE_PLAYBACK, true);
    }

    private String getPlaybackCardType() {
        // get PLAYBACK_CARD_TYPE shared pref to play back data for a specific card
        return mPrefs.getString(PLAYBACK_CARD_TYPE, "NONE");
    }

    private int getValueBasedOnInterface(List<CardInterfaceType> types, int value) {
        int updatedValue = value;
        byte cardInterface = (byte) 0x00;
        for (CardInterfaceType cardInterfaceType : types) {
            switch (cardInterfaceType) {
                case MSR:
                    cardInterface = (byte) (cardInterface | 0x01);
                    break;
                case CT:
                    cardInterface = (byte) (cardInterface | 0x04);
                    break;
                case CL:
                    cardInterface = (byte) (cardInterface | 0x02);
                    break;
            }
        }

        if (cardInterface == 0x01) {
            updatedValue = MockCardreaderHelper.getValueFromCardScheme("VISA_MAGSTRIPE");
        } else if (cardInterface == 0x04) {
            updatedValue = MockCardreaderHelper.getValueFromCardScheme("VISA_CONTACT");
        }

        return updatedValue;
    }

}