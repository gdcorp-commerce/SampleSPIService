package com.spi.mockcardreaderservice.builder;

import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import co.poynt.cardreader.ApplicationItem;
import co.poynt.cardreader.BytesUtils;
import co.poynt.cardreader.POSEntryMode;
import co.poynt.cardreader.PaymentTags;
import co.poynt.cardreader.TLV;
import co.poynt.cardreader.TlvUtil;

public class ApplicationListBuilder {
    private static final String TAG = "ApplicationListBuilder";

    boolean cardHolderConfirmationSupported;
    private List<String> commonDebitAIDs;
    private List<String> creditAIDs;
    private List<String> voucherAIDs;
    private boolean retry;
    private boolean contactless;
    private boolean consumerDefinedApplicationPreference;

    public ApplicationListBuilder() {
    }

    public ArrayList<ApplicationItem> getApplicationList(byte[] data) {
        Log.d(TAG, "Processing applications from " + BytesUtils.bytesToStringNoSpace(data));
        // app selection required response contains
        // tlv card holder confirmation and tlv application item
        ArrayList<ApplicationItem> applicationItemList = new ArrayList<>();
        if (data != null) {
            List<TLV> tlvList = TlvUtil.getTLVs(data);
            int appIndex = 0;
            if (tlvList != null && tlvList.size() > 0) {
                for (TLV tlv : tlvList) {
                    if (tlv.getTag().isConstructed()
                            && tlv.getTag() == PaymentTags.APPLICATION_ITEM) {
                        List<TLV> subTlvs = TlvUtil.getlistTLV(tlv.getValueBytes(), tlv.getTag(),
                                true);
                        try {
                            ApplicationItem applicationItem = ApplicationItem.parseApplicationItem(
                                    subTlvs);
                            applicationItem.setIndex(appIndex++);
                            updateCardType(applicationItem);
                            applicationItemList.add(applicationItem);
                            Log.d(TAG, applicationItem.toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    } else if (tlv.getTag() == PaymentTags.CARDHOLDER_CONFIRMATION_SUPPORTED) {
                        if (tlv.getValueBytes()[0] == 0x01) {
                            cardHolderConfirmationSupported = true;
                        }
                    } else if (tlv.getTag() == PaymentTags.POINT_OF_SERVICE_ENTRY_MODE) {
                        String amountAuthzStr = BytesUtils.bytesToStringNoSpace(tlv.getValueBytes());
                        try {
                            POSEntryMode entryMode = POSEntryMode.find(Integer.valueOf(amountAuthzStr));
                            if (entryMode.equals(POSEntryMode.ICC_CONTACTLESS)
                                    || entryMode.equals(POSEntryMode.CONTACTLESS_LEGACY_MODE)
                                    || entryMode.equals(POSEntryMode.CONTACTLESS_MSR)) {
                                contactless = true;
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Number format exception happening while checking POSEntryMode");
                        }
                    } else if (tlv.getTag() == PaymentTags.CONSUMER_APPLICATION_SELECTION_PREFERENCE) {
                        // NUAT-970: Update App Selection process to handle PagoBancomat Digital specific requirements
                        if (tlv.getValueBytes()[0] == 0x01) {
                            consumerDefinedApplicationPreference = true;
                        } else {
                            consumerDefinedApplicationPreference = false;
                        }
                    } else {
                        Log.d(TAG, "Unexpected TLV while processing applications" + tlv.toString());
                    }
                }
            }
        }
        // update the contactless flag if necessary
        if(contactless && applicationItemList.size() > 0){
            for(ApplicationItem app: applicationItemList){
                app.setContactless(true);
            }
        } else {
            // Workaround - in case of newland the posentrymode is returned as part of the application
            for (ApplicationItem app : applicationItemList) {
                if (app.isContactless()) {
                    contactless = true;
                    break;
                }
            }
        }
        return applicationItemList;
    }

    public void updateCardType(ApplicationItem item) {
        if (item == null ) {
            return;
        }

        String aid = item.getCardAID();
        if (aid == null) {
            aid = item.getDfName();
        }

        // #1 first we need to check full match for all cases
        if (hasFullMatchAid(creditAIDs, aid)) {
            Log.d(TAG, " hasFullMatchAid Found credit AID " +
                    "for credit transaction %s" + item.toString());
            item.setAidType(ApplicationItem.AidType.CREDIT);
            return;
        }

        if (hasFullMatchAid(commonDebitAIDs, aid)) {
            Log.d(TAG, " hasFullMatchAid Found debit AID for" +
                    " debit transaction %s" + item.toString());
            item.setAidType(ApplicationItem.AidType.DEBIT);
            return;
        }

        if (hasFullMatchAid(voucherAIDs, aid)) {
            Log.d(TAG," hasFullMatchAid Found voucher AID " +
                    "for voucher transaction" + item.toString());
            item.setAidType(ApplicationItem.AidType.VOUCHER);
            return;
        }

        // #2 we need to check partial match for cases
        if (hasPartialMatchAid(creditAIDs, aid)) {
            Log.d(TAG, " hasPartialMatchAid Found credit " +
                    "AID for credit transaction" + item.toString());
            item.setAidType(ApplicationItem.AidType.CREDIT);
            return;
        }

        if (hasPartialMatchAid(commonDebitAIDs, aid)) {
            Log.d(TAG, " hasPartialMatchAid Found debit " +
                    "AID for debit transaction " + item.toString());
            item.setAidType(ApplicationItem.AidType.DEBIT);
            return;
        }

        if (hasPartialMatchAid(voucherAIDs, aid)) {
            Log.d(TAG, " hasPartialMatchAid Found voucher" +
                    " AID for voucher transaction " + item.toString());
            item.setAidType(ApplicationItem.AidType.VOUCHER);
            return;
        }
        // default is CREDIT
        item.setAidType(ApplicationItem.AidType.CREDIT);
        Log.d(TAG, " default type %s" + item.toString());
    }

    /* @param commonDebitAIDs, creditAIDs, or voucherAIDs list
     * @param AID return from the card
     * @return true or false*/
    private boolean hasPartialMatchAid(List<String> list, String aid) {
        if ((list == null) || (aid == null)) {
            return false;
        }
        for (String item: list) {
            if (aid.startsWith(item)) {
                Log.d(TAG, "hasPartialMatchAid = TRUE: AID from web is "+ item +" AID from card is "+ aid );
                return true;
            }
        }
        return false;
    }


    private boolean hasFullMatchAid(List<String> list, String aid) {
        if ((list == null) || (aid == null)) {
            return false;
        }
        for (String item: list) {
            if (TextUtils.equals(aid, item)) {
                Log.d(TAG, "hasFullMatchAid = TRUE, The AID from MC is (%s), AID from card is (%s) " +
                        item + aid );
                return true;
            }
        }
        return false;
    }

    public boolean isCardHolderConfirmationSupported() {
        return cardHolderConfirmationSupported;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public boolean isContactless() {
        return contactless;
    }

    public boolean isConsumerDefinedApplicationPreference() {
        return consumerDefinedApplicationPreference;
    }
}
