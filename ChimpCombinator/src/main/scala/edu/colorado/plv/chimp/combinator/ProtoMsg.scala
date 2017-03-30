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

  def mkUIEvent(event: pb.TryEvent): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.TRYEVENT, None, None, Some(event))
  }

  def mkUIEvent(event: pb.Decide): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.DECIDE, None, None, None, Some(event))
  }

  def mkUIEvent(event: pb.DecideMany): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.DECIDEMANY, None, None, None, None, Some(event))
  }

  def mkUIEvent(event: pb.Assert): pb.UIEvent = {
    pb.UIEvent(pb.UIEvent.UIEventType.ASSERT, None, None, None, None, None, Some(event))
  }
}
