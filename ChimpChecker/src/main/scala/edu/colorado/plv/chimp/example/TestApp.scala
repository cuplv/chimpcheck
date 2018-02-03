package edu.colorado.plv.chimp.example

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator._
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.coordinator.ChimpLoader
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * Created by edmund on 3/11/17.
  */
object TestApp {

  def main(args: Array[String]): Unit = {



    val buildDir = "PATH-TO-YOUR-APP-PROJECT"  //e.g /Users/Desktop/awesome-app/

    val appBuildDir = buildDir + "/app/build/outputs/apk/"
    val testerClass = "TestExpresso"
    val aaptHome = "PATH-TO-YOUR-AAPT" //e.g. /Users/Dev/Library/Android/sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = appBuildDir + "app-debug.apk"
    val chimpAPKPath = appBuildDir + "app-debug-androidTest.apk"

    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))
    implicit val ec = ExecutionContext.global


    val trace =  Click(*)  :>> Click(*)


    val outcome = ChimpLoader.quickLoad(emuID, trace, appAPKPath, chimpAPKPath, testerClass, aaptHome)

    println(s"\nOutcome: $outcome")

  }

}
