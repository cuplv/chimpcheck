package edu.colorado.plv.chimp.stub

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.{Assert, Rotate, Type, _}
import edu.colorado.plv.chimp.combinator.Implicits._
import edu.colorado.plv.chimp.generator.Implicits._
import edu.colorado.plv.chimp.coordinator.{ChimpLoader, CrashChimpOutcome, SuccChimpOutcome}
import edu.colorado.plv.chimp.generator.Gorilla
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext
import

object StubGenerator extends App {
  implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

  val test = "10"

  // To support Click(R.id._) please import the R class file
  val traceGen = Sleep(1000):>> Click("Begin") :>>
    Type("username", "test") :>> Type("password", "test") :>>
    Click("Login") :>> Click("Countdowntimer Testing") :>>
    Click("5 seconds") :>> Sleep(5000):>> ClickBack

  val samples: List[EventTrace] =
    List.fill(20)(traceGen.generator().sample.get)

  val output: List[(String, String)] =
    List.fill(20)(traceGen.generator().sample.get).map(
      tr => (tr.toString(), tr.toBase64())
    )

  output.foreach ( res => {
    res match {
      case (traceStr, trace64) =>
        println(s"${traceStr} => ${trace64}")
    }
  })
}
