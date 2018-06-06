package com.czt.mp3recorder;

import java.util.HashMap;

/**
 * Created by bolin on 2016/12/29.
 */

public interface RecorderStateListener {

    public void onRecorderState(String state, HashMap<String, Object> data);
}
