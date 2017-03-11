package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.{protobuf => pb}
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import Gen.{const}

import org.scalacheck.Test
import Test.{Parameters}

/*
  TraceGen = Path(Trace) | TraceGen |:| TraceGen | TraceGen |+| TraceGen | Optional TraceGen | Decide Condition TraceGen TraceGen | Choose TraceGen TraceGen
           | DecideMany [Alternative] | ChooseMany [TraceGen]
           | Monkey | LearnModel
 */

object TraceGen {

  val defaultUserInterrupts: Seq[EventTrace] = Seq(
    EventTrace.trace(RotateLeft()),
    EventTrace.trace(RotateRight()),
    RotateLeft() |:| RotateRight(),
    ClickHome() |:| ReturnToApp(),
    ClickMenu() |:| ReturnToApp(),
    PullDownSettings() |:| ReturnToApp()
  )

  def pick(n:Int): Gen[EventTrace] =
    if (n > 0) {
      for {
        event <- Gen.oneOf(defaultUserInterrupts)
        events <- pick(n - 1)
      } yield event |:| events
    } else {
      const(EventTrace.trace(Skip()))
    }
}

abstract class TraceGen {
   def |:| (event: UIEvent): TraceGen    = AtomSeq(this, Path(EventTrace.trace(event)))
   def |:| (trace: EventTrace): TraceGen = AtomSeq(this, Path(trace))
   def |:| (gen: TraceGen): TraceGen     = AtomSeq(this, gen)
   def |+| (event: UIEvent): TraceGen   = InterSeq(this, Path(EventTrace.trace(event)))
   def |+| (trace:EventTrace): TraceGen = InterSeq(this, Path(trace))
   def |+| (gen: TraceGen): TraceGen    = InterSeq(this, gen)

   def generator(): Gen[EventTrace]
}

case class Path(trace: EventTrace) extends TraceGen {
   override def generator(): Gen[EventTrace] = const(trace)
}
case class AtomSeq(gen1: TraceGen, gen2: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen1.generator()
     tr2 <- gen2.generator()
   } yield (tr1 |:| tr2)
}
case class InterSeq(gen1: TraceGen, gen2: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen1.generator()
     tr2 <- gen2.generator()
     n <- Gen.frequency( (1,0), (4,1), (2,2), (1,3) )
     itrs <- TraceGen.pick(n)
   } yield (tr1 |:| itrs |:| tr2)
}
case class Optional(gen: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen.generator()
     tr2 <- Gen.frequency( (1,EventTrace.trace(Skip())), (2,tr1) )
   } yield tr2
}
case class DecideG(cond: Condition, succ:TraceGen, fail: TraceGen) extends TraceGen {
  override def generator(): Gen[EventTrace] = for {
    succTr <- succ.generator()
    failTr <- fail.generator()
  } yield EventTrace.trace( Decide(cond, succTr, failTr) )
}
case class Choose(gen1: TraceGen, gen2: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen1.generator()
     tr2 <- gen2.generator()
     tr <- Gen.oneOf(Seq(tr1,tr2))
   } yield tr
}
case class ChooseMany(gens: Seq[TraceGen]) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     n <- Gen.choose(0,gens.length-1)
     tr <- gens(n).generator()
   } yield tr
}

case class AlternativeG(condition: Condition, gen: TraceGen) {
   def generator(): Gen[Alternative] = for {
      tr <- gen.generator()
   } yield Alternative(condition, tr)
}
case class DecideGMany(alternatives: AlternativeG*) extends TraceGen {
  override def generator(): Gen[EventTrace] = {
    if (alternatives.length > 1) {
       alternatives.head match {
         case AlternativeG(cond, gen) => for {
           tr <- gen.generator()
           EventTrace(trs) <- DecideGMany( alternatives.tail:_* ).generator()
         } yield {
           val decideMany = trs.head.asInstanceOf[DecideMany]
           EventTrace.trace( decideMany.add(Alternative(cond,tr)) )
         }
       }
    } else {
      alternatives.head match {
        case AlternativeG(cond, gen) => for {
           tr <- gen.generator()
        } yield EventTrace.trace( DecideMany( Alternative(cond,tr) ) )
      }
    }
  }
}


case class Monkey() extends TraceGen {
   // TODO
   override def generator(): Gen[EventTrace] = const(EventTrace.trace(Skip()))
}
case class LearnModel() extends TraceGen {
   // TODO
   override def generator(): Gen[EventTrace] = const(EventTrace.trace(Skip()))
}

object implicits {

  implicit class UIEventGen(event: UIEvent) {
     def |+| (next: UIEvent): TraceGen    = Path(EventTrace.trace(event)) |+| next
     def |+| (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) |+| trace
     def |+| (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) |+| gen
  }

  implicit  class EventTraceGen(trace: EventTrace) {
    def |+| (event: UIEvent): TraceGen    = Path(trace) |+| event
    def |+| (next: EventTrace): TraceGen = Path(trace) |+| next
    def |+| (gen: TraceGen): TraceGen     = Path(trace) |+| gen
  }

}


import edu.colorado.plv.chimp.combinator.implicits.UIEventGen

import UIID_Implicits._

object TestGen {

  def main(args: Array[String]): Unit = {

    val traces: TraceGen = Click("login") |+| Type("userbox","test") |+| Type("pwdbox","1234") |+| Click("Go")

    val prop = forAll (traces.generator()) {
      tr => {
        println(" ============================================================================= ")
        println("Generated: " + tr.toString())
        val b64 = tr.toBase64()
        println("Base64 Encoded ProtoBuf: " + b64)
        val trB = EventTrace.fromBase64( b64 )
        println("Recovered: " + trB.toString())
        println(" ============================================================================= ")
        true
      }
    }

    prop.check

  }

}