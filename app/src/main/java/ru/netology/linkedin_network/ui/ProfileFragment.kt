package ru.netology.linkedin_network.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.*

import ru.netology.linkedin_network.dto.Job
import ru.netology.linkedin_network.dto.Post
import ru.netology.linkedin_network.enumeration.AttachmentType
import ru.netology.linkedin_network.ui.PostFeedFragment.Companion.intArg
import ru.netology.linkedin_network.utils.StringArg
import ru.netology.linkedin_network.view.loadCircleCrop
import ru.netology.linkedin_network.viewmodel.AuthViewModel
import ru.netology.linkedin_network.viewmodel.PostViewModel
import ru.netology.linkedin_network.viewmodel.UserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import ru.netology.linkedin_network.databinding.FragmentProfileBinding
import ru.netology.linkedin_network.ui.ImageFragment.Companion.textArg

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    val userProfileViewModel: UserProfileViewModel by activityViewModels()
    val authViewModel: AuthViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        authViewModel.data.observeForever {
            if (!authViewModel.authenticated || arguments != null) {
                binding.addJob.visibility = View.GONE
                binding.addPost.visibility = View.GONE
                arguments?.textArg?.let {
                    val userId = it.toInt()
                    userProfileViewModel.getUserById(userId)
                    userProfileViewModel.getUserJobs(userId)
                }
            } else if (authViewModel.authenticated && arguments == null) {
                binding.addJob.visibility = View.VISIBLE
                binding.addPost.visibility = View.VISIBLE
                val myId = userProfileViewModel.myId.toInt()
                userProfileViewModel.getUserById(myId)
                userProfileViewModel.getMyJobs()
            }
        }

        val jobAdapter = JobAdapter(object : JobInteractionListener {
            override fun onLinkClick(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

            override fun onRemoveJob(job: Job) {
                userProfileViewModel.removeJobById(job.id)
            }
        })

        binding.jobList.adapter = jobAdapter

        userProfileViewModel.jobData.observe(viewLifecycleOwner) {
            if (authViewModel.authenticated && arguments == null) {
                it.forEach { job ->
                    job.ownedByMe = true
                }
            }
            if (it.isEmpty()) {
                binding.jobList.visibility = View.GONE
            } else {
                jobAdapter.submitList(it)
                binding.jobList.visibility = View.VISIBLE
            }
        }

        val userId:Int? = userProfileViewModel.userData.value?.id

        userProfileViewModel.userData.observe(viewLifecycleOwner) {
            (activity as AppCompatActivity?)?.supportActionBar?.title = it.name
            binding.name.text = it.name
            binding.avatar.loadCircleCrop(it.avatar)
        }

        val feedAdapter = FeedAdapter(object : OnPostInteractionListener {
            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    postViewModel.likePost(post)
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
                postViewModel.removePostById(post.id)
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
                        postViewModel.getLikedAndMentionedUsersList(post)
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
                    postViewModel.getMentionedUsersList(post)
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
                    postViewModel.getLikedUsersList(post)
                    findNavController().navigate(R.id.action_postFeedFragment_to_postLikedListFragment)
                }
            } else {
                Snackbar.make(binding.root, R.string.error_auth, Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_postFeedFragment_to_signInFragment)
            }}

            override fun onHide(post: Post) {postViewModel.hidePost(post.id)}
        })

        binding.postList.adapter = feedAdapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    feedAdapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    feedAdapter.retry()
                }
            }),
        )
        feedAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.postList.smoothScrollToPosition(0)
                }
            }
        })
        lifecycleScope.launchWhenCreated {
            println(postViewModel.data.toString())
                val data = postViewModel.data.map {
                    it.filter { feedItem ->
                        when (feedItem) {
                            is Post -> {
                                feedItem.authorId == userId
                            }
                            else -> {
                                false
                            }
                        }
                    }
                }
                data.collectLatest {
                    feedAdapter.submitData(it)
                }
            }


        binding.addJob.setOnClickListener {
            findNavController().navigate(R.id.newJobFragment)
        }

        binding.addPost.setOnClickListener {
            findNavController().navigate(R.id.newPostFragment)
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }
}