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
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;
            val binding = DialogHelpBinding.inflate(inflater)
            binding.text = text
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(binding.root)
                // Add action buttons
                .setPositiveButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // sign in the user ...
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}