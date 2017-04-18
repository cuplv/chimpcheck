package edu.colorado.plv.chimp.combinator

import chimp.{protobuf => pb}

/**
  * Created by edmund on 3/29/17.
  */

abstract class ViewID {
  def toArg: PropArg
}

case class isClickable(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isClickable",vID.toArg).toMsg()
}

case class isDisplayed(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isDisplayed",vID.toArg).toMsg()
}

case class isEnabled(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isEnabled", vID.toArg).toMsg()
}

case class supportsInputMethods(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("supportsInputMethods", vID.toArg).toMsg()
}

case class isSelected(vID: ViewID) extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("isSelected",vID.toArg).toMsg()
}

object dummy extends BaseProp {
  override def toMsg(): pb.BaseProp = Predicate("dummy", Seq():_*).toMsg()
}