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
import com.owncloud.android.R


object StubGenerator extends App {
  implicit val logger = Logger(LoggerFactory.getLogger("chimp-tester"))

  val test = "10"

  // To support Click(R.id) please import the R class file
  val traceGen = {
  Click(R.id.skip) :>> Type(R.id.hostUrlInput, "ncloud.zaclys.com"):>> Type(R.id.account_username, "22203"):>> Type(R.id.account_password, "12321qweqaz!") :>> Click(R.id.buttonOK) :>> (isDisplayed("Allow") Then Click("Allow"):>> Sleep(1000)) :>> LongClick("Documents") :>> ClickMenu :>> Click("Move") :>> Rotate
  }

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
