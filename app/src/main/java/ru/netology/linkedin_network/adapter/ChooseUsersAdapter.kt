package ru.netology.linkedin_network.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.databinding.CardUserToChooseBinding
import ru.netology.linkedin_network.dto.User
import ru.netology.linkedin_network.view.loadCircleCrop


interface ChooseUsersInteractionListener {
    fun check(id: Int) {}
    fun unCheck(id: Int) {}
}

class ChooseUsersAdapter(private val onInteractionListener: ChooseUsersInteractionListener) :
    ListAdapter<User, ChooseUsersViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseUsersViewHolder {
        val binding =
            CardUserToChooseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChooseUsersViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: ChooseUsersViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class ChooseUsersViewHolder(
    private val binding: CardUserToChooseBinding,
    private val listener: ChooseUsersInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            avatar.loadCircleCrop(user.avatar)
            author.text = user.name
            checkbox.apply {
                isChecked = user.isChecked
            }
            checkbox.setOnClickListener {
                if (checkbox.isChecked) {
                    listener.check(user.id)
                } else {
                    listener.unCheck(user.id)
                }

            }
        }
    }
}

