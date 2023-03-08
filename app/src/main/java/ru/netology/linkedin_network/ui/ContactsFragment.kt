package ru.netology.linkedin_network.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.ContactAdapter
import ru.netology.linkedin_network.adapter.ContactInteractionListener
import ru.netology.linkedin_network.databinding.FragmentContactsBinding
import ru.netology.linkedin_network.ui.ImageFragment.Companion.textArg
import ru.netology.linkedin_network.viewmodel.UserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ContactsFragment : Fragment() {

    val userViewModel: UserProfileViewModel by viewModels()
    var adapter: ContactAdapter? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        var searchView: SearchView? = null
        val search = menu.findItem(R.id.menu_search)
        searchView = search?.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    userViewModel.data.observe(viewLifecycleOwner) {
                        val searchText = newText.lowercase(Locale.getDefault())
                        val newArray = it.filter {
                            it.name.lowercase(Locale.getDefault()).contains(searchText)
                        }
                        adapter?.submitList(newArray)
                    }
                } else {
                    userViewModel.data.observe(viewLifecycleOwner) {
                        adapter?.submitList(it)
                    }
                }
                return true
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentContactsBinding.inflate(inflater, container, false)

        val users = userViewModel.getAllUsers()

        this.adapter = ContactAdapter(object : ContactInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(
                    R.id.userProfileFragment,
                    Bundle().apply { textArg = idAuthor })
            }
        })
        binding.list.adapter = adapter

        userViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.loading) {
                Snackbar.make(binding.root, R.string.server_error_message, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }


        userViewModel.data.observe(viewLifecycleOwner) {
            println(it.toString())
            adapter!!.submitList(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter=null
    }
}