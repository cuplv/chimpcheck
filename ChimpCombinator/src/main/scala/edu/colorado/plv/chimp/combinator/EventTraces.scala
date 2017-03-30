package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.protobuf.Orientation.OrientType.{DOWN, LEFT, RIGHT, UP, XY_TYPE}
import chimp.{protobuf => pb}
import Prop_Implicits.LitProp
import TryEvent_Implicits.{TryAppEvent, TryExtEvent, TryTrace}
import edu.colorado.plv.chimp.combinator.PropArg_Implicits.{BoolArg, IntArg, StrArg}
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

abstract class UIID extends ProtoMsg[pb.UIID]



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
         case pb.UIID.UIIDType.XY_ID => {
            val pbXY = pd_uiid.getXyid
            Coord(pbXY.x, pbXY.y)
         }
      }
   }
}


object Coord {
   def fromProto(pb_coord: pb.XYCoordin): Coord = Coord (pb_coord.x, pb_coord.y)
}
case class Coord(x:Int,y:Int) extends ProtoMsg[pb.XYCoordin] {
   override def toMsg(): pb.XYCoordin = pb.XYCoordin(x,y)
   override def toString: String = s"<$x,$y>"
}


abstract class Orientation extends ProtoMsg[pb.Orientation]


object Left extends Orientation {
   override def toMsg(): pb.Orientation = pb.Orientation(pb.Orientation.OrientType.LEFT, None)
   override def toString: String = "Left"
}
object Right extends Orientation {
   override def toMsg(): pb.Orientation = pb.Orientation(pb.Orientation.OrientType.RIGHT, None)
   override def toString: String = "Right"
}
object Up extends Orientation {
   override def toMsg(): pb.Orientation = pb.Orientation(pb.Orientation.OrientType.UP, None)
   override def toString: String = "Up"
}
object Down extends Orientation {
   override def toMsg(): pb.Orientation = pb.Orientation(pb.Orientation.OrientType.DOWN, None)
   override def toString: String = "Down"
}

import Orient_Implicits._

object Orientation {
   def fromProto(pb_orientation: pb.Orientation): Orientation =
      pb_orientation.orientType match {
         case XY_TYPE => Coord(pb_orientation.getXy.x, pb_orientation.getXy.y)
         case LEFT => Left
         case RIGHT => Right
         case UP => Up
         case DOWN => Down
      }
}


object AppEvent {
   def fromProto(appevent: pb.AppEvent): AppEvent = {
      appevent.eventType match {
         case pb.AppEvent.AppEventType.CLICK     => Click( UIID.fromProto(appevent.getClick.uiid) )
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

// http://www.mkyong.com/java/how-to-use-reflection-to-call-java-method-at-runtime/

// Properties

object PropArg {
   def fromProto(arg: pb.Arg): PropArg = {
      arg.argType match {
         case pb.Arg.ArgType.STR_ARG  => new StrArg(arg.getStrVal)
         case pb.Arg.ArgType.INT_ARG  => new IntArg(arg.getIntVal)
         case pb.Arg.ArgType.BOOL_ARG => new BoolArg(arg.getBoolVal)
      }
   }
}

abstract class PropArg extends ProtoMsg[pb.Arg] {
   val argType: PropArgType

   abstract class PropArgType
   object StrArgType extends PropArgType
   object IntArgType extends PropArgType
   object BoolArgType extends PropArgType
}



object BaseProp {
   def fromProto(pbBaseProp: pb.BaseProp): BaseProp = {
      pbBaseProp.propType match {
         case pb.BaseProp.BasePropType.PRIM_TYPE => {
            val pred = pbBaseProp.getPred
            Predicate(pred.name, pred.args.map(PropArg.fromProto(_)):_* )
         }
         case pb.BaseProp.BasePropType.CONJ_BASE_TYPE => {
            BaseConjunct( BaseProp.fromProto(pbBaseProp.getProp1), BaseProp.fromProto(pbBaseProp.getProp2) )
         }
         case pb.BaseProp.BasePropType.DISJ_BASE_TYPE => {
            BaseDisjunct( BaseProp.fromProto(pbBaseProp.getProp1), BaseProp.fromProto(pbBaseProp.getProp2) )
         }
      }
   }
}

abstract class BaseProp extends ProtoMsg[pb.BaseProp] {
   def /\ (baseProp: BaseProp): BaseProp = BaseConjunct(this, baseProp)
   def \/ (baseProp: BaseProp): BaseProp = BaseDisjunct(this, baseProp)
   def /\ (prop: Prop): Prop = Conjunct(this, prop)
   def \/ (prop: Prop): Prop = Disjunct(this, prop)
   def ==> (baseProp: BaseProp): Prop = ImpProp(this, baseProp)
}

case class Predicate(name:String, args: PropArg*) extends BaseProp {
   override def toMsg(): pb.BaseProp = {
      val pbArgs = args.map( _.toMsg() )
      val pbPred = pb.Pred(name,pbArgs)
      pb.BaseProp(pb.BaseProp.BasePropType.PRIM_TYPE, Some(pbPred))
   }
   override def toString: String = s"$name(${args.mkString(",")})"
}

case class BaseConjunct(b1: BaseProp, b2: BaseProp) extends BaseProp {
   override def toMsg(): pb.BaseProp = {
      pb.BaseProp(pb.BaseProp.BasePropType.CONJ_BASE_TYPE, None, Some(b1.toMsg()), Some(b2.toMsg()))
   }
   override def toString: String = s"$b1 /\\ $b2"
}

case class BaseDisjunct(b1: BaseProp, b2: BaseProp) extends BaseProp {
   override def toMsg(): pb.BaseProp = {
      pb.BaseProp(pb.BaseProp.BasePropType.DISJ_BASE_TYPE, None, Some(b1.toMsg()), Some(b2.toMsg()))
   }
   override def toString: String = s"$b1 \\/ $b2"
}

object Prop {

   def fromProto(pbProp: pb.Prop): Prop = {
      pbProp.propType match {
         case pb.Prop.PropType.LIT_TYPE => new LitProp( BaseProp.fromProto(pbProp.getPrem) )
         case pb.Prop.PropType.IMP_TYPE => {
            ImpProp( BaseProp.fromProto(pbProp.getPrem), BaseProp.fromProto(pbProp.getConc) )
         }
         case pb.Prop.PropType.CONJ_TYPE => {
            Conjunct( Prop.fromProto(pbProp.getProp1), Prop.fromProto(pbProp.getProp2) )
         }
         case pb.Prop.PropType.DISJ_TYPE => {
            Disjunct( Prop.fromProto(pbProp.getProp1), Prop.fromProto(pbProp.getProp2) )
         }
      }
   }

}

abstract class Prop extends ProtoMsg[pb.Prop] {
   def /\ (prop: Prop): Prop = Conjunct(this, prop)
   def \/ (prop: Prop): Prop = Disjunct(this, prop)
}



case class ImpProp(prem: BaseProp, conc: BaseProp) extends Prop {
   override def toMsg(): pb.Prop = {
      pb.Prop(pb.Prop.PropType.IMP_TYPE, Some(prem.toMsg()), Some(conc.toMsg()))
   }
   override def toString: String = s"$prem ==> $conc"
}

case class Conjunct(p1:Prop, p2:Prop) extends Prop {
   override def toMsg(): pb.Prop = {
      pb.Prop(pb.Prop.PropType.CONJ_TYPE, None, None, Some(p1.toMsg()), Some(p2.toMsg()))
   }
   override def toString: String = s"$p1 /\\ $p2"
}

case class Disjunct(p1:Prop, p2:Prop) extends Prop {
   override def toMsg(): pb.Prop = {
      pb.Prop(pb.Prop.PropType.DISJ_TYPE, None, None, Some(p1.toMsg()), Some(p2.toMsg()))
   }
   override def toString: String = s"$p1 \\/ $p2"
}

