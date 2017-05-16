package edu.colorado.plv.chimp.example

import akka.actor.ActorSystem
import plv.colorado.edu.chimpsample.R
import edu.colorado.plv.chimp.combinator.{Assert, Rotate, _}
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import edu.colorado.plv.chimp.coordinator.{ChimpConfig, ChimpContext, ChimpLoader}
import edu.colorado.plv.chimp.generator.{Gorilla, Repeat, SubservientGorilla}
import edu.colorado.plv.chimp.coordinator.MissionControl._
import org.scalacheck.Prop._
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by edmund on 3/11/17.
  */
object RunBasic {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    // IMPORTANT !! Reset this to your local path to the ChimpSample1 APKs.
    val apkDir = "/data/chimp/ChimpSample1"
    // IMPORTANT !! Reset this to your Android SDK path. You will also need to ensure that Aapt 24.0.3 is available.
    val androidSDKHome = "/Users/edmundlam/Library/Android/sdk"

    val config = ChimpConfig.defaultConfig().withStartEmulator(false).withTestRun(false).withTimeout(720 seconds)
      // .addDeviceInfo(DeviceInfo("emulator-5556"))
      // .addDeviceInfo(DeviceInfo("emulator-5558"))
      // .addDeviceInfo(DeviceInfo("emulator-5560"))
      .withAPKs(s"$apkDir/app-debug.apk", s"$apkDir/app-debug-androidTest.apk", "TestExpresso")
      .withAaptHome(s"$androidSDKHome/build-tools/24.0.3")

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    val myParam = Parameters.default.withMinSuccessfulTests(1).withWorkers(1)

    forAll(Gorilla.generator) {
      tr => tr chimpCheck { true }
    }.check(myParam)

    system.terminate();

  }

}

object RunCustom {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    // IMPORTANT !! Reset this to your local path to the ChimpSample1 APKs.
    val apkDir = "/data/chimp/ChimpSample1"
    // IMPORTANT !! Reset this to your Android SDK path. You will also need to ensure that Aapt 24.0.3 is available.
    val androidSDKHome = "/Users/edmundlam/Library/Android/sdk"

    val config = ChimpConfig.defaultConfig().withStartEmulator(false).withTestRun(false).withTimeout(720 seconds)
      // .addDeviceInfo(DeviceInfo("emulator-5556"))
      // .addDeviceInfo(DeviceInfo("emulator-5558"))
      // .addDeviceInfo(DeviceInfo("emulator-5560"))
      .withAPKs(s"$apkDir/app-debug.apk", s"$apkDir/app-debug-androidTest.apk", "TestExpresso")
      .withAaptHome(s"$androidSDKHome/build-tools/24.0.3")

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    implicit val gorillaConfig = Gorilla.defaultGorillaConfig

    val myParam = Parameters.default.withMinSuccessfulTests(1).withWorkers(1)

    val loginSeq = Type(R.id.input_username,"testuser") :>> Type(R.id.input_password,"1234") :>> Click(R.id.btn_login)

    val custom = Click(R.id.button_begin) :>> SubservientGorilla( isDisplayed(R.id.btn_login) Then (loginSeq :>> Assert(isDisplayed(R.id.interm_btn_back))) )

    forAll(custom.generator) {
      tr => tr chimpCheck { true }
    }.check(myParam)

    system.terminate()

  }

}

object RunTestit {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    // IMPORTANT !! Reset this to your local path to the ChimpSample1 APKs.
    val apkDir = "/data/chimp/ChimpSample1"
    // IMPORTANT !! Reset this to your Android SDK path. You will also need to ensure that Aapt 24.0.3 is available.
    val androidSDKHome = "/Users/edmundlam/Library/Android/sdk"

    val config = ChimpConfig.defaultConfig().withStartEmulator(false).withTestRun(false).withTimeout(720 seconds)
      // .addDeviceInfo(DeviceInfo("emulator-5556"))
      // .addDeviceInfo(DeviceInfo("emulator-5558"))
      // .addDeviceInfo(DeviceInfo("emulator-5560"))
      .withAPKs(s"$apkDir/app-debug.apk", s"$apkDir/app-debug-androidTest.apk", "TestExpresso")
      .withAaptHome(s"$androidSDKHome/build-tools/24.0.3")

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    implicit val gorillaConfig = Gorilla.defaultGorillaConfig

    val myParam = Parameters.default.withMinSuccessfulTests(1).withWorkers(1)

    val loginSeq = Type(R.id.input_username,"testuser") :>> Type(R.id.input_password,"1234") :>> Click(R.id.btn_login)

    val custom = Click(R.id.button_begin) :>> Click(R.id.btn_login) :>> Click(R.id.interm_btn_cdt) :>> Click(R.id.btn_count10) :>> Click(R.id.btn_back) :>> Repeat(30, Click(*) :>> Skip)

    forAll(Gorilla.generator) {
      tr => tr chimpCheck { true }
    }.check(myParam)

    system.terminate()

  }

}

object TestApp {

  def main(args: Array[String]): Unit = {

    val gorilla_test = Gorilla.generator().sample.get

    val aaptHome = "/usr/local/android-sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = "/data/chimp/ChimpSample1/app-debug.apk"
    val chimpAPKPath = "/data/chimp/ChimpSample1/app-debug-androidTest.apk"
    val testerClass = "TestExpresso"

    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

    // This is needed now, because in quickLoad, we spin off a future computation: The kick back routine
    implicit val ec = ExecutionContext.global

    val outcome = ChimpLoader.quickLoad(emuID, gorilla_test, appAPKPath, chimpAPKPath, testerClass, aaptHome)

    println(s"\nOutcome: $outcome")

  }

}
