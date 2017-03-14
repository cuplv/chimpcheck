package edu.colorado.plv.chimp.coordinator

import edu.colorado.plv.chimp.combinator.EventTrace

/**
  * Created by edmund on 3/14/17.
  */

case class ChimpTraceResults(completedTrace:EventTrace, success:Boolean) {
  override def toString: String = s"<$completedTrace,$success>"
}
