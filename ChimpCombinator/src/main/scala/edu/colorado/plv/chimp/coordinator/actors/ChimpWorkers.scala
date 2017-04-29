package edu.colorado.plv.chimp.coordinator.actors

import akka.actor.Actor
import akka.event.Logging
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.chimp.coordinator.{ChimpConfig, ChimpLoader, SuccChimpOutcome}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * Created by edmund on 4/29/17.
  */
object ChimpDeviceWorker {
  case class Job(event: EventTrace)
  case class Init(deviceID: String, chimpConfig: ChimpConfig)
}

class ChimpDeviceWorker extends Actor {
  val log = Logging(context.system, this)

  var deviceID:String = "emulator-5554"
  var chimpConfig: ChimpConfig = null

  override def receive: Receive = {
    case ChimpDeviceWorker.Init(deviceID, chimpConfig) => {
      this.deviceID = deviceID
      this.chimpConfig = chimpConfig
    }
    case ChimpDeviceWorker.Job(event) => {
      log.debug(s"Loader $deviceID : $event")

      // TODO replace hard-coded implicits
      implicit val logger = Logger(LoggerFactory.getLogger(s"Chimp-$deviceID"))
      implicit val ec = ExecutionContext.global

      val outcome = ChimpLoader.quickLoad(deviceID, event, chimpConfig.appAPKPath, chimpConfig.chimpAPKPath,
        chimpConfig.testerClass, chimpConfig.aaptHomePath:String, chimpConfig.packageNamesOpt, true)

      sender() ! ChimpMissionController.Results( deviceID, outcome )

    }
  }
}

class ChimpDummyWorker extends Actor {
  val log = Logging(context.system, this)

  var deviceID:String = "emulator-5554"

  override def receive: Receive = {
    case ChimpDeviceWorker.Init(deviceID,_) => this.deviceID = deviceID
    case ChimpDeviceWorker.Job(event) => {
      log.debug(s"Loader $deviceID : $event")
      Thread.sleep(3000)
      sender() ! ChimpMissionController.Results( deviceID, SuccChimpOutcome(event) )
    }
  }
}
