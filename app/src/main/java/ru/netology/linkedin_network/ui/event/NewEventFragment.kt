package ru.netology.linkedin_network.ui.event

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.adapter.CreatePageUsersListAdapter
import ru.netology.linkedin_network.adapter.CreatePageUsersListInteractionListener
import ru.netology.linkedin_network.databinding.FragmentNewEventBinding
import ru.netology.linkedin_network.enumeration.AttachmentType.*
import ru.netology.linkedin_network.enumeration.EventType
import ru.netology.linkedin_network.ui.event.EventFeedFragment.Companion.intArg
import ru.netology.linkedin_network.ui.MapsFragment.Companion.pointArg
import ru.netology.linkedin_network.utils.Utils
import ru.netology.linkedin_network.viewmodel.EventViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.linkedin_network.ui.AppActivity
import ru.netology.linkedin_network.utils.StringArg
import java.io.File

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewEventFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }
    private val viewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )

        (activity as AppActivity).supportActionBar?.title = getString(R.string.add_event)

        var file: MultipartBody.Part

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Snackbar.make(binding.root, R.string.skip, Snackbar.LENGTH_SHORT)
                .setAction(R.string.exit) {
                    viewModel.deleteEditEvent()
                    findNavController().navigate(R.id.eventFeedFragment)
                }.show()
        }

        if (arguments?.intArg != null) {
            val id = arguments?.intArg
            id?.let { viewModel.getEventRequest(it) }
        }

        val adapter = CreatePageUsersListAdapter(object : CreatePageUsersListInteractionListener {
            override fun openUserProfile(id: Int) {
                val idAuthor = id.toString()
                findNavController().navigate(R.id.userProfileFragment,
                    Bundle().apply { textArg = idAuthor })
            }

            override fun deleteFromList(id: Int) {
                viewModel.unCheck(id)
                viewModel.addSpeakerIds()
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
                        viewModel.addMediaToEvent(IMAGE, file)
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
                    val selectedVideoUri = data?.data!!
                    val selectedVideoPath =
                        Utils.getVideoPathFromUri(selectedVideoUri, requireActivity())
                    if (selectedVideoPath != null) {
                        val resultFile = File(selectedVideoPath)
                        file = MultipartBody.Part.createFormData(
                            "file", resultFile.name, resultFile.asRequestBody()
                        )
                        viewModel.changeMedia(
                            selectedVideoUri,
                            resultFile,
                            VIDEO
                        )
                        viewModel.addMediaToEvent(VIDEO, file)
                    }
                }
            }

        binding.pickVideo.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
            pickVideoLauncher.launch(intent)
        }

        val pickAudioLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val selectedAudioUri = data?.data!!
                    val selectedAudioPath =
                        Utils.getAudioPathFromUri(selectedAudioUri, requireActivity())
                    if (selectedAudioPath != null) {
                        val resultFile = File(selectedAudioPath)
                        file = MultipartBody.Part.createFormData(
                            "file", resultFile.name, resultFile.asRequestBody()
                        )
                        viewModel.changeMedia(
                            selectedAudioUri,
                            resultFile,
                            AUDIO
                        )
                        viewModel.addMediaToEvent(AUDIO, file)
                    }
                }
            }

        binding.pickAudio.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
            pickAudioLauncher.launch(intent)
        }

        viewModel.media.observe(viewLifecycleOwner) { mediaModel ->
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

        binding.addSpeakers.setOnClickListener {
            binding.addSpeakers.isChecked =
                viewModel.newEvent.value?.speakerIds?.isNotEmpty() ?: false
            findNavController().navigate(R.id.action_newEventFragment_to_chooseEventUsersFragment)
        }

        binding.addCoordinates.setOnClickListener {
            if (viewModel.newEvent.value?.coordinates != null) {
                binding.addCoordinates.text = null
                val point = Point(
                    viewModel.newEvent.value?.coordinates!!.latitude.toDouble(),
                    viewModel.newEvent.value?.coordinates!!.longitude.toDouble()
                )
                viewModel.isEventIntent = true
                findNavController().navigate(R.id.action_newEventFragment_to_mapsFragment,
                    Bundle().apply { pointArg = point })
            } else {
                viewModel.isEventIntent = true
                findNavController().navigate(R.id.action_newEventFragment_to_mapsFragment)
            }
        }

        if (viewModel.newEvent.value?.coordinates != null) {
            val latitude = viewModel.newEvent.value?.coordinates!!.latitude
            val longitude = viewModel.newEvent.value?.coordinates!!.longitude
            val coordinates = "$latitude, $longitude"
            binding.addCoordinates.setText(coordinates)
        } else {
            binding.addCoordinates.text = null
        }

        binding.addLink.setOnClickListener {
            val link: String = binding.link.text.toString()
            viewModel.addLink(link)
        }

        binding.speakerIds.adapter = adapter
        viewModel.speakersData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.scrollSpeakers.visibility = View.GONE
            } else {
                adapter.submitList(it)
                binding.scrollSpeakers.visibility = View.VISIBLE
            }
        }

        viewModel.newEvent.observe(viewLifecycleOwner) {
            it.content.let(binding.edit::setText)
            it.link.let(binding.addLink::setText)
            binding.online.isChecked = it.type == EventType.ONLINE
            if (it.attachment != null) {
                binding.mediaContainer.visibility = View.VISIBLE
            } else {
                binding.mediaContainer.visibility = View.GONE
            }
        }

        binding.removeMedia.setOnClickListener {
            viewModel.changeMedia(null, null, null)
            viewModel.newEvent.value = viewModel.newEvent.value?.copy(attachment = null)
            binding.mediaContainer.visibility = View.GONE
        }

        binding.addDateTime.setOnClickListener {
            Utils.selectDateTimeDialog(binding.addDateTime, requireContext())
        }

        binding.save.setOnClickListener {
            Utils.hideKeyboard(requireView())
            val content = binding.edit.text.toString()
            val datetime = binding.addDateTime.text.toString()
            viewModel.addDateAndTime(datetime)
            val event = viewModel.newEvent.value!!.copy(content = content)
            if (content == "" || datetime == "") {
                Snackbar.make(binding.root, R.string.empty_content_error, Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.saveEvent(event)
            }
        }

        binding.online.setOnClickListener {
            viewModel.addEventType()
        }

        viewModel.Eventd.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        return binding.root
    }
}