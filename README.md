# AndroidDevHomeWork
移动开发课作业

## 第一次作业
内容：使用 3 个 SeekBar 提供参数作为 RGB 通道，拖动 SeekBar 设置 TextView 的背景色



![1st](https://res.cloudinary.com/evidence0john/image/upload/v1538392799/ADHW/1st.png)

## 第二次作业
内容：ListView 和 Service 的使用

~~说明：因为本次作业没有说要检查，也没说必须要做，所以我鸽了....~~

![Gugu](https://res.cloudinary.com/evidence0john/image/upload/v1538146418/ADHW/timg.jpg)

## 第三次作业
内容：Sqlite 的操作，能够添加姓名和工资，ListView 实时展示数据库的内容

说明：我附加了删除的功能，删除的语法是，时用一个具体的 id 作为参数则删除对应 id 的记录，例如 5，则删除 id 为 5 的记录。使用 `-` 连接一个范围删除一个 id 范围中的所有记录，例如 `3-7` 可以删除 id 属于 [3, 7] 的所有记录。

![3rd](https://res.cloudinary.com/evidence0john/image/upload/v1538392800/ADHW/3nd.png)

输入参数非法则提示错误，删除 id 范围在 [2, 5] 的记录

## 第四次作业
内容：实现一个播放器...
详细内容 ---> [https://github.com/johnyu666/xa0930a/blob/master/readme.MD](https://github.com/johnyu666/xa0930a/blob/master/readme.MD)

说明：本次作业的周期长达 1 个月，分很多个版本实现，无聊时就写一点，请注意更新

### 临时版本1

说明：这个版本是一个最基本的实现，存在架构问题（实际上目前并未确定明确的架构），只是对功能实现的测试，仅部分代码供参考。
* 路径：`~\SipleMusicPlayer`

在 `MainActivity.java` 中，`workDir` 指定了搜索路径，例如，我在 `SipleMusicPlayer` 中将它设置为 `Environment.getExternalStorageDirectory().getAbsolutePath() + "/netease/cloudmusic/Music/"`，这样，程序会遍历 `sdcard` 的 `/netease/cloudmusic/Music/` 路径（这实际上是网易云音乐的默认下载路径），编译前修改它设置搜索路径，例如 `Environment.getExternalStorageDirectory().getAbsolutePath() + "/mp3/"` 则可以搜索 `sdcard` 上的 "mp3" 这个文件夹。

```java
private String workDir = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/netease/cloudmusic/Music/"; //Current work directory
```

### 临时版本2

说明：这个版本没有完全完成，使用了 NDK 编译了 Mongoose 服务器，emmm 供以后参考吧，`\TinyCloudMusic\app\src\main\cpp\mongoose\mongoose.c.res` 和 `mongoose.h.res`，是 Mongoose 的源文件，移除扩展名 `.res` 可直接使用，货值从 Mongoose Http Server 官方获得最新的源文件。
* 路径：`~\TinyMusicPlayer`

![t0](https://res.cloudinary.com/evidence0john/image/upload/v1541247530/ADHW/t0.jpg)