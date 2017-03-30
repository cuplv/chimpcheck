package edu.colorado.plv.chimp.combinator

import chimp.protobuf.BaseProp
import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.combinator.PropArg_Implicits.{IntArg, StrArg}

/**
  * Created by edmund on 3/29/17.
  */

abstract class ViewID {
  def toArg: PropArg
}

object ViewID_Implicits {

  implicit class ViewNameID(name: String) extends ViewID {
     def toArg(): PropArg = new StrArg(name)
     override def toString: String = name
  }

  implicit class ViewRID(rid: Int) extends ViewID {
     def toArg(): PropArg = new IntArg(rid)
     override def toString: String = s"$rid"
  }

}

case class isClickable(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isClickable",vID.toArg).toMsg()
}

case class isEnabled(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isEnabled", vID.toArg).toMsg()
}

case class supportInputMethods(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("supportInputMethods", vID.toArg).toMsg()
}

object dummy extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("dummy", Seq():_*).toMsg();
}