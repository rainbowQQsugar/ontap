package com.abinbev.dsa.fragments;

import android.content.DialogInterface;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.VisitState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wandersonblough on 11/17/15.
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    public static final String TAG = MapFragment.class.getSimpleName();

    MapListener mapListener;

    GoogleMap map;
    List<Event> visibleEvents;
    Marker lastSelectedMarker;
    Map<String, Event> markerAccountMap;
    boolean mapLoaded;
    public int slidePaneOffset = 0;

    public interface MapListener {
        void onMarkerSelected(Event event);
    }

    public MapFragment() {
        super();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mapLoaded = true;
                if (visibleEvents != null) {
                    setEvents(visibleEvents);
                }
            }
        });

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (lastSelectedMarker != null) {
                    Event event = markerAccountMap.get(lastSelectedMarker.getId());
                    if (event != null) {
                        if (event.getVisitState() == VisitState.completed) {
                            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_visited));
                        } else if (event.isCheckedIn()) {
                            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_checked_in));
                        } else {
                            lastSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                        }
                    }
                }
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sel));
                lastSelectedMarker = marker;
                marker.showInfoWindow();
                if (mapListener != null) {
                    mapListener.onMarkerSelected(markerAccountMap.get(marker.getId()));
                }
                LatLng latLong = new LatLng(lastSelectedMarker.getPosition().latitude, lastSelectedMarker.getPosition().longitude);
                float originalZoom = map.getCameraPosition().zoom;
                animateLatLngZoom(latLong, originalZoom, -slidePaneOffset, 0);
                return true;
            }
        });
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = map.getMyLocation();
                if (location == null) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.location_error))
                            .setMessage(getString(R.string.location_error_message))
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }
                return false;
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof MapListener) {
            mapListener = (MapListener) getActivity();
        } else {
            throw new IllegalArgumentException("Activity must implement " + MapListener.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapAsync(this);
    }

    /**
     * set list of visible events
     *
     * @param events {@link Event} to display on the Map
     */
    public void setEvents(List<Event> events) {
        this.visibleEvents = events;
        if (mapLoaded && visibleEvents != null) {
            setMarkers(getSelectedEvent());
        }
    }

    /**
     * @return last selected event
     */
    private Event getSelectedEvent() {
        if (markerAccountMap != null && lastSelectedMarker != null) {
            return markerAccountMap.get(lastSelectedMarker.getId());
        }
        return null;
    }

    private void setMarkers(Event selectedEvent) {
        markerAccountMap = new HashMap<>();
        map.clear();
        lastSelectedMarker = null;
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (Event event : visibleEvents) {
            Marker marker;
            double latitude = event.getAccount()
                    .getLatitude();
            double longitude = event.getAccount()
                    .getLongitude();
            if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
                LatLng latLng = new LatLng(latitude, longitude);
                latLngBuilder.include(latLng);
                boolean isSelected;
                MarkerOptions options = new MarkerOptions().position(latLng).title(event.getAccount().getName());
                if (selectedEvent != null && (event.equals(selectedEvent) ||
                        // hack to fix refreshing the map when Created event Id gets updated from salesforce
                        // Issue: when an event is created, it gets the temporary Id and Map gets updated fine with
                        // proper selection on marker. After Trigger refresh is done temp Id of newly created event
                        // gets updated and visibleEvents list doesn't contain the event with temp Id anymore
                        // and removes the desired selection on the marker
                        // please check with usanaga incase of further explanation
                        (selectedEvent.isTempObject() && visibleEvents.indexOf(event) == 0))) {
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sel));
                    isSelected = true;
                } else {
                    if (event.getVisitState() == VisitState.completed) {
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_visited));
                    } else if (event.isCheckedIn()) {
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_checked_in));
                    } else {
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                    }
                    isSelected = false;
                }
                marker = map.addMarker(options);
                if (isSelected) {
                    lastSelectedMarker = marker;
                    marker.showInfoWindow();
                }
                markerAccountMap.put(marker.getId(), event);
            } else {
                lastSelectedMarker = null;
            }
        }
        if (lastSelectedMarker != null && getResources().getBoolean(R.bool.isTablet)) {
            if (!visibleEvents.isEmpty()) {
                if (getResources().getBoolean(R.bool.isTablet)) {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), 200));
                } else {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), 150));
                }
            } else {
                LatLng latLong = new LatLng(lastSelectedMarker.getPosition().latitude, lastSelectedMarker.getPosition().longitude);
                float originalZoom = map.getCameraPosition().zoom;
                animateLatLngZoom(latLong, originalZoom, -slidePaneOffset, 0);
            }
        } else {
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), 100));
            } catch (Exception e) {

            }
        }
    }

    /**
     * Animate map to center it around below parameters
     *
     * @param latlng  latlng around which map will be centered
     * @param zoom    zoom level
     * @param offsetX x offset
     * @param offsetY y offset
     */
    private void animateLatLngZoom(LatLng latlng, float zoom, int offsetX, int offsetY) {

        try {
            float originalZoom = map.getCameraPosition().zoom;
            map.moveCamera(CameraUpdateFactory.zoomTo(zoom));

            Point pointInScreen = map.getProjection().toScreenLocation(latlng);
            Point newPoint = new Point();
            newPoint.x = pointInScreen.x + offsetX;
            newPoint.y = pointInScreen.y + offsetY;

            LatLng newCenterLatLng = map.getProjection().fromScreenLocation(newPoint);
            map.moveCamera(CameraUpdateFactory.zoomTo(originalZoom));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, zoom));
        } catch (Exception e) {
            // Ignore any exceptions on this event ...
        }
    }

    /**
     * Select an event to mark as selected
     *
     * @param event the {@link Event} that is currently selected
     */
    public void setSelectedEvent(Event event) {
        if (visibleEvents == null) {
            Log.e(TAG, "Empty account list");
        } else if (!visibleEvents.contains(event)) {
            Log.e(TAG, "This account is not contained in the list");
        } else {
            if (mapLoaded) {
                setMarkers(event);
            }
        }
    }
}
