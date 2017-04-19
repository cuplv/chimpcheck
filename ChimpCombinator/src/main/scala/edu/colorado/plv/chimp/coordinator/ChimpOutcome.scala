package edu.colorado.plv.chimp.coordinator

import edu.colorado.plv.chimp.combinator.{EventTrace, Prop}

/**
  * Created by edmund on 3/14/17.
  */

abstract class ChimpOutcome

// ChimpDriver outcomes
case class SuccChimpOutcome(executedTrace:EventTrace) extends ChimpOutcome
case class BlockChimpOutcome(executedTrace:EventTrace, error: Option[String]) extends ChimpOutcome
case class CrashChimpOutcome(executedTrace:EventTrace, error: String) extends ChimpOutcome
case class AssertFailChimpOutcome(executedTrace:EventTrace, violatedProp: Prop) extends ChimpOutcome
case class UnknownChimpDriverChimpOutcome(executedTraceOpt: Option[EventTrace], error: Option[String]) extends ChimpOutcome
case class ChimpDriverExceptChimpOutcome(executedTraceOpt: Option[EventTrace], error: Option[String]) extends ChimpOutcome

// MissionControl outcomes
object TimedoutChimpOutcome$ extends ChimpOutcome
case class ChimpLoaderFailedChimpOutcome(error: String) extends ChimpOutcome
case class ParseFailChimpOutcome(error: String) extends ChimpOutcome

/*
case class ChimpTraceResults(executedTrace:EventTrace, outcome: Outcome) {
  override def toString: String = s"<$executedTrace,$outcome>"
}
*/
