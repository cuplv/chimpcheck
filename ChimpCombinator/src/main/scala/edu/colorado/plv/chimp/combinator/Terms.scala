package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 4/17/17.
  */

import chimp.{protobuf => pb}

import Implicits._

abstract class UIID extends ProtoMsg[pb.UIID] {
  def onChild(child: ChildIdx): UIID = ChildAtPosition(this,child)
  def /\ (other: UIID): UIID = UIIDConjunct(this,other)
}


abstract class WildCard

object * extends WildCard {
  // override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.WILD_CARD, None, None)
  override def toString(): String = "*"
}

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
      case pb.UIID.UIIDType.ONCHILD_ID => {
        val parent = UIID.fromProto( pd_uiid.getParentId )
        val child  = ChildIdx.fromProto( pd_uiid.getChildIdx )
        ChildAtPosition(parent, child)
      }
      case pb.UIID.UIIDType.CONJUNCT_ID => {
        val uiid1 = UIID.fromProto( pd_uiid.getUiid1 )
        val uiid2 = UIID.fromProto( pd_uiid.getUiid2 )
        UIIDConjunct(uiid1, uiid2)
      }
    }
  }
}

object ChildIdx {
   def fromProto(pd_childIdx: pb.ChildIdx): ChildIdx = {
     pd_childIdx.idxType match {
       case pb.ChildIdx.IdxType.INT => pd_childIdx.getInt
       case pb.ChildIdx.IdxType.WILD_CARD => *
     }
   }
}

abstract class ChildIdx {
   def toMsg(): pb.ChildIdx
}

case class ChildAtPosition(parent: UIID, child: ChildIdx) extends UIID {
  override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.ONCHILD_ID, None, None, None, Some(parent.toMsg), Some(child.toMsg))
  override def toString: String = s"$parent onChild $child"
}

case class UIIDConjunct(uiid1: UIID, uiid2: UIID) extends UIID {
  override def toMsg(): pb.UIID = pb.UIID(pb.UIID.UIIDType.CONJUNCT_ID, None, None, None, None, None, None, Some(uiid1.toMsg), Some(uiid2.toMsg))
  override def toString: String = s"$uiid1 /\\ $uiid2"
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

object Orientation {
  def fromProto(pb_orientation: pb.Orientation): Orientation =
    pb_orientation.orientType match {
      case pb.Orientation.OrientType.XY_TYPE => Coord(pb_orientation.getXy.x, pb_orientation.getXy.y)
      case pb.Orientation.OrientType.LEFT => Left
      case pb.Orientation.OrientType.RIGHT => Right
      case pb.Orientation.OrientType.UP => Up
      case pb.Orientation.OrientType.DOWN => Down
      case pb.Orientation.OrientType.WILD_CARD => *
    }
}