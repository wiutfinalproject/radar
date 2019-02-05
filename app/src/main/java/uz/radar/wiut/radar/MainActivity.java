package uz.radar.wiut.radar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uz.radar.wiut.radar.activity.LoginActivity;
import uz.radar.wiut.radar.activity.MapFragment;
import uz.radar.wiut.radar.activity.SettingsActivity;
import uz.radar.wiut.radar.activity.SplashActivity;
import uz.radar.wiut.radar.db.AZSDb;
import uz.radar.wiut.radar.db.CameraDb;
import uz.radar.wiut.radar.db.IDbCRUD;
import uz.radar.wiut.radar.db.VulkanizatsiyaDb;
import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;
import uz.radar.wiut.radar.utils.CustomUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Const, View.OnClickListener {

    private static final int COARSE_LOCATION = 007;
    Context context;
    List<LocationObject> roadCamerasList;
    List<LocationObject> zapravkaPointsList;
    List<LocationObject> vulkanizaciyaPointsList;
    CameraDb dbCamera;
    AZSDb dbZapravka;
    VulkanizatsiyaDb dbVulkanizaciya;
//    IDbCRUD<LocationObject> db;

    private FirebaseAuth mAuth;
    DatabaseReference myRef;
    DatabaseReference azsRef;
    DatabaseReference camerasRef;
    DatabaseReference vulkansRef;


    GoogleApiClient mGoogleApiClient;
    MapFragment mapFragment;
    private MyApplication app;


    private void checkLanguage() {
        if (UZBEK.equals(CustomUtils.getSharedPreferencesString(MainActivity.this, LANGUAGE))) {
            CustomUtils.getSharedPreferencesString(this, LANGUAGE);

            Configuration conf = getResources().getConfiguration();
            conf.locale = new Locale(UZBEK);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        } else if (RUSSIAN.equals(CustomUtils.getSharedPreferencesString(MainActivity.this, LANGUAGE))) {
            CustomUtils.getSharedPreferencesString(MainActivity.this, LANGUAGE);
            Configuration conf = getResources().getConfiguration();
            conf.locale = new Locale(RUSSIAN);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mAuth = FirebaseAuth.getInstance();

        context = this;
        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        azsRef = myRef.child(Const.AZSFB);
        camerasRef = myRef.child(Const.CAMERASFB);
        vulkansRef = myRef.child(Const.VULKANSFB);
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mapFragment = (MapFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mapFragment");
        } else {
            mapFragment = MapFragment.newInstance();
            addFragment(mapFragment);

            getCameras();
            getAzsPoints();
            getVulkanizaciyaPoints();
        }
        if (mAuth.getCurrentUser() != null){
            TextView email = findViewById(R.id.etMyEmail);
            email.setText(mAuth.getCurrentUser().getEmail());
        }

    }

    private void getVulkanizaciyaPoints() {
        vulkansRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) return;
                vulkanizaciyaPointsList = new ArrayList<>();
                Log.e("FIREBASE ", "VULK " + dataSnapshot.getChildrenCount());
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    vulkanizaciyaPointsList.add(data.getValue(LocationObject.class));
                }
                saveVulkanizaciyaPointsToDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load Vulkan data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAzsPoints() {
        azsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) return;
                zapravkaPointsList = new ArrayList<>();
                Log.e("FIREBASE ", "azs " + dataSnapshot.getChildrenCount());
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    zapravkaPointsList.add(data.getValue(LocationObject.class));
                }
                saveZapravkaPointsToDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCameras() {
        camerasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) return;
                roadCamerasList = new ArrayList<>();
                Log.e("FIREBASE ", "cam " + dataSnapshot.getChildrenCount());
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    roadCamerasList.add(data.getValue(LocationObject.class));
                }
                saveCamerasToDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw databaseError.toException(); // Don't ignore errors
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case COARSE_LOCATION:
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                if (fragments != null) {
                    for (Fragment fragment : fragments) {
                        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
                }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (mapFragment != null) {
            //Save the fragment's instance
            getSupportFragmentManager().putFragment(outState, "mapFragment", mapFragment);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            showLogoutAlertMessage();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void addFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_container, fragment);
            transaction.commit();
        }
    }

    public void showLogoutAlertMessage() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        // Setting Dialog Title
        alertDialog.setTitle(R.string.logout_tittle);
        // On pressing Settings button
        alertDialog.setPositiveButton(R.string.log_out, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                logout();
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

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, SplashActivity.class));
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {

    }

    private void saveCamerasToDb() {
        if (roadCamerasList != null && roadCamerasList.size() > 0) {
            dbCamera = new CameraDb(context);
            dbCamera.delete(Const.TABLE_CAM);
            for (int i = 0; i < roadCamerasList.size(); i++) {
                dbCamera.insert(roadCamerasList.get(i));
            }
            dbCamera.close();
        }
    }

    private void saveZapravkaPointsToDb() {
        if (zapravkaPointsList != null && zapravkaPointsList.size() > 0) {
            dbZapravka = new AZSDb(context);
            dbZapravka.delete(Const.TABLE_ZAPRAVKA);
            for (int i = 0; i < zapravkaPointsList.size(); i++) {
                dbZapravka.insert(zapravkaPointsList.get(i));
            }
            dbZapravka.close();
        }
    }

    private void saveVulkanizaciyaPointsToDb() {
        if (vulkanizaciyaPointsList != null && vulkanizaciyaPointsList.size() > 0) {
            dbVulkanizaciya = new VulkanizatsiyaDb(context);
            dbVulkanizaciya.delete(Const.TABLE_VULKANIZACIYA);
            for (int i = 0; i < vulkanizaciyaPointsList.size(); i++) {
                dbVulkanizaciya.insert(vulkanizaciyaPointsList.get(i));
            }
            dbVulkanizaciya.close();
        }
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.settings:
//                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//                break;
//            case R.id.logout:
//                ShowLogoutAlertMessage();
//                break;
//            case R.id.menu:
//                drawerLayout.openDrawer(Gravity.LEFT);
//                break;
//            case R.id.aboutLayout:
//                drawerLayout.closeDrawer(Gravity.LEFT);
//                startActivity(new Intent(MainActivity.this, AboutUs.class));
//                break;
//        }
//    }

    protected void onStart() {
        checkLanguage();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        //  this.finishDownloading();
        super.onDestroy();
    }
}
