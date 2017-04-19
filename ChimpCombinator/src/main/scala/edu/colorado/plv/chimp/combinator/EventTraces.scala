package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */


import chimp.{protobuf => pb}
import Implicits.{TryAppEvent, TryExtEvent, TryTrace}

import edu.colorado.plv.chimp.generator.{AtomSeq, Path, TraceGen}
import edu.colorado.plv.chimp.utils.Base64

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
   def :>>(event: UIEvent): EventTrace = EventTrace( events :+ event )
   def :>>(trace: EventTrace): EventTrace = EventTrace( events ++ trace.events )
   def :>>(traceGen: TraceGen): TraceGen = AtomSeq(Path(this),traceGen)
   override def toMsg(): pb.EventTrace = pb.EventTrace( events.map(_.toMsg()) )
   override def toString(): String = events.mkString(" :> ")
   def toBase64(): String = Base64.encode( toMsg().toByteArray )
}

object UIEvent {
   def fromProto(event: pb.UIEvent): UIEvent = {
      event.eventType match {
         case pb.UIEvent.UIEventType.APPEVENT => {
            AppEvent.fromProto( event.getAppEvent )
         }
         case pb.UIEvent.UIEventType.EXTEVENT => {
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

         case pb.UIEvent.UIEventType.ASSERT => {
             Assert( Prop.fromProto(event.getAssert.props) )
         }
      }
   }
}

abstract class UIEvent extends ProtoMsg[pb.UIEvent] {
   def :>>(event: UIEvent): EventTrace = EventTrace(Seq(this, event))
   def :>>(trace: EventTrace): EventTrace = EventTrace( this +: trace.events )
   def :>>(traceGen: TraceGen): TraceGen = AtomSeq(Path(EventTrace(Seq(this))) , traceGen)
}






abstract class TryEvent {
   def event(): UIEvent
   def trace(): EventTrace
   def isTrace(): Boolean
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

case class Assert(prop: Prop) extends UIEvent {
   override def toMsg(): pb.UIEvent = ProtoMsg.mkUIEvent( pb.Assert(prop.toMsg()) )
}

case class Qualifies(prop: Prop, trace:EventTrace) extends UIEvent {
   override def toMsg(): pb.UIEvent = ProtoMsg.mkUIEvent( pb.Qualifies(prop.toMsg(), trace.toMsg()) )
}

// http://www.mkyong.com/java/how-to-use-reflection-to-call-java-method-at-runtime/

