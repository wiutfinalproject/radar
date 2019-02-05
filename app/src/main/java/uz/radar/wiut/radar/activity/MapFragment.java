package uz.radar.wiut.radar.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import uz.radar.wiut.radar.MainActivity;
import uz.radar.wiut.radar.R;
import uz.radar.wiut.radar.db.AZSDb;
import uz.radar.wiut.radar.db.CameraDb;
import uz.radar.wiut.radar.db.IDbCRUD;
import uz.radar.wiut.radar.db.VulkanizatsiyaDb;
import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements Const, SensorEventListener, View.OnClickListener {

    Context context;
    private ArrayList<LocationObject> cameraList, zapravkaList, vulkanizaciyaList = new ArrayList<>();
    private ArrayList<LocationObject> zapravka_list_nearest_2km;
    private ArrayList<LocationObject> zapravka_list_nearest_5km;
    private ArrayList<LocationObject> vulkanizaciya_list_nearest_2km;
    private ArrayList<LocationObject> vulkanizaciya_list_nearest_5km;

    private double myLastDistance = 0, angle = 0;
    private float bearing = 0, mDeclination;
    private float[] mRotationMatrix = new float[16];
    private boolean focus = false, dialog_active = false, isMapTouched = false, first = true, gpsOn = false;

    private GoogleMap maps;
    private Marker marker;
    private LocationManager locationManager;
    private LocationListener listener;
    private Timer gpsTimer;
    private Location lastLocation, currentLocation;

    private SensorManager mSensorManager;
    private Sensor mCompass;
    private ImageView gps, addMarker;
    private Circle mCircle;
    private MediaPlayer player;
    private TextView speed;
    private RelativeLayout markerLayout;
    private CoordinatorLayout coordinatorLayout;
    private AudioManager am;
    private View mapView;


    public String TAG = ">>> TAG";


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        cameraList = getFromDb(new CameraDb(context));
        vulkanizaciyaList = getFromDb(new VulkanizatsiyaDb(context));
        zapravkaList = getFromDb(new AZSDb(context));

        gps = view.findViewById(R.id.gps);
        addMarker = view.findViewById(R.id.addMarker);
        speed = view.findViewById(R.id.speed);
        markerLayout = view.findViewById(R.id.markerLayout);
        mapView = view.findViewById(R.id.mapFrame);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);

        view.findViewById(R.id.close_marker_layout).setOnClickListener(this);
        view.findViewById(R.id.azs_markers).setOnClickListener(this);
        view.findViewById(R.id.camera_markers).setOnClickListener(this);
        view.findViewById(R.id.vulk_markers).setOnClickListener(this);

        gps.setOnClickListener(this);
        addMarker.setOnClickListener(this);

        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActionBar();
        setUpMap(1);

        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
//            Toast.makeText(getApplicationContext, R.string.need_permission, Toast.LENGTH_SHORT).show();
            Toast.makeText(this.getContext(), R.string.need_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private void setActionBar() {
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private ArrayList<LocationObject> getFromDb(IDbCRUD db) {
        ArrayList<LocationObject> tmpList = (ArrayList<LocationObject>) db.getAll();
        db.close();
        return tmpList;
    }

    public void setUpMap(final int status) {
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        fm.beginTransaction().add(R.id.mapFrame, mapFragment).commit();
        fm.executePendingTransactions();
        try {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap map) {
                    if (map != null) {
                        maps = map;
                        maps.setTrafficEnabled(true);
                        maps.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                switchToDefaultMode();
                            }
                        });
                        map.getUiSettings().setRotateGesturesEnabled(true);
                        map.getUiSettings().setIndoorLevelPickerEnabled(true);
                        map.getUiSettings().setCompassEnabled(false);
                        map.getUiSettings().setZoomControlsEnabled(false);

                        if (currentLocation == null) {
                            maps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.311484, 69.244441), 14));
                        } else {
                            maps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 14));
                        }
                        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                startRecording();
                                moveMarker(currentLocation);
                                if (status == 1) {
                                    putMarkers(map, R.drawable.ic_cameras_btn, cameraList);
                                } else if (status == 2) {
                                    putZapravkaObjectMarkersToNearest(map, R.drawable.ic_azs_btn, zapravkaList);
                                } else if (status == 3) {
                                    putVulkanToNearest(map, R.drawable.ic_service_btn, vulkanizaciyaList);
                                } else {
                                    putMarkers(map, R.drawable.ic_cameras_btn, cameraList);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_map), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception ex) {
            Log.d("Map", "Error while initializing Google map: " + ex);
        }

    }

    private void putVulkanToNearest(GoogleMap map, int ic_vulkan, ArrayList<LocationObject> vulkanizaciyaList) {
        if (currentLocation != null && vulkanizaciyaList.size() > 0) {
            vulkanizaciya_list_nearest_2km = new ArrayList<>();
            vulkanizaciya_list_nearest_5km = new ArrayList<>();
            for (int i = 0; i < vulkanizaciyaList.size(); i++) {
                LocationObject vulkanizaciya = vulkanizaciyaList.get(i);
                Location endLocation = new Location("");
                endLocation.setLatitude(vulkanizaciya.getLattitude());
                endLocation.setLongitude(vulkanizaciya.getLongitude());
                double distance = currentLocation.distanceTo(endLocation);
                if (distance <= 2000) {
                    vulkanizaciya_list_nearest_2km.add(vulkanizaciya);
                    vulkanizaciya_list_nearest_5km.add(vulkanizaciya);
                } else if (distance <= 5000) {
                    vulkanizaciya_list_nearest_5km.add(vulkanizaciya);
                }
            }
            if (vulkanizaciya_list_nearest_2km.isEmpty()) {
                if (vulkanizaciya_list_nearest_5km.isEmpty()) {
                    for (int i = 0; i < vulkanizaciyaList.size(); i++) {
                        LocationObject vulkanizaciya = vulkanizaciyaList.get(i);
                        if (vulkanizaciya != null) {
                            map.addMarker(new MarkerOptions()
                                    .title(vulkanizaciya.getName())
                                    .position(new LatLng(vulkanizaciya.getLattitude(), vulkanizaciya.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(ic_vulkan)));
                        }
                    }
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.vul_all, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                    snackbar.show();
                } else {
                    for (int i = 0; i < vulkanizaciya_list_nearest_5km.size(); i++) {
                        LocationObject vulkanizaciya = vulkanizaciya_list_nearest_5km.get(i);
                        if (vulkanizaciya != null) {
                            map.addMarker(new MarkerOptions()
                                    .title(vulkanizaciya.getName())
                                    .position(new LatLng(vulkanizaciya.getLattitude(), vulkanizaciya.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(ic_vulkan)));
                        }
                    }
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.vul_5km, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                    snackbar.show();
                }
            } else {
                for (int i = 0; i < vulkanizaciya_list_nearest_2km.size(); i++) {
                    LocationObject vulkanizaciya = vulkanizaciya_list_nearest_2km.get(i);
                    if (vulkanizaciya != null) {
                        map.addMarker(new MarkerOptions()
                                .title(vulkanizaciya.getName())
                                .position(new LatLng(vulkanizaciya.getLattitude(), vulkanizaciya.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(ic_vulkan)));
                    }
                }
                Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.vul_2km, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                snackbar.show();
            }
        } else {
            showSettingsAlert();
        }
    }

    private void putZapravkaObjectMarkersToNearest(GoogleMap map, int ic_azs_btn, ArrayList<LocationObject> zapravkaList) {
        if (currentLocation != null && zapravkaList.size() > 0) {
            zapravka_list_nearest_2km = new ArrayList<>();
            zapravka_list_nearest_5km = new ArrayList<>();
            for (int i = 0; i < zapravkaList.size(); i++) {
                LocationObject zapravka = zapravkaList.get(i);
                Location endLocation = new Location("");
                endLocation.setLatitude(zapravka.getLattitude());
                endLocation.setLongitude(zapravka.getLongitude());
                double distance = currentLocation.distanceTo(endLocation);
                if (distance <= 2000) {
                    zapravka_list_nearest_2km.add(zapravka);
                    zapravka_list_nearest_5km.add(zapravka);

                } else if (distance <= 5000) {
                    zapravka_list_nearest_5km.add(zapravka);
                }
            }
            if (zapravka_list_nearest_2km.isEmpty()) {
                if (zapravka_list_nearest_5km.isEmpty()) {
                    for (int i = 0; i < zapravkaList.size(); i++) {
                        LocationObject zapravka = zapravkaList.get(i);
                        if (zapravka != null) {
                            map.addMarker(new MarkerOptions()
                                    .title(zapravka.getName())
                                    .position(new LatLng(zapravka.getLattitude(), zapravka.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(ic_azs_btn)));
                        }
                    }
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.azs_all, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                    snackbar.show();
                } else {
                    for (int i = 0; i < zapravka_list_nearest_5km.size(); i++) {
                        LocationObject zapravka = zapravka_list_nearest_5km.get(i);
                        if (zapravka != null) {
                            map.addMarker(new MarkerOptions()
                                    .title(zapravka.getName())
                                    .position(new LatLng(zapravka.getLattitude(), zapravka.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(ic_azs_btn)));
                        }
                    }
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.azs_5km, Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                    snackbar.show();
                }
            } else {
                for (int i = 0; i < zapravka_list_nearest_2km.size(); i++) {
                    LocationObject zapravka = zapravka_list_nearest_2km.get(i);
                    if (zapravka != null) {
                        map.addMarker(new MarkerOptions()
                                .title(zapravka.getName())
                                .position(new LatLng(zapravka.getLattitude(), zapravka.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(ic_azs_btn)));
                    }
                }
                Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.azs_2km, Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor((ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                snackbar.show();
            }
        } else {
            showSettingsAlert();
        }
    }

    private void switchToDefaultMode() {
        isMapTouched = true;
        if (currentLocation != null) {
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .zoom(18f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(currentPlace);
            maps.animateCamera(cameraUpdate);
            if (mCircle != null) {
                mCircle.setVisible(false);
            }
            gps.setImageResource(R.drawable.ic_track_off);
        }
    }

    private void putMarkers(GoogleMap map, int drawable, ArrayList<LocationObject> list) { // R.drawable.ic_cameras_btn
        for (int i = 0; i < list.size(); i++) {
            LocationObject locObject = list.get(i);
            if (locObject != null) {
                map.addMarker(new MarkerOptions()
                        .title(locObject.getName())
                        .position(new LatLng(locObject.getLattitude(), locObject.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(drawable)));
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        startRecording();
    }

    @Override
    public void onStop() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && listener != null) {
                locationManager.removeUpdates(listener);
            }
            if (gpsTimer != null) {
                gpsTimer.cancel();
                gpsTimer.purge();
                gpsTimer = null;
            }
            if (player != null) {
                player.release();
                player = null;
            }
        }
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (gpsTimer != null) {
            gpsTimer.cancel();
            gpsTimer.purge();
            gpsTimer = null;
        }

    }

    @Override
    public void onPause() {
        // Unregister the listener on the onPause() event to preserve battery life;
        super.onPause();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mCompass,
                SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        startRecording();
    }


    public void startRecording() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsTimer = new Timer();
            locationManager = (LocationManager) getActivity().getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !gpsOn) {
                showSettingsAlert();
            } else {
                listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location.getProvider().equals(LocationManager.GPS_PROVIDER) || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                            currentLocation = location;
                            if (!isMapTouched) {
                                moveMarker(location);
                                calculateDistance(location);
                                GeomagneticField field = new GeomagneticField(
                                        (float) location.getLatitude(),
                                        (float) location.getLongitude(),
                                        (float) location.getAltitude(),
                                        System.currentTimeMillis()
                                );
                                // getDeclination returns degrees
                                mDeclination = field.getDeclination();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                for (String s : locationManager.getAllProviders()) {
                    locationManager.requestLocationUpdates(s, 1000, 10, listener);
                    gpsTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (gpsTimer == null) {
                                return;
                            }
                            final Location location = getBestLocation();
                            if (location != null && !isMapTouched) {
                                MainActivity mainActivity = (MainActivity) context;
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        moveMarker(location);
                                        calculateDistance(location);
                                    }
                                });
                            }
                        }

                        @Override
                        public boolean cancel() {
                            return super.cancel();
                        }
                    }, 0, 10000);
                }
            }
        } else {
            //request for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 007);
        }
    }

    public void moveMarker(Location location) {
        if (location != null && !isMapTouched) {
            float userSpeed = 0;
            if (currentLocation != null) {
                userSpeed = (float) (Math.sqrt(
                        Math.pow(location.getLongitude() - currentLocation.getLongitude(), 2)
                                + Math.pow(location.getLatitude() - currentLocation.getLatitude(), 2)
                ) / (location.getTime() - currentLocation.getTime()));
            }
            if (location.hasSpeed()) {
                userSpeed = location.getSpeed();
            }
            userSpeed = userSpeed * 3.6f;
            if (userSpeed >= VISIBLE_SPEED) {
                speed.setTextSize(30);
                speed.setVisibility(View.VISIBLE);
            } else if (userSpeed > 99) {
                speed.setVisibility(View.VISIBLE);
                speed.setTextSize(22);
            } else {
                speed.setVisibility(View.GONE);
            }
            speed.setText(Math.round(userSpeed) + "");
            // currentLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker == null) {
                marker = maps.addMarker(new MarkerOptions().
                        title(getResources().getString(R.string.my_location)).position(latLng)
                        .flat(true).anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
            } else {
                marker.setPosition(latLng);
            }
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .tilt(55f).zoom(18f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(currentPlace);
            if (first) {
                maps.animateCamera(cameraUpdate);
                first = false;
            } else {
                updateCamera(bearing);
            }
            if (mCircle == null) {
                drawMarkerWithCircle(latLng);
            } else {
                updateMarkerWithCircle(latLng);
            }
        } else if (location != null && isMapTouched) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker == null) {
                marker = maps.addMarker(new MarkerOptions().
                        title(getResources().getString(R.string.my_location)).position(latLng)
                        .flat(true).anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
            } else {
                marker.setPosition(latLng);
            }
        }
    }

    private void updateMarkerWithCircle(LatLng position) {
        mCircle.setCenter(position);
    }

    private void drawMarkerWithCircle(LatLng position) {
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        CircleOptions circleOptions = new CircleOptions()
                .center(position).radius(RADIUS)
                .fillColor(shadeColor).strokeColor(strokeColor)
                .strokeWidth(4);
        mCircle = maps.addCircle(circleOptions);
    }

    public Location getBestLocation() {
        Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
        Location netLocation = getLocationByProvider(LocationManager.NETWORK_PROVIDER);

        if (gpslocation == null) {
            Log.d("Gps", "Gps not working ");
            //            showSettingsAlert();
        }
        if (netLocation == null) {
            Log.d("network", "network is not working ");
        }
        return gpslocation == null ? netLocation : gpslocation;
    }


    private Location getLocationByProvider(String provider) {
        Location location = null;
        LocationManager locationManager = (LocationManager) getActivity().getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(provider)) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(provider);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Cannot acces Provider " + provider);
        }
        return location;
    }

    public void calculateDistance(Location location) {
        for (int i = 0; i < cameraList.size(); i++) {
            LocationObject road = cameraList.get(i);
            float d[] = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), road.getLattitude(), road.getLongitude(), d);

            if (d[0] < RADIUS) {
                if (!road.isNear()) {
                    if (player == null) {
                        player = MediaPlayer.create(context, R.raw.sound);
                        am.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                                0);
                        player.start();
                    } else if (!player.isPlaying()) {
                        player.start();
                    }
                }
                road.setNear(true);
            } else
                road.setNear(false);
        }
    }


    public void displayDialog() {
        //LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!dialog_active) {
            if (!gps_enabled && !network_enabled) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Location data is not available");
                builder.setMessage("Please, enable your GPS");
                builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        dialog.dismiss();
                        startActivityForResult(callGPSSettingIntent, 1);
                        dialog_active = false;
                    }
                });
                builder.create().show();
            }
        }
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        // Setting Dialog Title
        alertDialog.setTitle(R.string.gps_settings);
        // Setting Dialog Message
        alertDialog.setMessage(R.string.gps_not_enabled);
        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gpsOn = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);
                if (currentLocation != null) {
                    dialog.dismiss();
                }
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float in = (float) Math.acos(mRotationMatrix[8]);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (Math.abs(Math.toDegrees(orientation[0]) - angle) > 0.8) {
                int orientationMode = getResources().getConfiguration().orientation;
                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                if (orientationMode == Configuration.ORIENTATION_LANDSCAPE
                        && (rotation == Surface.ROTATION_0
                        || rotation == Surface.ROTATION_90)) {
                    bearing = (float) Math.toDegrees(orientation[0]) + mDeclination + 90;
                } else if (orientationMode == Configuration.ORIENTATION_LANDSCAPE
                        && (rotation == Surface.ROTATION_180
                        || rotation == Surface.ROTATION_270)) {
                    bearing = (float) Math.toDegrees(orientation[0]) + mDeclination - 90;
                } else {
                    bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                }

                if (!isMapTouched) {
                    updateCamera(bearing);
                }
            }
            angle = Math.toDegrees(orientation[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updateCamera(float bearing) {
        if (maps != null) {
            if (marker != null) {
                marker.setRotation(bearing);
                CameraPosition oldPos = maps.getCameraPosition();
                if (marker.getPosition() != null) {
                    CameraPosition pos = CameraPosition.builder(oldPos).target(marker.getPosition()).bearing(bearing).tilt(55.0f).zoom(18.0f).build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(pos);
                    if (focus) {
                        maps.animateCamera(cameraUpdate, 800, null);
                        focus = false;
                    } else
                        maps.moveCamera(cameraUpdate);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gps:
                if (isMapTouched) {
                    speed.setVisibility(View.GONE);
                    if (currentLocation != null) {
                        isMapTouched = false;
                        focus = true;
                        moveMarker(currentLocation);
                        gps.setImageResource(R.drawable.ic_track_on);
                        mCircle.setVisible(true);
                    } else {
                        Toast.makeText(context, R.string.error_location, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (currentLocation != null) {
                        isMapTouched = true;
                        CameraPosition currentPlace = new CameraPosition.Builder()
                                .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                                .zoom(18f).build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(currentPlace);
                        moveMarker(currentLocation);
                        maps.animateCamera(cameraUpdate);
                        gps.setImageResource(R.drawable.ic_track_off);
                        mCircle.setVisible(false);
                    } else {
                        showSettingsAlert();
                    }
                }
                break;
            case R.id.addMarker:
                markerLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.close_marker_layout:
                markerLayout.setVisibility(View.GONE);
                break;
            case R.id.camera_markers:
                marker = null;
                mCircle = null;
                switchToDefaultMode();
                setUpMap(1);
                markerLayout.setVisibility(View.GONE);
                break;
            case R.id.azs_markers:
                marker = null;
                mCircle = null;
                switchToDefaultMode();
                setUpMap(2);
                markerLayout.setVisibility(View.GONE);
                break;
            case R.id.vulk_markers:
                marker = null;
                mCircle = null;
                switchToDefaultMode();
                setUpMap(3);
                markerLayout.setVisibility(View.GONE);
                break;
        }
    }
}
