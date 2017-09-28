package com.hutu.zhang;

import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;

import java.io.IOException;

/**
 * Created by Administrator on 2017/5/16.
 */

public class PublicUtils {

    /**
     * 判断照片是否被旋转
     * @param path
     * @return
     */
    public static  boolean PictureIsRotated(String path) {
        int tag = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            tag = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取视频文件的方向信息
     * @param FilePath
     * @return
     */
    public static String  GetVideoOrientation(String FilePath){
    MediaMetadataRetriever retr = new MediaMetadataRetriever();
    retr.setDataSource(FilePath);
    String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
    return rotation;
}


}
