package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.protobuf.UIEvent
import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.combinator.TryEvent_Implicits.{TryAppEvent, TryExtEvent, TryTrace}
import edu.colorado.plv.chimp.utils.Base64
import org.scalacheck.Prop.False

object EventTrace {
   def trace(event: UIEvent): EventTrace = EventTrace(Seq(event))
   def trace(events: Seq[UIEvent]): EventTrace = EventTrace(events)
   def fromProto(trace: pb.EventTrace): EventTrace = {
       EventTrace( trace.events.map(UIEvent.fromProto(_)) )
   }
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
            /*
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
            } */
            AppEvent.fromProto( event.getAppEvent )
         }
         case pb.UIEvent.UIEventType.EXTEVENT => {
            /*
            event.getExtEvent.eventType match {
               case pb.ExtEvent.ExtEventType.CLICKBACK => ClickBack()
               case pb.ExtEvent.ExtEventType.CLICKHOME => ClickHome()
               case pb.ExtEvent.ExtEventType.CLICKMENU => ClickMenu()
               case pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS => PullDownSettings()
               case pb.ExtEvent.ExtEventType.RETURNTOAPP => ReturnToApp()
               case pb.ExtEvent.ExtEventType.ROTATELEFT  => RotateLeft()
               case pb.ExtEvent.ExtEventType.ROTATERIGHT => RotateRight()
            } */
            ExtEvent.fromProto( event.getExtEvent )
         }
         case pb.UIEvent.UIEventType.TRYEVENT => {
            val tryevent = event.getTryEvent
            tryevent.tryType match {
               case pb.TryEvent.TryType.APPEVENT => {
                  Try( new TryAppEvent( AppEvent.fromProto( tryevent.getAppEvent ) ) )
               }
               case pb.TryEvent.TryType.EXTEVENT => {
                  Try( new TryExtEvent( ExtEvent.fromProto( tryevent.getExtEvent ) ) )
               }
               case pb.TryEvent.TryType.TRACE => {
                  Try( new TryTrace( EventTrace.fromProto( tryevent.getTrace ) ) )
               }
            }
         }
         case pb.UIEvent.UIEventType.DECIDE => {
             val cond = Condition.fromProto( event.getDecide.testCond )
             val succ = EventTrace.fromProto( event.getDecide.succTrace )
             val fail = EventTrace.fromProto( event.getDecide.succTrace )
             Decide(cond, succ, fail)
         }

         case pb.UIEvent.UIEventType.DECIDEMANY => {
             DecideMany( event.getDecideMany.alternatives.map(Alternative.fromProto(_)):_* )
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

object * extends UIID {
   override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.WILD_CARD, None, None)
   override def toString(): String = "*"
}

import UIID_Implicits._

object UIID {
   def fromProto(pd_uiid: pb.UIID): UIID = {
      pd_uiid.idType match {
         case pb.UIID.UIIDType.R_ID    => pd_uiid.getRid
         case pb.UIID.UIIDType.NAME_ID => pd_uiid.getNameid
         case pb.UIID.UIIDType.WILD_CARD => *
      }
   }
}


object Coord {
   def fromProto(pb_coord: pb.XYCoordin): Coord = Coord (pb_coord.x) (pb_coord.y)
}
case class Coord(x:Int)(y:Int) extends ProtoMsg[pb.XYCoordin] {
   override def toMsg(): pb.XYCoordin = pb.XYCoordin(x,y)
}


object AppEvent {
   def fromProto(appevent: pb.AppEvent): AppEvent = {
      appevent.eventType match {
         case pb.AppEvent.AppEventType.CLICK     => Click( UIID.fromProto(appevent.getClick.uiid) )
         case pb.AppEvent.AppEventType.LONGCLICK => LongClick( UIID.fromProto(appevent.getLongclick.uiid) )
         case pb.AppEvent.AppEventType.TYPE      => Type( UIID.fromProto(appevent.getType.uiid), appevent.getType.input)
         case pb.AppEvent.AppEventType.DRAG      => Drag( UIID.fromProto(appevent.getDrag.uiid), Coord.fromProto(appevent.getDrag.disp) )
         case pb.AppEvent.AppEventType.PINCH     => Pinch( Coord.fromProto(appevent.getPinch.start), Coord.fromProto(appevent.getPinch.end) )
         case pb.AppEvent.AppEventType.SWIPE     => Swipe( Coord.fromProto(appevent.getSwipe.start), Coord.fromProto(appevent.getSwipe.end) )
         case pb.AppEvent.AppEventType.SLEEP     => Sleep(appevent.getSleep.time)
         case pb.AppEvent.AppEventType.SKIP      => Skip
      }
   }
}

abstract class AppEvent extends UIEvent {
   def ==> (trace: EventTrace): Alternative = Alternative( AppEventCondition(this), trace )
   def ==> (event: UIEvent): Alternative = Alternative( AppEventCondition(this), event |:| Skip )
   def ==> (gen: TraceGen): AlternativeG = AlternativeG( AppEventCondition(this), gen)
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
object Skip extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SKIP))
   }
   override def toString(): String = "Skip"
}

abstract class TryEvent {
   def event(): UIEvent
   def trace(): EventTrace
   def isTrace(): Boolean
}

object TryEvent_Implicits {

   implicit class TryAppEvent(appevent: AppEvent) extends TryEvent {
      override def event(): UIEvent = appevent
      override def trace(): EventTrace = null
      override def isTrace(): Boolean = false
   }

   implicit class TryExtEvent(extevent: ExtEvent) extends TryEvent {
      override def event(): UIEvent = extevent
      override def trace(): EventTrace = null
      override def isTrace(): Boolean = false
   }

   implicit class TryTrace(tr: EventTrace) extends TryEvent {
      override def event(): UIEvent = null
      override def trace(): EventTrace = tr
      override def isTrace(): Boolean = true
   }

}

case class Try(event: TryEvent) extends UIEvent {
   override def toMsg(): pb.UIEvent = {
      if (!event.isTrace()) {
         val pbevent = event.event().toMsg()
         pbevent.eventType match {
            case pb.UIEvent.UIEventType.APPEVENT => {
               ProtoMsg.mkUIEvent(
                  pb.TryEvent(pb.TryEvent.TryType.APPEVENT, Some(pbevent.getAppEvent))
               )
            }
            case pb.UIEvent.UIEventType.EXTEVENT => {
               ProtoMsg.mkUIEvent(
                  pb.TryEvent(pb.TryEvent.TryType.APPEVENT, None, Some(pbevent.getExtEvent))
               )
            }
         }
      } else {
         ProtoMsg.mkUIEvent(
            pb.TryEvent(pb.TryEvent.TryType.TRACE, None, None, Some(event.trace().toMsg()))
         )
      }
   }
   override def toString(): String =
      if (!event.isTrace()) s"Try ${event.event()}" else s"Try ${event.trace()}"
}

object Condition {
   def fromProto(pbCond: pb.Condition ): Condition = {
      pbCond.condType match {
         case pb.Condition.CondType.APPEVENT => AppEventCondition( AppEvent.fromProto( pbCond.getAppEvent ) )
         case pb.Condition.CondType.EXTEVENT => ExtEventCondition( ExtEvent.fromProto( pbCond.getExtEvent ) )
      }
   }
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

object Alternative {
   def fromProto(alt: pb.Alternatives): Alternative = {
      Alternative(Condition.fromProto( alt.cond ) , EventTrace.fromProto( alt.events ))
   }
}

case class Alternative(cond:Condition, trace:EventTrace) extends ProtoMsg[pb.Alternatives] {
   override def toMsg(): pb.Alternatives = pb.Alternatives(cond.toMsg(), trace.toMsg())
}

case class Decide(cond:Condition, alt1: EventTrace, alt2: EventTrace) extends UIEvent {
   override def toMsg(): pb.UIEvent = ProtoMsg.mkUIEvent( pb.Decide(cond.toMsg(), alt1.toMsg(), alt2.toMsg()) )
}

case class DecideMany(alts: Alternative*) extends UIEvent {
   override def toMsg(): pb.UIEvent = ProtoMsg.mkUIEvent( pb.DecideMany(alts.map(_.toMsg())) )
   def add(alt: Alternative): DecideMany = DecideMany( (alt +: alts):_*  )
}


object ExtEvent {
   def fromProto(extevent: pb.ExtEvent): ExtEvent = {
      extevent.eventType match {
        case pb.ExtEvent.ExtEventType.CLICKBACK => ClickBack
        case pb.ExtEvent.ExtEventType.CLICKHOME => ClickHome
        case pb.ExtEvent.ExtEventType.CLICKMENU => ClickMenu
        case pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS => PullDownSettings
        case pb.ExtEvent.ExtEventType.RESUME => Resume
        case pb.ExtEvent.ExtEventType.ROTATE => Rotate
     }
   }
}

abstract class ExtEvent extends UIEvent {
   def ==>(trace: EventTrace): Alternative = Alternative(ExtEventCondition(this), trace)
   def ==>(event: UIEvent): Alternative = Alternative(ExtEventCondition(this), event |:| Skip)
   def ==>(gen: TraceGen): AlternativeG = AlternativeG(ExtEventCondition(this), gen)
}

object ClickBack extends ExtEvent {
   override def toMsg(): pb.UIEvent =  {
     ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKBACK))
   }
   override def toString: String = "ClickBack"
}
object ClickHome extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKHOME))
   }
   override def toString: String = "ClickHome"
}
object ClickMenu extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKMENU))
   }
   override def toString: String = "ClickMenu"
}
object PullDownSettings extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS))
   }
   override def toString: String = "PullDownSettings"
}
object Resume extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.RESUME))
   }
   override def toString: String = "ReturnToApp"
}
object Rotate extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.ROTATE))
   }
   override def toString: String = "Rotate"
}
