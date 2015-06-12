package info.tongrenlu.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.View;



public interface OnFragmentInteractionListener {

    void onFragmentInteraction(Fragment target,  Bundle data,
                               Pair<View,String>... sharedElements);

}
