package ru.netology.linkedin_network.adapter

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.databinding.CardPostBinding
import ru.netology.linkedin_network.enumeration.AttachmentType.*
import ru.netology.linkedin_network.ui.MapsFragment.Companion.pointArg
import ru.netology.linkedin_network.utils.Utils
import ru.netology.linkedin_network.view.load
import ru.netology.linkedin_network.view.loadCircleCrop
import com.google.android.exoplayer2.MediaItem
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.utils.toText

interface OnPostInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun loadLikedAndMentionedUsersList(post: Post) {}
    fun onFullscreenAttachment(post: Post){}
    fun onViewMentors(post: Post) {}
    fun onViewLikeOwner(post: Post) {}
}

class FeedAdapter(
    private val listener: OnPostInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(FeedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  PostViewHolder(binding,listener)
    }
    override fun onBindViewHolder(holder:PostViewHolder, position: Int) {
        val post = getItem(position) ?: return
        holder.bind(post)
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
            payloads.forEach {
                if (it is Payload) {
                    holder.bind(it)
                }
            }
            onBindViewHolder(holder, position)
        }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: OnPostInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(payload: Payload) {
        payload.liked?.also { liked ->
            binding.like.setIconResource(
                if (liked) R.drawable.ic_liked_24 else R.drawable.ic_like_24
            )
            if (liked) {
                ObjectAnimator.ofPropertyValuesHolder(
                    binding.like,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                ).start()
            } else {
                ObjectAnimator.ofFloat(
                    binding.like,
                    View.ROTATION,
                    0F, 360F
                ).start()
            }
        }

        payload.content?.let(binding.content::setText)
    }

    private val parentView = binding.root
    val videoThumbnail = binding.videoThumbnail
    val videoContainer = binding.video
    val videoProgressBar = binding.videoProgressBar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = binding.videoButton

    @OptIn(ExperimentalCoroutinesApi::class)
    fun bind(post: Post) {
        parentView.tag = this
        binding.apply {
            if (!post.mentionedMe) mentionedMe.visibility = View.GONE else mentionedMe.visibility =
                View.VISIBLE

            avatar.loadCircleCrop(post.authorAvatar)

            if (post.attachment?.url != "") {
                when (post.attachment?.type) {
                    IMAGE -> {
                        videoPreview = null
                        image.visibility = View.VISIBLE
                        videoContainer.visibility = View.GONE
                        image.load(post.attachment.url)
                    }
                    VIDEO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment.url)
                        videoThumbnail.load(post.attachment.url)
                    }
                    AUDIO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment.url)
                        videoThumbnail.setImageDrawable(
                            AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.ic_music
                            )
                        )
                    }
                    null -> {
                        videoContainer.visibility = View.GONE
                        image.visibility = View.GONE
                        videoPreview = null
                    }
                }
            }
            author.text = post.author
            published.text = Utils.convertDateAndTime(post.published.toString())
            val linkText = if (post.link != null) {
                "\n" + post.link
            } else {
                ""
            }
            val postText = post.content + linkText
            content.text = postText
            like.isChecked = post.likedByMe
            coordinates.visibility = if (post.coordinates != null) View.VISIBLE else View.INVISIBLE
            mentionedMe.isVisible = post.mentionedMe
            menu.isVisible = post.ownedByMe


            val usersList = post.users
            val mentorslist = mutableListOf<UserPreview>()
            val likersList =mutableListOf<UserPreview>()

            if (usersList != null) {
                for ((key, value) in usersList) {
                    if (post.likeOwnerIds.contains(key)){
                        value.isLiked = true
                        likersList.add(value)
                    }
                    if (post.mentionIds.contains(key)) {
                        value.isMentioned = true
                        mentorslist.add(value)
                    }
                }
            }

            if (post.mentionIds!!.isEmpty()) {
                postUsersGroup.visibility = View.INVISIBLE
            } else {
                postUsersGroup.isVisible = true
                val firstUserAvatarUrl = mentorslist.first().avatar
                firstUserAvatar.loadCircleCrop(firstUserAvatarUrl)
                toText(mentorslist, users)
            }
            if (post.likeOwnerIds!!.isEmpty()) {
                postLikersGroup.visibility = View.INVISIBLE
            } else {
                postLikersGroup.isVisible = true
                val firstLikedAvatarUrl = likersList.first().avatar
                firstLikedAvatar.loadCircleCrop(firstLikedAvatarUrl)
                toText(likersList, likedUsers)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.object_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.object_remove -> {
                                listener.onRemove(post)
                                true
                            }
                            R.id.object_edit -> {
                                listener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                listener.onLike(post)
            }

            postUsersGroup.setOnClickListener {
                listener.onViewMentors(post)
            }
            postLikersGroup.setOnClickListener {
                listener.onViewLikeOwner(post)
            }
            share.setOnClickListener {
                listener.onShare(post)
            }

            image.setOnClickListener {
                listener.onFullscreenAttachment(post)
            }

            coordinates.setOnClickListener { view ->
                view.findNavController().navigate(R.id.action_postFeedFragment_to_mapsFragment,
                    Bundle().apply {
                        Point(
                            post.coordinates?.latitude!!.toDouble(), post.coordinates.longitude.toDouble()
                        ).also { pointArg = it }
                    })
            }
        }
    }
}

class FeedDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Post, newItem: Post): Any {
        return if (oldItem::class != newItem::class) {
        } else if (oldItem is Post && newItem is Post) {
            Payload(
                liked = newItem.likedByMe.takeIf { oldItem.likedByMe != it },
                content = newItem.content.takeIf { oldItem.content != it },
            )
        } else {

        }
    }
}

