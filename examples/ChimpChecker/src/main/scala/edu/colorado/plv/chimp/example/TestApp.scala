package edu.colorado.plv.chimp.example

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.{Assert, Rotate, _}
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

    val buildDir = "/Users/Pezh/ChimpCheck/examples/ChimpTrainer/app"
    val appBuildDir = buildDir + "/build/outputs/apk/"
    val testerClass = "TestExpresso"
    val aaptHome = "/Users/Pezh/Library/Android/sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = appBuildDir + "app-debug.apk"
    val chimpAPKPath = appBuildDir + "app-debug-androidTest.apk"


    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

    implicit val ec = ExecutionContext.global


    // To support Click(R.id._) please import the R class file
    val trace = Sleep(3000):>> Click("Begin") :>> Type(*, "test") :>> Type(*, "test") :>> Click("Login") :>> Click("Countdowntimer Testing") :>> Click("5 seconds") :>> ClickBack

    val outcome = ChimpLoader.quickLoad(emuID, trace, appAPKPath, chimpAPKPath, testerClass, aaptHome)
    outcome match {
      case SuccChimpOutcome(tr) => println(tr)
      case CrashChimpOutcome(tr, st) => println(tr)
    }

  }
}