package ru.netology.linkedin_network.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.databinding.FragmentNewJobBinding
import ru.netology.linkedin_network.dto.Job
import ru.netology.linkedin_network.utils.Utils
import ru.netology.linkedin_network.viewmodel.UserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewJobFragment: Fragment() {
    private val viewModel: UserProfileViewModel by viewModels()
    @SuppressLint("FragmentBackPressedCallback", "UseRequireInsteadOfGet",
        "ClickableViewAccessibility"
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Snackbar.make(binding.root, R.string.skip, Snackbar.LENGTH_SHORT).setAction(R.string.exit) {
                viewModel.deleteEditJob()
                findNavController().navigateUp()
            }.show()
        }

        binding.addStartDate.setOnClickListener {
            Utils.selectDateDialog(binding.addStartDate, requireContext())
            val startDate = binding.addStartDate.text.toString()
            viewModel.addStartDate(startDate)
        }

        binding.addEndDate.setOnClickListener {
            Utils.selectDateDialog(binding.addEndDate, requireContext())
            val endDate = binding.addEndDate.text.toString()
            viewModel.addEndDate(endDate)
        }

        binding.save.setOnClickListener {
            Utils.hideKeyboard(requireView())
            if (binding.company.text.toString() =="" || binding.position.text.toString()=="" || binding.addStartDate.text.toString() == "") {
                Snackbar.make(binding.root, R.string.empty_content_error, Snackbar.LENGTH_SHORT).show()
            } else {
                val id = if (viewModel.newJob.value!!.id == 0) {0} else {viewModel.newJob.value!!.id}
                val name = binding.company.text.trim().toString()
                val position = binding.position.text.trim().toString()
                val start = binding.addStartDate.text.trim().toString()
                val finish = if (binding.addEndDate.text.toString() == "") {null} else {binding.addEndDate.text.trim().toString()}
                val link = if (binding.link.text.toString() == "") {null} else {binding.link.text.trim().toString()}
                val job = Job(id, name, position, start, finish, link)
                //viewModel.editJob(job)
                viewModel.saveJob(job)
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

}