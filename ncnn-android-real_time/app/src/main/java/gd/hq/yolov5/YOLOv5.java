package gd.hq.yolov5;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class YOLOv5 {

    public static native void Init(AssetManager manager);

    public static native Box[] Detect(Bitmap bitmap, double threshold, double nms_threshold);   // 与 static 中的 Detect 方法参数不同，可能在 cpp 文件有改动！

    static {
        System.loadLibrary("yolov5");
    }
}
