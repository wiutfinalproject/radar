package uz.radar.wiut.radar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import uz.radar.wiut.radar.activity.MapFragment;
import uz.radar.wiut.radar.db.AZSDb;
import uz.radar.wiut.radar.db.CameraDb;
import uz.radar.wiut.radar.db.VulkanizatsiyaDb;
import uz.radar.wiut.radar.models.LocationObject;
import uz.radar.wiut.radar.utils.Const;
import uz.radar.wiut.radar.utils.CustomUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Const, View.OnClickListener {

    private static final int COARSE_LOCATION = 007;
    Context context;
    List<LocationObject> roadCameras;
    List<LocationObject> zapravkaPointsList;
    List<LocationObject> vulkanizaciyaPointsList;
    CameraDb dbCamera;
    AZSDb dbZapravka;
    VulkanizatsiyaDb dbVulkanizaciya;

    GoogleApiClient mGoogleApiClient;
    MapFragment mapFragment;

    private MyApplication app;

    private void checkLanguage() {
        if (UZBEK.equals(CustomUtils.getSharedPreferencesString(MainActivity.this, LANGUAGE))) {
            CustomUtils.getSharedPrefString(this, LANGUAGE);

            Configuration conf = getResources().getConfiguration();
            conf.locale = new Locale(UZBEK);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        } else if (RUSSIAN.equals(CustomUtils.getSharedPrefString(MainActivity.this, LANGUAGE))) {
            CustomUtils.getSharedPrefString(MainActivity.this, LANGUAGE);

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

        context = this;

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mapFragment = (MapFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mapFragment");
        } else {
            mapFragment = MapFragment.newInstance();
            addFragment(mapFragment);

            getCameras();
            getZapravkaPoints();
            getVulkanizaciyaPoints();
        }

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






    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {

    }
}
