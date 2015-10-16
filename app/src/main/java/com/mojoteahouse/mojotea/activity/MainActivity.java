package com.mojoteahouse.mojotea.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.MojoImage;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.Topping;
import com.mojoteahouse.mojotea.fragment.ClosedNowDialogFragment;
import com.mojoteahouse.mojotea.fragment.LoadingDialogFragment;
import com.mojoteahouse.mojotea.fragment.MojoMenuFragment;
import com.mojoteahouse.mojotea.fragment.OrderHistoryFragment;
import com.mojoteahouse.mojotea.util.DataUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MojoMenuFragment.GoToCartClickListener {

    public static final String EXTRA_SHOW_ORDER_PAGE = "EXTRA_SHOW_ORDER_PAGE";

    private static final String TAG_MOJO_MENU = "TAG_MOJO_MENU";
    private static final String TAG_ORDER_HISTORY = "TAG_ORDER_HISTORY";
    private static final String TAG_ABOUT = "TAG_ABOUT";
    private static final String TAG_LOADING = "TAG_LOADING";
    private static final String TAG_CLOSED_NOW = "TAG_CLOSED_NOW";

    private static final String UPDATED_AT = "updatedAt";

    private Toolbar toolbar;
    private DrawerLayout navigationDrawer;
    private ActionBarDrawerToggle navigationDrawerToggle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_menu_title);

        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationDrawerToggle = new ActionBarDrawerToggle(this, navigationDrawer, toolbar,
                R.string.action_open_navigation_drawer, R.string.action_close_navigation_drawer);
        navigationDrawer.setDrawerListener(navigationDrawerToggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            boolean isClosedNow = sharedPreferences.getBoolean(MojoTeaApp.PREF_CLOSED_NOW, false);
            if (isClosedNow) {
                showClosedNowDialog();
            }
            if (!isNetworkConnected) {
                showErrorMessage(R.string.no_network_relaunch_error_message);
            } else {
                showLoadingDialog();
                if (!sharedPreferences.contains(MojoTeaApp.PREF_REMOTE_DATA_LOADED)) {
                    sharedPreferences.edit().putBoolean(MojoTeaApp.PREF_REMOTE_DATA_LOADED, true).apply();
                    fetchRemoteDataToLocal();
                } else {
                    syncLocalDataWithRemote();
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigationDrawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_SHOW_ORDER_PAGE)) {
            updateFragment(TAG_ORDER_HISTORY, true);
        }
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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // Handle navigation view item clicks here.
        switch (menuItem.getItemId()) {
            case R.id.nav_menu:
                updateFragment(TAG_MOJO_MENU, false);
                break;

            case R.id.nav_orders:
                updateFragment(TAG_ORDER_HISTORY, false);
                break;

            case R.id.nav_about:
                updateFragment(TAG_ABOUT, false);
                break;
        }

        navigationDrawer.closeDrawer(GravityCompat.START);
        return true;
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

    @Override
    public void onGoToCartButtonClicked() {
        Intent intent = new Intent(this, CartActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, toolbar, getString(R.string.toolbar_transition));
        startActivity(intent, optionsCompat.toBundle());
    }

    private void updateFragment(String tag, boolean createNewFragment) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_content);
        switch (tag) {
            case TAG_MOJO_MENU:
                getSupportActionBar().setTitle(R.string.nav_menu_title);
                if (fragment == null || !(fragment instanceof MojoMenuFragment) || createNewFragment) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_content, MojoMenuFragment.newInstance(), tag)
                            .commit();
                }
                break;

            case TAG_ORDER_HISTORY:
                getSupportActionBar().setTitle(R.string.nav_orders_title);
                if (fragment == null || !(fragment instanceof OrderHistoryFragment) || createNewFragment) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_content, OrderHistoryFragment.newInstance(), tag)
                            .commit();
                }
                break;

            case TAG_ABOUT:
                getSupportActionBar().setTitle(R.string.nav_about_title);
                break;
        }
    }

    private void showErrorMessage(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private void showLoadingDialog() {
        LoadingDialogFragment fragment
                = (LoadingDialogFragment) getFragmentManager().findFragmentByTag(TAG_LOADING);
        if (fragment == null) {
            fragment = LoadingDialogFragment.newInstance();
        }
        fragment.show(getFragmentManager(), TAG_LOADING);
    }

    private void dismissLoadingDialog() {
        LoadingDialogFragment loadingFragment
                = (LoadingDialogFragment) getFragmentManager().findFragmentByTag(TAG_LOADING);
        if (loadingFragment != null) {
            loadingFragment.dismiss();
        }
    }

    private void showClosedNowDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_CLOSED_NOW);
        if (fragment == null) {
            ClosedNowDialogFragment.newInstance().show(getFragmentManager(), TAG_CLOSED_NOW);
        }
    }

    private void fetchRemoteDataToLocal() {
        ParseQuery<Topping> toppingQuery = Topping.getQuery();
        toppingQuery.findInBackground(new FindCallback<Topping>() {
            @Override
            public void done(List<Topping> toppingList, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(MojoTeaApp.TOPPING_GROUP, toppingList);
                }
            }
        });

        ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
        mojoImageQuery.findInBackground(new FindCallback<MojoImage>() {
            @Override
            public void done(List<MojoImage> mojoImageList, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_IMAGE_GROUP, mojoImageList);
                }
                fetchRemoteMojoMenuInfo();
            }
        });
    }

    private void fetchRemoteMojoMenuInfo() {
        ParseQuery<MojoMenu> mojoMenuQuery = MojoMenu.getQuery();
        mojoMenuQuery.findInBackground(new FindCallback<MojoMenu>() {
            @Override
            public void done(final List<MojoMenu> mojoMenuList, ParseException e) {
                if (e != null) {
                    showErrorMessage(R.string.fetch_remote_data_error_message);
                    fetchRemoteOrderInfo();
                } else {
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_MENU_GROUP, mojoMenuList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                showErrorMessage(R.string.fetch_remote_data_error_message);
                            } else {
                                Set<String> categorySet = new HashSet<>();
                                categorySet.addAll(DataUtils.getMojoMenuCategoryList(mojoMenuList));
                                sharedPreferences.edit().putStringSet(MojoTeaApp.PREF_MOJO_MENU_CATEGORY_SET, categorySet).apply();
                            }
                            fetchRemoteOrderInfo();
                        }
                    });
                }
            }
        });
    }

    private void fetchRemoteOrderInfo() {
        ParseQuery<Order> orderQuery = Order.getQuery();
        orderQuery.whereEqualTo(Order.ANONYMOUS_USER_ID, DataUtils.getDeviceId(this));
        orderQuery.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orderList, ParseException e) {
                if (e != null) {
                    dismissLoadingDialog();
                    showErrorMessage(R.string.fetch_remote_data_error_message);
                } else {
                    ParseObject.pinAllInBackground(MojoTeaApp.ORDER_GROUP, orderList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dismissLoadingDialog();
                            if (e != null) {
                                showErrorMessage(R.string.fetch_remote_data_error_message);
                            } else {
                                sharedPreferences.edit().putLong(MojoTeaApp.PREF_LAST_SYNC_TIMESTAMP, new Date().getTime()).apply();
                            }

                            showInitialFragment(false);
                        }
                    });
                }
            }
        });
    }

    private void syncLocalDataWithRemote() {
        long lastUpdatedTime = sharedPreferences.getLong(MojoTeaApp.PREF_LAST_SYNC_TIMESTAMP, 0);
        Date lastUpdatedDate = new Date(lastUpdatedTime);

        ParseQuery<Topping> toppingQuery = Topping.getQuery();
        toppingQuery.whereGreaterThan(UPDATED_AT, lastUpdatedDate);
        toppingQuery.findInBackground(new FindCallback<Topping>() {
            @Override
            public void done(final List<Topping> toppingList, ParseException e) {
                if (e == null && !toppingList.isEmpty()) {
                    ParseObject.pinAllInBackground(MojoTeaApp.TOPPING_GROUP, toppingList);
                }
            }
        });

        ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
        mojoImageQuery.whereGreaterThan(UPDATED_AT, lastUpdatedDate);
        mojoImageQuery.findInBackground(new FindCallback<MojoImage>() {
            @Override
            public void done(final List<MojoImage> mojoImageList, ParseException e) {
                if (e == null && !mojoImageList.isEmpty()) {
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_IMAGE_GROUP, mojoImageList);
                }
            }
        });

        syncLocalMojoMenuInfo(lastUpdatedDate);
    }

    private void syncLocalMojoMenuInfo(final Date lastUpdatedDate) {
        ParseQuery<MojoMenu> remoteMojoMenuQuery = MojoMenu.getQuery();
        remoteMojoMenuQuery.whereGreaterThan(UPDATED_AT, lastUpdatedDate);
        remoteMojoMenuQuery.findInBackground(new FindCallback<MojoMenu>() {
            @Override
            public void done(final List<MojoMenu> mojoMenuList, ParseException e) {
                if (e == null && !mojoMenuList.isEmpty()) {
                    Set<String> mojoMenuCategorySet = sharedPreferences.getStringSet(
                            MojoTeaApp.PREF_MOJO_MENU_CATEGORY_SET, new HashSet<String>());
                    for (MojoMenu mojoMenu : mojoMenuList) {
                        mojoMenuCategorySet.add(mojoMenu.getCategory());
                    }
                    sharedPreferences.edit().putStringSet(MojoTeaApp.PREF_MOJO_MENU_CATEGORY_SET, mojoMenuCategorySet).apply();
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_MENU_GROUP, mojoMenuList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            syncLocalOrderInfo(lastUpdatedDate);
                        }
                    });
                } else {
                    syncLocalOrderInfo(lastUpdatedDate);
                }
            }
        });
    }

    private void syncLocalOrderInfo(Date lastUpdatedDate) {
        ParseQuery<Order> remoteOrderQuery = Order.getQuery();
        remoteOrderQuery.whereEqualTo(Order.ANONYMOUS_USER_ID, DataUtils.getDeviceId(this))
                .whereGreaterThan(UPDATED_AT, lastUpdatedDate);
        remoteOrderQuery.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(final List<Order> orderList, ParseException e) {
                if (e == null && !orderList.isEmpty()) {
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_MENU_GROUP, orderList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dismissLoadingDialog();
                            showInitialFragment(true);
                        }
                    });
                } else {
                    dismissLoadingDialog();
                    showInitialFragment(true);
                }
            }
        });

        sharedPreferences.edit().putLong(MojoTeaApp.PREF_LAST_SYNC_TIMESTAMP, new Date().getTime()).apply();
    }

    private void showInitialFragment(boolean createNewFragment) {
        if (getIntent().hasExtra(EXTRA_SHOW_ORDER_PAGE)) {
            updateFragment(TAG_ORDER_HISTORY, createNewFragment);
        } else {
            updateFragment(TAG_MOJO_MENU, createNewFragment);
        }
    }
}
