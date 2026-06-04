package com.example.touristguide.ui.add_place

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.touristguide.R
import com.example.touristguide.core.storage.ImageStorageManager
import com.example.touristguide.data.local.entity.CategoryEntity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class AddPlaceFragment : Fragment(R.layout.fragment_add_place) {
    private val viewModel: AddPlaceViewModel by viewModels()
    private var categories: List<CategoryEntity> = emptyList()
    private var mapView: MapView? = null
    private var selectedPoint: GeoPoint? = null
    private var selectedPhotoPath: String? = null
    private var selectedPhotoPreview: ImageView? = null
    private var selectedLocationText: TextView? = null

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        selectedPhotoPath = ImageStorageManager(requireContext()).saveImage(uri)
        selectedPhotoPreview?.setImageURI(uri)
        view?.findViewById<TextView>(R.id.placePhotoText)?.text = "Фото выбрано"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureOsmdroid()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleEditText = view.findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = view.findViewById<EditText>(R.id.descriptionEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val categorySpinner = view.findViewById<Spinner>(R.id.categorySpinner)
        val saveButton = view.findViewById<Button>(R.id.savePlaceButton)
        selectedPhotoPreview = view.findViewById(R.id.placePhotoPreview)
        selectedLocationText = view.findViewById(R.id.selectedLocationText)

        view.findViewById<View>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        view.findViewById<View>(R.id.selectPlacePhotoButton).setOnClickListener {
            pickPhoto.launch("image/*")
        }

        setupMap(view)

        viewModel.categories.observe(viewLifecycleOwner) { loadedCategories ->
            categories = loadedCategories
            categorySpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                loadedCategories.map { it.name }
            )
        }

        saveButton.setOnClickListener {
            val category = categories.getOrNull(categorySpinner.selectedItemPosition)
            if (category == null) {
                Toast.makeText(requireContext(), "Категории еще загружаются", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val point = selectedPoint
            if (point == null) {
                Toast.makeText(requireContext(), "Нажмите на карту, чтобы выбрать точку", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.savePlace(
                title = titleEditText.text.toString(),
                description = descriptionEditText.text.toString(),
                categoryId = category.id,
                latitude = point.latitude,
                longitude = point.longitude,
                address = addressEditText.text.toString(),
                photoPath = selectedPhotoPath
            )
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            val message = result.error ?: "Место отправлено на модерацию"
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        mapView = null
        selectedPhotoPreview = null
        selectedLocationText = null
        super.onDestroyView()
    }

    private fun configureOsmdroid() {
        val context = requireContext().applicationContext
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    private fun setupMap(view: View) {
        mapView = view.findViewById<MapView>(R.id.placePickerMapView).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            controller.setCenter(PERM_CENTER)
            setOnTouchListener { map, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    map.parent.requestDisallowInterceptTouchEvent(true)
                }
                false
            }
            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                        if (point != null) selectPoint(point)
                        return true
                    }

                    override fun longPressHelper(point: GeoPoint?): Boolean {
                        if (point != null) selectPoint(point)
                        return true
                    }
                })
            )
        }
    }

    private fun selectPoint(point: GeoPoint) {
        selectedPoint = point
        selectedLocationText?.text = String.format(
            Locale.US,
            "Точка выбрана: %.5f, %.5f",
            point.latitude,
            point.longitude
        )

        val map = mapView ?: return
        map.overlays.removeAll { it is Marker }
        map.overlays.add(
            Marker(map).apply {
                position = point
                title = "Новое место"
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_marker)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
        )
        map.invalidate()
    }

    private companion object {
        val PERM_CENTER = GeoPoint(58.0105, 56.2502)
    }
}
