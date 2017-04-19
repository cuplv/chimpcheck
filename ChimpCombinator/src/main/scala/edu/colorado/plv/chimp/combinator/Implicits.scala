package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 3/29/17.
  */

import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.generator.{Path, TraceGen}

object Implicits {

  // UIID Implicits

  implicit class RId(rid: Int) extends UIID {
    override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.R_ID, Some(rid), None)
    override def toString(): String = s"$rid"
  }

  implicit class Name(name: String) extends UIID {
    override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.NAME_ID, None, Some(name))
    override def toString(): String = name
  }

  implicit class XY(xy: Coord) extends UIID {
    override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.XY_ID, None, None, Some(xy.toMsg()))
    override def toString: String = s"<${xy.x},${xy.y}>"
  }

  implicit class WildUIID(wc: WildCard) extends UIID {
    override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.WILD_CARD, None, None)
    override def toString: String = "*"
  }

  // ChildIdx Implicits

  implicit class CRId(rid: Int) extends ChildIdx {
    override def toMsg(): pb.ChildIdx = pb.ChildIdx(pb.ChildIdx.IdxType.INT,Some(rid))
    override def toString: String = s"$rid"
  }

  implicit class CWildCard(wc: WildCard) extends ChildIdx {
    override def toMsg(): pb.ChildIdx = pb.ChildIdx(pb.ChildIdx.IdxType.WILD_CARD)
    override def toString: String = "*"
  }

  // Orient Implicits

  implicit class XYOrient(xy : Coord) extends Orientation {
    override def toMsg(): pb.Orientation = pb.Orientation( pb.Orientation.OrientType.XY_TYPE, Some(xy.toMsg()) )
    override def toString: String = xy.toString
  }

  implicit class OWildCard(wc: WildCard) extends Orientation {
    override def toMsg(): pb.Orientation = pb.Orientation( pb.Orientation.OrientType.WILD_CARD )
    override def toString: String = "*"
  }

  // TryEvent Implicits

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

  // Property Argument Implicits

  implicit class StrArg(str:String) extends PropArg {
    override val argType = StrArgType
    override def toMsg(): pb.Arg = {
      pb.Arg(pb.Arg.ArgType.STR_ARG, None, Some(str))
    }
    override def toString: String = str
  }

  implicit class IntArg(i:Int) extends PropArg {
    override val argType = IntArgType
    override def toMsg(): pb.Arg = {
      pb.Arg(pb.Arg.ArgType.INT_ARG, Some(i))
    }
    override def toString: String = s"$i"
  }

  implicit class BoolArg(b:Boolean) extends PropArg {
    override val argType = BoolArgType
    override def toMsg(): pb.Arg = {
      pb.Arg(pb.Arg.ArgType.BOOL_ARG, None, None, Some(b))
    }
    override def toString: String = s"$b"
  }

  // Base Property Implicits

  implicit class BasePropUnit(b: Boolean) extends BaseProp {
    override def toMsg(): pb.BaseProp = {
      if (b) pb.BaseProp(pb.BaseProp.BasePropType.TOP_TYPE)
      else pb.BaseProp(pb.BaseProp.BasePropType.BOT_TYPE)
    }
    override def toString: String = s"$b"
  }

  // Property Implicits

  implicit class LitProp(b: BaseProp) extends Prop {
    override def toMsg(): pb.Prop = {
      pb.Prop(pb.Prop.PropType.LIT_TYPE, Some(b.toMsg()))
    }
    override def toString: String = b.toString
  }

  implicit class UnitProp(b: Boolean) extends Prop {
    override def toMsg(): pb.Prop = {
      (new LitProp(new BasePropUnit(b))).toMsg()
    }
    override def toString: String = s"$b"
  }

  // ViewID Implicits

  implicit class ViewNameID(name: String) extends ViewID {
    def toArg(): PropArg = new StrArg(name)
    override def toString: String = name
  }

  implicit class ViewRID(rid: Int) extends ViewID {
    def toArg(): PropArg = new IntArg(rid)
    override def toString: String = s"$rid"
  }

}

