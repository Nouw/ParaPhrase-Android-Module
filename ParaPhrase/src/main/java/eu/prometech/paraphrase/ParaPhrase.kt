package eu.prometech.paraphrase

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.neovisionaries.ws.client.*
import org.json.JSONObject
import java.io.IOException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ParaPhrase(
    private var context: AppCompatActivity,
    listener: (response: JSONObject) -> Unit,
    medical: Boolean = true,
    var url: String = "",
) {
    private val LOG_TAG = "ParaPhrase"
    private var ws: WebSocket? = null

    private var permissionGranted = false
    private var audioRecord: AudioRecord? = null
    private var thread: Thread? = null

    private val SAMPLE_RATE = 44100 // Hertz

    private val SAMPLE_INTERVAL = 5 // Milliseconds

    private val SAMPLE_SIZE = 6 // Bytes (default: 2)

    private val BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2 //Byte

    init {
        if (url == "") {
            println("${LOG_TAG} - Running in production mode")
            url = "http://161.35.154.113:8080"
        }

        if (!permissionGranted) {
            askPermission()
        }

        // Create websocket connection and add custom listener
        ws = WebSocketFactory()
            .createSocket(url)
            .addListener(MessageListener(listener))
            .addHeader("Medical", medical.toString())
    }

    companion object {
        fun getLogTag(): String {
            return "ParaPhrase"
        }
    }

    private fun askPermission() {
        permissionGranted = ActivityCompat.checkSelfPermission(
            context,
            arrayOf(Manifest.permission.RECORD_AUDIO)[0]) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10
        )
    }

    public fun connect() {
        val executor = Executors.newSingleThreadExecutor()
        val future: Future<WebSocket> = ws!!.connect(executor)

        try {
            future.get()
        } catch (e: OpeningHandshakeException) {
            throw e
        } catch (e: HostnameUnverifiedException) {
            throw e
        } catch (e: WebSocketException) {
            throw e
        }

        audioRecord!!.startRecording()

        this.thread = Thread(Runnable {
            val buf = ByteArray(BUF_SIZE)

            try {
                while (audioRecord!!.recordingState == 3 && ws!!.state == WebSocketState.OPEN) {
                    val msg = JSONObject(mapOf<String, Any>(
                        "message_type" to "transcribe",
                        "data" to buf
                    ))

                    ws!!.sendBinary(msg.toString().toByteArray())

                    Thread.sleep(SAMPLE_INTERVAL.toLong(), 0)
                }

                // Stop recording and release resources
                audioRecord!!.stop()
                audioRecord!!.release()
                ws!!.disconnect()

                return@Runnable
            } catch (e: InterruptedException) {
                Log.e(LOG_TAG, "InterruptedException: $e")
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException: $e")
            } catch (e: UnknownHostException) {
                Log.e(LOG_TAG, "UnknownHostException: $e")
            } catch (e: IOException) {
                Log.e(LOG_TAG, "IOException: $e")
            }
        })

        thread!!.start()
    }

    public fun disconnect() {
        thread!!.interrupt()
        audioRecord!!.stop()
        audioRecord!!.release()
        ws!!.disconnect()
    }
}