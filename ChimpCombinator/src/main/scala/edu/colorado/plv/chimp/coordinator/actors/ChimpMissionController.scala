package edu.colorado.plv.chimp.coordinator.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.chimp.coordinator.{ChimpConfig, ChimpOutcome, DeviceInfo}

/**
  * Created by edmund on 4/29/17.
  */
object ChimpMissionController {
  case class NewTrace(trace: EventTrace)
  case class Results(deviceID:String, outcome: ChimpOutcome)
  case class Init(chimpConfig: ChimpConfig)
}

class ChimpMissionController extends Actor {
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
    case ChimpMissionController.Init(chimpConfig) => {
      freeLoaders = chimpConfig.deviceInfos.map{
        deviceInfo:DeviceInfo => {
          val chimpLoader =
            if (!chimpConfig.testRun) context.actorOf(Props[ChimpDeviceWorker], name = deviceInfo.id)
            else context.actorOf(Props[ChimpDummyWorker], name = deviceInfo.id)
          chimpLoader ! ChimpDeviceWorker.Init(deviceInfo.id, chimpConfig)
          (deviceInfo.id,chimpLoader)
        }
      }
    }
    case ChimpMissionController.NewTrace(trace) => {
      popFreeLoader() match {
        case Some((deviceID,worker)) => {
          log.debug(s"Assigning $deviceID to $trace")
          worker ! ChimpDeviceWorker.Job(trace)
          addWorkingLoader(deviceID, trace, worker, sender)
        }
        case None => {
          log.debug(s"No available workers, enqueuing trace: $trace")
          pushWaitingJob(trace, sender)
        }
      }
    }
    case ChimpMissionController.Results(deviceID, res) => {
      val (trace,worker,client) = getWorkingLoader(deviceID).get
      // log.debug(s"Results received: $res")
      client ! res
      deleteWorkingLoader(deviceID)
      popWaitingJob() match {
        case Some((nextTrace,nextClient)) => {
          log.debug(s"Reassigning $deviceID to $nextTrace")
          worker ! ChimpDeviceWorker.Job(nextTrace)
          addWorkingLoader(deviceID, nextTrace, worker, nextClient)
        }
        case None => {
          log.debug(s"No waiting jobs, freeing $deviceID")
          pushFreeLoader(deviceID, worker)
        }
      }
    }
  }

}
