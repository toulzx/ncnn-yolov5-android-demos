# real time ncnn android yolo5 detect
实时人、物品检测

***笔者（@toulzx）注：***
> 此分支（review）为笔者所 fork 的项目的子分支，你目前来到了第一个子项目：**动态视频识别**
>
> 代码内容修改：
>
> - YOLOv5文件：
>
>   - detect() -> Detect()
>
>   - init() -> Init()
>
> - folder: cpp -> jni 失败？
>   - 注意：CMakeList 里面的 add_library()
>
> 下面是收集的相关资料：
>
> [stackOverFlow: what is the difference between ContextCompat.checkSelfPermission() and ActivityCompat.requestPermission()?](https://stackoverflow.com/questions/42832847/what-is-the-difference-between-contextcompat-checkselfpermission-and-activityc)
>
> [Android-Developer-Doc-of-requestPermission](https://developer.android.com/training/permissions/requesting?hl=zh-cn)
>
> [summary-of-AtomicBoolean](https://baijiahao.baidu.com/s?id=1647915101064077163&wfr=spider&for=pc)
>
> [The-Java™-Tutorials-of-AtomicBoolean](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicBoolean.html)
>
> [Android-Developer-Doc-of-view.post()](https://developer.android.com/reference/android/view/View#post(java.lang.Runnable))
>
> [Warning: the-remove-of-AndroidX.unbindAll](https://developer.android.com/jetpack/androidx/releases/camera#camera-core-1.0.0-alpha07)
>
> [Android-Developer-Doc-of-bindToLifeCycle](https://developer.android.com/reference/androidx/camera/lifecycle/ProcessCameraProvider#bindToLifecycle(androidx.lifecycle.LifecycleOwner,%20androidx.camera.core.CameraSelector,%20androidx.camera.core.UseCase...))
>
> [Android-Developer-Doc-of-ImageAnalysis](https://developer.android.com/reference/androidx/camera/core/ImageAnalysis.Analyzer)
>
> [Android-Developer-Doc-of-runOnUiThread](https://developer.android.com/reference/android/app/Activity#runOnUiThread(java.lang.Runnable))
>
> **三个子项目各自的 README.md：**
>
> + [静态图片识别](ncnn-android-static_img/README.md)
> + [动态视频识别](ncnn-android-real_time/README.md)
> + [多模型集合](ncnn-android-all_nets/README.md)
>
> 如果本项目对你有帮助，请 star 支持我一下
>
> 转载必须注明作者与出处：@toulzx @binzhouchn

***以下部分为本仓库原作者的 README.md 文档内容：***

## 部署及打包apk

不需要下载ncnn已经包在里面了，直接clone然后AS build就能玩~

如果你还没入门请到我的另一个[仓库](https://github.com/binzhouchn/ncnn-android-yolov5)查看具体步骤

`Gradle model version=5.4.1, NDK version=22.0.7026061`


## 程序闪退请到应用权限管理设置，开启摄像头和录音权限


## 实时检测示例

<img src="screenshot_yolov5.gif" width="260">
