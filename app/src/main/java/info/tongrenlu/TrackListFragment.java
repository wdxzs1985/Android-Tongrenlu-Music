package info.tongrenlu;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

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
public class TrackListFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<TrackBean>> {

    private static final int TRACK_LIST_LOADER_ID = 0;
    private OnFragmentInteractionListener mListener;

    private SimpleRecyclerViewAdapter mAdapter = null;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_cheese_list,
                                                                    null,
                                                                    false);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        mAdapter = new SimpleRecyclerViewAdapter(this,
                                                 new ArrayList<TrackBean>());
        recyclerView.setAdapter(mAdapter);
        return recyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

                    for (int i=0;i<20;i++) {
                        data.add(new TrackBean(Long.valueOf(52744),
                                               "Track",
                                               "802e5f850dbb388d99165e2eec784d4f",
                                               articleId));
                    }
                    return data;
                }
            };
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<ArrayList<TrackBean>> loader,
                               final ArrayList<TrackBean> data) {
        mAdapter.setValues(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(final Loader<ArrayList<TrackBean>> loader) {
        loader.reset();
    }

    @Override
    public void onAttach(Context activity) {
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

    public static class SimpleRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private final TrackListFragment mFragment;
        private int mBackground;
        private ArrayList<TrackBean> mValues;

        public SimpleRecyclerViewAdapter(TrackListFragment fragment, ArrayList<TrackBean> items) {
            mFragment = fragment;
            fragment.getActivity()
                    .getTheme()
                    .resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        public TrackBean getValueAt(int position) {
            return mValues.get(position);
        }

        public void setValues(ArrayList<TrackBean> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate( android.R.layout.simple_list_item_1, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final TrackBean selectTrackBean = getValueAt(position);
            holder.mBoundItem = selectTrackBean;
            holder.mTextView.setText(selectTrackBean.getName());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle data = new Bundle();
                    data.putParcelableArrayList(MusicService.PARAM_TRACK_LIST,mValues);
                    data.putInt(MusicService.PARAM_POSITION, position);

                    data.putString(MusicService.PARAM_TITLE, selectTrackBean.getName());
                    data.putParcelable(MusicService.PARAM_COVER, Uri.parse("http://files.tongrenlu.info/m" +
                                                                           selectTrackBean.getArticleId() +
                                                                           "/cover_400.jpg"));

                    mFragment.mListener.onFragmentInteraction(mFragment, data);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTextView;
            public TrackBean mBoundItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTextView = (TextView) view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }
    }
}
