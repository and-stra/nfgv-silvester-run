package com.nfgv.stopwatch.ui.component.view.about

import android.media.MediaPlayer
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nfgv.stopwatch.R
import com.nfgv.stopwatch.databinding.AboutFragmentBinding


class AboutFragment : Fragment() {
    private var _binding: AboutFragmentBinding? = null
    private lateinit var mediaPlayer: MediaPlayer

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initMediaPlayer()
        registerListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.textGithub.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.nananana)
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Failed to initialize media player $e")
        }
    }

    private fun registerListeners() {
        binding.logoLayout.setOnClickListener { playNanananaSound() }
    }

    private fun playNanananaSound() {
        try {
            mediaPlayer.stop()
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e("MediaPlayer", "Failed to play sound $e")
        }
    }
}