package edu.colorado.plv.chimp.generator

/**
  * Created by edmund on 2/5/17.
  */

import edu.colorado.plv.chimp.combinator._
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._

// {*, Alternative, Assert, Click, ClickHome, ClickMenu, Condition, Coord, Decide, DecideMany, EventTrace, Left, LongClick, Not, PullDownSettings, Rotate, Skip, Sleep, Swipe, Try, Type, UIEvent, UIID, isClickable}
import org.scalacheck.Gen
import org.scalacheck.Gen.const
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters

/*
  TraceGen = Path(Trace) | TraceGen |:| TraceGen | TraceGen |+| TraceGen | Optional TraceGen | Decide Condition TraceGen TraceGen | Choose TraceGen TraceGen
           | DecideMany [Alternative] | ChooseMany [TraceGen] | Repeat n TraceGen
           | Monkey | LearnModel
 */


object TraceGen {

  val defaultUserInterrupts: Seq[EventTrace] = Seq(
    EventTrace.trace(Rotate)
    // ClickHome :>> Resume,
    // ClickMenu :>> Resume,
    // PullDownSettings :>> Resume
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

case class QualifiesG(cond:Prop, gen:TraceGen) extends TraceGen {
  override def generator(): Gen[EventTrace] = for {
    tr <- gen.generator()
  } yield Qualifies(cond, tr) :>> Skip
}

// Derivable Generators

case class TypeG(idGen:Gen[UIID], strGen:Gen[String]) extends TraceGen {
  override def generator(): Gen[EventTrace] = const( EventTrace.trace( Type(idGen.sample.get, strGen.sample.get) ) )
}

case class SleepG(nGen:Gen[Int]) extends TraceGen {
  override def generator(): Gen[EventTrace] = const( EventTrace.trace( Sleep(nGen.sample.get) ) )
}



object TestGen {

  def main(args: Array[String]): Unit = {

    val R_id_list = 114

    // val traces: TraceGen = Click("login") :>> Click(*) *>> Type("userbox","test") *>> Type("pwdbox","1234") *>> Click("Go") *>> Click(* onChild 1) *>>
    //                       Swipe("nuts",Left) *>> Swipe("crap",Coord(1,2)) *>> (Click("button1") <+> Click("button2")) *>>
    //                        Assert( Not(isClickable("crap")) )

    val traces: TraceGen = isDisplayed(345) Then (Click( R_id_list /\ "happy" ) :>> Skip )


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