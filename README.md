<div align="center">
<img width="125" height="125" src="src/main/resources/icon.svg" alt="ecjtulogintool"/>  

<h1>华交校园网工具桌面端</h1>

</div>

## 介绍
使用[compose-multiplatform](https://github.com/JetBrains/compose-multiplatform)构建的华东交通大学校园网登录应用，仅支持Windows，安卓版请查看[这里](https://github.com/Agiens02/ECJTU-AutoLogin)

支持开机自启，系统通知等功能，更多功能等待开发中

## 使用
1. 在[release](https://github.com/Agiens02/ECJTU-AutoLogin-Desktop/releases)页面下载最新版，包含绿色版(zip压缩包)和安装版(msi文件)
2. 打开应用后，输入账号密码等信息，点击登录即可，每次打开应用时都会自动登录
3. 可以在主界面设置是否开启系统通知，以及开机自启，也可在托盘中设置
4. 设置开机自启后，登录完成后应用会自动退出

## Python登录脚本

1. 安装python并设置环境变量
2. 安装依赖：
```bash
pip install requests
pip install win10toast
```
3. 下载[login.pyw](./login.pyw)
4. 修改代码中的账号密码等参数
5. 双击运行
6. 如果不想弹出命令行窗口，可以设置文件默认打开方式为pythonw.exe
7. 设置开机自启请将脚本放在以下目录：
```
C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Startup
```

## 感谢

[compose-multiplatform](https://github.com/JetBrains/compose-multiplatform)

[SaltUI](https://github.com/Moriafly/SaltUI)

[CrossPaste](https://github.com/CrossPaste/crosspaste-desktop)

[wix-package](https://github.com/tangshimin/wix-package)

[multiplatform-settings](https://github.com/russhwolf/multiplatform-settings)

[okhttp](https://github.com/square/okhttp)