package edu.colorado.plv.chimp.generator

import edu.colorado.plv.chimp.combinator.{EventTrace, Skip}
import org.scalacheck.Gen
import org.scalacheck.Gen._

/**
  * Created by edmund on 4/18/17.
  */

// TODO: implement a basic model learning technique

case class KingKong() extends TraceGen {
  override def generator(): Gen[EventTrace] = const(EventTrace.trace(Skip))
}
