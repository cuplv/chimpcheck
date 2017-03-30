package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 3/29/17.
  */

import chimp.{protobuf => pb}

object UIID_Implicits {

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

}

object Orient_Implicits {

  implicit class XYOrient(xy : Coord) extends Orientation {
    override def toMsg(): pb.Orientation = pb.Orientation( pb.Orientation.OrientType.XY_TYPE, Some(xy.toMsg()) )
    override def toString: String = xy.toString
  }

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

// Property Implicits

object PropArg_Implicits {

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

}

object Prop_Implicits {

  implicit class LitProp(b: BaseProp) extends Prop {
    override def toMsg(): pb.Prop = {
      pb.Prop(pb.Prop.PropType.LIT_TYPE, Some(b.toMsg()))
    }
    override def toString: String = b.toString
  }

}