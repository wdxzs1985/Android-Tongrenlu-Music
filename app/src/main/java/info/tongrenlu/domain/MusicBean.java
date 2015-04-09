package info.tongrenlu.domain;

/**
 * Created by wangjue on 2015/04/02.
 */
public class MusicBean {
    public MusicBean(final Long id, final String title) {
        this.id = id;
        this.title = title;
    }

    public MusicBean() {
    }

    private Long id = null;

    private String title = null;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
