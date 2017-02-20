package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.utils.Base64

object EventTrace {
   def trace(event: UIEvent): EventTrace = EventTrace(Seq(event))
   def trace(events: Seq[UIEvent]): EventTrace = EventTrace(events)
   def fromBase64(b64: String): EventTrace = {
       val trace : pb.EventTrace = pb.EventTrace.parseFrom( Base64.decode(b64) )
       EventTrace( trace.events.map(UIEvent.fromProto(_)) )
   }
}

case class EventTrace(events: Seq[UIEvent]) extends ProtoMsg[pb.EventTrace] {
   def |:| (event: UIEvent): EventTrace = EventTrace( events :+ event )
   def |:| (trace: EventTrace): EventTrace = EventTrace( events ++ trace.events )
   override def toMsg(): pb.EventTrace = pb.EventTrace( events.map(_.toMsg()) )
   override def toString(): String = events.mkString(" |:| ")
   def toBase64(): String = Base64.encode( toMsg().toByteArray )
}

object UIEvent {
   def fromProto(event: pb.UIEvent): UIEvent = {
      event.eventType match {
         case pb.UIEvent.UIEventType.APPEVENT => {
            val appevent = event.getAppEvent
            appevent.eventType match {
               case pb.AppEvent.AppEventType.CLICK     => Click( UIID.fromProto(appevent.getClick.uiid) )
               case pb.AppEvent.AppEventType.LONGCLICK => LongClick( UIID.fromProto(appevent.getLongclick.uiid) )
               case pb.AppEvent.AppEventType.TYPE      => Type( UIID.fromProto(appevent.getType.uiid), appevent.getType.input)
               case pb.AppEvent.AppEventType.DRAG      => Drag( UIID.fromProto(appevent.getDrag.uiid), Coord.fromProto(appevent.getDrag.disp) )
               case pb.AppEvent.AppEventType.PINCH     => Pinch( Coord.fromProto(appevent.getPinch.start), Coord.fromProto(appevent.getPinch.end) )
               case pb.AppEvent.AppEventType.SWIPE     => Swipe( Coord.fromProto(appevent.getSwipe.start), Coord.fromProto(appevent.getSwipe.end) )
               case pb.AppEvent.AppEventType.SLEEP     => Sleep(appevent.getSleep.time)
               case pb.AppEvent.AppEventType.SKIP      => Skip()
            }
         }
         case pb.UIEvent.UIEventType.EXTEVENT => {
            event.getExtEvent.eventType match {
               case pb.ExtEvent.ExtEventType.CLICKBACK => ClickBack()
               case pb.ExtEvent.ExtEventType.CLICKHOME => ClickHome()
               case pb.ExtEvent.ExtEventType.CLICKMENU => ClickMenu()
               case pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS => PullDownSettings()
               case pb.ExtEvent.ExtEventType.RETURNTOAPP => ReturnToApp()
               case pb.ExtEvent.ExtEventType.ROTATELEFT  => RotateLeft()
               case pb.ExtEvent.ExtEventType.ROTATERIGHT => RotateRight()
            }
         }
      }
   }
}

abstract class UIEvent extends ProtoMsg[pb.UIEvent] {
   def |:| (event: UIEvent): EventTrace = EventTrace(Seq(this, event))
   def |:| (trace: EventTrace): EventTrace = EventTrace( this +: trace.events )
}

abstract class UIID extends ProtoMsg[pb.UIID]

object UIID_Implicits {

   implicit class RId(rid: Int) extends UIID {
      override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.R_ID, Some(rid), None)
      override def toString(): String = s"$rid"
   }

   implicit class Name(name: String) extends UIID {
      override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.NAME_ID, None, Some(name))
      override def toString(): String = name
   }

}

import UIID_Implicits._

object UIID {
   def fromProto(pd_uiid: pb.UIID): UIID = {
      pd_uiid.idType match {
         case pb.UIID.UIIDType.R_ID    => pd_uiid.getRid
         case pb.UIID.UIIDType.NAME_ID => pd_uiid.getNameid
      }
   }
}


object Coord {
   def fromProto(pb_coord: pb.XYCoordin): Coord = Coord (pb_coord.x) (pb_coord.y)
}
case class Coord(x:Int)(y:Int) extends ProtoMsg[pb.XYCoordin] {
   override def toMsg(): pb.XYCoordin = pb.XYCoordin(x,y)
}




abstract class AppEvent extends UIEvent {
   def ==> (trace: EventTrace): Alternative = Alternative( AppEventCondition(this), trace )
   def ==> (event: UIEvent): Alternative = Alternative( AppEventCondition(this), event |:| Skip() )
}

case class Click(uiid: UIID) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.CLICK,
        Some(pb.Click(uiid.toMsg()))))
   }
}
case class LongClick(uiid: UIID) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.LONGCLICK,
         None, Some(pb.LongClick(uiid.toMsg()))))
   }
}
case class Drag(uiid: UIID, coord: Coord) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.DRAG,
         None, None, Some(pb.Drag(uiid.toMsg(), coord.toMsg()))))
   }
}

case class Pinch(start: Coord, end: Coord) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.PINCH,
         None, None, None, Some(pb.Pinch(start.toMsg(), end.toMsg()))))
   }
}

case class Swipe(start: Coord, end: Coord) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SWIPE,
         None, None, None, None, Some(pb.Swipe(start.toMsg(), end.toMsg()))))
   }
}

case class Type(uiid:UIID, input:String) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.TYPE,
        None, None, None, None, None, Some(pb.Type(uiid.toMsg(), input))))
   }
}
case class Sleep(time: Int) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SLEEP,
        None, None, None, None, None, None, Some(pb.Sleep(time))))
   }
}
case class Skip() extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SKIP))
   }
   override def toString(): String = "Skip"
}

abstract class Condition extends ProtoMsg[pb.Condition]

case class AppEventCondition(appevent: AppEvent) extends Condition {
   override def toMsg(): pb.Condition = {
      pb.Condition(pb.Condition.CondType.APPEVENT, Some(appevent.toMsg().getAppEvent))
   }
   override def toString(): String = appevent.toString
}
case class ExtEventCondition(extevent: ExtEvent) extends Condition {
   override def toMsg(): pb.Condition = {
      pb.Condition(pb.Condition.CondType.EXTEVENT, None, Some(extevent.toMsg().getExtEvent))
   }
   override def toString(): String = extevent.toString
}

case class Alternative(cond:Condition, trace:EventTrace) extends ProtoMsg[pb.Alternatives] {
   override def toMsg(): pb.Alternatives = pb.Alternatives(cond.toMsg(), trace.toMsg())
}

case class Tactics(alts: Alternative*) extends UIEvent {
   override def toMsg(): pb.UIEvent = ProtoMsg.mkUIEvent( pb.Tactics(alts.map(_.toMsg())) )
}




abstract class ExtEvent extends UIEvent {
   def ==>(trace: EventTrace): Alternative = Alternative(ExtEventCondition(this), trace)
   def ==>(event: UIEvent): Alternative = Alternative(ExtEventCondition(this), event |:| Skip())
}

case class ClickBack() extends ExtEvent {
   override def toMsg(): pb.UIEvent =  {
     ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKBACK))
   }
   override def toString: String = "ClickBack"
}
case class ClickHome() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKHOME))
   }
   override def toString: String = "ClickHome"
}
case class ClickMenu() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKMENU))
   }
   override def toString: String = "ClickMenu"
}
case class PullDownSettings() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS))
   }
   override def toString: String = "PullDownSettings"
}
case class ReturnToApp() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.RETURNTOAPP))
   }
   override def toString: String = "ReturnToApp"
}
case class RotateLeft() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.ROTATELEFT))
   }
   override def toString: String = "RotateLeft"
}
case class RotateRight() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.ROTATERIGHT))
   }
   override def toString: String = "RotateRight"
}
