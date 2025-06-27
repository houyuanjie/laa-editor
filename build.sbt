scalaVersion := "3.7.1"

enablePlugins(GraalVMNativeImagePlugin)

graalVMNativeImageOptions ++= Seq(
  "--no-fallback",                              // 不要回退到 JVM
  "-Djava.awt.headless=false",                  // 使用 AWT 图形环境
  "-H:NativeLinkerOption=/SUBSYSTEM:WINDOWS",   // 使用 Windows 图形子系统
  "-H:NativeLinkerOption=/ENTRY:mainCRTStartup" // 使用 main 函数作为入口 (而不是 WinMain)
)
