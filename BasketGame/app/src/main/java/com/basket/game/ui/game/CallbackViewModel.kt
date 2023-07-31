package com.basket.game.ui.game

import androidx.lifecycle.ViewModel

class CallbackViewModel: ViewModel() {
    var callback: (() -> Unit)? = null
}