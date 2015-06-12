package info.tongrenlu.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import info.tongrenlu.R;
import info.tongrenlu.domain.TrackBean;

public class TrackListAdapter extends CursorAdapter {

    public TrackListAdapter(final Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(final Context context,
                        final Cursor c,
                        final ViewGroup viewGroup) {
        final View view = View.inflate(context, R.layout.list_item_track, null);
        final ViewHolder holder = new ViewHolder();
        holder.coverView = (ImageView) view.findViewById(R.id.article_cover);
        holder.titleView = (TextView) view.findViewById(R.id.track_title);
        holder.artistView = (TextView) view.findViewById(R.id.track_artist);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor c) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final TrackBean trackBean = new TrackBean();
        trackBean.setArticleId(c.getLong(c.getColumnIndex("articleId")));
        trackBean.setId(c.getLong(c.getColumnIndex("fileId")));
        trackBean.setName(c.getString(c.getColumnIndex("name")));
        trackBean.setArtist(c.getString(c.getColumnIndex("artist")));
        trackBean.setTrackNumber(c.getInt(c.getColumnIndex("trackNumber")));
        holder.update(context, trackBean);
    }

    public class ViewHolder {
        public ImageView coverView;
        public TextView titleView;
        public TextView artistView;
        public TrackBean trackBean;
        //public LoadImageTask task;

        @SuppressLint("InlinedApi")
        public void update(final Context context, final TrackBean trackBean) {
            this.trackBean = trackBean;
            this.titleView.setText(trackBean.getName());
            this.artistView.setText(trackBean.getArtist());

        }
    }
}
