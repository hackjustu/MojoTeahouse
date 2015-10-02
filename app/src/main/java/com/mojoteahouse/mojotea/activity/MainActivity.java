package com.mojoteahouse.mojotea.activity;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.fragment.MojoMenuFragment;
import com.mojoteahouse.mojotea.fragment.OrderHistoryFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG_MOJO_MENU = "TAG_MOJO_MENU";
    private static final String TAG_ORDER_HISTORY = "TAG_ORDER_HISTORY";
    private static final String TAG_ABOUT = "TAG_ABOUT";

    private DrawerLayout navigationDrawer;
    private ActionBarDrawerToggle navigationDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerToggle = new ActionBarDrawerToggle(
                this, navigationDrawer, toolbar, R.string.action_open_navigation_drawer, R.string.action_close_navigation_drawer);
        navigationDrawer.setDrawerListener(navigationDrawerToggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateFragment(TAG_MOJO_MENU);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (navigationDrawer.isDrawerOpen(GravityCompat.START)) {
            navigationDrawer.closeDrawer(GravityCompat.START);
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
        switch (item.getItemId()) {
            case R.id.nav_menu:
                updateFragment(TAG_MOJO_MENU);
                break;

            case R.id.nav_orders:
                updateFragment(TAG_ORDER_HISTORY);
                break;

            case R.id.nav_about:
                updateFragment(TAG_ABOUT);
                break;
        }

        navigationDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateFragment(String tag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        switch (tag) {
            case TAG_MOJO_MENU:
                if (fragment == null || !(fragment instanceof MojoMenuFragment)) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_content, MojoMenuFragment.newInstance(), TAG_MOJO_MENU)
                            .commit();
                }
                break;

            case TAG_ORDER_HISTORY:
                if (fragment == null || !(fragment instanceof OrderHistoryFragment)) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_content, OrderHistoryFragment.newInstance(), TAG_ORDER_HISTORY)
                            .commit();
                }
                break;

            case TAG_ABOUT:
                break;
        }
    }
}
