package com.gm.utils;

import java.lang.reflect.Array;

import android.content.Context;
import android.widget.Toast;

public class Logger {

	public static boolean DEBUG = false;

	private static final String TAG = "sdkcontrol";
	public static void ffkk() {
		Logger.DEBUG = true;
	}
	public static void d(Object obj) {
		if (DEBUG) {
			String s;
			if (obj == null) {
				s = "null";
			} else {
				Class<? extends Object> clz = obj.getClass();
				if (clz.isArray()) {
					StringBuilder sb = new StringBuilder(clz.getSimpleName());
					sb.append(" [ ");
					int len = Array.getLength(obj);
					for (int i = 0; i < len; i++) {
						if (i != 0) {
							sb.append(", ");
						}
						Object tmp = Array.get(obj, i);
						sb.append(tmp);
					}
					sb.append(" ]");
					s = sb.toString();
				} else {
					s = "" + obj;
				}
			}
			android.util.Log.d(TAG, s);
		}
	}
	public static void d(String tag,String s) {
		if (DEBUG) {
			android.util.Log.d(TAG, s);
		}
	}
	public static void d(String s) {
		if (DEBUG) {
			android.util.Log.d(TAG, s);
		}
	}
	public static void e(String s) {
		if (DEBUG) {
			android.util.Log.e(TAG, s);
		}
	}
	public static void i(String s) {
		if (DEBUG) {
			android.util.Log.i(TAG, s);
		}
	}
	public static void w(String s) {
		if (DEBUG) {
			android.util.Log.w(TAG, s);
		}
	}
	
	public static void s(Context context,String desc) {
		if (DEBUG) {
			Toast.makeText(context, desc, Toast.LENGTH_LONG).show();
		}
	}

}
