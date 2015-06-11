package info.tongrenlu.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import info.tongrenlu.R;
import info.tongrenlu.domain.MusicBean;

/**
 * Created by wangjue on 2015/04/02.
 */
public class MusicListAdapter extends BaseAdapter {

    public static final String TAG = MusicListAdapter.class.getName();

    private List<MusicBean> mData;

    public MusicListAdapter() {
        super();
        mData = new ArrayList<>();
    }

    public List<MusicBean> getData() {
        return mData;
    }

    public void setData(final List<MusicBean> mData) {
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public MusicBean getItem(final int position) {
        if (mData != null || !mData.isEmpty()) {
            return mData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(final int position) {
        if (mData != null || !mData.isEmpty()) {
            return mData.get(position).getId();
        }
        return 0;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        Context context = parent.getContext();
        View itemView;
        if (convertView == null) {
            itemView = createView(context, parent);
        } else {
            itemView = convertView;
        }

        return bindView(context, itemView, position);
    }

    public View createView(Context context, final ViewGroup parent) {
        final View view = View.inflate(context, R.layout.adapter_music_list, null);

        return view;
    }

    public View bindView(final Context context, final View itemView, final int position) {
        final ImageView coverView = (ImageView) itemView.findViewById(R.id.music_cover_view);
        Uri imageUri = Uri.parse("http://files.tongrenlu.info/m" +
                                 getItemId(position) +
                                 "/cover_400.jpg");
        Glide.with(context).load(imageUri).thumbnail(0.5f)
               .into(coverView);
        return itemView;
    }

}
