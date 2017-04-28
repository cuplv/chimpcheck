package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 4/17/17.
  */

import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.generator.{AlternativeG, TraceGen}

object AppEvent {
  def fromProto(appevent: pb.AppEvent): AppEvent = {
    appevent.eventType match {
      case pb.AppEvent.AppEventType.CLICK     => {
        appevent.getClick.display match {
          case None => Click (UIID.fromProto (appevent.getClick.uiid) )
          case Some(disp) => {
            Click(UIID.fromProto (appevent.getClick.uiid) ).setDisplay(disp)
          }
        }
      }
      case pb.AppEvent.AppEventType.LONGCLICK => LongClick( UIID.fromProto(appevent.getLongclick.uiid) )
      case pb.AppEvent.AppEventType.TYPE      => Type( UIID.fromProto(appevent.getType.uiid), appevent.getType.input)
      case pb.AppEvent.AppEventType.PINCH     =>
        Pinch( Coord.fromProto(appevent.getPinch.start1), Coord.fromProto(appevent.getPinch.start2),
          Coord.fromProto(appevent.getPinch.end1), Coord.fromProto(appevent.getPinch.end2) )
      case pb.AppEvent.AppEventType.SWIPE     => Swipe( UIID.fromProto(appevent.getSwipe.uiid),
        Orientation.fromProto( appevent.getSwipe.pos ) )
      case pb.AppEvent.AppEventType.SLEEP     => Sleep(appevent.getSleep.time)
      case pb.AppEvent.AppEventType.SKIP      => Skip
    }
  }
}

abstract class AppEvent extends UIEvent {
  def ==> (trace: EventTrace): Alternative = Alternative( AppEventCondition(this), trace )
  def ==> (event: UIEvent): Alternative = Alternative( AppEventCondition(this), event :>> Skip )
  def ==> (gen: TraceGen): AlternativeG = AlternativeG( AppEventCondition(this), gen)
}

case class Click(uiid: UIID) extends AppEvent {
  var optDisp: Option[String] = None
  def setDisplay(display: String): AppEvent = {
    optDisp = Some(display)
    this
  }
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.CLICK,
      Some(pb.Click(uiid.toMsg()))))
  }
  override def toString: String = {
    optDisp match {
      case None => s"Click($uiid)"
      case Some(display) => s"Click($display)"
    }
  }
}
case class LongClick(uiid: UIID) extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.LONGCLICK,
      None, Some(pb.LongClick(uiid.toMsg()))))
  }
}


case class Pinch(start1: Coord, start2:Coord, end1: Coord, end2:Coord) extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.PINCH,
      None, None, Some(pb.Pinch(start1.toMsg(), start2.toMsg(), end1.toMsg(), end2.toMsg()))))
  }
}

case class Swipe(uiid: UIID, orient: Orientation) extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SWIPE,
      None, None, None, Some(pb.Swipe(uiid.toMsg(), orient.toMsg()))))
  }
}

case class Type(uiid:UIID, input:String) extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.TYPE,
      None, None, None, None, Some(pb.Type(uiid.toMsg(), input))))
  }
}
case class Sleep(time: Int) extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SLEEP,
      None, None, None, None, None, Some(pb.Sleep(time))))
  }
}
object Skip extends AppEvent {
  override def toMsg(): pb.UIEvent = {
    ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SKIP))
  }
  override def toString(): String = "Skip"
}