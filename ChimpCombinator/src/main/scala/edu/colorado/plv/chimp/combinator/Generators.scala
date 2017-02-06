package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.{protobuf => pb}
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen
import Gen.const

/*
  TraceGen = Path(Trace) | TraceGen |:| TraceGen | TraceGen |+| TraceGen | Optional(TraceGen) | Choose [TraceGen]
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
case class Choose(gens: Seq[TraceGen]) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     n <- Gen.choose(0,gens.length)
     tr <- gens(n).generator()
   } yield tr
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
     def |+| (event: UIEvent): TraceGen    = Path(EventTrace.trace(event)) |+| event
     def |+| (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) |+| trace
     def |+| (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) |+| gen
  }

  implicit  class EventTraceGen(trace: EventTrace) {
    def |+| (event: UIEvent): TraceGen    = Path(trace) |+| event
    def |+| (trace: EventTrace): TraceGen = Path(trace) |+| trace
    def |+| (gen: TraceGen): TraceGen     = Path(trace) |+| gen
  }

}


import edu.colorado.plv.chimp.combinator.implicits.UIEventGen

object TestGen {

  def main(args: Array[String]): Unit = {

    val traces: TraceGen = ClickName("login") |+| TypeInName("userbox","test") |+| TypeInName("pwdbox","1234") |+| ClickName("Go")

    val prop = forAll (traces.generator()) {
      tr => {
        println(tr)
        true
      }
    }

    prop.check

  }

}