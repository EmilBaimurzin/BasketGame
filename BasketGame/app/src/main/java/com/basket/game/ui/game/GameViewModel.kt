package com.basket.game.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.basket.game.core.library.random
import com.basket.game.domain.game.GameSymbol
import com.basket.game.domain.game.SymbolPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _symbolList = MutableLiveData<List<GameSymbol>>(emptyList())
    val symbolList: LiveData<List<GameSymbol>> = _symbolList

    private val _lives = MutableLiveData(3)
    val lives: LiveData<Int> = _lives

    private val _scores = MutableLiveData(0)
    val scores: LiveData<Int> = _scores

    private var gameScope = CoroutineScope(Dispatchers.Default)
    var gameState = true
    var pauseState = false
    var vibroCallback:(() -> Unit)? = null

    private val _basketPosition = MutableLiveData(0f to 0f)
    val basketPosition: LiveData<Pair<Float, Float>> = _basketPosition

    fun start(
        topLeftXY: Pair<Float, Float>,
        topRightXY: Pair<Float, Float>,
        bottomLeftXY: Pair<Float, Float>,
        bottomRightXY: Pair<Float, Float>,
        speed: Long,
        parentX: Int,
        parentY: Int,
        shortBoardLength: Int,
        longBoardLength: Int,
        basketSize: Int,
        symbolSize: Int
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        generateSymbols(topLeftXY, topRightXY, bottomLeftXY, bottomRightXY)
        letSymbolsMove(
            speed,
            parentX,
            parentY,
            shortBoardLength,
            longBoardLength,
            basketSize,
            symbolSize
        )
    }

    fun stop() {
        gameScope.cancel()
    }

    private fun generateSymbols(
        topLeftXY: Pair<Float, Float>,
        topRightXY: Pair<Float, Float>,
        bottomLeftXY: Pair<Float, Float>,
        bottomRightXY: Pair<Float, Float>
    ) {
        gameScope.launch {
            while (true) {
                delay(2000)
                val newList = _symbolList.value!!.toMutableList()
                val randomPosition = when (1 random 4) {
                    1 -> SymbolPosition.TOP_LEFT
                    2 -> SymbolPosition.TOP_RIGHT
                    3 -> SymbolPosition.BOTTOM_LEFT
                    else -> SymbolPosition.BOTTOM_RIGHT
                }
                val xy = when (randomPosition) {
                    SymbolPosition.TOP_LEFT -> topLeftXY
                    SymbolPosition.TOP_RIGHT -> topRightXY
                    SymbolPosition.BOTTOM_LEFT -> bottomLeftXY
                    SymbolPosition.BOTTOM_RIGHT -> bottomRightXY
                }
                val symbol = if (1 random 30 == 1) {
                    0
                } else {
                    1 random 6
                }
                newList.add(GameSymbol(symbol = symbol, position = randomPosition, xy = xy))
                _symbolList.postValue(newList)
            }
        }
    }

    private fun letSymbolsMove(
        speed: Long,
        parentX: Int,
        parentY: Int,
        shortBoardLength: Int,
        longBoardLength: Int,
        basketSize: Int,
        symbolSize: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(speed)
                val currentList = _symbolList.value!!.toMutableList()
                val newList = mutableListOf<GameSymbol>()
                currentList.forEach {
                    when (it.position) {
                        SymbolPosition.TOP_LEFT -> {
                            if (it.xy.first >= 0f + shortBoardLength - symbolSize) {
                                it.xy = it.xy.first + 1 to it.xy.second + 3
                            } else {
                                it.xy = it.xy.first + 3 to it.xy.second + 1
                            }
                            val horizontalSymbol =
                                (it.xy.first).toInt()..(it.xy.first + symbolSize).toInt()
                            val verticalSymbol =
                                (it.xy.second).toInt()..(it.xy.second + symbolSize).toInt()

                            val horizontalBasket =
                                (_basketPosition.value!!.first).toInt()..(_basketPosition.value!!.first + basketSize).toInt()
                            val verticalBasket =
                                (_basketPosition.value!!.second).toInt()..(_basketPosition.value!!.second + basketSize).toInt()
                            if (horizontalSymbol.any { value -> value in horizontalBasket } && verticalSymbol.any { value -> value in verticalBasket }) {
                                when (it.symbol) {
                                    0 -> {
                                        if (_lives.value!! + 1 <=3) {
                                            _lives.postValue(_lives.value!! + 1)
                                        }
                                    }
                                    6 -> {
                                        if (_lives.value!! - 1 >= 0) {
                                            vibroCallback?.invoke()
                                            _lives.postValue(_lives.value!! - 1)
                                        }
                                    }
                                    else -> {
                                        _scores.postValue(_scores.value!! + 1)
                                    }
                                }
                            } else {
                                if (it.xy.second >= parentY) {
                                    if (_lives.value!! - 1 >= 0 && it.symbol != 6) {
                                        vibroCallback?.invoke()
                                        _lives.postValue(_lives.value!! - 1)
                                    }
                                } else {
                                    newList.add(it)
                                }
                            }
                        }

                        SymbolPosition.TOP_RIGHT -> {
                            if (it.xy.first <= parentX - shortBoardLength + symbolSize / 2) {
                                it.xy = it.xy.first - 1 to it.xy.second + 3
                            } else {
                                it.xy = it.xy.first - 3 to it.xy.second + 1
                            }
                            val horizontalSymbol =
                                (it.xy.first).toInt()..(it.xy.first + symbolSize).toInt()
                            val verticalSymbol =
                                (it.xy.second).toInt()..(it.xy.second + symbolSize).toInt()

                            val horizontalBasket =
                                (_basketPosition.value!!.first).toInt()..(_basketPosition.value!!.first + basketSize).toInt()
                            val verticalBasket =
                                (_basketPosition.value!!.second).toInt()..(_basketPosition.value!!.second + basketSize).toInt()
                            if (horizontalSymbol.any { value -> value in horizontalBasket } && verticalSymbol.any { value -> value in verticalBasket }) {
                                when (it.symbol) {
                                    0 -> {
                                        if (_lives.value!! + 1 <=3) {
                                            _lives.postValue(_lives.value!! + 1)
                                        }
                                    }
                                    6 -> {
                                        if (_lives.value!! - 1 >= 0) {
                                            vibroCallback?.invoke()
                                            _lives.postValue(_lives.value!! - 1)
                                        }
                                    }
                                    else -> {
                                        _scores.postValue(_scores.value!! + 1)
                                    }
                                }
                            } else {
                                if (it.xy.second >= parentY) {
                                    if (_lives.value!! - 1 >= 0 && it.symbol != 6) {
                                        vibroCallback?.invoke()
                                        _lives.postValue(_lives.value!! - 1)
                                    }
                                } else {
                                    newList.add(it)
                                }
                            }
                        }

                        SymbolPosition.BOTTOM_LEFT -> {
                            if (it.xy.first >= 0f + longBoardLength + symbolSize) {
                                it.xy = it.xy.first + 1 to it.xy.second + 3
                            } else {
                                it.xy = it.xy.first + 3 to it.xy.second + 1
                            }
                            val horizontalSymbol =
                                (it.xy.first).toInt()..(it.xy.first + symbolSize).toInt()
                            val verticalSymbol =
                                (it.xy.second).toInt()..(it.xy.second + symbolSize).toInt()

                            val horizontalBasket =
                                (_basketPosition.value!!.first).toInt()..(_basketPosition.value!!.first + basketSize).toInt()
                            val verticalBasket =
                                (_basketPosition.value!!.second).toInt()..(_basketPosition.value!!.second + basketSize).toInt()
                            if (horizontalSymbol.any { value -> value in horizontalBasket } && verticalSymbol.any { value -> value in verticalBasket }) {
                                when (it.symbol) {
                                    0 -> {
                                        if (_lives.value!! + 1 <=3) {
                                            _lives.postValue(_lives.value!! + 1)
                                        }
                                    }
                                    6 -> {
                                        if (_lives.value!! - 1 >= 0) {
                                            vibroCallback?.invoke()
                                            _lives.postValue(_lives.value!! - 1)
                                        }
                                    }
                                    else -> {
                                        _scores.postValue(_scores.value!! + 1)
                                    }
                                }
                            } else {
                                if (it.xy.second >= parentY) {
                                    if (_lives.value!! - 1 >= 0 && it.symbol != 6) {
                                        vibroCallback?.invoke()
                                        _lives.postValue(_lives.value!! - 1)
                                    }
                                } else {
                                    newList.add(it)
                                }
                            }
                        }

                        SymbolPosition.BOTTOM_RIGHT -> {
                            if (it.xy.first <= parentX - longBoardLength - symbolSize - symbolSize) {
                                it.xy = it.xy.first - 1 to it.xy.second + 3
                            } else {
                                it.xy = it.xy.first - 3 to it.xy.second + 1
                            }
                            val horizontalSymbol =
                                (it.xy.first).toInt()..(it.xy.first + symbolSize).toInt()
                            val verticalSymbol =
                                (it.xy.second).toInt()..(it.xy.second + symbolSize).toInt()

                            val horizontalBasket =
                                (_basketPosition.value!!.first).toInt()..(_basketPosition.value!!.first + basketSize).toInt()
                            val verticalBasket =
                                (_basketPosition.value!!.second).toInt()..(_basketPosition.value!!.second + basketSize).toInt()
                            if (horizontalSymbol.any { value -> value in horizontalBasket } && verticalSymbol.any { value -> value in verticalBasket }) {
                                when (it.symbol) {
                                    0 -> {
                                        if (_lives.value!! + 1 <=3) {
                                            _lives.postValue(_lives.value!! + 1)
                                        }
                                    }
                                    6 -> {
                                        if (_lives.value!! - 1 >= 0) {
                                            vibroCallback?.invoke()
                                            _lives.postValue(_lives.value!! - 1)
                                        }
                                    }
                                    else -> {
                                        _scores.postValue(_scores.value!! + 1)
                                    }
                                }
                            } else {
                                if (it.xy.second >= parentY) {
                                    if (_lives.value!! - 1 >= 0 && it.symbol != 6) {
                                        vibroCallback?.invoke()
                                        _lives.postValue(_lives.value!! - 1)
                                    }
                                } else {
                                    newList.add(it)
                                }
                            }
                        }
                    }
                    _symbolList.postValue(newList)
                }
            }
        }
    }

    fun playerMoveUp(limit: Float) {
        if (_basketPosition.value!!.second - 4f > limit) {
            _basketPosition.postValue(_basketPosition.value!!.first to _basketPosition.value!!.second - 4f)
        }
    }

    fun playerMoveDown(limit: Float) {
        if (_basketPosition.value!!.second + 4f < limit) {
            _basketPosition.postValue(_basketPosition.value!!.first to _basketPosition.value!!.second + 4f)
        }
    }

    fun playerMoveLeft(limit: Float) {
        if (_basketPosition.value!!.first - 4f > limit) {
            _basketPosition.postValue(_basketPosition.value!!.first - 4 to _basketPosition.value!!.second)
        }
    }

    fun playerMoveRight(limit: Float) {
        if (_basketPosition.value!!.first + 4f < limit) {
            _basketPosition.postValue(_basketPosition.value!!.first + 4 to _basketPosition.value!!.second)
        }
    }

    fun initBasketPosition(x: Float, y: Float) {
        _basketPosition.postValue(x to y)
    }

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}