package com.gm.album;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.cocos2dx.lib.Cocos2dxActivity;

/**
 * Created by 周旭 on 2017/4/9.
 */

public class AlbumUtils {

    public static Cocos2dxActivity m_context;
    public static void init(Cocos2dxActivity context) {
        m_context = context;
    }
    //保存文件到指定路径
    public static boolean saveImageToGallery(Context context, Bitmap bmp,String albumdir) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + albumdir;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean saveImageToGallery(Context context, String imgfile,String albumdir) {
        Bitmap bmp = BitmapFactory.decodeFile(imgfile);
        if(bmp!=null)
        {
            return saveImageToGallery(context,bmp,albumdir);
        }
        return false;
    }

    public static boolean saveImage(String imgfile,String albumdir) {
        reqPermission();
        return saveImageToGallery(m_context,imgfile,albumdir);
    }

    public static void reqPermission(){
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        try {
            Class cls = m_context.getClass();
            Method setMethod = cls.getDeclaredMethod("requestPermission",int.class, String[].class, String.class);
            setMethod.invoke(cls.newInstance(), 102, permissions, "保存图片需要读取sd卡的权限");
        } catch (Exception e) {
            Log.d("AlbumUtils", "reqPermission: Exec failure");
        }
//       m_context.requestPermission(10000,permissions,"保存图片需要读取sd卡的权限");
    }
}
