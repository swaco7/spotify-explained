package com.example.spotifyexplained.ui.general

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.spotifyexplained.R
import com.example.spotifyexplained.databinding.DialogHelpBinding

class HelpDialogFragment(val text: String): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val binding = DialogHelpBinding.inflate(inflater)
            binding.text = text
            builder.setView(binding.root)
                .setPositiveButton(R.string.cancel
                ) { dialog, id ->
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}