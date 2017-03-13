package edu.colorado.plv.chimp.example

import com.peilunzhang.contractiontimerdistilled.R
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator._
import edu.colorado.plv.chimp.combinator.UIID_Implicits._
import edu.colorado.plv.chimp.coordinator.ChimpLoader
import org.slf4j.LoggerFactory

/**
  * Created by edmund on 3/11/17.
  */
object TestApp {

  def main(args: Array[String]): Unit = {

    println( s"Yay: ${R.id.fragmentBtn2}" )

    val testTrace:EventTrace = RotateLeft() |:| RotateRight() |:| Click(R.id.fragmentBtn1) |:| Sleep(11000) |:| Click(R.id.fragmentBtn2)

    val aaptHome = "/usr/local/android-sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = "/data/chimp/ContractionTimer/app-debug.apk"
    val chimpAPKPath = "/data/chimp/ContractionTimer/app-debug-androidTest.apk"
    val testerClass = "TestExpresso"

    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

    ChimpLoader.quickLoad(emuID, testTrace, appAPKPath, chimpAPKPath, testerClass, aaptHome)

  }

}