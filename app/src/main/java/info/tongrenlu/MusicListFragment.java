package info.tongrenlu;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import info.tongrenlu.adapter.MusicListAdapter;
import info.tongrenlu.domain.MusicBean;
import info.tongrenlu.util.BaseLoader;
import info.tongrenlu.util.OnFragmentInteractionListener;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class MusicListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
                                                           AbsListView.OnItemClickListener,
                                                           LoaderManager.LoaderCallbacks<List<MusicBean>> {
    public static final int MUSIC_LIST_LOADER_ID = 0;
    private static final int SPAN_COUNT = 3;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private MusicListAdapter mAdapter;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private OnFragmentInteractionListener mListener = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MusicListFragment() {
    }


    @Override
    public void onRefresh() {
        this.getLoaderManager().restartLoader(MUSIC_LIST_LOADER_ID, Bundle.EMPTY, this);
    }


    @Override
    public Loader<List<MusicBean>> onCreateLoader(final int id, final Bundle args) {
        mSwipeRefreshLayout.setRefreshing(true);
        return new BaseLoader<List<MusicBean>>(this.getActivity().getApplicationContext()) {
            @Override
            public List<MusicBean> loadInBackground() {

                List<MusicBean> data = new ArrayList<>();
                data.add(new MusicBean(Long.valueOf(1001), "Dolls"));
                data.add(new MusicBean(Long.valueOf(1002), "Dolls"));
                data.add(new MusicBean(Long.valueOf(1003), "Dolls"));
                data.add(new MusicBean(Long.valueOf(1004), "Dolls"));
                data.add(new MusicBean(Long.valueOf(1005), "Dolls"));

                return data;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<List<MusicBean>> loader, final List<MusicBean> data) {
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(final Loader<List<MusicBean>> loader) {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent,
                            final View view,
                            final int position,
                            final long id) {
        MusicBean musicBean = mAdapter.getItem(position);

        Bundle data = new Bundle();
        data.putLong("id", musicBean.getId());
        data.putString("title", musicBean.getTitle());

        View shareCoverView = view.findViewById(R.id.music_cover_view);
        this.mListener.onFragmentInteraction(this, data, new Pair<>(shareCoverView, "share_cover"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.material_pink_500,
                                                    R.color.material_light_green_500,
                                                    R.color.material_cyan_500,
                                                    R.color.material_yellow_500);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);

        mAdapter = new MusicListAdapter();
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getLoaderManager().initLoader(MUSIC_LIST_LOADER_ID, Bundle.EMPTY, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnFragmentInteractionListener) {
            try {
                mListener = (OnFragmentInteractionListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() +
                                             " must implement OnFragmentInteractionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        //        View emptyView = mListView.getEmptyView();
        //
        //        if (emptyView instanceof TextView) {
        //            ((TextView) emptyView).setText(emptyText);
        //        }
    }

}
