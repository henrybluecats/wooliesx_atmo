package com.woolworths.android.digital.food.atompoc

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.bluecats.sdk.*
import com.google.gson.Gson
import com.woolworths.android.digital.food.atompoc.DataRequestHelper.notDeclinedPayload
import com.woolworths.android.digital.food.atompoc.model.BCKLassoOffer
import com.woolworths.android.digital.food.atompoc.widget.CircularProgressDrawable

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class TapOnActivity : AppCompatActivity() {

    private val TAG = "TapOnActivity"
    val BCK_KITTY_REGISTER_KEY = "KittyRegKey"

    private lateinit var mBeaconManager: BCBeaconManager
    lateinit var mBCLassoManager : BCLassoManager

    private val PERMISSION_RESPONSE: Int = 0x0110

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        title =  getString(R.string.title_tap_on) + " - v4"
        mBCLassoManager = BCLassoManager()


        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
        // BlueCats
        // dont use a service anymore, just run direct from activity in foregroun
        */
        //val intent = Intent(this, KittyService::class.java)
        //startService(intent)

        mBeaconManager = BCBeaconManager()
        startCircle()

        refresh.setOnClickListener {
            mReadyForTapping = false
            refresh.visibility = View.GONE
        }

    }

    private val indeterminedAnimator: Animator? = null
    private var circle: CircularProgressDrawable? = null

    private fun startCircle() {
        if (indeterminedAnimator != null) {
            indeterminedAnimator.cancel()
        }
        circle = CircularProgressDrawable.Builder().setRingWidth(6)
                .setRingColor(resources.getColor(android.R.color.holo_blue_light))
                .create()
        iv_circle.setImageDrawable(circle)
        iv_circle.visibility = View.VISIBLE
        circle?.isIndeterminate = true
    }

    private fun showProgress(show: Boolean) {
        runOnUiThread {
            iv_circle.visibility = (if (show) View.VISIBLE else View.GONE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        Handler().post {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_RESPONSE)
            }
        }

        /*
        // BlueCats
        // start the BlueCatsSDK and run in foreground mode
        */
        startBlueCatsSDK()
        BlueCatsSDK.didEnterForeground()

        mReadyForTapping = false

        showMessage("Looking for tap on device")
        showProgress(true)
        mBeaconManager.registerCallback(mBeaconManagerCallback)

        /*
        // BlueCats
        // register Lasso Manager Callback here
        */
        mBCLassoManager.registerLassoManagerCallback(mWOWBCLassoManagerCallback)
        mBCLassoManager.addLassoKeys(Arrays.asList(BCK_KITTY_REGISTER_KEY))
        mBCLassoManager.setEnabled(true)

        Handler().postDelayed({
            DataRequestHelper.makeDummyServicePayload(DataRequestHelper.notDeclinedPayload)
        }, 2000);

    }

    public override fun onPause() {
        BlueCatsSDK.didEnterBackground()
        mBCLassoManager.registerLassoManagerCallback(null)
        mBCLassoManager.setEnabled(false)
        mBeaconManager?.unregisterCallback(mBeaconManagerCallback)

        super.onPause()
    }

    /*
    // BlueCats
    // make a call to start the SDK on each activity. If the SDK is already running it will do nothing
    // else it will start it running again in high frequency mode
    */
    private fun startBlueCatsSDK() {
        val options = HashMap<String, String>()
        options[BlueCatsSDK.BC_OPTION_ENERGY_SAVER_SCAN_STRATEGY] = "false" // scan in high frequency all the time
        options[BlueCatsSDK.BC_OPTION_CROWD_SOURCE_BEACON_UPDATES] = "false" // don't bother beacon updates
//        options[BlueCatsSDK.BC_OPTION_USE_RSSI_SMOOTHING] = "true" // don't try to smooth the ranging as it takes longer
//        options["BC_OPTION_SCAN_TIME"] = "20000" //20 seconds

        BlueCatsSDK.setOptions(options)
        BlueCatsSDK.startPurringWithAppToken(this, BC_APP_TOKEN)
    }


    private lateinit var mLassoOffer: BCKLassoOffer


    private var mBeaconManagerCallback: BCBeaconManagerCallback = object : BCBeaconManagerCallback() {
        override fun didRangeBeacons(beacons: List<BCBeacon>) {

            if (mReadyForTapping) return

            Log.d(TAG," # of beacons found " + beacons.size)
            loop@ for (beacon in beacons) {

                /*
                // BlueCats
                // check for null categories
                */
                if (beacon.categories == null) {
                    continue;
                }

                Log.d(TAG," # of categories found " + beacon.categories.size + " for " + beacon.beaconID)

                for (category in beacon.categories){
                    if (category.name.equals(BCK_KITTY_CATEGORY_STORE)){
                        mLassoOffer = BCKLassoOffer(category, beacon.serialNumber)
                        mReadyForTapping = true

                        runOnUiThread {
                            showMessage("Hold Phone near Tap On Device")
                            iv_circle.visibility = View.GONE
                        }

                        break@loop
                    }
                }
            }
            //showMessage("didRangeBeacons")
        }

        override fun didExitBeacons(beacons: List<BCBeacon>?) {
            mReadyForTapping = false
            showMessage("didExitBeacons")
        }
    }

    private var mReadyForTapping = false

    private var mWOWBCLassoManagerCallback: WOWBCLassoManagerCallback = object: WOWBCLassoManagerCallback() {

        override fun onRedeem() {
            vibrate()
            showProgress(true)
            showMessage("Checking in, please wait...")
        }

        override fun getRequest(): String? {
            return null
        }

        override fun onRange(lasso: BCLasso) {
            showMessage("Accuracy " + String.format("%.3fm", lasso.beacon.accuracy) + " : Radius " + lasso.radius)
        }

        override fun onOutRange(lasso: BCLasso) {
            showProgress(true)
            showMessage("Out of range now")
        }

        override fun onNoNetwork() {
            runOnUiThread({
                showRetryBtn()
            })

        }

        override fun onTapError() {
            showMessage("Tap on failed, please try again")
        }

        override fun onSuccess(response: String) {
            mBeaconManager?.unregisterCallback(mBeaconManagerCallback)
            showHomeActivity(response)
        }

    }

    private fun showRetryBtn(){
        refresh.visibility = View.VISIBLE
    }

    private fun vibrate() {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                v.vibrate(500)
            }
    }

    private fun showHomeActivity(data: String){
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("DATA", data)
        startActivity(intent)
        finish()
    }


    private fun showMessage(msg: String) {
        runOnUiThread {
            progressStatus.text = msg
        }
    }

}
