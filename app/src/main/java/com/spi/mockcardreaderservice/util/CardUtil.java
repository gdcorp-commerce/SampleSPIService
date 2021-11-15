package com.spi.mockcardreaderservice.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.util.List;

import co.poynt.api.model.Card;
import co.poynt.api.model.CardType;
import co.poynt.api.model.FundingSource;
import co.poynt.api.model.FundingSourceAccountType;
import co.poynt.cardreader.CardInterfaceType;
import co.poynt.os.model.ConnectionResult;
import co.poynt.vendor.model.CardReaderRequest;
import co.poynt.vendor.model.CardReaderEvent;
//import co.poynt.os.common.R;

public class CardUtil {

    public static final String ENABLE_PLAYBACK = "enable_playback";

    public static final String DISABLE_PLAYBACK = "disable_playback";
    public static final String DISABLE_PLAYBACK_FOR_CURRENT_TXN = "disable_playback_for_current_txn";
    public static final String PLAYBACK_CARD_TYPE = "playback_card_type";

    public static class CardReaderUiEvent {
        public static final String BASE = "poynt.intent.action.";
        public static final String PRESENT_CARD = BASE+"PRESENT_CARD";
        public static final String INSERT_CARD = BASE+ "INSERT_CARD";
        public static final String SWIPE_CARD = BASE + "SWIPE_CARD";
        public static final String INSERT_OR_SWIPE_CARD = BASE + "INSERT_OR_SWIPE_CARD";
        public static final String CARD_FOUND = BASE + "CARD_FOUND";
        public static final String CARD_READ_SUCCESSFULLY = BASE + "CARD_READ_SUCCESSFULLY";
        public static final String CARD_REMOVED = BASE + "CARD_REMOVED";
        public static final String CARD_NOT_REMOVED = BASE + "CARD_NOT_REMOVED";
        public static final String SWIPE_CARD_FALLBACK = BASE + "SWIPE_CARD_FALLBACK";
        public static final String PROCESSING = BASE + "PROCESSING";
        public static final String PIN_ENTRY_REQUIRED = BASE + "PIN_ENTRY_REQUIRED";
        public static final String PIN_DIGIT_ENTERED  = BASE + "PIN_DIGIT_ENTERED"; //(for each digit)
        public static final String PIN_ENTERED = BASE + "PIN_ENTERED";
        public static final String ONLINE_AUTHORIZATION = BASE + "ONLINE_AUTHORIZATION";
        public static final String APPROVED = BASE + "APPROVED";
        public static final String DECLINED = BASE + "DECLINED";
        public static final String APPROVED_OR_APPROVED_WITH_SIGNATURE = BASE + "APPROVED or APPROVED WITH SIGNATURE";
        public static final String PRESENT_CARD_AGAIN = BASE + "PRESENT_CARD_AGAIN";
        public static final String PROCESSING_ERROR_SEE_PHONE = BASE + "PROCESSING_ERROR_SEE_PHONE";
    }

    public static CardReaderEvent getEvent(List<CardInterfaceType> interfaces) {
        final boolean enableCT = interfaces.contains(CardInterfaceType.CT);
        final boolean enableCL = interfaces.contains(CardInterfaceType.CL);
        final boolean enableMSR = interfaces.contains(CardInterfaceType.MSR);

        final CardReaderEvent event = new CardReaderEvent();
        if(enableCT && enableCL && enableMSR){
            event.setEvent(CardReaderUiEvent.PRESENT_CARD);
        }else if(enableCT && (!enableCL) && (!enableMSR)){
            event.setEvent(CardReaderUiEvent.INSERT_CARD);
        }else if(enableMSR && (!enableCL) && (!enableCT)){
            event.setEvent(CardReaderUiEvent.SWIPE_CARD);
        }else if(enableMSR && (enableCT) && (!enableCL)){
            event.setEvent(CardReaderUiEvent.INSERT_OR_SWIPE_CARD);
        } else {
            // default
            event.setEvent(CardReaderUiEvent.PRESENT_CARD);
        }

        if(enableCT|enableCL|enableMSR){
            return event;
        }

        return null;
    }

   /* public static Drawable getCardLogo(Context context, CardType cardType, FundingSourceAccountType accountType) {
        return getCardLogo(context.getResources(), cardType, accountType);
    }

    // this will used in PF , the white version for P5 and dark version of P6
    public static Drawable getCardLogo(final Resources resources, CardType cardType, FundingSourceAccountType accountType) {
        Drawable drawable;
        if (accountType != null && accountType == FundingSourceAccountType.EBT) {
            drawable = resources.getDrawable(R.drawable.pf_ebt_ic);
        } else {
            // we will override the base implementation only if we know the card
            // type
            if (cardType == CardType.AMERICAN_EXPRESS) {
                drawable = resources.getDrawable(R.drawable.pf_amex_merchant_ic);
            } else if (cardType == CardType.VISA) {
                drawable = resources.getDrawable(R.drawable.pf_visa_merchant_ic);
            } else if (cardType == CardType.MASTERCARD) {
                drawable = resources.getDrawable(R.drawable.pf_mastercard_merchant_ic);
            } else if (cardType == CardType.MAESTRO) {
                drawable = resources.getDrawable(R.drawable.pf_maestro_merchant_ic);
            } else if (cardType == CardType.DISCOVER) {
                drawable = resources.getDrawable(R.drawable.pf_discover_merchant_ic);
            } else if (cardType == CardType.UNIONPAY) {
                drawable = resources.getDrawable(R.drawable.pf_poynt_unionpay_blue_ic);
            } else if (cardType == CardType.INTERAC) {
                drawable = resources.getDrawable(R.drawable.pf_poynt_interac_ic);
            } else if (cardType == CardType.ALIPAY) {
                drawable = resources.getDrawable(R.drawable.pf_alipay_dark_ic);
            } else if (cardType == CardType.PAYPAL) {
                drawable = resources.getDrawable(R.drawable.pf_paypal_ic);
            } else if (cardType == CardType.BANCOMAT) {
                drawable = resources.getDrawable(R.drawable.poynt_bancomat_ic);
            } else {
                drawable = resources.getDrawable(R.drawable.pf_unknown_card_ic);
            }
        }
        return drawable;
    }


    public static Drawable getSmallCardLog(Context context, CardType cardType, FundingSourceAccountType accountType) {
        return context.getResources().getDrawable(
                getCardLogoResourceId(cardType, accountType));
    }


    public static int getCardLogoResourceId(FundingSource fundingSource) {
        if (fundingSource == null) {
            return  R.drawable.poynt_unkown_card_sm;
        }

        Card card = fundingSource.getCard();
        if (card == null) {
            return  R.drawable.poynt_unkown_card_sm;
        }

        return getCardLogoResourceId(card.getType(), fundingSource.getAccountType());
    }


   /* public static int getCardLogoResourceId(CardType cardType, FundingSourceAccountType accountType) {
        int resourceId;
        if (accountType != null && accountType == FundingSourceAccountType.EBT) {
            resourceId = R.drawable.ebt_dark_ic;
        } else {
            if(cardType == null) {
                cardType = CardType.OTHER;
            }
            switch (cardType){
                case AMERICAN_EXPRESS:
                    resourceId = R.drawable.poynt_amex_logo_sm;
                    break;
                case VISA:
                    resourceId = R.drawable.poynt_visa_logo_sm;
                    break;
                case MASTERCARD:
                    resourceId = R.drawable.poynt_master_logo_sm;
                    break;
                case MAESTRO:
                    resourceId = R.drawable.poynt_maestro_logo_sm;
                    break;
                case DISCOVER:
                    resourceId = R.drawable.poynt_discover_logo_sm;
                    break;
                case ALIPAY:
                    resourceId = R.drawable.alipay_txn_dark_ic;
                    break;
                case BANCOMAT:
                    resourceId = R.drawable.poynt_bancomat_ic;
                    break;
                case UNIONPAY:
                    resourceId = R.drawable.pf_poynt_unionpay_blue_ic;
                    break;
                case INTERAC:
                    resourceId = R.drawable.pf_poynt_interac_ic;
                    break;
                case PAYPAL:
                    resourceId = R.drawable.pf_paypal_ic;
                    break;
                default:
                    resourceId = R.drawable.poynt_unkown_card_sm;
                    break;
            }
        }
        return resourceId;
    }

    public static Drawable getWhiteCardLogo(Context context, CardType cardType) {
        return getWhiteCardLogo(context.getResources(), cardType);
    }

    public static Drawable getWhiteCardLogo(final Resources resources, CardType cardType) {
        Drawable drawable;
        if (cardType == CardType.AMERICAN_EXPRESS) {
            drawable = resources.getDrawable(R.drawable.amex_white);
        } else if (cardType == CardType.VISA) {
            drawable = resources.getDrawable(R.drawable.visa_white);
        } else if (cardType == CardType.MASTERCARD) {
            drawable = resources.getDrawable(R.drawable.mastercard_white);
        } else if (cardType == CardType.MAESTRO) {
            drawable = resources.getDrawable(R.drawable.maestro_white);
        } else if (cardType == CardType.DISCOVER) {
            drawable = resources.getDrawable(R.drawable.discover_white);
        } else if (cardType == CardType.UNIONPAY) {
            drawable = resources.getDrawable(R.drawable.unionpay_white);
        } else if (cardType == CardType.PAYPAL) {
            drawable = resources.getDrawable(R.drawable.paypal_white);
        } else if (cardType == CardType.BANCOMAT) {
            drawable = resources.getDrawable(R.drawable.bancomat_white);
        }  else {
            drawable = resources.getDrawable(R.drawable.unknown_white);
        }

        return drawable;
    }


    public static Drawable getSecondScreenWhiteCardLogo(Resources resources, CardType cardType) {

        Drawable drawable;
        if (cardType == CardType.AMERICAN_EXPRESS) {
            drawable = resources.getDrawable(R.drawable.consumer_screen_white_amex);
        } else if (cardType == CardType.VISA) {
            drawable = resources.getDrawable(R.drawable.consumer_screen_white_visa);
        } else if (cardType == CardType.MASTERCARD) {
            drawable = resources.getDrawable(R.drawable.consumer_screen_white_mastercard);
        } else if (cardType == CardType.MAESTRO) {
            drawable = resources.getDrawable(R.drawable.consumer_screen_white_maestro);
        } else if (cardType == CardType.DISCOVER) {
            drawable = resources.getDrawable(R.drawable.consumer_screen_white_discover);
        } else {
            drawable = resources.getDrawable(R.drawable.unknown_white);
        }
        return drawable;
    }


    public static CardType cardTypeByFirst6(String first6)
            throws NumberFormatException {
        if (first6 == null)
            throw new NumberFormatException("Null credit card number");

        CreditCardType creditCardType = CreditCardType.detect(first6);
        switch (creditCardType) {
            case VISA:
                return CardType.VISA;
            case MASTERCARD:
                return CardType.MASTERCARD;
            case AMERICAN_EXPRESS:
                return CardType.AMERICAN_EXPRESS;
            case DISCOVER:
                return CardType.DISCOVER;
            case DINERS_CLUB:
                return CardType.DINERS_CLUB;
            case JCB:
                return CardType.JCB;
            case MAESTRO:
                return CardType.MAESTRO;
            case UNKNOWN:
            default:
                return CardType.OTHER;
        }
    }

    /**
     * Returns the first and last name in a cardhodler name (eg. CardUser/John)
     *
     * @param cardHolderName
     * @return string array index 0 containing first name, and 1 containing last name
     */
   /* public static String[] splitCardHolderName(String cardHolderName) {
        Ln.d("CardHolderName provided (%s)", cardHolderName);
        if (Strings.notEmpty(cardHolderName)) {
            int iNameDelim = cardHolderName.indexOf("/");
            if (iNameDelim == -1) {
                Ln.e("Missing delimiter [/] in account holder name (%s)", cardHolderName);
                String[] name = new String[2];
                name[1] = cardHolderName.trim();
                name[0] = "";
                return name;
            } else {
                String[] name = new String[2];
                name[1] = cardHolderName.substring(0, iNameDelim).trim();
                name[0] = cardHolderName.substring(iNameDelim + 1).trim();
                return name;
            }
        }

        return null;
    }

    public static String getCardHolderNameForDisplay(Card card) {
        StringBuilder nameForDisplay = new StringBuilder();
        if (card != null) {
            if (Strings.notEmpty(card.getCardHolderFirstName())) {
                nameForDisplay.append(card.getCardHolderFirstName());
                if (Strings.notEmpty(card.getCardHolderLastName())) {
                    nameForDisplay.append(" ");
                    nameForDisplay.append(card.getCardHolderLastName());
                }
            } else if (Strings.notEmpty(card.getCardHolderLastName())) {
                nameForDisplay.append(card.getCardHolderLastName());
            } else if (Strings.notEmpty(card.getCardHolderFullName())) {
                String[] names = splitCardHolderName(card.getCardHolderFullName());
                if (names != null && names.length == 2) {
                    nameForDisplay.append(names[0]);
                    nameForDisplay.append(" ");
                    nameForDisplay.append(names[1]);
                }
            }
        }
        return nameForDisplay.toString();
    }


    public static String getNumberLast4(Card card) {

        String cardLast4Digits = "";

        if (card == null) {
            return cardLast4Digits;
        }

        if (Strings.notEmpty(card.getNumberLast4())) {
            cardLast4Digits = card.getNumberLast4();
        }

        return cardLast4Digits;
    }*/

    /**
     * This function is added specifically to make sure we always send bin range
     * as only 6 digits to server even if firmware returns more than 6 digits as
     * per PCI-DSS compliance.
     * @param binRange: value returned by firmware in tag PaymentTags.PAN_FIRST_6
     * @return: bin value modified to 6 digits.
     */
    public static String getPanFirst6(String binRange) {
        if(!TextUtils.isEmpty(binRange) && binRange.length() > 6) {
            binRange = binRange.substring(0,6);
        }
        return binRange;
    }

    public static String getLogoUrlForPngImage(String logoUrl) {
        if(TextUtils.isEmpty(logoUrl)) {
            return null;
        }

        String pngExtension = ".png";
        String svgExtension = ".svg";

        if(logoUrl.endsWith(pngExtension)) {
            return logoUrl;
        }
        if(logoUrl.endsWith(svgExtension)) {
            logoUrl = logoUrl.substring(0, logoUrl.length()-4).concat(pngExtension);
            return logoUrl;
        }
        logoUrl = logoUrl.concat(pngExtension);
        return logoUrl;
    }
}
