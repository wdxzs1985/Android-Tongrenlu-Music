package info.tongrenlu.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;


/**
 * Created by wangjue on 2015/04/03.
 */
public interface OnFragmentInteractionListener {

    void onFragmentInteraction(Fragment target,  Bundle data,  Pair... sharedElements);

}
