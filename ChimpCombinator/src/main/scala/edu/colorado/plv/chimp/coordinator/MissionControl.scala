package edu.colorado.plv.chimp.coordinator

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.actor.Actor.Receive
import akka.event.Logging
import akka.util.Timeout
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import edu.colorado.plv.chimp.combinator.{*, Assert, Click, Coord, EventTrace, Left, Prop, Swipe, Type, isClickable, isEnabled}
import edu.colorado.plv.chimp.coordinator.actors.ChimpMissionController
import edu.colorado.plv.chimp.generator.{Gorilla, TraceGen}
import org.scalacheck.Prop._
import org.scalacheck.Test.Parameters
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by edmund on 3/28/17.
  */

object MissionControl {

  implicit class TraceTester(trace: EventTrace) {

    def chimpCheck(prop: Prop) (implicit chimpContext:ChimpContext): Boolean = {
      implicit val timeout = chimpContext.config.timeout
      val future = chimpContext.controller ? ChimpMissionController.NewTrace(trace :>> Assert(prop))
      val result = Await.result(future, chimpContext.config.timeout.duration).asInstanceOf[ChimpOutcome]
      // print(result)
      // true
      result match {
        case SuccChimpOutcome(executedTrace) => {
          chimpContext.resultLogger ! s"$result"
          true
        }
        case BlockChimpOutcome(executedTrace,error) => {
          chimpContext.resultLogger ! s"$result"
          true
        }
        case default => {
          chimpContext.resultLogger ! s"$result"
          false
        }
      }
    }

  }

}


import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._

import MissionControl._

import org.scalacheck.Properties

object TestActors {


  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    val config = ChimpConfig.defaultConfig().withStartEmulator(true).withTestRun(true)
                      .addDeviceInfo(DeviceInfo("emulator-5560"))
                      .addDeviceInfo(DeviceInfo("emulator-5580"))

    /*
    implicit val control = system.actorOf(Props[MissionControl], name = "ChimpMissionController")
    implicit val resultlogger = system.actorOf(Props[ChimpResultLogger], name = "ChimpResultLogger")
    control ! MissionControl.Init(config)
    implicit val timeout = Timeout(60 seconds)
    */

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    val traces: TraceGen = Click("login") :>> Click(*) *>> Type("userbox", "test") *>> Type("pwdbox", "1234") *>> Click("Go") *>>
      Swipe("nuts", Left) *>> Swipe("crap", Coord(1, 2)) *>> (Click("button1") <+> Click("button2"))


    val myParam = Parameters.default.withMinSuccessfulTests(10).withWorkers(4)

    forAll(traces.generator()) {
      tr => tr chimpCheck {
        (isClickable(1) ==> isEnabled(1)) /\ (isClickable(2) ==> isEnabled(2)) /\ true
      }
      /* {
         val future = control ? MissionControl.NewTrace(tr)
         val result = Await.result(future, timeout.duration).asInstanceOf[ChimpTraceResults]
         print(result)
         true
      } */
    }.check(myParam)

    system.terminate()

  }

}


object TestChimpSample1 {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    val config = ChimpConfig.defaultConfig().withStartEmulator(false).withTestRun(false).withTimeout(360 seconds)
      // .addDeviceInfo(DeviceInfo("emulator-5556"))
      // .addDeviceInfo(DeviceInfo("emulator-5558"))
      .withAPKs("/data/chimp/ChimpSample1/app-debug.apk", "/data/chimp/ChimpSample1/app-debug-androidTest.apk", "TestExpresso")
      .withAaptHome("/usr/local/android-sdk/build-tools/24.0.3")

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    val myParam = Parameters.default.withMinSuccessfulTests(3).withWorkers(1)

    val trace = Gorilla

    forAll(trace.generator()) {
      tr => tr chimpCheck { true }
    }.check(myParam)

    system.terminate()

  }

}


object TestContractionTimer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    val config = ChimpConfig.defaultConfig().withStartEmulator(false).withTestRun(false).withTimeout(360 seconds)
      // .addDeviceInfo(DeviceInfo("emulator-5556"))
      // .addDeviceInfo(DeviceInfo("emulator-5558"))
      .withAPKs("/data/chimp/ContractionTimerRealBug/app-debug.apk", "/data/chimp/ContractionTimerRealBug/app-debug-androidTest.apk", "ChimpDriverHarness")
      .withAaptHome("/usr/local/android-sdk/build-tools/24.0.3")

    implicit val chimpContext = ChimpContext.initDefaultChimpContext(config)

    val myParam = Parameters.default.withMinSuccessfulTests(3).withWorkers(1)

    val trace = Gorilla

    forAll(trace.generator()) {
      tr => tr chimpCheck { true }
    }.check(myParam)

    system.terminate()

  }

}