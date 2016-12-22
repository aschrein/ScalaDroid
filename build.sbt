scalaVersion := "2.11.8"

enablePlugins(AndroidApp)
useSupportVectors

versionCode := Some(1)
version := "0.1-SNAPSHOT"

minSdkVersion in Android := "15"
targetSdkVersion in Android := "21"
platformTarget in Android := "android-21"
proguardScala in Android := true
dexMulti in Android := true
useProguardInDebug := false
javacOptions ++= Seq(
	"-source", "1.7",
	"-target", "1.7",
	"-Xlint:unchecked",
	"-Xlint:deprecation"
)

scalacOptions ++= Seq(
	"-target:jvm-1.7",
	"-encoding", "UTF-8",
	"-feature",
	"-unchecked",
	"-deprecation",
	"-Xcheckinit"
)
//javacOptions in Compile ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil
libraryDependencies += "com.vk" % "androidsdk" % "1.6.7"
libraryDependencies += "com.android.support" % "appcompat-v7" % "21.0.2"
//libraryDependencies += "com.android.support" % "appcompat-v7" % "25.0.0"
libraryDependencies += "com.android.support" % "multidex" % "1.0.0"
//libraryDependencies += "com.android.support" % "support-v4" % "25.0.0"