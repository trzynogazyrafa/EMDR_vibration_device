package com.example.emdrvibration

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.example.emdrvibrationapp.R
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.MessageClient

class MainActivity : Activity(), MessageClient.OnMessageReceivedListener {

    private lateinit var vibrator: Vibrator
    private lateinit var handler: Handler
    private var toggle = true
    private var delayTime = 600L // Odstęp w ms
    private var vibrationTime = 100L // Czas trwania wibracji
    private var nodeId: String? = null // ID zegarka

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        handler = Handler(Looper.getMainLooper())

        val delaySeekBar = findViewById<SeekBar>(R.id.delaySeekBar)
        val vibrationSeekBar = findViewById<SeekBar>(R.id.vibrationSeekBar)
        val startButton = findViewById<Button>(R.id.startButton)
        val statusText = findViewById<TextView>(R.id.statusText)

        delaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                delayTime = progress.toLong() + 100
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        vibrationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vibrationTime = progress.toLong() + 50
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startButton.setOnClickListener {
            startAlternatingVibrations()
            statusText.text = "Wibracje uruchomione"
        }

        // Pobierz ID zegarka
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isNotEmpty()) {
                nodeId = nodes[0].id // Zakładamy, że pierwszy node to nasz zegarek
                statusText.text = "Połączono z zegarkiem"
            } else {
                statusText.text = "Nie znaleziono zegarka"
            }
        }
    }

    private fun startAlternatingVibrations() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (toggle) {
                    vibratePhone()
                } else {
                    nodeId?.let { id ->
                        Wearable.getMessageClient(this@MainActivity).sendMessage(id, "/vibrate", null)
                    }
                }
                toggle = !toggle
                handler.postDelayed(this, delayTime)
            }
        }, delayTime)
    }

    private fun vibratePhone() {
        val effect = VibrationEffect.createOneShot(vibrationTime, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(p0: MessageEvent) {
        // Tu nie robimy nic, bo telefon tylko wysyła
    }
}
