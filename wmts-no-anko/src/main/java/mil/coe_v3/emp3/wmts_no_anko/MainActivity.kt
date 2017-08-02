package mil.coe_v3.emp3.wmts_no_anko

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import java.lang.Exception
import java.util.ArrayList
import android.view.View
import mil.coe_v3.emp3.wmts_no_anko.databinding.ActivityMainBinding

import org.cmapi.primitives.IGeoAltitudeMode

import mil.emp3.api.WMTS
import mil.emp3.api.enums.MapStateEnum
import mil.emp3.api.exceptions.EMP_Exception
import mil.emp3.api.interfaces.IMap


class MainActivity : AppCompatActivity() {
    lateinit private var wmtsService: WMTS
    lateinit private var oldWMTSService: WMTS
    lateinit private var map: IMap
    lateinit var dataBinding : ActivityMainBinding
    val camera = mil.emp3.api.Camera()

    fun onClickCancel(view : View) { finish() }

    fun onClickZoomOut(view : View) {
        var initAltitude = camera.altitude
        if (initAltitude <= 1e8 / 1.2) {
            initAltitude *= 1.2
            camera.altitude = initAltitude
            camera.apply(false)
            Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.latitude
                    + " longitude " + camera.longitude)
        } else {
            Toast.makeText(this@MainActivity, "Can't zoom out any more, altitude " + initAltitude, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickZoomIn(view : View) {
        var initAltitude = camera.altitude
        if (initAltitude >= 1.2) {
            initAltitude /= 1.2
            camera.altitude = initAltitude
            camera.apply(false)
            Log.i(TAG, "camera altitude " + initAltitude + " latitude " + camera.latitude
                    + " longitude " + camera.longitude)
        } else {
            Toast.makeText(this@MainActivity, "Can't zoom in any more, altitude " + initAltitude, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickPanLeft(view : View)  {
        try {
            var dPan = camera.heading

            dPan -= 5.0
            if (dPan < 0.0) {
                dPan += 360.0
            }

            camera.heading = dPan
            camera.apply(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickPanRight(view : View)  {
        try {
            var dPan = camera.heading

            dPan += 5.0
            if (dPan >= 360.0) {
                dPan -= 360.0
            }

            camera.heading = dPan
            camera.apply(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickTiltUp(view : View)  {
        try {
            var dTilt = camera.tilt

            if (dTilt <= 85.0) {
                dTilt += 5.0
                camera.tilt = dTilt
                camera.apply(false)
            } else
                Toast.makeText(this@MainActivity, "Can't tilt any higher", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickTiltDown(view : View)  {
        try {
            var dTilt = camera.tilt

            if (dTilt >= -85.0) {
                dTilt -= 5.0
                camera.tilt = dTilt
                camera.apply(false)
            } else
                Toast.makeText(this@MainActivity, "Can't tilt any lower", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickRollCCW(view : View)  {
        try {
            var dRoll = camera.roll

            if (dRoll >= -175.0) {
                dRoll -= 5.0
                camera.tilt = dRoll
                camera.apply(false)
            } else
                Toast.makeText(this@MainActivity, "Can't tilt any lower", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickRollCW(view : View)  {
        try {
            var dRoll = camera.roll

            if (dRoll <= 175.0) {
                dRoll += 5.0
                camera.tilt = dRoll
                camera.apply(false)
            } else
                Toast.makeText(this@MainActivity, "Can't tilt any lower", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClickOK (view : View) {

        try {
            val url = dataBinding.UrlText.text.toString()
            val layer = dataBinding.LayerText.text.toString()
            val layers = ArrayList<String>()
            layers.add(layer)
            wmtsService = WMTS(
                    url, null, null, layers)
            map.addMapService(this@MainActivity.wmtsService)
            val camera = map.camera
            camera.latitude = 64.27
            camera.longitude = 10.12
            camera.altitude = 225000.0
            camera.apply(false)
            if (wmtsService != null) {
                if (wmtsService !== oldWMTSService) {
                    if (oldWMTSService != null)
                        map.removeMapService(oldWMTSService)
                    else
                        Log.i(TAG, "No previous WMTS service")
                    map.addMapService(wmtsService)
                    oldWMTSService = wmtsService
                } else {
                    Log.i(TAG, "Layer unchanged")
                }
            } else {
                Log.i(TAG, "Got null WMTS service")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.cwms = this
        camera.name = "Main Cam"
        camera.altitudeMode = IGeoAltitudeMode.AltitudeMode.ABSOLUTE
        camera.altitude = 1e6
        camera.heading = 0.0
        camera.latitude = 40.0
        camera.longitude = -100.0
        camera.roll = 0.0
        camera.tilt = 0.0

        map = dataBinding.map
        try {
            map.addMapStateChangeEventListener { mapStateChangeEvent ->
                Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.newState)
                if (mapStateChangeEvent.newState ==
                        MapStateEnum.MAP_READY) try {
                    map.setCamera(camera, false)
                } catch (empe: EMP_Exception) {
                    empe.printStackTrace()
                }
            }
        } catch (e: EMP_Exception) {
            Log.e(TAG, "addMapStateChangeEventListener", e)
        }

        try {
            map.addMapInteractionEventListener { mapUserInteractionEvent -> Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.point.x) }
        } catch (e: EMP_Exception) {
            Log.e(TAG, "addMapInteractionEventListener", e)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
    }
}
