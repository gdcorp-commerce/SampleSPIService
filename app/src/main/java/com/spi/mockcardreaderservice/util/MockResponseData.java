package com.spi.mockcardreaderservice.util;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.spi.mockcardreaderservice.util.MockCardreaderHelper;

import java.math.BigDecimal;
import java.util.List;

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
import co.poynt.vendor.model.CardReaderRequest;
import co.poynt.vendor.model.CardReaderResponse;
import co.poynt.vendor.model.DataEntrySkipReason;
import co.poynt.vendor.model.HostResponse;
import co.poynt.vendor.model.InvalidPANContinueOptions;
import co.poynt.vendor.services.v1.IPoyntPaymentSPI;
import co.poynt.vendor.services.v1.IPoyntPaymentSPICancelListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIListener;
import co.poynt.vendor.services.v1.IPoyntPaymentSPIStatusListener;

public class MockResponseData {
    public MockResponseData() {
    }

    private String scheme;
    private List<ResponseData> responseData;


    public String getScheme() {
        return scheme;
    }

    public List<ResponseData> getResponseData() {
        return responseData;
    }

    public class ResponseData {
        private String status;
        private String packet;

        public String getStatus() {
            return status;
        }

        public String getPacket() {
            return packet;
        }

        public void setPacket(String data) {
            packet = data;
        }

    }
}