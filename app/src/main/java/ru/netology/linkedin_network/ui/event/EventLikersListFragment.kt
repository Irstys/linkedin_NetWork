package ru.netology.linkedin_network.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.UsersListAdapter
import ru.netology.linkedin_network.adapter.UsersListInteractionListener
import ru.netology.linkedin_network.databinding.FragmentPostUsersListBinding
import ru.netology.linkedin_network.ui.ImageFragment.Companion.textArg
import ru.netology.linkedin_network.viewmodel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventLikersListFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostUsersListBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).supportActionBar?.title = context?.getString(R.string.likers)
        val viewModel: EventViewModel by viewModels()

        val adapter = UsersListAdapter(object : UsersListInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(
                    R.id.userProfileFragment,
                    Bundle().apply { textArg = idAuthor })
            }
        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.likersList.observe(viewLifecycleOwner) {
            val newUser = adapter.itemCount < it.size
            adapter.submitList(it) {
                if (newUser) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        return binding.root
    }
}