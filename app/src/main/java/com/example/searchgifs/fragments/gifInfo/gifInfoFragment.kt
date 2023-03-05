package com.example.searchgifs.fragments.gifInfo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.searchgifs.GifInfoViewModel
import com.example.searchgifs.R
import com.example.searchgifs.databinding.FragmentGifInfoBinding

class gifInfoFragment : Fragment() {

    private var _binding: FragmentGifInfoBinding? = null
    private val binding get() = _binding!!

    private val gifInfoViewModel: GifInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGifInfoBinding.inflate(inflater, container, false)

        gifInfoViewModel.selectedItem.observe(this, Observer { selectedGif ->
            activity?.let { Glide.with(it).asGif().load(selectedGif.url).into(binding.gif) };
            binding.title.text = selectedGif.title
            binding.date.text = selectedGif.date

        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}