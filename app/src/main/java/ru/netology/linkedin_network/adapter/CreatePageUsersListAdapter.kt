package ru.netology.linkedin_network.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.databinding.CardUserPreviewBinding
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.dto.UserPreview
import ru.netology.linkedin_network.view.loadCircleCrop

interface CreatePageUsersListInteractionListener {
    fun openUserProfile(id: Int)
    fun deleteFromList(id:Int)
}

class CreatePageUsersListAdapter(private val onInteractionListener: CreatePageUsersListInteractionListener) :
    ListAdapter<User, CreatePageUsersListViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatePageUsersListViewHolder {
        val binding = CardUserPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CreatePageUsersListViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: CreatePageUsersListViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class CreatePageUsersListViewHolder(
    private val binding: CardUserPreviewBinding,
    private val listener: CreatePageUsersListInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            avatar.loadCircleCrop(user.avatar)
            this.avatar.setOnClickListener {
                listener.openUserProfile(user.id)
            }
            author.text = user.name
            close.setOnClickListener {
                listener.deleteFromList(user.id)
            }
        }
    }
}
class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}