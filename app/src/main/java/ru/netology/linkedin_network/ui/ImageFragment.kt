package ru.netology.linkedin_network.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.linkedin_network.databinding.FragmentImageBinding
import ru.netology.linkedin_network.utils.StringArg
import ru.netology.linkedin_network.view.load

class ImageFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(
            inflater,
            container,
            false
        )
        val url = arguments?.textArg
        binding.imageView.load(url)

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}