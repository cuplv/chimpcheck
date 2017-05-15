package edu.colorado.plv.chimp.coordinator.actors

/**
  * Created by edmund on 4/29/17.
  */

import akka.actor.Actor
import akka.event.Logging

class ChimpResultLogger extends Actor {
  val log = Logging(context.system, this)
  override def preStart() = {
    log.debug("Starting Chimp Result Logger")
  }
  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]",
      reason.getMessage, message.getOrElse(""))
  }
  def receive = {
    case "<< Hello World >>" => log.info("Received Hello world test")
    case msg    => log.info(s"$msg")
  }
}
