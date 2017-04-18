package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 4/17/17.
  */
import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.generator.{AlternativeG, TraceGen}

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
  def ==>(event: UIEvent): Alternative = Alternative(ExtEventCondition(this), event :>> Skip)
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
