package edu.colorado.plv.chimp.coordinator

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.actor.Actor.Receive
import akka.event.Logging
import akka.util.Timeout
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import edu.colorado.plv.chimp.combinator.{*, Assert, Click, Coord, EventTrace, Left, Orient_Implicits, Predicate, Prop, Swipe, TraceGen, Type, UIID_Implicits, ViewID_Implicits, isClickable, isEnabled}
import org.scalacheck.Prop._
import org.scalacheck.Test.Parameters
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by edmund on 3/28/17.
  */

object MissionControl {
  case class NewTrace(trace: EventTrace)
  case class Results(deviceID:String, outcome: ChimpOutcome)
  case class Init(chimpConfig: ChimpConfig)
}

class MissionControl extends Actor {
  val log = Logging(context.system, this)

  var freeLoaders: Seq[(String,ActorRef)] = Seq()
  var workingLoaders: Map[String,(EventTrace,ActorRef,ActorRef)] = Map()
  var waitingJobs: Seq[(EventTrace,ActorRef)] = Seq()

  def pushFreeLoader(deviceID:String, worker: ActorRef): Unit = {
     freeLoaders = freeLoaders :+ (deviceID,worker)
  }

  def popFreeLoader(): Option[(String,ActorRef)] = {
     if (freeLoaders.length > 0) {
       val next = freeLoaders.head
       freeLoaders = freeLoaders.tail
       Some(next)
     } else {
       None
     }
  }

  def pushWaitingJob(trace:EventTrace, client:ActorRef): Unit = {
     waitingJobs = (trace,client) +: waitingJobs
  }

  def popWaitingJob(): Option[(EventTrace,ActorRef)] = {
    if (waitingJobs.length > 0) {
      val next = waitingJobs.head
      waitingJobs = waitingJobs.tail
      Some(next)
    } else {
      None
    }
  }

  def addWorkingLoader(deviceID: String, trace: EventTrace, worker:ActorRef, client:ActorRef): Unit = {
    workingLoaders = workingLoaders + (deviceID -> (trace,worker,client))
  }

  def getWorkingLoader(deviceID: String): Option[(EventTrace,ActorRef,ActorRef)] = {
    workingLoaders.get(deviceID)
  }

  def deleteWorkingLoader(deviceID: String): Unit = {
    workingLoaders = workingLoaders - deviceID
  }

  override def receive: Receive = {
    case MissionControl.Init(chimpConfig) => {
      freeLoaders = chimpConfig.deviceInfos.map{
        deviceInfo:DeviceInfo => {
          val chimpLoader =
            if (!chimpConfig.testRun) context.actorOf(Props[ChimpLoaderActor], name = deviceInfo.id)
            else context.actorOf(Props[ChimpTesterActor], name = deviceInfo.id)
          chimpLoader ! ChimpLoaderActor.Init(deviceInfo.id, chimpConfig)
          (deviceInfo.id,chimpLoader)
        }
      }
    }
    case MissionControl.NewTrace(trace) => {
      popFreeLoader() match {
        case Some((deviceID,worker)) => {
          log.info(s"Assigning $deviceID to $trace")
          worker ! ChimpLoaderActor.Job(trace)
          addWorkingLoader(deviceID, trace, worker, sender)
        }
        case None => {
          log.info(s"No available workers, enqueuing trace: $trace")
          pushWaitingJob(trace, sender)
        }
      }
    }
    case MissionControl.Results(deviceID, res) => {
       val (trace,worker,client) = getWorkingLoader(deviceID).get
       log.info(s"Results received: $res")
       client ! res
       deleteWorkingLoader(deviceID)
       popWaitingJob() match {
         case Some((nextTrace,nextClient)) => {
            log.info(s"Reassigning $deviceID to $nextTrace")
            worker ! ChimpLoaderActor.Job(nextTrace)
            addWorkingLoader(deviceID, nextTrace, worker, nextClient)
         }
         case None => {
            log.info(s"No waiting jobs, freeing $deviceID")
            pushFreeLoader(deviceID, worker)
         }
       }
    }
  }

}

object ChimpLoaderActor {
  case class Job(event: EventTrace)
  case class Init(deviceID: String, chimpConfig: ChimpConfig)
}

class ChimpLoaderActor extends Actor {
  val log = Logging(context.system, this)

  var deviceID:String = "emulator-5554"
  var chimpConfig: ChimpConfig = null

  override def receive: Receive = {
    case ChimpLoaderActor.Init(deviceID, chimpConfig) => {
      this.deviceID = deviceID
      this.chimpConfig = chimpConfig
    }
    case ChimpLoaderActor.Job(event) => {
      log.info(s"Loader $deviceID : $event")

      // TODO replace hard-coded implicits
      implicit val logger = Logger(LoggerFactory.getLogger(s"Chimp-$deviceID"))
      implicit val ec = ExecutionContext.global

      val outcome = ChimpLoader.quickLoad(deviceID, event, chimpConfig.appAPKPath, chimpConfig.chimpAPKPath,
                                        chimpConfig.testerClass, chimpConfig.aaptHomePath:String, chimpConfig.packageNamesOpt)

      sender() ! MissionControl.Results( deviceID, outcome )

    }
  }
}

class ChimpTesterActor extends Actor {
  val log = Logging(context.system, this)

  var deviceID:String = "emulator-5554"

  override def receive: Receive = {
    case ChimpLoaderActor.Init(deviceID,_) => this.deviceID = deviceID
    case ChimpLoaderActor.Job(event) => {
      log.info(s"Loader $deviceID : $event")
      Thread.sleep(3000)
      sender() ! MissionControl.Results( deviceID, SuccChimpOutcome(event) )
    }
  }
}

object Trace_Implicits {

  implicit class TraceTester(trace: EventTrace) {

    def chimpCheck(prop: Prop) (implicit control: ActorRef, timeout: Timeout): Boolean = {
       val future = control ? MissionControl.NewTrace(trace :>> Assert(prop))
       val result = Await.result(future, timeout.duration).asInstanceOf[ChimpOutcome]
       print(result)
       true
    }

  }

}

import edu.colorado.plv.chimp.combinator.Generator_Implicits._
import UIID_Implicits._
import Orient_Implicits._
import ViewID_Implicits._
import Trace_Implicits._

object TestActors {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val executionContext = system.dispatcher

    val config = ChimpConfig.defaultConfig().withStartEmulator(true).withTestRun(true)
                      .addDeviceInfo(DeviceInfo("emulator-5560"))
                      .addDeviceInfo(DeviceInfo("emulator-5580"))

    implicit val control = system.actorOf(Props[MissionControl], name = "ChimpCentral")
    control ! MissionControl.Init(config)

    val traces: TraceGen = Click("login") :>> Click(*) *>> Type("userbox", "test") *>> Type("pwdbox", "1234") *>> Click("Go") *>>
      Swipe("nuts", Left) *>> Swipe("crap", Coord(1, 2)) *>> (Click("button1") <+> Click("button2"))


    val myParam = Parameters.default.withMinSuccessfulTests(10).withWorkers(4)

    implicit val timeout = Timeout(60 seconds)

    forAll(traces.generator()) {
      tr => tr chimpCheck {
        (isClickable(1) ==> isEnabled(1)) /\ (isClickable(2) ==> isEnabled(2))
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