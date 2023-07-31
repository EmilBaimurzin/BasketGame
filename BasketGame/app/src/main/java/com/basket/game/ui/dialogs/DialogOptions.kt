package com.basket.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.basket.game.R
import com.basket.game.core.library.ViewBindingDialog
import com.basket.game.databinding.DialogOptionsBinding
import com.basket.game.domain.other.AppPrefs
import com.basket.game.ui.game.CallbackViewModel
import com.google.android.material.slider.Slider

class DialogOptions : ViewBindingDialog<DialogOptionsBinding>(DialogOptionsBinding::inflate) {
    private val viewModel: CallbackViewModel by activityViewModels()
    private val sp by lazy {
        AppPrefs(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.Dialog_No_Border)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setCancelable(false)
        dialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                findNavController().popBackStack()
                viewModel.callback?.invoke()
                true
            } else {
                false
            }
        }
        binding.speedSlider.value = sp.getSpeed().toFloat()
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
            viewModel.callback?.invoke()
        }

        binding.speedSlider.setCustomThumbDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.img_speed_thumb,
                null
            )!!
        )

        binding.yesButton.setOnClickListener {
            sp.setVibroState(true)
            setButtons()
        }
        binding.noButton.setOnClickListener {
            sp.setVibroState(false)
            setButtons()
        }
        binding.speedSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                sp.setSpeed(slider.value.toInt())
            }
        })
        binding.applyButton.setOnClickListener {
            findNavController().popBackStack()
            viewModel.callback?.invoke()
        }
    }

    private fun setButtons() {
        binding.apply {
            yesButton.setImageResource(if (sp.getVibroState()) R.drawable.button_yes_active else R.drawable.button_yes_not_active)
            noButton.setImageResource(if (sp.getVibroState()) R.drawable.button_close_not_active else R.drawable.button_close)
        }
    }
}