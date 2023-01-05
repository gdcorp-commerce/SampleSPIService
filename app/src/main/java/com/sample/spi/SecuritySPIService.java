package com.sample.spi;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import co.poynt.os.model.DukptData;
import co.poynt.os.services.v1.IPoyntBancomantTrustedCertsSummaryListener;
import co.poynt.os.services.v1.IPoyntDataEncryptionListener;
import co.poynt.os.services.v1.IPoyntDeleteAcquirerConfigKeysListener;
import co.poynt.os.services.v1.IPoyntKeyIndicesValidationListener;
import co.poynt.os.services.v1.IPoyntKeyUpdateListener;
import co.poynt.os.services.v1.IPoyntMutualAuthenticationListener;
import co.poynt.os.services.v1.IPoyntMutualAuthenticationVerificationListener;
import co.poynt.os.services.v1.IPoyntSaveDataForFinancialServiceMsgListener;
import co.poynt.os.services.v1.IPoyntSelectMessageProcessingKeyListener;
import co.poynt.os.services.v1.IPoyntServiceFinMessageDecryptionListener;
import co.poynt.os.services.v1.IPoyntServiceFinMessageEncryptionListener;
import co.poynt.os.services.v1.IPoyntServiceMessageDecryptionListener;
import co.poynt.os.services.v1.IPoyntServiceMessageEncryptionListener;
import co.poynt.vendor.services.v1.IPoyntCardHashStatusListener;
import co.poynt.vendor.services.v1.IPoyntDeviceCertificateListener;
import co.poynt.vendor.services.v1.IPoyntDeviceCertificateUpdateListener;
import co.poynt.vendor.services.v1.IPoyntDeviceCertificatesSummaryListener;
import co.poynt.vendor.services.v1.IPoyntPCIKeyNamesListener;
import co.poynt.vendor.services.v1.IPoyntPCIKeyStatusListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSecuritySPI;
import co.poynt.vendor.services.v1.IPoyntRemoteKeyLoadListener;
import co.poynt.vendor.services.v1.IPoyntRemoteKeyRequestListener;
import co.poynt.vendor.services.v1.IPoyntRemoteKeyValidationRequestListener;
import co.poynt.vendor.services.v1.IPoyntSecurityEventLogsListener;

public class SecuritySPIService extends Service {
    public SecuritySPIService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private final IPoyntPaymentSecuritySPI.Stub mBinder = new IPoyntPaymentSecuritySPI.Stub() {
        @Override
        public void eraseAllKeys(IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void resetCardReader(IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void enablePciSecurity(IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void encryptDukpt(DukptData dukptData, IPoyntDataEncryptionListener iPoyntDataEncryptionListener) throws RemoteException {

        }

        @Override
        public void updateSessionKey(int i, byte[] bytes, byte[] bytes1, byte[] bytes2, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void updateSessionKeyLegacy(int i, byte[] bytes, byte[] bytes1, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void initMutualAuthentication(Bundle bundle, IPoyntMutualAuthenticationListener iPoyntMutualAuthenticationListener) throws RemoteException {

        }

        @Override
        public void verifyMutualAuthentication(Bundle bundle, IPoyntMutualAuthenticationVerificationListener iPoyntMutualAuthenticationVerificationListener) throws RemoteException {

        }

        @Override
        public void injectKeys(Bundle bundle, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void decryptServiceMessage(Bundle bundle, IPoyntServiceMessageDecryptionListener iPoyntServiceMessageDecryptionListener) throws RemoteException {

        }

        @Override
        public void encryptServiceMessage(Bundle bundle, IPoyntServiceMessageEncryptionListener iPoyntServiceMessageEncryptionListener) throws RemoteException {

        }

        @Override
        public void selectServiceMessageProcessingKey(Bundle bundle, IPoyntSelectMessageProcessingKeyListener iPoyntSelectMessageProcessingKeyListener) throws RemoteException {

        }

        @Override
        public void decryptFinancialServiceMessage(Bundle bundle, IPoyntServiceFinMessageDecryptionListener iPoyntServiceFinMessageDecryptionListener) throws RemoteException {

        }

        @Override
        public void encryptFinancialServiceMessage(Bundle bundle, IPoyntServiceFinMessageEncryptionListener iPoyntServiceFinMessageEncryptionListener) throws RemoteException {

        }

        @Override
        public void validateKeyIndices(Bundle bundle, IPoyntKeyIndicesValidationListener iPoyntKeyIndicesValidationListener) throws RemoteException {

        }

        @Override
        public void injectTerminalKey(Bundle bundle, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void loadBancomatCertificates(Bundle bundle, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void deleteAcquirerConfigKeys(Bundle bundle, IPoyntDeleteAcquirerConfigKeysListener iPoyntDeleteAcquirerConfigKeysListener) throws RemoteException {

        }

        @Override
        public void getDeviceCSR(String s, IPoyntDeviceCertificateListener iPoyntDeviceCertificateListener) throws RemoteException {

        }

        @Override
        public void setDeviceSigningCertificate(String s, IPoyntDeviceCertificateUpdateListener iPoyntDeviceCertificateUpdateListener) throws RemoteException {

        }

        @Override
        public void getDeviceSigningCertificate(IPoyntDeviceCertificateListener iPoyntDeviceCertificateListener) throws RemoteException {

        }

        @Override
        public void loadTrustedSourceCertificate(String s, IPoyntDeviceCertificateUpdateListener iPoyntDeviceCertificateUpdateListener) throws RemoteException {

        }

        @Override
        public void getDeviceCertificatesStatus(IPoyntDeviceCertificatesSummaryListener iPoyntDeviceCertificatesSummaryListener) throws RemoteException {

        }

        @Override
        public void getDeviceTrustedCertificatesSummary(IPoyntDeviceCertificatesSummaryListener iPoyntDeviceCertificatesSummaryListener) throws RemoteException {

        }

        @Override
        public void getDeviceSigningCertificateSummary(IPoyntDeviceCertificatesSummaryListener iPoyntDeviceCertificatesSummaryListener) throws RemoteException {

        }

        @Override
        public void initiateRemoteKeyLoading(String s, String s1, String s2, IPoyntRemoteKeyLoadListener iPoyntRemoteKeyLoadListener) throws RemoteException {

        }

        @Override
        public void generateDeviceKeyRequest(IPoyntRemoteKeyRequestListener iPoyntRemoteKeyRequestListener) throws RemoteException {

        }

        @Override
        public void loadDeviceKeyResponse(int i, String s, String s1, String s2, String s3, long l, IPoyntRemoteKeyLoadListener iPoyntRemoteKeyLoadListener) throws RemoteException {

        }

        @Override
        public void generateDeviceKeyValidationRequest(IPoyntRemoteKeyValidationRequestListener iPoyntRemoteKeyValidationRequestListener) throws RemoteException {

        }

        @Override
        public void validateDeviceKeyValidationResponse(boolean b, String s, String s1, String s2, long l, IPoyntRemoteKeyLoadListener iPoyntRemoteKeyLoadListener) throws RemoteException {

        }

        @Override
        public void getPCIKeyStatus(IPoyntPCIKeyStatusListener iPoyntPCIKeyStatusListener) throws RemoteException {

        }

        @Override
        public void getPCIKeyNames(IPoyntPCIKeyNamesListener iPoyntPCIKeyNamesListener) throws RemoteException {

        }

        @Override
        public void loadCardHashSalt(String s, IPoyntCardHashStatusListener iPoyntCardHashStatusListener) throws RemoteException {

        }

        @Override
        public void getCardHashSaltLoadedStatus(IPoyntCardHashStatusListener iPoyntCardHashStatusListener) throws RemoteException {

        }

        @Override
        public void saveDataForFinancialServiceMessage(Bundle bundle, IPoyntSaveDataForFinancialServiceMsgListener iPoyntSaveDataForFinancialServiceMsgListener) throws RemoteException {

        }

        @Override
        public void eraseAllKeysV2(Bundle bundle, IPoyntKeyUpdateListener iPoyntKeyUpdateListener) throws RemoteException {

        }

        @Override
        public void getBancomatTrustedCerts(IPoyntBancomantTrustedCertsSummaryListener iPoyntBancomantTrustedCertsSummaryListener) throws RemoteException {

        }

        @Override
        public void removeBancomatTrustedCerts(IPoyntBancomantTrustedCertsSummaryListener iPoyntBancomantTrustedCertsSummaryListener) throws RemoteException {

        }

        @Override
        public void getSecurityEventLogs(Bundle bundle, IPoyntSecurityEventLogsListener iPoyntSecurityEventLogsListener) throws RemoteException {

        }
    };
}
