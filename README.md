# MediaProject

使用安卓5.0新出的MediaProject API，实现界面的截屏和录屏功能。

使用说明：
点击start会启动一个悬浮窗，悬浮窗第一个按钮是截屏，第二个按钮是录屏，使用完毕后点击stop按钮即可。
截屏和录屏的结果，在sdcard/MediaProject/目录下


使用注意：
1：因为是调用的谷歌新的API，所以请确保执行手机系统版本在5.0或以上版本。
2：可以适用于绝大部分场景，并且能实现非本apk中（比如桌面截图，游戏录屏/截图功能）。


版本更新：


V1.1.0
发布第一版MediaProject，增加了悬浮窗的提示信息，修复了权限未提供时候可能会造成的崩溃的问题。
新增悬浮窗权限的判定,非常感谢[https://github.com/zhaozepeng/FloatWindowPermission](https://github.com/zhaozepeng/FloatWindowPermission) 里面的对权限判定的内容。