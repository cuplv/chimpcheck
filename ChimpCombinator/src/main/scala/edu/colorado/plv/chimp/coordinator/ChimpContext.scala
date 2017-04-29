package edu.colorado.plv.chimp.coordinator

import akka.actor.{ActorRef, ActorSystem, Props}
import edu.colorado.plv.chimp.coordinator.actors.{ChimpMissionController, ChimpResultLogger}

/**
  * Created by edmund on 4/29/17.
  */
object ChimpContext {
  def initDefaultChimpContext(config: ChimpConfig) (implicit system: ActorSystem): ChimpContext = {
    val controller = system.actorOf(Props[ChimpMissionController], name = "ChimpMissionController")
    val resultLogger = system.actorOf(Props[ChimpResultLogger], name = "ChimpResultLogger")
    controller ! ChimpMissionController.Init(config)

    ChimpContext(config, controller, resultLogger)
  }
}

case class ChimpContext(config: ChimpConfig, controller: ActorRef, resultLogger: ActorRef)

