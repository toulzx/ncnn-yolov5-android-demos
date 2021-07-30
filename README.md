# NCNN YOLOv5

笔者（@toulzx）注：
> 此分支（review）为笔者所 fork 的项目的子分支
>
> 笔者在本项目的基础上审查代码，增加注释，以便理解。
>
> 同时,笔者还将查阅的相关资料收集添加到各个子项目的 README.md 中
>
> 如果本项目对你有帮助，请 star 支持我一下
>
> 转载必须注明作者与出处：@toulzx @binzhouchn
>
> 三个子项目各自的 README.md
>
> + [静态图片识别](ncnn-android-static_img/README.md)
> + [动态视频识别](ncnn-android-real_time/README.md)
> + [多模型集合](ncnn-android-all_nets/README.md)

***以下部分为本仓库原作者的 README.md 文档内容：***

本项目下有三个子项目，可以用Android Studio(AS)单独加载各子项目编译后生成apk安装~<br>
从安装android studio到打包apk文件安装全流程详细讲解请查看[项目二](ncnn-android-static_img/)

## 项目一

### 移动端NCNN部署，项目支持YOLOv5s、YOLOv4-tiny、MobileNetV2-YOLOv3-nano、Simple-Pose、Yolact、ChineseOCR-lite、ENet、Landmark106、DBFace、MBNv2-FCN与MBNv3-Seg-small模型，摄像头实时捕获视频流进行检测，也可以上传图片或者视频进行检测！

### Android:
- Android Studio 4.1.3
- Gradle model version=6.1.1, NDK version=21.0.6113669
- MACOS(10.15.7)本地编译
- 测试机型: 小米6(处理器-高通骁龙835, RAM-4GB, Android版本-8.0.0)

### 项目一效果展示

<img src="ncnn-android-all_nets/Screenshots/screenrecorder_2021_03_25.gif" width="300">

[项目一地址](ncnn-android-all_nets/)<br>
[可直接安装我打包好的APK包](https://download.csdn.net/download/quantbaby/16108085)

## 项目二

### 选取静态图像识别

### Android:
- Android Studio 4.1.3
- Gradle model version=5.4.1, NDK version=22.0.7026061
- MACOS(10.15.7)本地编译
- 测试机型: 小米6(处理器-高通骁龙835, RAM-4GB, Android版本-8.0.0)

### 项目二效果展示

<img src="ncnn-android-static_img/images/screenshot2.jpg" width="300">

[项目二地址](ncnn-android-static_img/)

## 项目三

### 实时人、物品检测

### Android:
- Android Studio 4.1.3
- Gradle model version=5.4.1, NDK version=22.0.7026061
- MACOS(10.15.7)本地编译
- 测试机型: 小米6(处理器-高通骁龙835, RAM-4GB, Android版本-8.0.0)


[项目三地址](ncnn-android-real_time/)




感谢:<br/>
- sunnyden, dog-qiuqiu, ..., nihui
- https://github.com/Tencent/ncnn
