package com.basket.game.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.basket.game.R
import com.basket.game.core.library.ViewBindingDialog
import com.basket.game.databinding.DialogEndBinding
import com.basket.game.domain.other.AppPrefs

class DialogEnd: ViewBindingDialog<DialogEndBinding>(DialogEndBinding::inflate) {
    private val args: DialogEndArgs by navArgs()
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
                findNavController().popBackStack(R.id.fragmentMain, false, false)
                true
            } else {
                false
            }
        }
        binding.scoresTextView.text = args.scores.toString()
        binding.bestScoresTextView.text = sp.getBestScores().toString()
        binding.playButton.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentGame)
        }
        binding.menuButton.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentMain, false, false)
        }
    }
}