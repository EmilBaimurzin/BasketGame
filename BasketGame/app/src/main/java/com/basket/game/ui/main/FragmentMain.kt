package com.basket.game.ui.main

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.basket.game.R
import com.basket.game.databinding.FragmentMainBinding
import com.basket.game.ui.game.CallbackViewModel
import com.basket.game.ui.other.ViewBindingFragment

class FragmentMain : ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    private val viewModel: CallbackViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            buttonPlay.setOnClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentGame)
            }
            buttonOptions.setOnClickListener {
                viewModel.callback = null
                findNavController().navigate(R.id.action_fragmentMain_to_dialogOptions)
            }
            buttonExit.setOnClickListener {
                requireActivity().finish()
            }
        }
        binding.privacyText.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )
            )
        }
    }
}