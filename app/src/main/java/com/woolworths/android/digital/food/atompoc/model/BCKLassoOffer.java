package com.woolworths.android.digital.food.atompoc.model;

import android.util.Log;

import com.bluecats.sdk.BCCategory;
import com.bluecats.sdk.BCCustomValue;

import static com.woolworths.android.digital.food.atompoc.ConstantsKt.*;
public class BCKLassoOffer {
	private final static String TAG = "BCKLassoOffer";
	
	public BCCategory category;
	public String serialNumber;
	
	//Data stuff
	public String offerType;
	public String itemCode;
	public Integer offerAmount;
	public String message;
	
	//Apppearance Stuff
	public String titleText;
	public String detailText;
	public String bgColor; //#aarrggbb
	public String textColor;
	public String logoURL;
	public String storeName;
	
	public BCKLassoOffer(BCCategory c, String sn) {
		category = c;
		serialNumber = sn;
		if (c != null) {
			for (BCCustomValue cv : category.getCustomValues()) {
				if (BCK_KITTY_OFFER_TRANSACTION_TYPE_KEY.equals(cv.getKey())) {
					offerType = cv.getValue();
				} else if (BCK_KITTY_OFFER_AMOUNT_KEY.equals(cv.getKey())) {
//					offerAmount
					String str = cv.getValue();
					try {
						offerAmount = Integer.valueOf(str);
					} catch (NumberFormatException e) {
						Log.e(TAG, e.toString());
						offerAmount = 0;
					}
				} else if (BCK_KITTY_OFFER_CODE_KEY.equals(cv.getKey())) {
					itemCode = cv.getValue();
				} else if (BCK_KITTY_OFFER_BG_HEX_COLOR_KEY.equals(cv.getKey())) {
					bgColor = cv.getValue();
				} else if (BCK_KITTY_OFFER_TEXT_HEX_COLOR_KEY.equals(cv.getKey())) {
					textColor = cv.getValue();
				} else if (BCK_KITTY_OFFER_TITLE_TEXT_KEY.equals(cv.getKey())) {
					titleText = cv.getValue();
				} else if (BCK_KITTY_OFFER_DETAIL_TEXT_KEY.equals(cv.getKey())) {
					detailText = cv.getValue();
				} else if (BCK_KITTY_OFFER_LOGO_URL_KEY.equals(cv.getKey())) {
					logoURL = cv.getValue();
				} else if (BCK_KITTY_STORE_NAME.equals(cv.getKey())) {
					storeName = cv.getValue();
				}
			}
			
		}
	}
}
