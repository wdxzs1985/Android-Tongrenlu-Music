package info.tongrenlu;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import info.tongrenlu.adapter.MusicTrackAdapter;
import info.tongrenlu.domain.TrackBean;
import info.tongrenlu.util.BaseLoader;
import info.tongrenlu.util.OnFragmentInteractionListener;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class TrackListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<TrackBean>> {

    private static final int TRACK_LIST_LOADER_ID = 0;
    private OnFragmentInteractionListener mListener;

    private MusicTrackAdapter mAdapter = null;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content
        mAdapter = new MusicTrackAdapter();
        setListAdapter(mAdapter);

        this.getLoaderManager().initLoader(TRACK_LIST_LOADER_ID, this.getArguments(), this);
    }


    @Override
    public Loader<ArrayList<TrackBean>> onCreateLoader(final int id, final Bundle args) {
        if (id == TRACK_LIST_LOADER_ID) {
            return new BaseLoader<ArrayList<TrackBean>>(this.getActivity()
                                                            .getApplicationContext()) {
                @Override
                public ArrayList<TrackBean> loadInBackground() {
                    ArrayList<TrackBean> data = new ArrayList<>();

                    Long articleId = args.getLong("articleId");


                    data.add(new TrackBean(Long.valueOf(52744),
                                           "Intro",
                                           "802e5f850dbb388d99165e2eec784d4f",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52745),
                                           "Dolls",
                                           "bf397640f5b025eabecc8895ecd8996b",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52746),
                                           "Dreaming",
                                           "ce9f2e30215155d77a1bf4a58693d651",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52747),
                                           "Shinto Shrine",
                                           "015233c129a8e8e915835ed1f7599943",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52748),
                                           "Eighteen Four",
                                           "da91667641ea5c8bb26831ccf829f413",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52749),
                                           "Scolded By The Princess",
                                           "17057fef57a6ad757c11fd7c85b71ad4",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52750),
                                           "Infinite Being",
                                           "06a92d985fd8b81e71915e17c63ae757",
                                           articleId));
                    data.add(new TrackBean(Long.valueOf(52751),
                                           "The Last Judgement",
                                           "5f27b57aca2b89fa780717f5083122a8",
                                           articleId));
                    return data;
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<ArrayList<TrackBean>> loader,
                               final ArrayList<TrackBean> data) {
        mAdapter.setTrackList(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(final Loader<ArrayList<TrackBean>> loader) {
        loader.reset();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                                         " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (null != mListener) {
            Bundle data = new Bundle();
            data.putParcelableArrayList(MusicService.PARAM_TRACK_LIST, mAdapter.getTrackList());
            data.putInt(MusicService.PARAM_POSITION, position);

            TrackBean selectTrackBean = mAdapter.getItem(position);
            data.putString(MusicService.PARAM_TITLE, selectTrackBean.getName());
            data.putParcelable(MusicService.PARAM_COVER, Uri.parse("http://files.tongrenlu.info/m" +
                                                                   selectTrackBean.getArticleId() +
                                                                   "/cover_400.jpg"));

            mListener.onFragmentInteraction(this, data);
        }
    }


}
