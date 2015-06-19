/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.tongrenlu;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import info.tongrenlu.util.OnFragmentInteractionListener;

/**
 * TODO
 */
public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final int ALBUM_LOADER = 0;
    public static final int PLAYLIST_LOADER = 1;
    public static final int TRACK_LOADER = 2;


    private DrawerLayout mDrawerLayout;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    public void onFragmentInteraction(final Fragment target, Bundle data, Pair<View,String>[] sharedElements) {
        if (target instanceof MusicListFragment) {
            // get the element that receives the click event

            Intent intent = new Intent(this.getApplicationContext(), MusicDetailActivity.class);
            intent.putExtras(data);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                                                                               sharedElements);
            // start the new activity
            ActivityCompat.startActivity(this,intent, options.toBundle());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if(ab!=null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        // mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new MusicListFragment(), getString(R.string.label_album));
        adapter.addFragment(new PlaylistFragment(),getString(R.string.label_playlist));
        adapter.addFragment(new TrackFragment(), getString(R.string.label_track));
        viewPager.setAdapter(adapter);

        mTabLayout.setupWithViewPager(viewPager);
    }

    private void setupDrawerContent(NavigationView navigationView) {

        ImageView userAvatar = (ImageView) navigationView.findViewById(R.id.avatar);
        Glide.with(this).load(R.drawable.cheese_2).into(userAvatar);

        TextView username = (TextView) navigationView.findViewById(R.id.username);
        username.setText("Guest");

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                //mViewPager.setCurrentItem(0,true);
                                //setupViewPager(mViewPager);
                                break;
                            case R.id.nav_messages:
                                //mViewPager.setCurrentItem(1,true);
                                //setupViewPager2(mViewPager);
                                break;
                        }

                        return true;
                    }
                });
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
