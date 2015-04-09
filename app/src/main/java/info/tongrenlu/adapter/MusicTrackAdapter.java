package info.tongrenlu.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import info.tongrenlu.domain.TrackBean;

/**
 * Created by wangjue on 2015/04/06.
 */
public class MusicTrackAdapter extends BaseAdapter {

    private ArrayList<TrackBean> mTrackList;

    public MusicTrackAdapter() {
        this.mTrackList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mTrackList.size();
    }

    @Override
    public TrackBean getItem(final int position) {
        if (!mTrackList.isEmpty()) {
            return mTrackList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(final int position) {
        if (!mTrackList.isEmpty()) {
            return mTrackList.get(position).getId();
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
        final View view = View.inflate(context, android.R.layout.simple_list_item_1, null);
        return view;
    }

    public View bindView(final Context context, final View itemView, final int position) {
        TextView textView = (TextView) itemView.findViewById(android.R.id.text1);
        TrackBean trackBean = this.getItem(position);
        textView.setText(trackBean.getName());
        return itemView;
    }

    public ArrayList<TrackBean> getTrackList() {
        return mTrackList;
    }

    public void setTrackList(final ArrayList<TrackBean> mTrackList) {
        this.mTrackList = mTrackList;
    }
}
