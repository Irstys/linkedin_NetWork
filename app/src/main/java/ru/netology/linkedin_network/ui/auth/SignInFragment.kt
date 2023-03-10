package ru.netology.linkedin_network.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.databinding.FragmentSignInBinding
import ru.netology.linkedin_network.repository.aurh.AuthRepository
import ru.netology.linkedin_network.utils.Utils
import ru.netology.linkedin_network.viewmodel.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var auth: AppAuth

    @Inject
    lateinit var repository: AuthRepository

    private val viewModel: SignInViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.signInButton.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            if (binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank()) {
                Toast.makeText(
                    activity,
                    getString(R.string.empty_content_error),
                    Toast.LENGTH_LONG
                )
                    .show()
            } else {
                viewModel.signIn(login, pass)
                Utils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }
        }

        binding.goToRegisterPage.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
        return binding.root
    }
}