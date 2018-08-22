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

object StubGenerator extends App {
  implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

  val test = "10"

  // To support Click(R.id) please import the R class file
  val traceGen =
    Sleep(1000) :>> Click("Begin") :>> Type("username","test") :>> Type("password","test") :>> Click("Login") :>> Click("Countdowntimer Testing") :>> Click("10 seconds") :>> Sleep(10000) :>> Click("5 seconds") :>> ClickBack :>> Sleep(5000)
  
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
