package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.activity.EditMojoItemActivity;
import com.mojoteahouse.mojotea.adapter.MojoMenuItemAdapter;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MojoMenuFragment extends BaseFragment implements View.OnClickListener,
        MojoMenuItemAdapter.MojoMenuItemClickListener {

    private static final int REQUEST_CODE_EDIT_ITEM = 1;

    private Button goToCartButton;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SharedPreferences sharedPreferences;
    private List<String> mojoMenuCategories;
    private MojoMenuItemAdapter[] mojoMenuItemAdapters;
    private GoToCartClickListener goToCartClickListener;
    private int totalCount;

    public static MojoMenuFragment newInstance() {
        MojoMenuFragment fragment = new MojoMenuFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public MojoMenuFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            goToCartClickListener = (GoToCartClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement "
                    + GoToCartClickListener.class.getSimpleName());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mojo_menu, container, false);

        goToCartButton = (Button) view.findViewById(R.id.go_to_cart_button);
        goToCartButton.setOnClickListener(this);

        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        Set<String> mojoMenuCategorySet = sharedPreferences.getStringSet(
                MojoTeaApp.PREF_MOJO_MENU_CATEGORY_SET, new HashSet<String>());
        mojoMenuCategories = new ArrayList<>();
        mojoMenuCategories.addAll(mojoMenuCategorySet);
        Collections.sort(mojoMenuCategories);
        setupViewPager();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDataInBackground();
        updateButtonText(sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0));
    }

    @Override
    public void onItemClicked(MojoMenu mojoMenu) {
        if (mojoMenu.isSoldOut()) {
            Toast.makeText(getActivity(), R.string.mojo_menu_sold_out_message, Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(getActivity(), EditMojoItemActivity.class);
            intent.putExtra(EditMojoItemActivity.EXTRA_MOJO_MENU_ID, mojoMenu.getMenuId());
            ArrayList<Integer> availableToppingList = new ArrayList<>();
            availableToppingList.addAll(mojoMenu.getToppingIdList());
            intent.putIntegerArrayListExtra(EditMojoItemActivity.EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS, availableToppingList);
            startActivityForResult(intent, REQUEST_CODE_EDIT_ITEM);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_cart_button:
                goToCartClickListener.onGoToCartButtonClicked();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_ITEM && resultCode == Activity.RESULT_OK) {
            int quantity = data.getIntExtra(EditMojoItemActivity.EXTRA_QUANTITY, 1);
            updateButtonText(quantity);
        }
    }

    private void setupViewPager() {
        int count = mojoMenuCategories.size();
        RecyclerView[] recyclerViews = new RecyclerView[count];
        mojoMenuItemAdapters = new MojoMenuItemAdapter[count];
        for (int i = 0; i < count; i++) {
            RecyclerView recyclerView = new RecyclerView(getActivity());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            mojoMenuItemAdapters[i] = new MojoMenuItemAdapter(getActivity(), new ArrayList<MojoMenu>(), this);
            recyclerView.setAdapter(mojoMenuItemAdapters[i]);
            recyclerViews[i] = recyclerView;
        }
        MojoMenuTabsAdapter tabsAdapter = new MojoMenuTabsAdapter(mojoMenuCategories, recyclerViews);
        viewPager.setAdapter(tabsAdapter);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
    }

    private void loadDataInBackground() {
        ParseQuery<MojoMenu> localMojoMenuQuery = MojoMenu.getQuery();
        localMojoMenuQuery.fromLocalDatastore();
        localMojoMenuQuery.findInBackground(new FindCallback<MojoMenu>() {
            @Override
            public void done(List<MojoMenu> mojoMenuList, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), R.string.get_mojo_menu_error_message, Toast.LENGTH_LONG).show();
                } else {
                    setupDataAdapters(mojoMenuList);
                }
            }
        });
    }

    private void setupDataAdapters(List<MojoMenu> mojoMenus) {
        int count = mojoMenuCategories.size();
        for (int i = 0; i < count; i++) {
            List<MojoMenu> mojoMenuList = new ArrayList<>();
            for (MojoMenu mojoMenu : mojoMenus) {
                String category = mojoMenuCategories.get(i);
                if (category == null) {
                    break;
                } else if (category.equals(mojoMenu.getCategory())) {
                    mojoMenuList.add(mojoMenu);
                }
            }
            mojoMenuItemAdapters[i].updateMojoMenuList(mojoMenuList);
        }
    }

    private void updateButtonText(int quantity) {
        totalCount += quantity;
        goToCartButton.setText(String.format(getString(R.string.go_to_cart_button_text), totalCount));
        goToCartButton.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);

        if (sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0) != totalCount) {
            sharedPreferences.edit().putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, totalCount).apply();
        }
    }


    public interface GoToCartClickListener {

        void onGoToCartButtonClicked();
    }

    private class MojoMenuTabsAdapter extends PagerAdapter {

        private List<String> categories;
        private RecyclerView[] pages;

        public MojoMenuTabsAdapter(List<String> categories, RecyclerView[] pages) {
            this.categories = categories;
            this.pages = pages;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page = pages[position];
            container.addView(page);
            return page;
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
