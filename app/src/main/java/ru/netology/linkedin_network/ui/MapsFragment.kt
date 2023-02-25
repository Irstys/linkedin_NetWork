package ru.netology.linkedin_network.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.linkedin_network.R
import ru.netology.linkedin_network.databinding.FragmentMapsBinding
import ru.netology.linkedin_network.utils.PointArg
import ru.netology.linkedin_network.viewmodel.EventViewModel
import ru.netology.linkedin_network.viewmodel.PostViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MapsFragment : Fragment() {
    private var binding: FragmentMapsBinding? = null
    val postViewModel: PostViewModel by activityViewModels()
    val eventViewModel: EventViewModel by activityViewModels()

    companion object {
        var Bundle.pointArg: Point by PointArg
    }

    var mapObjects: MapObject? = null
    private var mapView: MapView? = null
    private lateinit var userLocation: UserLocationLayer

    private val objectTapListener = GeoObjectTapListener { geo ->
        val selectionMetadata: GeoObjectSelectionMetadata =
            geo.geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)
        binding?.map?.map?.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)
        true
    }

    private val locationObjectListener = object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) = Unit

        override fun onObjectRemoved(view: UserLocationView) = Unit

        override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
            userLocation.cameraPosition()?.target?.let {
                mapView?.map?.move(
                    CameraPosition(it, 14F, 0F, 0F), Animation(Animation.Type.SMOOTH, 5F),
                    null
                )
            }
            userLocation.setObjectListener(null)
        }
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            binding?.map?.map?.mapObjects?.clear()
        }

        override fun onMapLongTap(map: Map, point: Point) {
            if (mapObjects != null) {
                binding?.map?.map?.mapObjects?.clear()
            }
            binding?.map?.map?.deselectGeoObject()


            if (postViewModel.isPostIntent) {
                mapObjects = binding?.map?.map?.mapObjects?.addPlacemark(
                    point,
                    ImageProvider.fromResource(context, R.drawable.ic_location_24)
                )
                Snackbar.make(
                    binding?.root!!, R.string.add_coordinates,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                ).setAction(R.string.add)
                {
                    postViewModel.addcoordinates(point)
                    Log.d("Post",  postViewModel.newPost.value?.coordinates.toString())
                    Log.d("Post",  postViewModel.cords.value.toString())
                    findNavController().navigateUp()
                }.show()
            }
            else if (eventViewModel.isEventIntent) {
                mapObjects = binding?.map?.map?.mapObjects?.addPlacemark(
                    point,
                    ImageProvider.fromResource(context, R.drawable.ic_location_24)
                )
                Snackbar.make(
                    binding?.root!!, R.string.add_coordinates,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                ).setAction(R.string.add)
                {
                    eventViewModel.addcoordinates(point)
                    findNavController().navigateUp()
                }.show()
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    userLocation.isVisible = true
                    userLocation.isHeadingEnabled = false
                    userLocation.cameraPosition()?.target?.apply {
                        val map = mapView?.map ?: return@registerForActivityResult
                        val cameraPosition = map.cameraPosition
                        map.move(
                            CameraPosition(
                                this,
                                cameraPosition.zoom,
                                cameraPosition.azimuth,
                                cameraPosition.tilt,
                            ), Animation(Animation.Type.SMOOTH, 5F),
                            null
                        )
                    }
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMapsBinding.inflate(inflater, container, false)

        this.binding = binding
        binding.map.apply {
            userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
            if (requireActivity()
                    .checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                userLocation.isVisible = true
                userLocation.isHeadingEnabled = false
            }

            map.addInputListener(inputListener)
            map.addTapListener(objectTapListener)
            userLocation.setObjectListener(locationObjectListener)

            if (arguments?.pointArg != null) {
                val point = Point(arguments?.pointArg?.latitude!!, arguments?.pointArg?.longitude!!)
                binding.map.map?.mapObjects?.addPlacemark(
                    point,
                    ImageProvider.fromResource(context, R.drawable.ic_location_24)
                )
                mapView?.map?.move(
                    CameraPosition(point, 14.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 5F), null
                )


            } else {
                if (requireActivity()
                        .checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                }

            }
        }

        binding.location.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding?.map?.onStart()
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
        binding?.map?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}