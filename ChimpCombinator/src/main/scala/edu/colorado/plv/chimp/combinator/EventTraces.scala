package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.{protobuf => pb}

object EventTrace {
   def trace(event: UIEvent): EventTrace = EventTrace(Seq(event))
   def trace(events: Seq[UIEvent]): EventTrace = EventTrace(events)
}

case class EventTrace(events: Seq[UIEvent]) extends ProtoMsg[pb.EventTrace] {
   def |:| (event: UIEvent): EventTrace = EventTrace( events :+ event )
   def |:| (trace: EventTrace): EventTrace = EventTrace( events ++ trace.events )
   override def toMsg(): pb.EventTrace = pb.EventTrace( events.map(_.toMsg()) )
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
}

abstract class ExtEvent extends UIEvent

case class ClickBack() extends ExtEvent {
  override def toMsg(): pb.UIEvent =  {
     ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKBACK))
  }
}
case class ClickHome() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKHOME))
   }
}
case class ClickMenu() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.CLICKMENU))
   }
}
case class PullDownSettings() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.PULLDOWNSETTINGS))
   }
}
case class ReturnToApp() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.RETURNTOAPP))
   }
}
case class RotateLeft() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.ROTATELEFT))
   }
}
case class RotateRight() extends ExtEvent {
   override def toMsg(): pb.UIEvent = {
      ProtoMsg.mkUIEvent (pb.ExtEvent(pb.ExtEvent.ExtEventType.ROTATERIGHT))
   }
}
