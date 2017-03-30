package edu.colorado.plv.chimp.combinator

/**
  * Created by edmund on 2/5/17.
  */

import chimp.{protobuf => pb}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Test}
import Gen.const
import Test.Parameters

import TryEvent_Implicits._

/*
  TraceGen = Path(Trace) | TraceGen |:| TraceGen | TraceGen |+| TraceGen | Optional TraceGen | Decide Condition TraceGen TraceGen | Choose TraceGen TraceGen
           | DecideMany [Alternative] | ChooseMany [TraceGen] | Repeat n TraceGen
           | Monkey | LearnModel
 */


object TraceGen {

  val defaultUserInterrupts: Seq[EventTrace] = Seq(
    EventTrace.trace(Rotate),
    ClickHome :>> Resume,
    ClickMenu :>> Resume,
    PullDownSettings :>> Resume
  )

  def pick(n:Int): Gen[EventTrace] =
    if (n > 0) {
      for {
        event <- Gen.oneOf(defaultUserInterrupts)
        events <- pick(n - 1)
      } yield event :>> events
    } else {
      const(EventTrace.trace(Skip))
    }
}

abstract class TraceGen {
   def :>> (event: UIEvent): TraceGen    = AtomSeq(this, Path(EventTrace.trace(event)))
   def :>> (trace: EventTrace): TraceGen = AtomSeq(this, Path(trace))
   def :>> (gen: TraceGen): TraceGen     = AtomSeq(this, gen)
   def *>> (event: UIEvent): TraceGen    = InterSeq(this, Path(EventTrace.trace(event)))
   def *>> (trace:EventTrace): TraceGen  = InterSeq(this, Path(trace))
   def *>> (gen: TraceGen): TraceGen     = InterSeq(this, gen)
   def <+> (event: UIEvent): TraceGen    = Choose(this, Path(EventTrace.trace(event)))
   def <+> (trace: EventTrace): TraceGen = Choose(this, Path(trace))
   def <+> (gen: TraceGen): TraceGen     = Choose(this, gen)

   def generator(): Gen[EventTrace]
}

case class Path(trace: EventTrace) extends TraceGen {
   override def generator(): Gen[EventTrace] = const(trace)
}
case class AtomSeq(gen1: TraceGen, gen2: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen1.generator()
     tr2 <- gen2.generator()
   } yield (tr1 :>> tr2)
}
case class InterSeq(gen1: TraceGen, gen2: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen1.generator()
     tr2 <- gen2.generator()
     n <- Gen.frequency( (1,0), (4,1), (2,2), (1,3) )
     itrs <- TraceGen.pick(n)
   } yield (tr1 :>> itrs :>> tr2)
}
case class Optional(gen: TraceGen) extends TraceGen {
   override def generator(): Gen[EventTrace] = for {
     tr1 <- gen.generator()
     tr2 <- Gen.frequency( (1,EventTrace.trace(Skip)), (2,tr1) )
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
case class ChooseWithFreq(gens: Seq[(Int,TraceGen)]) extends TraceGen {
  override def generator(): Gen[EventTrace] = {
    val freqs = gens.map( (freq:(Int,TraceGen)) => (freq._1,freq._2.generator()) )
    for {
      tr <- Gen.frequency[EventTrace]( freqs:_* )
    } yield tr
  }
}

case class TryG(gen: TraceGen) extends TraceGen {
  override def generator(): Gen[EventTrace] = for {
    tr <- gen.generator()
  } yield EventTrace.trace( Try(tr) )
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

case class Repeat(n:Int, gen:TraceGen) extends TraceGen {
  override def generator(): Gen[EventTrace] =
    if (n > 0) {
      for {
        tr1 <- gen.generator()
        tr2 <- Repeat(n-1, gen).generator()
      } yield tr1 :>> tr2
    } else {
      const(EventTrace.trace(Skip))
    }
}

// Derivable Generators

case class TypeG(idGen:Gen[UIID], strGen:Gen[String]) extends TraceGen {
  override def generator(): Gen[EventTrace] = const( EventTrace.trace( Type(idGen.sample.get, strGen.sample.get) ) )
}

case class SleepG(nGen:Gen[Int]) extends TraceGen {
  override def generator(): Gen[EventTrace] = const( EventTrace.trace( Sleep(nGen.sample.get) ) )
}


// Gorilla Combinator

case class GorillaConfig(steps:Int, freqs:Seq[(Int,TraceGen)]) {
}

object Gorilla extends TraceGen {

  val defaultGorillaEvents:Seq[(Int,TraceGen)] = Seq(
    (20,Path(EventTrace.trace(Click(*)))),
    (5,Path(EventTrace.trace(LongClick(*)))),
    (5,TypeG(const(*), Arbitrary.arbitrary[String])),
    (1,SleepG(Gen.choose(2000,5000)))
  ).map( (f:(Int,TraceGen)) => (f._1, TryG(f._2) ) )

  val defaultGorillaConfig:GorillaConfig = GorillaConfig(50, defaultGorillaEvents)

  override def generator(): Gen[EventTrace] = Gorilla(defaultGorillaConfig).generator()

}

case class Gorilla(gorillaConfig: GorillaConfig) extends TraceGen {
  override def generator(): Gen[EventTrace] = {
    Repeat(gorillaConfig.steps, ChooseWithFreq(gorillaConfig.freqs)).generator()
  }
}

// Monkey Combinator

case class Monkey() extends TraceGen {
   override def generator(): Gen[EventTrace] = ???
}
case class LearnModel() extends TraceGen {
   // TODO
   override def generator(): Gen[EventTrace] = const(EventTrace.trace(Skip))
}

object Generator_Implicits {

  implicit class UIEventGen(event: UIEvent) {
     def *>> (next: UIEvent): TraceGen     = Path(EventTrace.trace(event)) *>> next
     def *>> (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) *>> trace
     def *>> (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) *>> gen
     def <+> (next: UIEvent): TraceGen     = Path(EventTrace.trace(event)) <+> next
     def <+> (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) <+> trace
     def <+> (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) <+> gen
  }

  implicit  class EventTraceGen(trace: EventTrace) {
    def *>>(event: UIEvent): TraceGen   = Path(trace) *>> event
    def *>>(next: EventTrace): TraceGen = Path(trace) *>> next
    def *>>(gen: TraceGen): TraceGen    = Path(trace) *>> gen
    def <+> (event: UIEvent): TraceGen   = Path(trace) <+> event
    def <+> (next: EventTrace): TraceGen = Path(trace) <+> next
    def <+> (gen: TraceGen): TraceGen    = Path(trace) <+> gen
  }

}


import edu.colorado.plv.chimp.combinator.Generator_Implicits._

import UIID_Implicits._
import Orient_Implicits._

object TestGen {

  def main(args: Array[String]): Unit = {

    val traces: TraceGen = Click("login") :>> Click(*) *>> Type("userbox","test") *>> Type("pwdbox","1234") *>> Click("Go") *>>
                           Swipe("nuts",Left) *>> Swipe("crap",Coord(1,2)) *>> (Click("button1") <+> Click("button2"))


    val myParam = Parameters.default.withMinSuccessfulTests(10)

    forAll(traces.generator()) {
      tr => {
        println(" ============================================================================= ")
        println("Generated: " + tr.toString())
        val b64 = tr.toBase64()
        println("Base64 Encoded ProtoBuf: " + b64)
        val trB = EventTrace.fromBase64(b64)
        println("Recovered: " + trB.toString())
        println(" ============================================================================= ")
        true
      }
    }.check(myParam)


  }

}