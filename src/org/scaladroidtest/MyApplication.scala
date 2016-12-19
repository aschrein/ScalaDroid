package org.scaladroidtest
import android.support.multidex.MultiDexApplication
import android.support.multidex.MultiDex
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.vk.sdk.VKSdk
/**
  * Created by anton on 12/19/2016.
  */
class MyApplication extends MultiDexApplication {
	override def onConfigurationChanged ( newConfig : Configuration ) {
		super.onConfigurationChanged ( newConfig )
	}
	override def onCreate ( ) {
		super.onCreate ( )
		VKSdk.initialize(this)
	}
	override def attachBaseContext( base:Context) {
		super.attachBaseContext(base)
		MultiDex.install(this)
	}
	override def onLowMemory ( ) {
		super.onLowMemory ( )
	}
	override def onTerminate ( ) {
		super.onTerminate ( )
	}
}
