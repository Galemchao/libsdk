package com.gm.sdk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.gm.sdkconfig.sdkconfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jake on 2018\6\19 0019.
 */

public class sdk_photo {
    public static sdk_photo instance = null;
    private static Activity m_context = null;
    //裁剪图片 数据
    private static Bundle pickImgBundle;
    //定义图片的Uri
    private static Uri photoUri;
    //裁剪图片的Uri
    private static Uri cropImageUri;
    //拍照图片保存路径
    private static String cameraPath;
    //裁剪图片保存路径
    private static String clipPath;

    public static void init(Activity context) {
        m_context = context;
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == m_context.RESULT_OK) {
            //从相册取图片，有些手机有异常情况，请注意
            if (requestCode == sdkconfig.PICK_REQCODE) { //选择
                pickResult(data);
            } else if (requestCode == sdkconfig.CAMERA_REQCODE) { //打开相机
                cameraResult();
            } else if (requestCode == sdkconfig.CLIP_REQCODE) { //裁剪
                clipResult();
            }
        }
    }

    public static boolean initPickImgBuild(String param) {
        try {
            if (pickImgBundle != null) {
                pickImgBundle = null;
            }
            pickImgBundle = new Bundle();
            int clip = 0;
            JSONObject obj = new JSONObject(param);
            if (obj.has("clip")) {
                clip = obj.getInt("clip");
            }
            if (clip != 0) {
                int aspectX = 1;
                int aspectY = 1;
                if (obj.has("aspectX")) {
                    aspectX = obj.getInt("aspectX");
                }
                if (obj.has("aspectY")) {
                    aspectY = obj.getInt("aspectY");
                }
                if (obj.has("outputX")) {
                    int outputX = obj.getInt("outputX");
                    pickImgBundle.putInt("outputX", outputX);
                }
                if (obj.has("outputY")) {
                    int outputY = obj.getInt("outputY");
                    pickImgBundle.putInt("outputY", outputY);
                }
                pickImgBundle.putInt("aspectX", aspectX);
                pickImgBundle.putInt("aspectY", aspectY);
            }
            if (obj.has("filename")) {
                clipPath = obj.getString("filename");
            }
            pickImgBundle.putInt("clip", clip);
        } catch (Exception E) {
            Log.d("sdk", "pickImg parmes error");
            return false;
        }
        return true;
    }

    //获取文件的uri
    private static Uri getFileUri() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "camera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        cameraPath = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        File mediaFile = new File(cameraPath);
        Uri mUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mUri = FileProvider.getUriForFile(m_context, m_context.getPackageName() + ".fileprovider", mediaFile);
        } else {
            mUri = Uri.fromFile(mediaFile);
        }
        return mUri;
    }

    //打开相机
    public static void openCamera(final String param) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                boolean ret = initPickImgBuild(param);
                if (!ret) {
                    return;
                }
                pickImgBundle.putString(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_CAMERA);
                // 指定相机拍摄照片保存地址
                Intent intent = new Intent();
                // 指定开启系统相机的Action
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                // 把文件地址转换成Uri格式
                photoUri = getFileUri();
                if (photoUri == null) {
                    Log.d(sdkconfig.SDK_TAG, "照相机图片保存地址为空");
                    return;
                }
                // 设置系统相机拍摄照片完成后图片文件的存放地址
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                m_context.startActivityForResult(intent, sdkconfig.CAMERA_REQCODE);
            }
        });
    }

    /**
     * 相册选择图片，替换，能裁剪
     */
    public static void pickImg(final String param) {
        m_context.runOnUiThread(new Runnable() {
            public void run() {
                boolean ret = initPickImgBuild(param);
                if (!ret) {
                    return;
                }
                pickImgBundle.putString(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_PICKIMG);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                m_context.startActivityForResult(intent, sdkconfig.PICK_REQCODE);
            }
        });
    }

    private static void pickResult(Intent data) {
        if (null != data && null != data.getData()) {
            Log.i("sdk", "图片选择成功");
            photoUri = data.getData();
            if (pickImgBundle.getInt("clip") != 0) {
                Log.d("sdk", "pickResult photoUri:" + photoUri);
                startPhotoZoom(photoUri);
            } else {
                final String picPath = handlerImageOnKitKat(photoUri);
                Log.d("sdk", "picPath:" + picPath);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        HashMap<String, Object> nmap = new HashMap<String, Object>();
                        nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_PICKIMG);
                        nmap.put(sdkconfig.SDK_FILENAME, picPath);
                        nmap.put(sdkconfig.SDK_ERROR, 0);
                        sdk.notifyEventByObject(nmap);
                    }
                }, 300);
            }
        } else {
            Log.d("sdk", "图片选择失败");
        }
    }

    private static void cameraResult() {
        Log.d("sdk", "拍照成功");
        galleryAddPic(cameraPath);
        if (pickImgBundle.getInt("clip") != 0) {
            startPhotoZoom(photoUri);
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    HashMap<String, Object> nmap = new HashMap<String, Object>();
                    nmap.put(sdkconfig.SDK_EVT, sdkconfig.SDK_EVT_CAMERA);
                    nmap.put(sdkconfig.SDK_FILENAME, cameraPath);
                    nmap.put(sdkconfig.SDK_ERROR, 0);
                    sdk.notifyEventByObject(nmap);
                }
            }, 300);
        }
    }

    private static void clipResult() {
        if (cropImageUri == null) {
            Log.d("sdk", "cropImageUri is null");
            return;
        }
        Bitmap bitmap = decodeUriAsBitmap(cropImageUri);
        if (bitmap != null) {
            final String fileName = writeFileByBitmap(bitmap, clipPath);
            Log.d("sdk", "fileName: " + fileName);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    HashMap<String, Object> nmap = new HashMap<String, Object>();
                    nmap.put(sdkconfig.SDK_EVT, pickImgBundle.getString(sdkconfig.SDK_EVT));
                    nmap.put(sdkconfig.SDK_FILENAME, fileName);
                    nmap.put(sdkconfig.SDK_ERROR, 0);
                    sdk.notifyEventByObject(nmap);
                    pickImgBundle = null;
                }
            }, 300);
        } else {
            Log.d("sdk", "uri地址转换为Bitmap失败");
        }
    }

    /**
     * @description 裁剪图片
     */
    private static void startPhotoZoom(Uri uri) {
        File cropPhoto = new File(Environment.getExternalStorageDirectory(), "tmp");
        if (!cropPhoto.exists()) {
            if (!cropPhoto.mkdirs()) {
                return;
            }
        }
        File cropFile = new File(cropPhoto.getPath() + File.separator + m_context.getPackageName() + "_tmp.jpg");
        cropImageUri = Uri.fromFile(cropFile);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // 去黑边
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        // aspectX aspectY 是宽高的比例，根据自己情况修改
        intent.putExtra("aspectX", pickImgBundle.getInt("aspectX"));
        intent.putExtra("aspectY", pickImgBundle.getInt("aspectY"));
        // outputX outputY 是裁剪图片宽高像素
        intent.putExtra("outputX", pickImgBundle.getInt("outputX"));
        intent.putExtra("outputY", pickImgBundle.getInt("outputY"));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //取消人脸识别功能
        intent.putExtra("noFaceDetection", true);
        //设置返回的uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        //设置为不返回数据
        intent.putExtra("return-data", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        m_context.startActivityForResult(intent, sdkconfig.CLIP_REQCODE);
    }

    //把图片写入文件
    public static String writeFileByBitmap(Bitmap bitmap, String filename) {
        if (filename == null) {
            return "";
        }
        final File imageFile = new File(filename);
        try {
            imageFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    //uri转换bitmap
    private static Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                InputStream IS = m_context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(IS);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(uri.getPath());
        }
        return bitmap;
    }

    //获取图片的真实路径
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String handlerImageOnKitKat(Uri uri){
        String imagePath=null;
        if (DocumentsContract.isDocumentUri(m_context,uri)){
            //如果是document类型的Uri,则通过document id处理
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];//解析出数字格式的id
                String selection=MediaStore.Images.Media._ID+"="+id;
                imagePath=uriToFilePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=uriToFilePath(contentUri, null);
            }
        }else{
            imagePath=uriToFilePath(uri, null);
        }
        return imagePath;
    }

    //把Uri转换为文件路径
    private static String uriToFilePath(Uri uri, String selection) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = m_context.getContentResolver().query(uri, null, selection, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /*
     * 将照片添加到相册中
     */
    private static void galleryAddPic(String photoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        m_context.sendBroadcast(mediaScanIntent);
    }
}
