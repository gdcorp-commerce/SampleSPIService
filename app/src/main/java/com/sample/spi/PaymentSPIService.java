package com.sample.spi;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.List;

import co.poynt.os.model.APDUData;
import co.poynt.os.model.ConnectionOptions;
import co.poynt.os.model.KernelType;
import co.poynt.os.services.v1.IPoyntCardPresenceListener;
import co.poynt.os.services.v1.IPoyntConfigurationUpdateListener;
import co.poynt.os.services.v1.IPoyntConnectToCardListener;
import co.poynt.os.services.v1.IPoyntDisconnectFromCardListener;
import co.poynt.os.services.v1.IPoyntExchangeAPDUListListener;
import co.poynt.os.services.v1.IPoyntKernelVersionListener;
import co.poynt.vendor.model.CardReaderRequest;
import co.poynt.vendor.model.DataEntrySkipReason;
import co.poynt.vendor.model.HostResponse;
import co.poynt.vendor.model.InvalidPANContinueOptions;
import co.poynt.vendor.services.v1.IPoyntPaymentSPI;
import co.poynt.vendor.services.v1.IPoyntPaymentSPICancelListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIStatusListener;

public class PaymentSPIService extends Service {
    public PaymentSPIService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPoyntPaymentSPI.Stub mBinder = new IPoyntPaymentSPI.Stub() {

        @Override
        public void connectToCard(ConnectionOptions connectionOptions, IPoyntConnectToCardListener iPoyntConnectToCardListener) throws RemoteException {

        }

        @Override
        public void disconnectFromCard(ConnectionOptions connectionOptions, IPoyntDisconnectFromCardListener iPoyntDisconnectFromCardListener) throws RemoteException {

        }

        @Override
        public void checkCardPresence(ConnectionOptions connectionOptions, IPoyntCardPresenceListener iPoyntCardPresenceListener) throws RemoteException {

        }

        @Override
        public void exchangeAPDUList(List<APDUData> list, IPoyntExchangeAPDUListListener iPoyntExchangeAPDUListListener) throws RemoteException {

        }

        @Override
        public void isCardReaderConnected(IPoyntPaymentSPIStatusListener iPoyntPaymentSPIStatusListener) throws RemoteException {

        }

        @Override
        public void startTransaction(CardReaderRequest cardReaderRequest, IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {

        }

        @Override
        public void cancelTransaction(IPoyntPaymentSPICancelListener iPoyntPaymentSPICancelListener) throws RemoteException {

        }

        @Override
        public void completeTransaction(HostResponse hostResponse, IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {

        }

        @Override
        public void continueTransaction(CardReaderRequest cardReaderRequest, IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {

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
        public void getKernelVersion(KernelType kernelType, IPoyntKernelVersionListener iPoyntKernelVersionListener) throws RemoteException {

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
        public void setRTC(String s, String s1, IPoyntConfigurationUpdateListener iPoyntConfigurationUpdateListener) throws RemoteException {

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
        public void collectManualEntryData(Bundle bundle, IPoyntPaymentSPIListener iPoyntPaymentSPIListener) throws RemoteException {

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
}
