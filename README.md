# LAA Editor

## 开发

推荐使用 [Liberica NIK 23 (JDK 17)](https://bell-sw.com/pages/downloads/native-image-kit/#nik-23-(jdk-17))

构建

```shell
sbt 'show GraalVMNativeImage/packageBin'
```

运行

```shell
.\target\graalvm-native-image\laa-editor.exe
```
