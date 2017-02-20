package edu.colorado.plv.chimp.combinator

import chimp.{protobuf => pb}

/**
  * Created by edmund on 2/5/17.
  */

trait ProtoMsg[Msg] {
  def toMsg(): Msg
}

object ProtoMsg {
  def mkUIEvent(event: pb.AppEvent): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.APPEVENT, Some(event), None)
  }

  def mkUIEvent(event: pb.ExtEvent): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.EXTEVENT, None, Some(event))
  }

  def mkUIEvent(event: pb.Tactics): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.TACTICS, None, None, Some(event))
  }
}
