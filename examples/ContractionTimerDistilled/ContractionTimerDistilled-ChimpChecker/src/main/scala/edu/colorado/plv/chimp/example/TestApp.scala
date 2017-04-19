package edu.colorado.plv.chimp.example

import com.peilunzhang.contractiontimerdistilled.R
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

    println( s"Yay: ${R.id.fragmentBtn2}" )

    val testGoodTrace:EventTrace = Rotate :>> Sleep(3000) :>> Rotate :>> Click(R.id.fragmentBtn1) :>> Sleep(11000) :>> Click(R.id.fragmentBtn2)

    val testCrashTrace:EventTrace = Rotate :>> Sleep(3000) :>> Rotate :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Sleep(20000)

    val testCrashTrace2:EventTrace = Rotate :>> Sleep(3000) :>> Rotate :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Sleep(7000) :>>
       Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>>
       Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>>
       Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2) :>> Click(R.id.fragmentBtn1) :>> Click(R.id.fragmentBtn2)

    val testGorilla:EventTrace = Assert( isClickable(R.id.fragmentBtn1) ) :>> Rotate :>> Assert( isClickable(R.id.fragmentBtn1) ) :>> Sleep(2000) :>> Rotate :>> Sleep(2000) :>> Gorilla.generator().sample.get

    val testAssert:EventTrace = Assert( isClickable(R.id.fragmentBtn1) ) :>> Rotate :>> Assert( isClickable("Crap") ) :>> Sleep(2000) :>> Rotate :>> Sleep(2000)

    val testReflect:EventTrace = Assert( isClickable(R.id.fragmentBtn1) ) :>> Rotate :>> Assert( Predicate("moreThanThree", 20) ) :>> Sleep(2000) :>> Rotate :>> Sleep(2000)

    val testRotate:EventTrace = Rotate :>> Sleep(2000) :>> Rotate :>> Sleep(2000) :>> Rotate :>> Sleep(2000) :>> Rotate

    val testSync:EventTrace = Assert( Predicate("checkCount", 1) ) :>> Click(R.id.fragmentBtn2) :>> Assert( Predicate("checkCount", 2) ) :>> Click(R.id.fragmentBtn2) :>> Assert( Predicate("checkCount", 3) ) :>>
                                Click(R.id.fragmentBtn2) :>> Assert( Predicate("checkCount", 4) ) :>> Sleep(2000)

    val aaptHome = "/usr/local/android-sdk/build-tools/24.0.3"
    val emuID = "emulator-5554"
    val appAPKPath = "/data/chimp/ContractionTimer/app-debug.apk"
    val chimpAPKPath = "/data/chimp/ContractionTimer/app-debug-androidTest.apk"
    val testerClass = "TestExpresso"

    implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

    // This is needed now, because in quickLoad, we spin off a future computation: The kick back routine
    implicit val ec = ExecutionContext.global

    val outcome = ChimpLoader.quickLoad(emuID, testSync, appAPKPath, chimpAPKPath, testerClass, aaptHome)

    println(s"\nOutcome: $outcome")

  }

}
