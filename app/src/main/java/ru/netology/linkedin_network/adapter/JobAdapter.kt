package ru.netology.linkedin_network.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.linkedin_network.databinding.CardJobBinding
import ru.netology.linkedin_network.dto.Job
import ru.netology.linkedin_network.utils.Utils

interface JobInteractionListener {
    fun onLinkClick(url: String) {}
    fun onRemoveJob(job: Job) {}
}

class JobAdapter(private val onInteractionListener: JobInteractionListener) : ListAdapter<Job,
        JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val listener: JobInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            companyName.text = job.name
            position.text = job.position
            startDate.text = Utils.convertDate(job.start)
            endDate.text = job.finish?.let { Utils.convertDate(it) }
            link.text = job.link
            link.setOnClickListener{
                listener.onLinkClick(link.text.toString())
            }
            delete.isVisible =job.ownedByMe
            delete.setOnClickListener {
                listener.onRemoveJob(job)
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}