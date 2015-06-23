package info.tongrenlu.domain;

/**
 * Created by wangjue on 2015/06/23.
 */
public class UserBean {

    public static final Long GUEST = 0L;

    private Long id = GUEST;

    private String nickname = null;

    public boolean isGuest() {
        return GUEST.equals(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }
}
