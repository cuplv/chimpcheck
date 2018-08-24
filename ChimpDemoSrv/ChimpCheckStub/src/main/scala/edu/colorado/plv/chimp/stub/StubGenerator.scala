package edu.colorado.plv.chimp.stub

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator._
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import edu.colorado.plv.chimp.coordinator.{ChimpLoader, CrashChimpOutcome, SuccChimpOutcome}
import edu.colorado.plv.chimp.generator.Gorilla
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import edu.colorado.plv.chimp.generator._
import de.d120.ophasekistenstapeln.R


object StubGenerator extends App {
  implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

  val test = "10"

  // To support Click(R.id) please import the R class file
  //If we land on the "Turm" screen, then Click(*) won't work, so we need to go back to the previous screen.
  val checkTurm = Try((isDisplayed("Turm") Then ClickBack:>>Skip).generator.sample.get)
  //This clicks randomly 500 times, unless it gets to the Turm screen, where it goes back a screen.
  val traceGen = Repeat(500, Click(*) :>> checkTurm) :>> Skip

  val samples: List[EventTrace] =
    List.fill(1)(traceGen.generator().sample.get)

  val output: List[(String, String)] =
    List.fill(1)(traceGen.generator().sample.get).map(
      tr => (tr.toString(), tr.toBase64())
    )

  output.foreach ( res => {
    res match {
      case (traceStr, trace64) =>
        println(s"${trace64}")
    }
  })
}
