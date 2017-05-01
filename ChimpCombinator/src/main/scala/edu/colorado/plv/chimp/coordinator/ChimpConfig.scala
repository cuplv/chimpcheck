package edu.colorado.plv.chimp.coordinator

import akka.util.Timeout
import scala.concurrent.duration._

/**
  * Created by edmund on 3/28/17.
  */

// quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String,
//           testerClass:String, aaptHomePath:String, packageNamesOpt:Option[(String,String)])

abstract class ChimpConfig {

  /** Indicates if this should be a test run */
  val testRun: Boolean

  /** Indicates if emulators need to be started. TODO: Current not implemented */
  val startEmulators: Boolean

  /** List of device names to use */
  val deviceInfos: Seq[DeviceInfo]

  /** Path to the App APK */
  val appAPKPath: String

  /** Path to the ChimpDriver APK */
  val chimpAPKPath: String

  /** Name of the main ChimpDriver Tester Class */
  val testerClass: String

  /** Aapt Tool Path */
  val aaptHomePath: String

  /** Custom package names (Optional) */
  val packageNamesOpt: Option[(String,String)]

  /** Time limit for each chimp run */
  val timeout: Timeout

  def withTestRun(isTest: Boolean): ChimpConfig = chimpConfig( testRun=isTest )

  def withStartEmulator(start: Boolean): ChimpConfig = chimpConfig( startEmulators=start )

  def addDeviceInfo(dInfo: DeviceInfo): ChimpConfig = chimpConfig( deviceInfos= deviceInfos :+ dInfo  )

  def withAPKs(appAPKPath: String, chimpAPKPath: String, testerClass: String): ChimpConfig =
    chimpConfig( appAPKPath=appAPKPath, chimpAPKPath=chimpAPKPath, testerClass=testerClass )

  def withAaptHome(aaptHomePath: String): ChimpConfig = chimpConfig( aaptHomePath = aaptHomePath )

  def withCustomPackageNames(appPackageName: String, chimpPackageName: String): ChimpConfig =
    chimpConfig( packageNamesOpt=Some(appPackageName,chimpPackageName) )

  def withTimeout(timeout: Timeout): ChimpConfig =
    chimpConfig( timeout=timeout )

  case class chimpConfig(
     testRun: Boolean         = testRun,
     startEmulators : Boolean = startEmulators,
     deviceInfos: Seq[DeviceInfo] = deviceInfos,
     appAPKPath: String   = appAPKPath,
     chimpAPKPath: String = chimpAPKPath,
     testerClass: String  = testerClass,
     aaptHomePath: String = aaptHomePath,
     packageNamesOpt: Option[(String,String)] = packageNamesOpt,
     timeout: Timeout = timeout
  ) extends ChimpConfig

}

object ChimpConfig {

  def defaultConfig(): ChimpConfig = new ChimpConfig {
    override val testRun = false
    override val startEmulators = false
    override val deviceInfos  = Seq(DeviceInfo("emulator-5554"))
    override val appAPKPath   = "defaultApp.apk"
    override val chimpAPKPath = "defaultChimp.apk"
    override val testerClass  = "DefaultTester"
    override val aaptHomePath: String = "default/aapt/home"
    override val packageNamesOpt = None
    override val timeout = Timeout(60 seconds)
  }

}

case class DeviceInfo(id:String)

object TestChimpConfig {

  def main(args: Array[String]): Unit = {

     val config = ChimpConfig.defaultConfig().withStartEmulator(true).addDeviceInfo(DeviceInfo("emulator-5560"))

     println(config)

  }

}


