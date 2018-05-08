package edu.colorado.plv.chimp.example

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.{Assert, Rotate, Type, _}
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import edu.colorado.plv.chimp.coordinator.{ChimpLoader, CrashChimpOutcome, SuccChimpOutcome}
import edu.colorado.plv.chimp.generator.Gorilla
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * Created by edmund on 3/11/17.
  */
object TestApp {

  def main(args: Array[String]): Unit = {

    //val buildDir = "/Users/Pezh/Documents/Tutorial/chimpcheck/examples/ChimpTrainer/app"
    val buildDir = "/opt/ChimpCheck/examples/ChimpTrainer/app"
    val appBuildDir = buildDir + "/build/outputs/apk/"
    val testerClass = "TestExpresso"
    val aaptHome = "/opt/android-sdk/build-tools/25.0.2" //"/Users/Pezh/Library/Android/sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = appBuildDir + "app-debug.apk"
    val chimpAPKPath = appBuildDir + "app-debug-androidTest-unaligned.apk"


    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

    implicit val ec = ExecutionContext.global


    // To support Click(R.id._) please import the R class file
    val trace = Sleep(1000):>> Click("Begin") :>> Type("username", "test") :>> Type("password", "test") :>>
                Click("Login") :>> Click("Countdowntimer Testing") :>> Click("5 seconds") :>> Sleep(5000):>> ClickBack

    val outcome = ChimpLoader.quickLoad(emuID, trace, appAPKPath, chimpAPKPath, testerClass, aaptHome)
    outcome match {
      case SuccChimpOutcome(tr) => println(tr)
      case CrashChimpOutcome(tr, st) => println(tr)
    }

  }
}
