package com.spi.mockcardreaderservice.builder;

import android.text.TextUtils;
import android.util.Log;

//import com.acmetech.cc.MagStripeCard;
//import com.acmetech.cc.MagstripeParseException;
import com.spi.mockcardreaderservice.util.CardUtil;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import co.poynt.cardreader.BytesUtils;
import co.poynt.cardreader.CardScheme;
import co.poynt.cardreader.ITag;
import co.poynt.cardreader.POSEntryMode;
import co.poynt.cardreader.PaymentTags;
import co.poynt.cardreader.TLV;
import co.poynt.cardreader.TlvUtil;
/*import co.poynt.os.common.util.CardUtil;
import co.poynt.os.common.util.Ln;
import co.poynt.os.common.util.Strings;
import co.poynt.os.util.StringUtil;
import co.poynt.reader.common.hid.PoyntReaderResponse;
import co.poynt.reader.common.hid.enums.Command;
import co.poynt.reader.common.hid.enums.Status;*/
import co.poynt.vendor.model.CardReaderResponse;
//import fr.devnied.bitlib.BytesUtils;

public class CardReaderResponseBuilder {
    CardReaderResponse cardReaderResponse;

    public CardReaderResponseBuilder() {
        this.cardReaderResponse = new CardReaderResponse();
    }

    public void process(CardReaderResponse.Status status, byte[] data){
        cardReaderResponse.setStatus(status);
        setCardData(status, data);
    }

    private void setCardData(CardReaderResponse.Status status, byte[] data){
        int offset = 0x00;

        if(status == CardReaderResponse.Status.STOP_AFTER_READ_RECORDS){
            // in case of Poynt Terminals stop after read records is always for EMV CT
            cardReaderResponse.setCardScheme(CardScheme.EMV_CONTACT);
        } else {
            cardReaderResponse.setCardScheme(CardScheme.find(data[offset]));
            offset++;
        }

        if (data.length > 1) {
            byte[] tlvBytes = Arrays.copyOfRange(data, offset, data.length);
            // get all TLVs
            Map<ITag, TLV> tlvs = TlvUtil.getTLVMap(tlvBytes);
            if (tlvs != null && tlvs.size() > 0) {
               // Ln.d("TLVs: (%s)", tlvs);

                if (tlvs.containsKey(PaymentTags.AMOUNT_AUTHORIZED_NUMERIC)) {
                    TLV amountAuthorizedTlv = tlvs.get(PaymentTags.AMOUNT_AUTHORIZED_NUMERIC);
                    String amountAuthzStr = BytesUtils.bytesToStringNoSpace(amountAuthorizedTlv.getValueBytes());
                    BigDecimal amountAuthz = new BigDecimal(amountAuthzStr);
                    cardReaderResponse.setAmount(amountAuthz.longValue());
                }

                if (tlvs.containsKey(PaymentTags.AMOUNT_OTHER_NUMERIC)) {
                    TLV amountCashbackTlv = tlvs.get(PaymentTags.AMOUNT_OTHER_NUMERIC);
                    String amountCashbackStr = BytesUtils.bytesToStringNoSpace(amountCashbackTlv.getValueBytes());
                  //  Ln.d("Cashback amount from card (%s)", amountCashbackStr);
                    BigDecimal amountAuthz = new BigDecimal(amountCashbackStr);
                    cardReaderResponse.setAmountOther(amountAuthz.longValue());
                }
                if (tlvs.containsKey(PaymentTags.POINT_OF_SERVICE_ENTRY_MODE)) {
                    TLV entryModeTlv = tlvs.get(PaymentTags.POINT_OF_SERVICE_ENTRY_MODE);
                    String amountAuthzStr = BytesUtils.bytesToStringNoSpace(entryModeTlv.getValueBytes());
                    POSEntryMode entryMode = POSEntryMode.find(Integer.valueOf(amountAuthzStr));
                    cardReaderResponse.setPosEntryMode(entryMode);
                }

                if (tlvs.containsKey(PaymentTags.PAN_FIRST_6)) {
                    String first6 = new String(tlvs.get(PaymentTags.PAN_FIRST_6).getValueBytes());
                    if (!TextUtils.isEmpty(first6)) {
                        cardReaderResponse.setBinRange(CardUtil.getPanFirst6(first6));
                     //   Ln.d("First6 (%s)", first6);
                    }
                } else if (tlvs.containsKey(PaymentTags.CARD_BIN_RANGE)) {
                    TLV binRange = tlvs.get(PaymentTags.CARD_BIN_RANGE);
                    String binRangeStr = binRange != null ?
                            new String(binRange.getValueBytes()) : null;
                    if (!TextUtils.isEmpty(binRangeStr)) {
                        cardReaderResponse.setBinRange(CardUtil.getPanFirst6(binRangeStr));
                      //  Ln.d("First6 (%s)", binRangeStr);
                    }
                }

                if (tlvs.containsKey(PaymentTags.PAN_LAST_4)) {
                    String last4 = new String(tlvs.get(PaymentTags.PAN_LAST_4).getValueBytes());
                  //  Ln.d("Last4 (%s)", last4);
                    cardReaderResponse.setLast4(last4);
                }

                if (tlvs.containsKey(PaymentTags.PAN)) {
                    byte[] panBytes = tlvs.get(PaymentTags.PAN).getValueBytes();
                  //  Ln.d("Encrypted PAN (%s)", BytesUtils.bytesToStringNoSpace(panBytes));
                    cardReaderResponse.setAccountNumber(BytesUtils.bytesToStringNoSpace(panBytes));
                }
                //TODO - Check  if Elavon acquirer check is needed for this
                // NOTE: Elavon if the acquirer is ELAVON then use new TAG 1F815C
                // that contains PAN + Expiry combined
                if (tlvs.containsKey(PaymentTags.PAN_WITH_EXPIRY)) {
                    byte[] panBytes = tlvs.get(PaymentTags.PAN_WITH_EXPIRY).getValueBytes();
                   // Ln.d("FOR ELAVON Setting Encrypted PAN + EXP as card number (%s)", BytesUtils.bytesToStringNoSpace(panBytes));
                    cardReaderResponse.setAccountNumber(BytesUtils.bytesToStringNoSpace(panBytes));
                }

                if (tlvs.containsKey(PaymentTags.PAN_HASH)) {
                 //   Ln.d("Pan Hash (%s)", BytesUtils.bytesToStringNoSpace(
                        //    tlvs.get(PaymentTags.PAN_HASH).getValueBytes()));
                    cardReaderResponse.setAccountNumberHash(BytesUtils.bytesToStringNoSpace(
                            tlvs.get(PaymentTags.PAN_HASH).getValueBytes()));
                }

                // if we have the card holder name always pass it
                String cardHolderFullName = null;
                if (tlvs.containsKey(PaymentTags.CARDHOLDER_NAME)) {
                    cardHolderFullName = new String(
                            tlvs.get(PaymentTags.CARDHOLDER_NAME).getValueBytes());
                 //   Ln.d("Card holder name (%s)", cardHolderFullName);
                    cardReaderResponse.setCardHolderName(cardHolderFullName);
                }

                // if we have application expiry date - set the corresponding fields in the card object
                if (tlvs.containsKey(PaymentTags.APP_EXPIRATION_DATE)) {
                    TLV applicationExpiryDate = tlvs.get(PaymentTags.APP_EXPIRATION_DATE);
                    String expiry = BytesUtils.bytesToStringNoSpace(
                            applicationExpiryDate.getValueBytes());
                    cardReaderResponse.setExpirationDate(expiry);
                }

                // load all fields in card reader response
                if (tlvs.containsKey(PaymentTags.DATA_KSN)) {
                    byte[] ksn = tlvs.get(PaymentTags.DATA_KSN).getValueBytes();
                  //  Ln.d("Found DATA KSN (%s)", BytesUtils.bytesToStringNoSpace(ksn));
                    cardReaderResponse.setKeyIdentifier(BytesUtils.bytesToStringNoSpace(ksn));
                    cardReaderResponse.setEncrypted(true);
                }  else if (tlvs.containsKey(PaymentTags.SESSION_DATA_KEY_ID)) {
                    byte[] sessionKeyId = tlvs.get(PaymentTags.SESSION_DATA_KEY_ID).getValueBytes();
                //    Ln.d("Found DATA SessionKey ID (%s)", BytesUtils.bytesToStringNoSpace(sessionKeyId));
                    cardReaderResponse.setKeyIdentifier(BytesUtils.bytesToStringNoSpace(sessionKeyId));
                    cardReaderResponse.setEncrypted(true);
                }

                if (tlvs.containsKey(PaymentTags.TRACK_2_EQV_DATA)) {
                    byte[] track2Bytes = tlvs.get(PaymentTags.TRACK_2_EQV_DATA).getValueBytes();
                 //   Ln.d("Encrypted Track2 equivalent (%s)", BytesUtils.bytesToStringNoSpace(track2Bytes));
                    cardReaderResponse.setTrack2(BytesUtils.bytesToStringNoSpace(track2Bytes));
                }
                if (tlvs.containsKey(PaymentTags.TRACK1_DATA)) {
                    byte[] track1Bytes = tlvs.get(PaymentTags.TRACK1_DATA).getValueBytes();
                 //   Ln.d("Encrypted Track1 (%s)", BytesUtils.bytesToStringNoSpace(track1Bytes));
                    cardReaderResponse.setTrack1(BytesUtils.bytesToStringNoSpace(track1Bytes));
                }

                // PIN Block and KSN
                if (tlvs.containsKey(PaymentTags.TRANSACTION_PIN_DATA)) {
                    byte[] pinBytes = tlvs.get(PaymentTags.TRANSACTION_PIN_DATA).getValueBytes();
                 //   Ln.d("Encrypted PIN (%s)", BytesUtils.bytesToStringNoSpace(pinBytes));
                    cardReaderResponse.setPinData(BytesUtils.bytesToStringNoSpace(pinBytes));

                    if (tlvs.containsKey(PaymentTags.PIN_KSN)) {
                        byte[] ksn = tlvs.get(PaymentTags.PIN_KSN).getValueBytes();
                    //    Ln.d("Found DATA KSN (%s)", BytesUtils.bytesToStringNoSpace(ksn));
                        cardReaderResponse.setPinKeyIdentifier(BytesUtils.bytesToStringNoSpace(ksn));
                    } else if (tlvs.containsKey(PaymentTags.SESSION_PIN_KEY_ID)) {
                        byte[] sessionKeyId = tlvs.get(PaymentTags.SESSION_PIN_KEY_ID).getValueBytes();
                     //   Ln.d("Found Session Key Id for Pin KSN (%s)", BytesUtils.bytesToStringNoSpace(sessionKeyId));
                        cardReaderResponse.setPinKeyIdentifier(BytesUtils.bytesToStringNoSpace(sessionKeyId));
                    }
                }

                // following used for checkCard/stop after read records
                if(tlvs.containsKey(PaymentTags.SERVICE_CODE)) {
                    TLV serviceCode = tlvs.get(PaymentTags.SERVICE_CODE);
                    String servicecodeStr = (serviceCode != null) ?
                            BytesUtils.bytesToStringNoSpace(serviceCode.getValueBytes()) : null;
                    cardReaderResponse.setServiceCode(servicecodeStr);
                }

                // Use Acquirer
                if(tlvs.containsKey(PaymentTags.ACQUIRER_IDENTIFIER)) {
                    TLV acquirer = tlvs.get(PaymentTags.ACQUIRER_IDENTIFIER);
                    String acquirerStr = acquirer != null ?
                            BytesUtils.bytesToStringNoSpace(acquirer.getValueBytes()) : null;
                    cardReaderResponse.setAcquirerId(acquirerStr);
                }
                if(tlvs.containsKey(PaymentTags.APPLICATION_LABEL)) {
                    TLV applicationLabel = tlvs.get(PaymentTags.APPLICATION_LABEL);
                    String applicationLabelStr = applicationLabel != null ?
                            new String(applicationLabel.getValueBytes()) : null;
                    cardReaderResponse.setApplicationLabel(applicationLabelStr);
                }
                if(tlvs.containsKey(PaymentTags.PAN_SEQUENCE_NUMBER)) {
                    final TLV panSequenceNumber = tlvs.get(PaymentTags.PAN_SEQUENCE_NUMBER);
                    String panSequenceNumberStr = panSequenceNumber != null ?
                            BytesUtils.bytesToStringNoSpace(panSequenceNumber.getValueBytes()) : null;
                    cardReaderResponse.setPanSequenceNumber(panSequenceNumberStr);
                }
                if(tlvs.containsKey(PaymentTags.ISSUER_COUNTRY_CODE)) {
                    TLV issuerCountryCode = tlvs.get(PaymentTags.ISSUER_COUNTRY_CODE);
                    String issuerCountryCodeStr = issuerCountryCode != null ?
                            BytesUtils.bytesToStringNoSpace(issuerCountryCode.getValueBytes()) : null;
                    cardReaderResponse.setIssuerCountryCode(issuerCountryCodeStr);
                }
                if(tlvs.containsKey(PaymentTags.APPLICATION_CURRENCY_CODE)) {
                    TLV appCurrencyCode = tlvs.get(PaymentTags.APPLICATION_CURRENCY_CODE);
                    String appCurrencyCodeStr = appCurrencyCode != null ?
                            BytesUtils.bytesToStringNoSpace(appCurrencyCode.getValueBytes()) : null;
                    cardReaderResponse.setAppCurrencyCode(appCurrencyCodeStr);
                }
                int issuerCodeTableIndex = -1;
                if (tlvs.containsKey(PaymentTags.ISSUER_CODE_TABLE_INDEX)) {
                    TLV issuerCodeTableIndexTlv = tlvs.get(PaymentTags.ISSUER_CODE_TABLE_INDEX);
                    issuerCodeTableIndex = BytesUtils.byteArrayToInt(issuerCodeTableIndexTlv.getValueBytes());
                    cardReaderResponse.setIssuerCodeTableIndex(issuerCodeTableIndex);
                }

                if(tlvs.containsKey(PaymentTags.APP_PREFERRED_NAME)) {
                    String applicationPreferredName = null;
                    TLV applicationPreferredNameTlv = tlvs.get(PaymentTags.APP_PREFERRED_NAME);
                    applicationPreferredName = applicationPreferredNameTlv != null ?
                            new String(applicationPreferredNameTlv.getValueBytes()) : null;
                    cardReaderResponse.setApplicationPreferredName(applicationPreferredName);
                }

                // Todo: Non Payment card support in the future
            }
            // everything else goes into additional TLVs
            cardReaderResponse.setAdditionalTlvs(tlvBytes);
        }
    }

    public CardReaderResponse getCardReaderResponse() {
        return cardReaderResponse;
    }
}
