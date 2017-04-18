package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 4/17/17.
  */

import chimp.{protobuf => pb}
import edu.colorado.plv.chimp.utils.Base64

import edu.colorado.plv.chimp.combinator.Implicits._

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
      case pb.BaseProp.BasePropType.TOP_TYPE => new BasePropUnit(true)
      case pb.BaseProp.BasePropType.BOT_TYPE => new BasePropUnit(false)
      case pb.BaseProp.BasePropType.NEG_TYPE => Not( BaseProp.fromProto(pbBaseProp.getProp1) )
    }
  }
}

abstract class BaseProp extends ProtoMsg[pb.BaseProp] {
  def /\ (baseProp: BaseProp): BaseProp = BaseConjunct(this, baseProp)
  def \/ (baseProp: BaseProp): BaseProp = BaseDisjunct(this, baseProp)
  def /\ (prop: Prop): Prop = Conjunct(this, prop)
  def \/ (prop: Prop): Prop = Disjunct(this, prop)
  def ==> (baseProp: BaseProp): Prop = ImpProp(this, baseProp)
  def unary_!(): BaseProp = Not(this)
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

case class Not(b: BaseProp) extends BaseProp {
  override def toMsg(): pb.BaseProp = {
    pb.BaseProp(pb.BaseProp.BasePropType.NEG_TYPE, None, Some(b.toMsg()))
  }
  override def toString: String = s"!$b"
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

  def fromBase64(b64: String): Prop = {
    fromProto( pb.Prop.parseFrom( Base64.decode(b64) ) )
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


