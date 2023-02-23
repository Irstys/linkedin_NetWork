package ru.netology.linkedin_network.ui

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
import ru.netology.linkedin_network.adapter.PagingLoadStateAdapter
import ru.netology.linkedin_network.adapter.FeedAdapter
import ru.netology.linkedin_network.adapter.OnPostInteractionListener
import ru.netology.linkedin_network.adapter.PostRecyclerView
import ru.netology.linkedin_network.databinding.FragmentPostFeedBinding
import ru.netology.linkedin_network.dto.Post
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.ui.ImageFragment.Companion.textArg
import ru.netology.linkedin_network.utils.IntArg
import ru.netology.linkedin_network.viewmodel.AuthViewModel
import ru.netology.linkedin_network.viewmodel.PostViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostFeedFragment : Fragment() {

    private var binding: FragmentPostFeedBinding? = null

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var mediaRecyclerView: PostRecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostFeedBinding.inflate(inflater, container, false)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated) {
                binding.fab.visibility = View.GONE
            } else {
                binding.fab.visibility = View.VISIBLE
            }
        }

        mediaRecyclerView = binding.list

        val adapter = FeedAdapter(object : OnPostInteractionListener {
            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    viewModel.likePost(post)
                } else {
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_postFeedFragment_to_newPostFragment,
                    Bundle().apply { intArg = post.id })
            }

            override fun onRemove(post: Post) {
                viewModel.removePostById(post.id)
            }

            override fun onShare(post: Post) {
                if (authViewModel.authenticated) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.share))
                    startActivity(shareIntent)
                } else {
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun loadLikedAndMentionedUsersList(post: Post) {
                if (authViewModel.authenticated) {
                    if (post.users.values.isEmpty()) {
                        return
                    } else {
                        viewModel.getLikedAndMentionedUsersList(post)
                        findNavController().navigate(R.id.action_postFeedFragment_to_postUsersListFragment)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
                }
            }

            override fun onFullscreenAttachment(post: Post) {
                if (post.attachment?.url != "") {
                    when (post.attachment?.type) {
                        AttachmentType.IMAGE -> {
                            findNavController().navigate(R.id.action_postFeedFragment_to_showPhotoFragment,
                                Bundle().apply { textArg = post.attachment.url })
                        }
                        else -> return
                    }
                }
            }
            override fun onViewMentors(post: Post) { if (authViewModel.authenticated) {
                if (post.users.values.isEmpty()) {
                    return
                } else {
                    viewModel.getMentionedUsersList(post)
                    findNavController().navigate(R.id.action_postFeedFragment_to_postMentionListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
            }}
            override fun onViewLikeOwner(post: Post) { if (authViewModel.authenticated) {
                if (post.users.values.isEmpty()) {
                    return
                } else {
                    viewModel.getLikedUsersList(post)
                    findNavController().navigate(R.id.action_postFeedFragment_to_postLikedListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
            }}

            override fun onHide(post: Post) {viewModel.hidePost(post.id)}

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
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT).show()
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
            findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)

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