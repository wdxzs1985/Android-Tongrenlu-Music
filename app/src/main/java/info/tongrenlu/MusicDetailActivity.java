package info.tongrenlu;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import info.tongrenlu.util.OnFragmentInteractionListener;


public class MusicDetailActivity extends ActionBarActivity implements OnFragmentInteractionListener {

    private ImageView mCoverView = null;
    private View mContainer = null;
    private TextView mTitleView = null;

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
        intent.setAction(MusicService.ACTION_CMD);
        intent.putExtras(data);
        intent.putExtra(MusicService.CMD_NAME, MusicService.CMD_PLAY);
        startService(intent);
    }

    private void startPlayerActivity(Context context, final Bundle data) {
        Intent intent = new Intent(context, FullScreenPlayerActivity.class);
        intent.putExtras(data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                                                                                   mCoverView,
                                                                                   "share_cover");
            // start the new activity
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        final long articleId = this.getIntent().getLongExtra("id", 0);

        mCoverView = (ImageView) this.findViewById(R.id.music_cover_view);
        String imageUrl = "http://files.tongrenlu.info/m" + articleId + "/cover_400.jpg";

        mContainer = this.findViewById(R.id.header_container);
        mTitleView = (TextView) this.findViewById(R.id.music_title_view);


        Picasso.with(this).load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {

                mCoverView.setImageBitmap(bitmap);

                Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {
                        if (palette != null) {
                            Palette.Swatch defaultSwatch = palette.getLightMutedSwatch();
                            if (defaultSwatch == null) {
                                defaultSwatch = palette.getDarkVibrantSwatch();
                            }
                            mContainer.setBackgroundColor(defaultSwatch.getRgb());
                            mTitleView.setTextColor(defaultSwatch.getTitleTextColor());

                        }
                    }
                });
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

                mCoverView.setImageDrawable(errorDrawable);

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

                mCoverView.setImageDrawable(placeHolderDrawable);
            }
        });

        TrackListFragment trackListFragment = new TrackListFragment();
        Bundle args = new Bundle();
        args.putLong("articleId", articleId);
        trackListFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, trackListFragment)
                                   .commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music_detail, menu);
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

}
