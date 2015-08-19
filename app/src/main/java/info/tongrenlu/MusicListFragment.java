package info.tongrenlu;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import info.tongrenlu.domain.MusicBean;
import info.tongrenlu.ui.SimpleRecyclerView;
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
public class MusicListFragment extends Fragment implements
                                                           LoaderManager.LoaderCallbacks<List<MusicBean>> {


    /**
     * The fragment's ListView/GridView.
     */
    private SimpleStringRecyclerViewAdapter mAdapter;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */


    private OnFragmentInteractionListener mListener = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MusicListFragment() {
    }

    @Override
    public Loader<List<MusicBean>> onCreateLoader(final int id, final Bundle args) {
        return new BaseLoader<List<MusicBean>>(this.getActivity().getApplicationContext()) {
            @Override
            public List<MusicBean> loadInBackground() {
                List<MusicBean> data = new ArrayList<>();
                for (int i= 0;i<30; i++){
                    data.add(new MusicBean(2582L,
                                           "house-set-of-touhou-project-rare-tracks"));
                }
                return data;
            }
        };
    }

    @Override
    public void onLoadFinished(final Loader<List<MusicBean>> loader, final List<MusicBean> data) {
        mAdapter.setValues(data);
        mAdapter.notifyDataSetChanged();
        //mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(final Loader<List<MusicBean>> loader) {
       // mSwipeRefreshLayout.setRefreshing(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        SimpleRecyclerView recyclerView = (SimpleRecyclerView) view.findViewById(android.R.id.list);
        recyclerView.setEmptyView(view.findViewById(android.R.id.empty));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mAdapter = new SimpleStringRecyclerViewAdapter(getActivity(), this,
                                                       new ArrayList<MusicBean>());
        recyclerView.setAdapter(mAdapter);
        return recyclerView;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity activity = this.getActivity();


        activity.getSupportLoaderManager()
                .initLoader(MainActivity.ALBUM_LOADER, Bundle.EMPTY, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            try {
                mListener = (OnFragmentInteractionListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() +
                                             " must implement OnFragmentInteractionListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    //    public void setEmptyText(CharSequence emptyText) {
    //                View emptyView = mListView.getEmptyView();
    //
    //                if (emptyView instanceof TextView) {
    //                    ((TextView) emptyView).setText(emptyText);
    //                }
    //    }

    public static class SimpleStringRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> implements
                                                                                                 ItemClickListener {

        public final MusicListFragment mFragment;
        private final TypedValue mTypedValue = new TypedValue();
        private Context mContext;
        private int mBackground;
        private List<MusicBean> mValues;

        public SimpleStringRecyclerViewAdapter(Context context,
                                               MusicListFragment fragment,
                                               List<MusicBean> items) {
            mContext = context;
            mFragment = fragment;
            //            Context context = mFragment.getActivity();
            //            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        //        public MusicBean getValueAt(int position) {
        //            return mValues.get(position);
        //        }

        public void setValues(List<MusicBean> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.list_item, parent, false);

            view.setBackgroundResource(mBackground);
            return new ViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            MusicBean item =  mValues.get(position);

            holder.mTitleTextView.setText(item.getTitle());


            Uri imageUri = Uri.parse("http://files.tongrenlu.info/m" +
                                     item.getId() +
                                     "/cover_400.jpg");

            Glide.with(holder.mImageView.getContext())
                 .load(imageUri)
                 .fitCenter()
                 .into(holder.mImageView);

            Glide.with(mContext)
                 .load(imageUri)
                 .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                 .placeholder(R.drawable.cover)
                 .override(400, 400)
                 .into(holder.mImageView);

        }

        @Override
        public int getItemCount() {
            return mValues == null ? 0 : mValues.size();
        }

        @Override
        public void onItemClick(View view, int position) {
            View heroView = view.findViewById(android.R.id.icon);
            MusicBean item = mValues.get(position);

            Bundle data = new Bundle();
            data.putLong("id", item.getId());
            data.putString("title", item.getTitle());

            mFragment.mListener.onFragmentInteraction(mFragment,
                                                      data,
                                                      new Pair<>(heroView, "share_cover"));

        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitleTextView;
        TextView mDescriptionTextView;
        TextView mOverlayTextView;
        ImageView mImageView;
        ItemClickListener mItemClickListener;

        public ViewHolder(View view, ItemClickListener itemClickListener) {
            super(view);
            mTitleTextView = (TextView) view.findViewById(android.R.id.text1);
            mDescriptionTextView = (TextView) view.findViewById(android.R.id.text2);
            mOverlayTextView = (TextView) view.findViewById(R.id.overlaytext);
            mImageView = (ImageView) view.findViewById(android.R.id.icon);
            mItemClickListener = itemClickListener;
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
