package com.woolworths.android.digital.food.atompoc

import android.util.Log
import com.bluecats.sdk.BCError
import com.bluecats.sdk.BCLasso
import com.bluecats.sdk.BCLassoManagerCallback
import java.nio.ByteBuffer
import java.util.*

abstract class WOWBCLassoManagerCallback : BCLassoManagerCallback {

    private val TAG = "WOWBCLassoManager"

    private var mUserTapped = false
    private var mProcessing = false
    private var mLasso: BCLasso? = null

    // method which determines whether the request should be sent or not. If its sent once, do not send unless error
    override fun shouldBeginRedemptionRequest(lasso: BCLasso): Boolean {
        Log.d(TAG, "should BeginRedemptionRequest User Tapped " + mUserTapped + " Ready " )

        if (mUserTapped || DataRequestHelper.serviceKeyHMAC == null || DataRequestHelper.serviceKeyPayload == null)
            return false;

        mProcessing = true

        return true

    }

    override fun getDataRequestForLasso(p0: BCLasso?): ByteBuffer? {
        if (!mProcessing || DataRequestHelper.serviceKeyHMAC == null || DataRequestHelper.serviceKeyPayload == null)
            return null

        onRedeem()
        return DataRequestHelper.makeDataRequest(p0?.beacon?.bluetoothAddress);
    }

    override fun getMappingForLasso(lasso: BCLasso): Map<String, String>? {

        val param = HashMap<String, String>()

//        param.put(BCLasso.BCK_LASSO_VALUE_TYPE_AUX_DATA_KEY, "requestMethod")
//        param.put(BCLasso.BCK_LASSO_VALUE_TYPE_OFFER_ID_KEY, "oid")
//        param.put(BCLasso.BCK_LASSO_VALUE_TYPE_AMOUNT_KEY, "2")
//        param.put(BCLasso.BCK_LASSO_VALUE_TYPE_TRANSACTION_KEY, "Code")

        return param
    }

    //gets us the response
    override fun didLassoResponse(lasso: BCLasso?, respMap: Map<String, Any>?, error: BCError?) {

        Log.d(TAG, "Response : --- " + respMap?.get("Message").toString());
        var hasResponse = respMap?.get("Message")
        val rspCode = respMap?.get(BCLasso.BCK_LASSO_VALUE_TYPE_RESPONSE_CODE_KEY);
        Log.d(TAG, "Response code: --- " + rspCode.toString() + " -- " + rspCode);
        if (hasResponse == null) {
            hasResponse = DataRequestHelper.getMessageByCode(rspCode.toString());
        }
        val isFailed = (error != null) || (hasResponse?.toString()?:"SYS ERR").contains("SYS ERR")

        if (error != null) {
            hasResponse = "Fail: "+error.message
        }


        mProcessing = false

        if (isFailed) {
            mUserTapped = false
            onNoNetwork()
        } else {

            hasResponse?.let {
                mUserTapped = true
                onSuccess(it.toString())
            } ?: run {
                mUserTapped = false
                onTapError()
            }
        }


    }

    override fun didExitLasso(lasso: BCLasso) {
        if (mLasso == null || mLasso?.getBeacon() == null) {
            return
        }
        Log.d(TAG, "didExitLasso " + lasso.beacon.serialNumber + ", accuracy: " + lasso.beacon.accuracy)

        if (mLasso?.getBeacon()?.getSerialNumber() == lasso.beacon.serialNumber) {
            mLasso = null
        }

        onOutRange(lasso)

        return

    }

    //2nd method that gets called to find the lasso details
    override fun didRangeLasso(lasso: BCLasso) {
        if (!mProcessing)
            onRange(lasso)

        if (mLasso != null && mLasso?.getBeacon()?.getSerialNumber() == lasso.beacon.serialNumber)
            return

        mLasso = lasso


    }

    // First method that enters when the user goes near for tapping
    override fun didEnterLasso(lasso: BCLasso) {
        Log.d(TAG, "didEnterLasso " + lasso.beacon.serialNumber + ", accuracy: " + lasso.beacon.accuracy)
    }

    abstract fun onRedeem()
    abstract fun getRequest(): String?
    abstract fun onRange(lasso: BCLasso)
    abstract fun onOutRange(lasso: BCLasso)
    abstract fun onNoNetwork()
    abstract fun onTapError()
    abstract fun onSuccess(response: String)

}