package gd.hq.yolov5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.camera.core.CameraX.unbindAll;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA = 3;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ImageView resultImageView;
    private SeekBar nmsSeekBar;
    private SeekBar thresholdSeekBar;
    private TextView thresholdTextview;
    private TextView tvInfo;
    private double threshold = 0.3, nms_threshold = 0.7;
    private TextureView view_realTime;

    // 并发原子类， default 值就是 false
    private AtomicBoolean detecting = new AtomicBoolean(false);     // real-time 检测中
    private AtomicBoolean detectPhoto = new AtomicBoolean(false);   // static-photo 检测中

    private long startTime = 0;
    private long endTime = 0;
    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 内存读取权限申请
        // 修改 ContextCompat（用于检查权限） 为 ActivityCompat（用于申请权限）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        // 相机权限申请（新增）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // You can directly ask for the permission.
            requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA);
        }

        // YOLOv5 初始化
        YOLOv5.Init(getAssets());    //获取 assets 目录下资源文件

        // 组件初始化
        Button btn_imgSelected = findViewById(R.id.btn_imgSelected);
        resultImageView = findViewById(R.id.imageView);
        thresholdTextview = findViewById(R.id.valTxtView);
        tvInfo = findViewById(R.id.tv_info);
        nmsSeekBar = findViewById(R.id.nms_seek);
        thresholdSeekBar = findViewById(R.id.threshold_seek);
        final String format = "Thresh: %.2f, NMS: %.2f";
        thresholdTextview.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
        view_realTime = findViewById(R.id.view_realTime);

        // 设置监听
        // nms 滑动条监听
        nmsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nms_threshold = i / 100.f;
                thresholdTextview.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // thresholdSeekBar 滑动条监听
        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                threshold = i / 100.f;
                thresholdTextview.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // 监听按钮：选择照片
        btn_imgSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICK_IMAGE);         // 调用 onActivityResult()
            }
        });
        // 监听 resultImageView
        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 当我触摸识别结果展示的图层时，终止展示，重新开始
                detectPhoto.set(false);
            }
        });
        // 监听 view_realTime
        // APP 启动时候调用了 2 次，删除后效果没改变
//        view_realTime.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View view,int left,
//                                       int top,
//                                       int right,
//                                       int bottom,
//                                       int oldLeft,
//                                       int oldTop,
//                                       int oldRight,
//                                       int oldBottom) {
//                Log.e("params", "onLayoutChange: left:" + left + " top:" + top + " right:" + right + " bottom:" + bottom + " ol:" + oldBottom + " ot:" + oldTop + " or:" + oldRight + " ob:" + oldBottom);
//                updateTransform();
//            }
//        });

        // Android-Developer: 将 Runnable 添加到消息队列中。 runnable 将在用户界面线程上运行。
        view_realTime.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }

        // 似乎是想解决摄像头获取的图像的旋转角度问题，但似乎不影响显示
        // 上下方都调用了
//    private void updateTransform() {
//        Matrix matrix = new Matrix();
//        // Compute the center of the view finder
//        float centerX = view_realTime.getWidth() / 2f;
//        float centerY = view_realTime.getHeight() / 2f;
//
//        float[] rotations = {0, 90, 180, 270};
//        // Correct preview output to account for display rotation
//        float rotationDegrees = rotations[view_realTime.getDisplay().getRotation()];
//
//        matrix.postRotate(-rotationDegrees, centerX, centerY);
//
//        // Finally, apply transformations to our TextureView
//        view_realTime.setTransform(matrix);
//    }

    private void startCamera() {
        // 从生命周期中解除所有用例的绑定
        CameraX.unbindAll();
        // 警告：最新版已移除 CameraX 类，应该通过 ProcessCameraProvider 使用
        // 但我好像找不到 ProcessCameraProvider 这个类


        // 1. preview

        // 选择后置摄像头为主摄像头、配置参数
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
//                .setTargetAspectRatio(Rational.NEGATIVE_INFINITY)  // 宽高比
//                .setTargetResolution(new Size(416, 416))  // 分辨率
                .build();

        // realTime 预览框实时更新
        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) view_realTime.getParent();
                parent.removeView(view_realTime);
                parent.addView(view_realTime, 0);

                view_realTime.setSurfaceTexture(output.getSurfaceTexture());
                //updateTransform();
            }
        });

        //将 UseCase（见下） 集合绑定到 LifecycleOwner。生命周期的状态将决定摄像机何时打开、启动、停止和关闭。启动时，用例接收相机数据。
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, gainAnalyzer(new DetectAnalyzer()));
        // 警告：最新版已移除 CameraX 类，应该通过 ProcessCameraProvider 使用
        // 但我好像找不到 ProcessCameraProvider 这个类
    }

    // Android Developer：UseCase 提供的功能是将用例中的参数集映射为摄像机可使用的参数。UseCase 还将向摄像机传达活动/非活动状态。
    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        //analysisConfigBuilder.setTargetResolution(new Size(416, 416));  // 输出预览图像尺寸。会自动选择
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }

    private class DetectAnalyzer implements ImageAnalysis.Analyzer {
        // 分析图象以产生结果
        // 这里通过重写 analyze() 函数，实现输入图像的格式转换、分析、以及分析结果输出
        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            // 当`实时监测的前 1 帧` 或 `照片识别`进行时，不进行画面处理
            if (detecting.get() || detectPhoto.get()) {
                return;
            }
            // 否则，记录开始时间，并将当前帧设置为 `实时监测的状态
            startTime = System.currentTimeMillis();
            detecting.set(true);
            final Bitmap bitmapsrc = imageToBitmap(image);  // 格式转换
            // 创建名为 "detect" 的线程，将此线程变量命名为 "detectThread"
            Thread detectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationDegrees);
                    width = bitmapsrc.getWidth();
                    height = bitmapsrc.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, width, height, matrix, false);

                    // 交给模型，进行目标检测
                    Box[] result = YOLOv5.Detect(bitmap, threshold, nms_threshold);
                    final Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    // 绘制 boxs 和 params
                    Canvas canvas = new Canvas(mutableBitmap);
                    final Paint boxPaint = new Paint();
                    boxPaint.setAlpha(200);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800);
                    boxPaint.setTextSize(40 * mutableBitmap.getWidth() / 800);
                    for (Box box : result) {
                        boxPaint.setColor(box.getColor());
                        boxPaint.setStyle(Paint.Style.FILL);
                        canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + 40 * mutableBitmap.getWidth() / 1000, boxPaint);
                        boxPaint.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(box.getRect(), boxPaint);
                    }

                    // 处理结束后，将绘制了检测结果的图像展示在组件上（可在这里保存照片），并展示相关性能指标
                    // 在 UI 线程上运行指定的操作。如果当前线程是 UI 线程，则立即执行操作。如果当前线程不是 UI 线程，则将操作发布到 UI 线程的事件队列中
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultImageView.setImageBitmap(mutableBitmap);  // 将位图设置为此 ImageView 的内容
                            detecting.set(false);                           // 标记实时监测的结束
                            endTime = System.currentTimeMillis();           // 记录结束时间
                            long dur = endTime - startTime;
                            float fps = (float) (1000.0 / dur);
                            tvInfo.setText(String.format(Locale.CHINESE,
                                    "Size: %dx%d\nTime: %.3f s\nFPS: %.3f",
                                    height, width, dur / 1000.0, fps));
                        }
                    });
                }
            }, "detect");
            detectThread.start();
        }

        // 将 image 转成 bitmap
        private Bitmap imageToBitmap(ImageProxy image) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ImageProxy.PlaneProxy y = planes[0];
            ImageProxy.PlaneProxy u = planes[1];
            ImageProxy.PlaneProxy v = planes[2];
            ByteBuffer yBuffer = y.getBuffer();
            ByteBuffer uBuffer = u.getBuffer();
            ByteBuffer vBuffer = v.getBuffer();
            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();
            byte[] nv21 = new byte[ySize + uSize + vSize];
            // U and V are swapped
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }

    }


    @Override
    protected void onDestroy() {
        unbindAll();
        super.onDestroy();
    }

    // 权限请求的返回，这里没发现这段的用处
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        for (int result : grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                this.finish();
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            Log.e("onActivityResult()", "no intent!");
            return;
        }
        detectPhoto.set(true);
        Bitmap image = getPicture(data.getData());
        Box[] result = YOLOv5.Detect(image, threshold, nms_threshold);
        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * image.getWidth() / 800);
        boxPaint.setTextSize(40 * image.getWidth() / 800);
        for (Box box : result) {
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + 17, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
        }
        resultImageView.setImageBitmap(mutableBitmap);
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


}
