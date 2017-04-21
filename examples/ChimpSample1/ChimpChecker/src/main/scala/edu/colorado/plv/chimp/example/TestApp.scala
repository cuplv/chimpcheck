package edu.colorado.plv.chimp.example

import plv.colorado.edu.chimpsample.R
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.{Assert, Rotate, _}
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import edu.colorado.plv.chimp.coordinator.ChimpLoader
import edu.colorado.plv.chimp.generator.Gorilla
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * Created by edmund on 3/11/17.
  */
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
