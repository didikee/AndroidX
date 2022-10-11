# AndroidX
Android面向Android10的兼容工具类

## How to use：

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```
第二步 2. Add the dependency
```
dependencies {
	implementation 'com.github.didikee:AndroidX:{$Version}'
}
```
## 使用介绍：
### 加载媒体（视频、图片、音频）

获取所有的照片
```
ContentResolver contentResolver = getContentResolver();
        ArrayList<MediaItem> mediaItems = new MediaLoader.Builder(contentResolver)
                .ofImage()
                .get();
```

或者设置一些配置项
```
// 查看源码
ContentResolver contentResolver = getContentResolver();
        ArrayList<MediaItem> mediaItems = new MediaLoader.Builder(contentResolver)
                .ofImage()
                .setOrder()// 返回的数据按照什么排序
                .setTargetFolder() // 只返回目标目录的媒体，参见：AndroidStorage.getFolderPath()
                .setTargetMimeTypes() // 只返回目标mimetype的媒体
                .setBlockMimeTypes() // 过滤掉指定类型的媒体
                .setSelection() // 自定义selection
                .get();
```

