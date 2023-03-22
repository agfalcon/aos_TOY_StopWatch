package kgb.plum.stopwatch

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import kgb.plum.stopwatch.databinding.ActivityMainBinding
import kgb.plum.stopwatch.databinding.DialogCountdownSettingBinding
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var countDownSecond = 10
    private var currentCountDownDeciSecond = countDownSecond * 10
    private var currentDeciSecond = 0
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countDownTextView.setOnClickListener {
            showCountDownSettingDialog()
        }

        binding.startButton.setOnClickListener {
            start()
            binding.startButton.isVisible = false
            binding.stopButton.isVisible = false
            binding.pauseButton.isVisible = true
            binding.checkButton.isVisible = true
        }
        binding.stopButton.setOnClickListener {
            showAlertDialog()
        }
        binding.pauseButton.setOnClickListener {
            pause()
            binding.startButton.isVisible = true
            binding.stopButton.isVisible = true
            binding.pauseButton.isVisible = false
            binding.checkButton.isVisible = false
        }
        binding.checkButton.setOnClickListener {
            lap()
        }
        initCountDownViews()
    }

    private fun initCountDownViews(){
        binding.countDownTextView.text = String.format("%02d", countDownSecond)

        binding.progressBar.progress = 100
    }

    private fun lap() {
        if(currentDeciSecond==0) return
        val container = binding.lapContainerLinearLayout
        TextView(this).apply{
            textSize = 20f
            gravity = Gravity.CENTER
            val minute = currentDeciSecond.div(10)/60
            val second = currentDeciSecond.div(10)%60
            val deciSecond = currentDeciSecond % 10
            text = container.childCount.inc().toString() + String.format(". %02d:%02d %01d", minute, second, deciSecond)
            setPadding(30)
        }.let {
            container.addView(it, 0)
        }
    }

    private fun pause() {
        timer?.cancel()
        timer = null
    }

    private fun stop() {
        currentDeciSecond = 0
        binding.startButton.isVisible = true
        binding.stopButton.isVisible = true
        binding.pauseButton.isVisible = false
        binding.checkButton.isVisible = false
        binding.timeTextView.text = "00:00"
        binding.tickTextView.text = "0"
        binding.countdownGroup.isVisible = true
        initCountDownViews()
        binding.lapContainerLinearLayout.removeAllViews()
    }

    private fun start() {
        timer = timer(initialDelay = 0, period = 100) {
            if (currentCountDownDeciSecond == 0) {
                currentDeciSecond += 1
                val minute = currentDeciSecond.div(10) / 60
                val second = currentDeciSecond.div(10) % 60
                val deciSeconds = currentDeciSecond % 10
                runOnUiThread {
                    binding.timeTextView.text = String.format("%02d:%02d", minute, second)
                    binding.tickTextView.text = deciSeconds.toString()
                    binding.countdownGroup.isVisible = false
                }
            } else {
                currentCountDownDeciSecond -= 1
                val seconds = currentCountDownDeciSecond/10
                binding.root.post{
                    binding.countDownTextView.text = String.format("%02d", seconds)
                    binding.progressBar.progress = ((currentCountDownDeciSecond / (countDownSecond * 10f)) * 100).toInt()
                }
            }
            if(currentDeciSecond == 0 && currentCountDownDeciSecond < 31 && currentCountDownDeciSecond%10 ==0){
                val toneType = if(currentCountDownDeciSecond ==0) ToneGenerator.TONE_CDMA_HIGH_L else ToneGenerator.TONE_CDMA_ANSWER
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
                    .startTone(toneType, 100)
            }
        }
    }

    private fun showCountDownSettingDialog() {
        AlertDialog.Builder(this).apply {
            val dialogBinding = DialogCountdownSettingBinding.inflate(layoutInflater)
            with(dialogBinding.countDownSecondPicker) {
                maxValue = 20
                minValue = 0
                value = countDownSecond
            }
            setTitle("카운트다운 설정")
            setView(dialogBinding.root)
            setPositiveButton("확인") { _, _ ->
                countDownSecond = dialogBinding.countDownSecondPicker.value
                currentCountDownDeciSecond = countDownSecond * 10
                binding.countDownTextView.text = String.format("%02d", countDownSecond)
            }
            setNegativeButton("취소", null)
        }.show()
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("종료하시겠습니까?")
            setPositiveButton("네") { _, _ ->
                stop()
            }
            setNegativeButton("아니요", null)
        }.show()
    }

}