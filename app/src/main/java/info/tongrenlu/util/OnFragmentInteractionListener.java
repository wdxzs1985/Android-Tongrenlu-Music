package info.tongrenlu.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.View;

import java.util.Map;

/**
 * Created by wangjue on 2015/04/03.
 */
public interface OnFragmentInteractionListener {

    public void onFragmentInteraction(Fragment target,  Bundle data,  Pair... sharedElements);

}
