package edu.colorado.plv.chimp.generator

import edu.colorado.plv.chimp.combinator._
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import org.scalacheck.Gen
import org.scalacheck.Gen._

/**
  * Created by edmund on 4/18/17.
  */


// Gorilla Combinator

case class GorillaConfig(steps:Int, freqs:Seq[(Int,TraceGen)]) {
}

object Gorilla extends TraceGen {

  val defaultGorillaEvents:Seq[(Int,TraceGen)] = Seq(
    (40,Path(EventTrace.trace(Click(*)))),
    (15,Path(EventTrace.trace(LongClick(*)))),
    (8,TypeG(const(*), Gen.alphaStr)),
    (20,Path(EventTrace.trace(Rotate))),
    (10,Path(ClickBack :>> Resume)),
    (1,SleepG(Gen.choose(500,2000)))
  ).map( (f:(Int,TraceGen)) => (f._1, TryG(f._2) ) )

  val defaultGorillaConfig:GorillaConfig = GorillaConfig(50, defaultGorillaEvents)

  override def generator(): Gen[EventTrace] = Gorilla(defaultGorillaConfig).generator()

}

case class Gorilla(gorillaConfig: GorillaConfig) extends TraceGen {
  override def generator(): Gen[EventTrace] = {
    Repeat(gorillaConfig.steps, ChooseWithFreq(gorillaConfig.freqs)).generator()
  }
}


case class SubservientGorilla(gen: TraceGen) (implicit gorillaConfig: GorillaConfig) extends TraceGen {
  override def generator(): Gen[EventTrace] = {
    Repeat(gorillaConfig.steps, gen :>> ChooseWithFreq(gorillaConfig.freqs)).generator()
  }
}