package com.basket.game.domain.other

import android.content.Context

class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getBestScores(): Int = sp.getInt("BEST", 0)
    fun setBestScores(score: Int) = sp.edit().putInt("BEST", score).apply()

    fun getVibroState(): Boolean = sp.getBoolean("VIBRO", true)
    fun setVibroState(value: Boolean) = sp.edit().putBoolean("VIBRO", value).apply()

    fun setSpeed(value: Int) = sp.edit().putInt("SPEED", value).apply()
    fun getSpeed(): Int = sp.getInt("SPEED", 5)
}