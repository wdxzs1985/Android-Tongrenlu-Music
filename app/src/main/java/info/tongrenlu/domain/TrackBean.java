package info.tongrenlu.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class TrackBean implements Parcelable {

    private Long id = null;

    private Long articleId = null;

    private String checksum = null;

    private String album = null;

    private String artist = null;

    private String name = null;

    private String original = null;

    private int trackNumber = 0;

    private String downloadFlg = "0";

    public TrackBean() {
    }

    public TrackBean(final Long id,
                     final String name,
                     final String checksum,
                     final Long articleId) {
        this.id = id;
        this.articleId = articleId;
        this.checksum = checksum;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    public String getOriginal() {
        return this.original;
    }

    public void setOriginal(final String original) {
        this.original = original;
    }

    public int getTrackNumber() {
        return this.trackNumber;
    }

    public void setTrackNumber(final int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getDownloadFlg() {
        return this.downloadFlg;
    }

    public void setDownloadFlg(final String downloadFlg) {
        this.downloadFlg = downloadFlg;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((this.getChecksum() == null) ? 0 : this.getChecksum().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final TrackBean other = (TrackBean) obj;
        if (this.getChecksum() == null) {
            if (other.getChecksum() != null) {
                return false;
            }
        } else if (!this.getChecksum().equals(other.getChecksum())) {
            return false;
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeStringArray(new String[] { String.valueOf(this.getArticleId()),
                                             this.getChecksum(),
                                             this.album,
                                             this.name,
                                             this.artist,
                                             this.original,
                                             String.valueOf(this.trackNumber),
                                             this.downloadFlg });
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
        this.checksum = checksum;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public static final Parcelable.Creator<TrackBean> CREATOR = new Parcelable.Creator<TrackBean>() {
        @Override
        public TrackBean createFromParcel(final Parcel in) {
            final String[] data = new String[8];
            in.readStringArray(data);

            final TrackBean trackBean = new TrackBean();
            trackBean.setArticleId(Long.valueOf(data[0]));
            trackBean.setChecksum(data[1]);
            trackBean.album = data[2];
            trackBean.name = data[3];
            trackBean.artist = data[4];
            trackBean.original = data[5];
            trackBean.trackNumber = Integer.valueOf(data[6]);
            trackBean.downloadFlg = data[7];
            return trackBean;
        }

        @Override
        public TrackBean[] newArray(final int size) {
            return new TrackBean[size];
        }
    };
}