scalaVersion := "2.11.8"

enablePlugins(AndroidApp)
useSupportVectors

versionCode := Some(1)
version := "0.1-SNAPSHOT"


platformTarget in Android := "android-21"
proguardScala in Android := true
dexMulti in Android := true
useProguardInDebug := false
javacOptions in Compile ++= "-source" :: "1.6" :: "-target" :: "1.6" :: Nil
libraryDependencies += "com.vk" % "androidsdk" % "1.6.7"
libraryDependencies += "com.android.support" % "appcompat-v7" % "25.0.0"
libraryDependencies += "com.android.support" % "multidex" % "1.0.0"
libraryDependencies += "com.android.support" % "support-v4" % "25.0.0"