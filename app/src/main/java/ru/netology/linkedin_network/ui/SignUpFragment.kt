package ru.netology.linkedin_network.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.linkedin_network.auth.AppAuth
import ru.netology.linkedin_network.repository.AuthRepository
import ru.netology.linkedin_network.viewmodel.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.databinding.FragmentSignUpBinding
import ru.netology.linkedin_network.dto.MediaUpload
import ru.netology.linkedin_network.utils.Utils


@AndroidEntryPoint
class SignUpFragment : Fragment() {

    @Inject
    lateinit var auth: AppAuth
    @Inject
    lateinit var repository: AuthRepository

    private val viewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)

        binding.addAvatar.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_addAvatarFragment)
         }

        binding.signUpButton.setOnClickListener {
            val login = binding.login.text.toString()
            val pass = binding.password.text.toString()
            val name = binding.userName.text.toString()

            when {
                binding.login.text.isNullOrBlank() || binding.password.text.isNullOrBlank() -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.empty_field),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                binding.confirmPassword.text.toString() != pass -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.password_error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                viewModel.avatar.value?.file == null -> {
                    viewModel.registerUser(login, pass, name)
                    Utils.hideKeyboard(requireView())
                    findNavController().navigateUp()
                }
                else -> {
                    val file = viewModel.avatar.value?.file?.let { MediaUpload(it) }
                    file?.let { viewModel.registerUserWithAvatar(login, pass, name, it) }
                    Utils.hideKeyboard(requireView())
                    findNavController().navigateUp()
                }
            }
        }
        return binding.root
    }
}