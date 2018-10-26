package com.abinbev.dsa.fragments;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abinbev.dsa.R;
import com.abinbev.dsa.model.Event;
import com.abinbev.dsa.model.VisitState;
import com.abinbev.dsa.utils.amap.LatLngBoundsBuilder;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Amap

/**
 * Created by wandersonblough on 11/17/15.
 */
public class AMapFragment extends SupportMapFragment implements LocationSource, AMapLocationListener { //implements OnMapReadyCallback {

    public static final String TAG = AMapFragment.class.getSimpleName();

    final static long DEFAULT_INTERVAL = 5 * 1000L;
    private MapListener mapListener;
    private OnLocationChangedListener mListener;
    private AMap amap;

    private List<Event> visibleEvents;
    public int slidePaneOffset = 0;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker mMarkerPreviouslyClicked;

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mapListener == null) {
            return;
        }

        if (amapLocation == null) {
            return;
        }

        if (amapLocation.getErrorCode() == 0) {
            mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
        } else {
            String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
            Log.e("AmapErr", errText);
        }
    }

    public interface MapListener {
        void onMarkerSelected(Event event);

    }

    public AMapFragment() {
        super();
    }


    public void onMapReady(AMap aMap) {
        amap = aMap;

        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
//        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
//        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        amap.getUiSettings().setZoomControlsEnabled(true);

        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setLocationSource(this);// 设置定位监听


        amap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Object extra = marker.getObject();
                if (extra == null) {
                    return true;
                }

                if (mMarkerPreviouslyClicked != null) {
                    Event associatedEvent = (Event) marker.getObject();
                    mMarkerPreviouslyClicked.setIcon(pickIconForEvent(associatedEvent));
                }

                Event event = (Event) extra;

                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_sel));
                marker.showInfoWindow();

                mMarkerPreviouslyClicked = marker;

                if (mapListener != null) {
                    mapListener.onMarkerSelected(event);
                }

                LatLng latLong = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

                float originalZoom = amap.getCameraPosition().zoom;
                animateLatLngZoom(latLong, originalZoom, -slidePaneOffset, 0);

                return true;
            }
        });


        if (visibleEvents != null) {
            setVisibleEvents(visibleEvents);
        }


    }

    public void setVisibleEvents(List<Event> events) {
        this.visibleEvents = events;
        changeVisibleMarks();
    }

    private void changeVisibleMarks() {

        if (amap != null) {
            if (amap.getMapScreenMarkers() != null && !amap.getMapScreenMarkers().isEmpty()) {
                mMarkerPreviouslyClicked = null;
                amap.clear();
            }
        }


        LatLngBoundsBuilder latLngBuilder = null;
        if (visibleEvents != null && visibleEvents.size() != 0) {
            latLngBuilder = new LatLngBoundsBuilder();
            for (Event event : visibleEvents) {

                double latitude = event.getAccount()
                        .getLatitude();
                double longitude = event.getAccount()
                        .getLongitude();

                if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
                    continue;
                }

                LatLng latLng = new LatLng(latitude, longitude);
                latLngBuilder.include(latLng);

                MarkerOptions options = new MarkerOptions().position(latLng).title(event.getAccount().getName());

                options.icon(pickIconForEvent(event));

                Marker marker = amap.addMarker(options);
                marker.setObject(event);

            }
        }


        if (latLngBuilder == null) {
            return;
        }

        amap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(), 100));
    }

    private BitmapDescriptor pickIconForEvent(Event event) {
        if (event.getVisitState() == VisitState.completed) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_visited);
        } else if (event.isCheckedIn()) {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_checked_in);
        } else {
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
        }
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

        if (amap == null) {
            amap = getMap();
            onMapReady(amap);
        }
    }


    /**
     * 激活定位
     */
    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getContext());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationOption.setInterval(DEFAULT_INTERVAL);
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();
        deactivate();
    }

    /**
     * Animate amap to center it around below parameters
     *
     * @param latlng  latlng around which amap will be centered
     * @param zoom    zoom level
     * @param offsetX x offset
     * @param offsetY y offset
     */
    private void animateLatLngZoom(LatLng latlng, float zoom, int offsetX, int offsetY) {

        try {
            float originalZoom = amap.getCameraPosition().zoom;
            amap.moveCamera(CameraUpdateFactory.zoomTo(zoom));

            Point pointInScreen = amap.getProjection().toScreenLocation(latlng);
            Point newPoint = new Point();
            newPoint.x = pointInScreen.x + offsetX;
            newPoint.y = pointInScreen.y + offsetY;

            LatLng newCenterLatLng = amap.getProjection().fromScreenLocation(newPoint);
            amap.moveCamera(CameraUpdateFactory.zoomTo(originalZoom));
            amap.animateCamera(CameraUpdateFactory.newLatLngZoom(newCenterLatLng, zoom));
        } catch (Exception e) {
            // Ignore any exceptions on this event ...
        }
    }

}
