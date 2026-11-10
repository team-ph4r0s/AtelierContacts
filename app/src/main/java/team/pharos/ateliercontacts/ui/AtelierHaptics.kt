package team.pharos.ateliercontacts.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AtelierHaptics(context: Context, private val enabled: () -> Boolean) {
    private val vibrator = context.getSystemService(Vibrator::class.java)
    fun tick() = pulse(8L)
    fun confirm() = if (Build.VERSION.SDK_INT >= 26) vibrate(VibrationEffect.createWaveform(longArrayOf(0, 16, 35, 24), -1)) else pulse(24L)
    fun warning() = if (Build.VERSION.SDK_INT >= 26) vibrate(VibrationEffect.createWaveform(longArrayOf(0, 28, 55, 28), -1)) else pulse(32L)
    private fun pulse(duration: Long) { if (Build.VERSION.SDK_INT >= 26) vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)) }
    private fun vibrate(effect: VibrationEffect) { if (enabled() && vibrator?.hasVibrator() == true) vibrator.vibrate(effect) }
}
