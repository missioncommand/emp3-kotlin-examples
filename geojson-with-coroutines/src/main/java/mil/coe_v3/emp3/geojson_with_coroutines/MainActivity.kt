package mil.coe_v3.emp3.geojson_with_coroutines

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import kotlinx.coroutines.experimental.*
import mil.coe_v3.emp3.geojson_with_coroutines.databinding.ActivityMainBinding
import mil.emp3.api.Overlay
import mil.emp3.api.enums.MapStateEnum
import mil.emp3.api.events.MapStateChangeEvent
import mil.emp3.api.events.MapUserInteractionEvent
import mil.emp3.api.exceptions.EMP_Exception
import mil.emp3.api.interfaces.IFeature
import mil.emp3.api.interfaces.IMap
import mil.emp3.api.listeners.IMapInteractionEventListener
import mil.emp3.api.listeners.IMapStateChangeEventListener
import mil.emp3.json.geoJson.GeoJsonParser
import org.cmapi.primitives.IGeoAltitudeMode
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var map: IMap
    private var overlay = Overlay()
    private lateinit var binding: ActivityMainBinding
    private val camera = mil.emp3.api.Camera()

    fun onClickCancel(view: View) = finish()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.gwc = this
        // Cancel button exits the app
        camera.name = "Main Cam"
        camera.altitudeMode = IGeoAltitudeMode.AltitudeMode.ABSOLUTE
        camera.altitude = 2e6
        camera.heading = 0.0
        camera.latitude = 40.0
        camera.longitude = -100.0
        camera.roll = 0.0
        camera.tilt = 0.0
        val geoJsonAdapter = ArrayAdapter.createFromResource(this,
                R.array.geojson, android.R.layout.simple_spinner_item)
        val geoJson = binding.geojsonfile as Spinner
        geoJson.adapter = geoJsonAdapter

        map = binding.map as IMap
        try {
            map.addMapStateChangeEventListener { mapStateChangeEvent ->
                Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.newState)
                when (mapStateChangeEvent.newState) {
                    MapStateEnum.MAP_READY -> try {
                        this@MainActivity.map.addOverlay(this@MainActivity.overlay, true)
                        map.setCamera(camera, false)
                    } catch (empe: EMP_Exception) {
                        empe.printStackTrace()
                    }

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

    suspend fun drawFeatures(resourceId : Int) : Unit {
        val stream = applicationContext.resources.openRawResource(resourceId)
        val featureList = GeoJsonParser.parse(stream)
        for (feature in featureList) {
            this@MainActivity.overlay.addFeature(feature, true)
            println("in feature  ${feature.name}")
        }
        stream.close()
    }

    // start all asynchronously and wait for all

    fun onClickAll(view : View) {
        val cmapi = async(CommonPool, CoroutineStart.LAZY) {
            drawFeatures(R.raw.cmapi)
        }
        val communes = async(CommonPool, CoroutineStart.LAZY) {
            drawFeatures(R.raw.communes_69)
        }
        val random = async(CommonPool, CoroutineStart.LAZY) {
            drawFeatures(R.raw.random_geoms)
        }
        val rhone = async(CommonPool, CoroutineStart.LAZY) {
            drawFeatures(R.raw.rhone)
        }
        val stations = async(CommonPool, CoroutineStart.LAZY) {
            drawFeatures(R.raw.stations)
        }
        launch(CommonPool) {
            cmapi.await()
            communes.await()
            random.await()
            rhone.await()
            stations.await()
        }

        // locate camera high over communes
        camera.latitude = 45.7
        camera.longitude = 5.2
        camera.altitude = 1e6
        camera.apply(false)
    }

    fun onClickClear(view : View) = this@MainActivity.overlay.clearContainer()

    fun onClickOK(view: View) {
        try {
            val selection = binding.geojsonfile.selectedItem.toString()
            val camera = this@MainActivity.map.camera
            when (selection) {
                "communes" -> {
                    val communes = async(CommonPool) {
                        drawFeatures(R.raw.communes_69)
                    }
                    launch(CommonPool) {
                        communes.await()
                    }
                    camera.latitude = 45.7
                    camera.longitude = 5.2
                    camera.altitude = 2e5
                }
                "random" -> {
                    launch(CommonPool) {
                        drawFeatures(R.raw.random_geoms)
                    }
                    camera.latitude = 48.0
                    camera.longitude = -1.0
                    camera.altitude = 1e4
                }
                "rhone" -> {
                    val stream = applicationContext.resources.openRawResource(R.raw.rhone)
                    val featureList = GeoJsonParser.parse(stream)
                    for (feature in featureList) {
                        this@MainActivity.overlay.addFeature(feature, true)
                    }
                    stream.close()
                    camera.latitude = 46.2
                    camera.longitude = 6.0
                    camera.altitude = 5e5
                }
                "cmapi" -> {
                    val stream = applicationContext.resources.openRawResource(R.raw.cmapi)
                    val featureList = GeoJsonParser.parse(stream)
                    for (feature in featureList) {
                        this@MainActivity.overlay.addFeature(feature, true)
                    }
                    stream.close()
                    camera.latitude = 0.0
                    camera.longitude = 20.0
                    camera.altitude = 2e6
                }
                "stations" -> {
                    val stream = applicationContext.resources.openRawResource(R.raw.stations)
                    val featureList = GeoJsonParser.parse(stream)
                    for (feature in featureList) {
                        this@MainActivity.overlay.addFeature(feature, true)
                    }
                    stream.close()
                    camera.latitude = 38.7
                    camera.longitude = -77.2
                    camera.altitude = 1e5
                }
            }
            camera.apply(false)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: EMP_Exception) {
            e.printStackTrace()
        }
    }


    companion object {

        private val TAG = MainActivity::class.java.simpleName
    }
}
