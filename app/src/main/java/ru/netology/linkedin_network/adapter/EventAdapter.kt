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
import ru.netology.linkedin_network.databinding.CardEventBinding
import ru.netology.linkedin_network.enumeration.AttachmentType.*
import ru.netology.linkedin_network.enumeration.EventType.*
import ru.netology.linkedin_network.ui.MapsFragment.Companion.pointArg
import ru.netology.linkedin_network.utils.Utils
import ru.netology.linkedin_network.view.load
import ru.netology.linkedin_network.view.loadCircleCrop
import com.google.android.exoplayer2.MediaItem
import com.yandex.mapkit.geometry.Point
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.linkedin_network.dto.*
import ru.netology.linkedin_network.util.numbersToString
import ru.netology.linkedin_network.utils.toText

interface OnEventInteractionListener {
    fun onLike(event: Event) {}
    fun onShare(event: Event) {}
    fun onParticipateInEvent(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun loadEventUsersList(event: Event) {}
    fun onFullscreenAttachment(event: Event) {}
    fun onViewParticipates(event: Event) {}
    fun onViewSpeakers(event: Event) {}
    fun onViewLikeOwner(event: Event) {}
}

class EventAdapter(
    private val listener: OnEventInteractionListener,
) : PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position) ?: return
        holder.bind(event)
    }

    override fun onBindViewHolder(
        holder: EventViewHolder,
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


class EventViewHolder(
    private val binding: CardEventBinding,
    private val listener: OnEventInteractionListener
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
        payload.join?.also { join ->
            binding.participate.setIconResource(
                if (join) R.drawable.ic_participated else R.drawable.ic_leave
            )
            if (join) {
                ObjectAnimator.ofPropertyValuesHolder(
                    binding.participate,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F, 1.0F, 1.2F),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F, 1.0F, 1.2F)
                ).start()
            } else {
                ObjectAnimator.ofFloat(
                    binding.participate,
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
    fun bind(event: Event) {
        parentView.tag = this
        binding.apply {
            when (event.type) {
                OFFLINE -> type.setImageResource(R.drawable.ic_offline_24)
                ONLINE -> type.setImageResource(R.drawable.ic_online_24)
                else -> {}
            }

            avatar.loadCircleCrop(event.authorAvatar)

            if (event.attachment?.url != "") {
                when (event.attachment?.type) {
                    IMAGE -> {
                        videoPreview = null
                        image.visibility = View.VISIBLE
                        videoContainer.visibility = View.GONE
                        image.load(event.attachment.url)
                    }
                    VIDEO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(event.attachment.url)
                        videoThumbnail.load(event.attachment.url)
                    }
                    AUDIO -> {
                        image.visibility = View.GONE
                        videoContainer.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(event.attachment.url)
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

            author.text = event.author
            published.text = Utils.convertDateAndTime(event.published.toString())
            dateTime.text = Utils.convertDateAndTime(event.datetime.toString())
            val linkText = if (event.link != null) {
                "\n" + event.link
            } else {
                ""
            }
            val eventText = event.content + linkText
            content.text = eventText
            like.isChecked = event.likedByMe
            participate.isChecked = event.participatedByMe
            coordinates.visibility = if (event.coordinates != null) View.VISIBLE else View.INVISIBLE
            menu.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE


            val usersList = event.users
            val speakerslist = mutableListOf<UserPreview>()
            val participatelist = mutableListOf<UserPreview>()
            val likersList =mutableListOf<UserPreview>()

            if (usersList != null) {
                for ((key, value) in usersList) {
                    if (event.likeOwnerIds.contains(key)){
                        value.isLiked = true
                        likersList.add(value)
                    }
                    if (event.speakerIds.contains(key)) {
                        value.isSpeaker = true
                        speakerslist.add(value)
                    }
                    if (event.participantsIds.contains(key)) {
                        value.isSpeaker = true
                        participatelist.add(value)
                    }
                }
            }
            if (event.participantsIds!!.isEmpty()) {
                participates.visibility = View.INVISIBLE
            } else {
                participates.isVisible = true
                val firstUserAvatarUrl = participatelist.first().avatar
                firstParticipateAvatar.loadCircleCrop(firstUserAvatarUrl)
                toText(participatelist, participates)
            }

            if (event.speakerIds!!.isEmpty()) {
                speakersUser.visibility = View.INVISIBLE
            } else {
                speakersUser.isVisible = true
                speakersUser.text = numbersToString(speakerslist.size )
            }
            if (event.likeOwnerIds!!.isEmpty()) {
                postLikersGroup.visibility = View.GONE
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
                                listener.onRemove(event)
                                true
                            }
                            R.id.object_edit -> {
                                listener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                listener.onLike(event)
            }
            share.setOnClickListener {
                listener.onShare(event)
            }
            participate.setOnClickListener {
                listener.onParticipateInEvent(event)
            }

            image.setOnClickListener {
                listener.onFullscreenAttachment(event)
            }

            coordinates.setOnClickListener { view ->
                view.findNavController().navigate(R.id.action_postFeedFragment_to_mapsFragment,
                    Bundle().apply {
                        Point(
                            event.coordinates?.latitude!!.toDouble(), event.coordinates.longitude.toDouble()
                        ).also { pointArg = it }
                    })
            }

            speakersUser.setOnClickListener {
                listener.onViewSpeakers(event)
            }
            participates.setOnClickListener {
                listener.onViewParticipates(event)
            }
            postLikersGroup.setOnClickListener {
                listener.onViewLikeOwner(event)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}