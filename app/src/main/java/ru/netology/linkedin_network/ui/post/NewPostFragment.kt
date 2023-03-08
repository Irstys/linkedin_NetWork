package ru.netology.linkedin_network.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Video
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.CreatePageUsersListAdapter
import ru.netology.linkedin_network.adapter.CreatePageUsersListInteractionListener
import ru.netology.linkedin_network.databinding.FragmentNewPostBinding
import ru.netology.linkedin_network.enumeration.AttachmentType.*
import ru.netology.linkedin_network.ui.MapsFragment.Companion.pointArg
import ru.netology.linkedin_network.viewmodel.PostViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.linkedin_network.ui.ProfileFragment.Companion.textArg

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )

        val viewModel: PostViewModel by viewModels()
        var file: MultipartBody.Part


        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Snackbar.make(binding.root, R.string.skip, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit) {
                    viewModel.deleteEditPost()
                    findNavController().navigate(R.id.postFeedFragment)
                }.show()
        }

        val adapter = CreatePageUsersListAdapter(object : CreatePageUsersListInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(
                    R.id.userProfileFragment,
                    Bundle().apply { textArg = idAuthor })
            }

            override fun deleteFromList(id: Int) {
                viewModel.unCheck(id)
                viewModel.addMentionIds()
            }
        })

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        val resultFile = uri?.toFile()
                        file = MultipartBody.Part.createFormData(
                            "file", resultFile?.name, resultFile!!.asRequestBody()
                        )
                        viewModel.changeMedia(uri, resultFile, IMAGE)
                        viewModel.addMediaToPost(IMAGE, file)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                        "image/jpg"
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        val pickVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedVideoUri = data?.data
                    if (selectedVideoUri != null) {
                        val contentResolver = requireContext().contentResolver
                        file = MultipartBody.Part.createFormData(
                            "file",
                            "video",
                            requireNotNull(contentResolver.openInputStream(selectedVideoUri)).readBytes()
                                .toRequestBody()
                        )
                        viewModel.changeMedia(selectedVideoUri, null, VIDEO)
                        viewModel.addMediaToPost(VIDEO, file)
                    }
                } else {
                    Snackbar.make(binding.root, R.string.video, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

        binding.pickVideo.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                Video.Media.EXTERNAL_CONTENT_URI
            )
            pickVideoLauncher.launch(intent)
        }

            val pickAudioLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                    val resultCode = activityResult.resultCode
                    val data = activityResult.data

                    if (resultCode == Activity.RESULT_OK) {
                        val selectedAudioUri = data?.data
                        if (selectedAudioUri != null) {
                            val contentResolver = requireContext().contentResolver
                            file = MultipartBody.Part.createFormData(
                                "file",
                                "audio",
                                requireNotNull(contentResolver.openInputStream(selectedAudioUri)).readBytes()
                                    .toRequestBody()
                            )
                            viewModel.changeMedia(selectedAudioUri, null, AUDIO)
                            viewModel.addMediaToPost(AUDIO, file)
                        }
                    } else {
                        Snackbar.make(binding.root, R.string.video, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

        binding.pickAudio.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"
            pickAudioLauncher.launch(intent)
        }

        viewModel.media.observe(viewLifecycleOwner)
        { mediaModel ->
            if (mediaModel.uri == null) {
                binding.mediaContainer.visibility = View.GONE
                return@observe
            }
            when (mediaModel.type) {
                IMAGE -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageURI(mediaModel.uri)
                }
                VIDEO -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageResource(R.drawable.ic_videocam)
                }
                AUDIO -> {
                    binding.mediaContainer.visibility = View.VISIBLE
                    binding.image.setImageResource(R.drawable.ic_music)
                }
                null -> return@observe
            }
        }

        binding.addMention.setOnClickListener {
            findNavController().navigate(R.id.action_newPostFragment_to_choosePostUsersFragment)
        }

        binding.addCoordinates.setOnClickListener {
            if (viewModel.newPost.value?.coordinates != null) {
                val point = Point(
                    viewModel.newPost.value?.coordinates!!.latitude.toDouble(),
                    viewModel.newPost.value?.coordinates!!.longitude.toDouble()
                )
                viewModel.isPostIntent = true
                findNavController().navigate(R.id.action_newPostFragment_to_mapsFragment,
                    Bundle().apply { pointArg = point })
            } else {
                viewModel.isPostIntent = true
                findNavController().navigate(R.id.action_newPostFragment_to_mapsFragment)
                Log.d("PostNew",  viewModel.newPost.value?.coordinates.toString())
            }
        }

        if (viewModel.newPost.value?.coordinates != null) {
            val latitude = viewModel.newPost.value?.coordinates!!.latitude
            val longitude = viewModel.newPost.value?.coordinates!!.longitude
            val coordinates = "$latitude, $longitude"
            binding.addCoordinates.text = coordinates
        } else {
            binding.addCoordinates.text = null
        }

        binding.addLink.setOnClickListener {
            val link: String = binding.link.text.toString()
            viewModel.addLink(link)
        }

        binding.mentionIds.adapter = adapter
        viewModel.mentionsData.observe(viewLifecycleOwner)
        {
            if (it.isEmpty()) {
                binding.scrollMentions.visibility = View.GONE
            } else {
                adapter.submitList(it)
                binding.scrollMentions.visibility = View.VISIBLE
            }
        }

        viewModel.newPost.observe(viewLifecycleOwner)
        {
            it.content.let(binding.edit::setText)
            it.link.let(binding.addLink::setText)
            if (it.attachment != null) {
                binding.mediaContainer.visibility = View.VISIBLE
            } else {
                binding.mediaContainer.visibility = View.GONE
            }
        }

        binding.removeMedia.setOnClickListener {
            viewModel.changeMedia(null, null, null)
            viewModel.newPost.value = viewModel.newPost.value?.copy(attachment = null)
            binding.mediaContainer.visibility = View.GONE
        }

        binding.save.setOnClickListener {
            Log.d("PostNew",  viewModel.cords.value.toString())
            val content = binding.edit.text.toString()
            if (content == "") {
                Snackbar.make(binding.root, R.string.empty_content_error, Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.savePost(content)
            }

                findNavController().navigateUp()


            viewModel.dataState.observe(viewLifecycleOwner) { state ->
                if (state.error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return binding.root
    }
}