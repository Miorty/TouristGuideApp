package com.example.touristguide.ui.map

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.core.content.ContextCompat
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.PlaceEntity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PlacesMapFragment : Fragment(R.layout.fragment_places_map) {
    private val viewModel: PlacesMapViewModel by viewModels()
    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureOsmdroid()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById<MapView>(R.id.mapView).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
            controller.setZoom(14.0)
            controller.setCenter(PERM_CENTER)
        }

        viewModel.places.observe(viewLifecycleOwner) { places ->
            renderPlaces(places)
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

    private fun renderPlaces(places: List<PlaceEntity>) {
        val map = mapView ?: return
        map.overlays.clear()

        val points = places.map { place ->
            GeoPoint(place.latitude, place.longitude).also { point ->
                map.overlays.add(
                    Marker(map).apply {
                        position = point
                        title = place.title
                        snippet = place.address.ifBlank { "г. Пермь" }
                        icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_marker)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                )
            }
        }

        when {
            points.size > 1 -> {
                map.controller.setZoom(13.6)
                map.controller.setCenter(points.centerPoint())
            }
            points.size == 1 -> {
                map.controller.setZoom(15.5)
                map.controller.setCenter(points.first())
            }
            else -> map.controller.setCenter(PERM_CENTER)
        }
        map.invalidate()
    }

    private companion object {
        val PERM_CENTER = GeoPoint(58.0105, 56.2502)
    }
}

private fun List<GeoPoint>.centerPoint(): GeoPoint =
    GeoPoint(map { it.latitude }.average(), map { it.longitude }.average())
