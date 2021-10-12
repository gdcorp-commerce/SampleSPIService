package com.spi.mockcardreaderservice.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class MockCardreaderHelper {
    private static final String VISA_MAGSTRIPE = "VISA_MAGSTRIPE";
    private static final String VISA_CONTACT = "VISA_CONTACT";
    private static final String VISA_CONTACTLESS = "VISA_CONTACTLESS";
    private static final String MASTERCARD_MAGSTRIPE = "MASTERCARD_MAGSTRIPE";
    private static final String MASTERCARD_CONTACT = "MASTERCARD_CONTACT";
    private static final String MASTERCARD_CONTACTLESS = "MASTERCARD_CONTACTLESS";
    private static final String AMEX_MAGSTRIPE = "AMEX_MAGSTRIPE";
    private static final String AMEX_CONTACT = "AMEX_CONTACT";
    private static final String AMEX_CONTACTLESS = "AMEX_CONTACTLESS";
    private static final String DISCOVER_MAGSTRIPE = "DISCOVER_MAGSTRIPE";
    private static final String DISCOVER_CONTACT = "DISCOVER_CONTACT";
    private static final String DISCOVER_CONTACTLESS = "DISCOVER_CONTACTLESS";

    private final String TAG = "MockCardreaderHelper";
    Context mContext = null;

    public MockCardreaderHelper(Context context) {
        mContext = context;
    }

    List<MockResponseData> responseDataList;

    public List<MockResponseData> loadResponseJsonFileFromAssets() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open("CardReaderResponse.json"),
                            "UTF-8"));
            String mLine;
            StringBuilder sb = new StringBuilder();
            while ((mLine = reader.readLine()) != null) {
                sb.append(mLine);
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<List<MockResponseData>>() {
            }.getType();
            List<MockResponseData> mockResponseDataList = gson.fromJson(sb.toString(), listType);
            responseDataList = mockResponseDataList;
            return mockResponseDataList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        return null;
    }

    public MockResponseData getMockResponseData(int amount) {
        MockResponseData mockResponseData = null;

        Log.d("Minor Amount ", Integer.toString(amount));
        if (amount >= 0 && amount < 5) {
            // Visa MSR
            mockResponseData = findMockResponseData(VISA_MAGSTRIPE);
        } else if (amount >= 5 && amount < 10) {
            // Visa CT
            mockResponseData = findMockResponseData(VISA_CONTACT);
        } else if (amount >= 10 && amount < 15) {
            // Visa CL
            mockResponseData = findMockResponseData(VISA_CONTACTLESS);
        } else if (amount >= 15 && amount < 20) {
            // Mastercard MSR
            mockResponseData = findMockResponseData(MASTERCARD_MAGSTRIPE);
        } else if (amount >= 20 && amount < 25) {
            // Mastercard CT
            mockResponseData = findMockResponseData(MASTERCARD_CONTACT);
        } else if (amount >= 25 && amount < 30) {
            // Mastercard CL
            mockResponseData = findMockResponseData(MASTERCARD_CONTACTLESS);
        } else if (amount >= 30 && amount < 35) {
            // Amex MSR
            mockResponseData = findMockResponseData(AMEX_MAGSTRIPE);
        } else if (amount >= 35 && amount < 40) {
            // Amex CT
            mockResponseData = findMockResponseData(AMEX_CONTACT);
        } else if (amount >= 40 && amount < 45) {
            // Amex CL
            mockResponseData = findMockResponseData(AMEX_CONTACTLESS);
        } else if (amount >= 45 && amount < 50) {
            // Discover MSR
            mockResponseData = findMockResponseData(DISCOVER_MAGSTRIPE);
        } else if (amount >= 50 && amount < 55) {
            // Discover CONTACT
            mockResponseData = findMockResponseData(DISCOVER_CONTACT);
        } else if (amount >= 55 && amount < 60) {
            // Discover CONTACTLESS
            mockResponseData = findMockResponseData(DISCOVER_CONTACTLESS);
        } else {
            Log.i(TAG, "Did not find a response data to mock for this amount ");
        }

        return mockResponseData;
    }

    private MockResponseData findMockResponseData(String scheme) {
        MockResponseData mockResponseData = null;
        for (MockResponseData data: responseDataList) {
            if(scheme.equals(data.getScheme())) {
                mockResponseData = data;
                break;
            }
        }

        return mockResponseData;
    }

    private MockResponseData.ResponseData updateAmountTag(long amount) {
        MockResponseData.ResponseData responseData = null;

        return responseData;
    }
}
