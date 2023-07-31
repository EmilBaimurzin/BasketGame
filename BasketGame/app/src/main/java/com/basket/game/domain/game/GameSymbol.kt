package com.basket.game.domain.game

data class GameSymbol(
    val symbol: Int,
    var xy: Pair<Float, Float>,
    val position: SymbolPosition
)