package ru.netology.linkedin_network.ui.event

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.*
import ru.netology.linkedin_network.databinding.FragmentEventFeedBinding
import ru.netology.linkedin_network.dto.Event
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.ui.ImageFragment.Companion.textArg
import ru.netology.linkedin_network.utils.IntArg
import ru.netology.linkedin_network.viewmodel.AuthViewModel
import ru.netology.linkedin_network.viewmodel.EventViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EventFeedFragment : Fragment() {

    private var binding: FragmentEventFeedBinding? = null

    private val authViewModel: AuthViewModel by viewModels()
    private val viewModel: EventViewModel by activityViewModels()
    private var mediaRecyclerView: EventRecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEventFeedBinding.inflate(inflater, container, false)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated) {
                binding.fab.visibility = View.GONE
            } else {
                binding.fab.visibility = View.VISIBLE
            }
        }

        mediaRecyclerView = binding.list

        val adapter = EventAdapter(object : OnEventInteractionListener {
            override fun onLike(event: Event) {
                if (authViewModel.authenticated) {
                    viewModel.likeEvent(event)
                } else {
                    Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
                }
            }

            override fun onShare(event: Event) {
                if (authViewModel.authenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, event.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.share))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
                }
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.action_eventFeedFragment_to_newEventFragment,
                    Bundle().apply { intArg = event.id })
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun loadEventUsersList(event: Event) {
                if (authViewModel.authenticated) {
                    if (event.speakerIds.isEmpty()) {
                        return
                    } else {
                        viewModel.getEventUsersList(event)
                        findNavController().navigate(R.id.action_eventFeedFragment_to_eventUsersListFragment)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
                }
            }

            override fun onjoin(event: Event) {
                if (authViewModel.authenticated) {
                    viewModel.join(event)
                } else {
                    Snackbar.make(binding.root, R.string.error, Snackbar.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
                }
            }

            override fun onFullscreenAttachment(event: Event) {
                if (event.attachment?.url != "") {
                    when (event.attachment?.type) {
                        AttachmentType.IMAGE -> {
                            findNavController().navigate(R.id.action_eventFeedFragment_to_showPhotoFragment,
                                Bundle().apply { textArg = event.attachment.url })
                        }
                        else -> return
                    }
                }
            }
            override fun onViewParticipates(event: Event) { if (authViewModel.authenticated) {
                if (event.users.values.isEmpty()) {
                    return
                } else {
                    viewModel.getParticipates(event)
                    findNavController().navigate(R.id.action_eventFeedFragment_to_eventParcipatesListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
            }}
            override fun onViewSpeakers(event: Event) { if (authViewModel.authenticated) {
                if (event.users.values.isEmpty()) {
                    return
                } else {
                    viewModel.getSpeakers(event)
                    findNavController().navigate(R.id.action_eventFeedFragment_to_eventSpeakersListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
            }}
            override fun onViewLikeOwner(event: Event) { if (authViewModel.authenticated) {
                if (event.users.values.isEmpty()) {
                    return
                } else {
                    viewModel.getLikers(event)
                    findNavController().navigate(R.id.action_eventFeedFragment_to_eventLikersListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_eventFeedFragment_to_signInFragment)
            }}
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        binding.list.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .show()
            }
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        adapter.loadStateFlow
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swipeRefresh.isRefreshing = it.refresh is LoadState.Loading
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_eventFeedFragment_to_newEventFragment)
        }

        return binding.root
    }

    companion object {
        var Bundle.intArg: Int by IntArg
    }

    override fun onResume() {
        mediaRecyclerView?.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        mediaRecyclerView?.releasePlayer()
        super.onPause()
    }

    override fun onStop() {
        mediaRecyclerView?.releasePlayer()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}