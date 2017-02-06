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
               case pb.AppEvent.AppEventType.CLICKNAME  => ClickName(appevent.getClickname.name)
               case pb.AppEvent.AppEventType.CLICKID    => ClickId(appevent.getClickid.id)
               case pb.AppEvent.AppEventType.TYPEINNAME => TypeInName(appevent.getTypeinname.name,appevent.getTypeinname.input)
               case pb.AppEvent.AppEventType.TYPEINID   => TypeInId(appevent.getTypeinid.id,appevent.getTypeinid.input)
               case pb.AppEvent.AppEventType.SLEEP      => Sleep(appevent.getSleep.time)
               case pb.AppEvent.AppEventType.SKIP       => Skip()
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
abstract class AppEvent extends UIEvent

case class ClickName(name: String) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.CLICKNAME,
        Some(pb.ClickName(name)), None, None, None, None))
   }
}
case class ClickId(id:Int) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.CLICKID,
        None, Some(pb.ClickId(id)), None, None, None))
   }
}
case class TypeInName(name:String, input:String) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.TYPEINNAME,
        None, None, Some(pb.TypeInName(name, input)), None, None))
   }
}
case class TypeInId(id: Int, input: String) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.TYPEINID,
        None, None, None, Some(pb.TypeInId(id, input)), None))
   }
}
case class Sleep(time: Int) extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SLEEP,
        None, None, None, None, Some(pb.Sleep(time))))
   }
}
case class Skip() extends AppEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.AppEvent(pb.AppEvent.AppEventType.SKIP,
        None, None, None, None, None))
   }
   override def toString(): String = "Skip"
}

abstract class ExtEvent extends UIEvent

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
