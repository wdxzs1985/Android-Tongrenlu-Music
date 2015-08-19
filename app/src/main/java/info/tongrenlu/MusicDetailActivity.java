package info.tongrenlu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import info.tongrenlu.util.OnFragmentInteractionListener;


public class MusicDetailActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ImageView mCoverView = null;

    @Override
    public void onFragmentInteraction(final Fragment target,
                                      final Bundle data,
                                      Pair... shareElements) {
        if (target instanceof TrackListFragment) {
            // get the element that receives the click event
            Context context = this.getApplicationContext();
            startMusicService(context, data);
            startPlayerActivity(context, data);
        }
    }

    private void startMusicService(Context context, final Bundle data) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.CMD_PLAY);
        intent.putExtras(data);
        startService(intent);
    }

    private void startPlayerActivity(Context context, final Bundle data) {
        Intent intent = new Intent(context, FullScreenPlayerActivity.class);
        intent.putExtras(data);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                                                                                   mCoverView,
                                                                                   "share_cover");

        // start the new activity
        ActivityCompat.startActivity(this,intent, options.toBundle());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        Intent intent = getIntent();
        final long articleId = intent.getLongExtra("id", 0);
        final String title = intent.getStringExtra("title");

        if (articleId == 0) {
            this.finish();
            return;
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(title);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String imageUrl = "http://files.tongrenlu.info/m" + articleId + "/cover_400.jpg";
        mCoverView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(imageUrl).centerCrop().into(mCoverView);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
        }



    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(getTrackListFragment(), "TrackList");
        viewPager.setAdapter(adapter);
    }

    private TrackListFragment getTrackListFragment() {
        TrackListFragment fragment = new TrackListFragment();
        Bundle args = new Bundle();
        args.putLong("articleId", getIntent().getLongExtra("id", 0));
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.sample_actions, menu);
        getMenuInflater().inflate(R.menu.menu_music_detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();

                ActivityCompat.finishAfterTransition(this);
                return true;
            case R.id.action_settings:

                return true;
        }
        return super.onOptionsItemSelected(item);
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
