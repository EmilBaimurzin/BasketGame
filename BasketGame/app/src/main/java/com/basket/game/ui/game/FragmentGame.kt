package com.basket.game.ui.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basket.game.R
import com.basket.game.databinding.FragmentGameBinding
import com.basket.game.domain.other.AppPrefs
import com.basket.game.ui.other.ViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FragmentGame : ViewBindingFragment<FragmentGameBinding>(FragmentGameBinding::inflate) {
    private var moveScope = CoroutineScope(Dispatchers.Default)
    private val viewModel: GameViewModel by viewModels()
    private val callbackViewModel: CallbackViewModel by activityViewModels()
    private val xy by lazy {
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Pair(size.x, size.y)
    }
    private val sp by lazy {
        AppPrefs(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMovement()
        initBasketPosition()

        lifecycleScope.launch {
            delay(20)
            if (viewModel.gameState && !viewModel.pauseState) {
                viewModel.start(
                    topLeftXY = 0f to binding.boardTopLeft.y - dpToPx(30),
                    topRightXY = xy.first - dpToPx(30).toFloat() to binding.boardTopRight.y - dpToPx(
                        30
                    ),
                    bottomLeftXY = 0f to binding.boardBottomLeft.y - dpToPx(30),
                    bottomRightXY = xy.first - dpToPx(30).toFloat() to binding.boardBottomRight.y - dpToPx(
                        30
                    ),
                    speed = (16 - sp.getSpeed()).toLong(),
                    parentX = xy.first,
                    parentY = xy.second,
                    shortBoardLength = dpToPx(250),
                    longBoardLength = dpToPx(250),
                    basketSize = dpToPx(60),
                    symbolSize = dpToPx(30)

                )
            }
        }

        viewModel.vibroCallback = {
            if (sp.getVibroState()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    (requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(
                        VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    (requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(150)
                }
            }
        }

        callbackViewModel.callback = {
            viewModel.pauseState = false
            viewModel.start(
                topLeftXY = 0f to binding.boardTopLeft.y - dpToPx(30),
                topRightXY = xy.first - dpToPx(30).toFloat() to binding.boardTopRight.y - dpToPx(
                    30
                ),
                bottomLeftXY = 0f to binding.boardBottomLeft.y - dpToPx(30),
                bottomRightXY = xy.first - dpToPx(30).toFloat() to binding.boardBottomRight.y - dpToPx(
                    30
                ),
                speed = (16 - sp.getSpeed()).toLong(),
                parentX = xy.first,
                parentY = xy.second,
                shortBoardLength = dpToPx(250),
                longBoardLength = dpToPx(250),
                basketSize = dpToPx(60),
                symbolSize = dpToPx(30)

            )
        }

        binding.pauseButton.setOnClickListener {
            viewModel.stop()
            viewModel.pauseState = true
            findNavController().navigate(R.id.action_fragmentGame_to_dialogPause)
        }

        binding.settingsButton.setOnClickListener {
            viewModel.stop()
            viewModel.pauseState = true
            findNavController().navigate(R.id.action_fragmentGame_to_dialogOptions)
        }

        binding.menuButton.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }

        viewModel.basketPosition.observe(viewLifecycleOwner) {
            binding.basket.apply {
                x = it.first
                y = it.second
            }
        }

        viewModel.scores.observe(viewLifecycleOwner) {
            binding.scores.text = it.toString()
        }

        viewModel.lives.observe(viewLifecycleOwner) {
            binding.apply {
                heart1.setImageResource(if (it >= 1) R.drawable.img_heart_active else R.drawable.img_heart_not_active)
                heart2.setImageResource(if (it >= 2) R.drawable.img_heart_active else R.drawable.img_heart_not_active)
                heart3.setImageResource(if (it >= 3) R.drawable.img_heart_active else R.drawable.img_heart_not_active)
            }

            if (it == 0 && viewModel.gameState && !viewModel.pauseState) {
                end()
            }
        }

        viewModel.symbolList.observe(viewLifecycleOwner) { list ->
            binding.symbolsLayout.removeAllViews()
            list.forEach {
                val symbolView = ImageView(requireContext())
                symbolView.layoutParams = ViewGroup.LayoutParams(dpToPx(30), dpToPx(30))
                symbolView.setImageResource(
                    when (it.symbol) {
                        0 -> R.drawable.img_heart_active
                        1 -> R.drawable.symbol01
                        2 -> R.drawable.symbol02
                        3 -> R.drawable.symbol03
                        4 -> R.drawable.symbol04
                        5 -> R.drawable.symbol05
                        else -> R.drawable.img_bomb
                    }
                )
                symbolView.x = it.xy.first
                symbolView.y = it.xy.second
                binding.symbolsLayout.addView(symbolView)
            }
        }
    }

    private fun end() {
        viewModel.gameState = false
        viewModel.stop()
        if (viewModel.scores.value!! > sp.getBestScores()) {
            sp.setBestScores(viewModel.scores.value!!)
        }
        findNavController().navigate(FragmentGameDirections.actionFragmentGameToDialogEnd(viewModel.scores.value!!))
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setMovement() {
        binding.buttonUp.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveUp(binding.boardTopLeft.y + dpToPx(125))
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveUp(binding.boardTopLeft.y + dpToPx(125))
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(binding.boardTopLeft.x + dpToPx(250))
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveLeft(binding.boardTopLeft.x + dpToPx(250))
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight(binding.boardTopRight.x - dpToPx(60))
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveRight(
                                binding.boardTopRight.x - dpToPx(60)
                            )
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }

        binding.buttonDown.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveDown((xy.second - dpToPx(60)).toFloat())
                            delay(2)
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    moveScope.launch {
                        while (true) {
                            viewModel.playerMoveDown(
                                (xy.second - dpToPx(60)).toFloat()
                            )
                            delay(2)
                        }
                    }
                    true
                }

                else -> {
                    moveScope.cancel()
                    moveScope = CoroutineScope(Dispatchers.Default)
                    false
                }
            }
        }
    }

    private fun initBasketPosition() {
        lifecycleScope.launch {
            delay(20)
            if (viewModel.basketPosition.value!!.first == 0f) {
                viewModel.initBasketPosition(
                    (xy.first / 2 - dpToPx(30).toFloat()),
                    binding.boardBottomRight.y + dpToPx(30)
                )
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}