package edu.colorado.plv.chimp.generator

import edu.colorado.plv.chimp.combinator.EventTrace
import org.scalacheck.Gen

/**
  * Created by edmund on 4/18/17.
  */

// Monkey Combinator
// TODO: Not implemented yet

case class Monkey() extends TraceGen {
  override def generator(): Gen[EventTrace] = ???
}
