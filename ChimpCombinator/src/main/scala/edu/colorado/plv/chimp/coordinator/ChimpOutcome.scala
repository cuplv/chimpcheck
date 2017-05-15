package edu.colorado.plv.chimp.coordinator

import edu.colorado.plv.chimp.combinator.{EventTrace, Prop}

/**
  * Created by edmund on 3/14/17.
  */

object ChimpOutcome {

  def prettyErr(err: String): String = {
     err.replace("\t", "\n ")
  }

}

abstract class ChimpOutcome

// ChimpDriver outcomes
case class SuccChimpOutcome(executedTrace:EventTrace) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution Completed successfully!\n" + s"Executed Trace: $executedTrace"
  }
}
case class BlockChimpOutcome(executedTrace:EventTrace, error: Option[String]) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution Blocked on some event!\n" + s"Executed Trace: $executedTrace\n" +
           (error match { case None => "" ; case Some(err) => s"There was an error: ${ChimpOutcome.prettyErr(err)}" })
  }
}
case class CrashChimpOutcome(executedTrace:EventTrace, error: String) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution Crashed!\n" + s"Executed Trace: $executedTrace\n" +
           s"Exception thrown: ${ChimpOutcome.prettyErr(error)}"
  }
}
case class AssertFailChimpOutcome(executedTrace:EventTrace, violatedProp: Prop) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution Failed an Assertion!\n" + s"Executed Trace: $executedTrace\n" +
           s"Violated Property: $violatedProp"
  }
}
case class UnknownChimpDriverChimpOutcome(executedTraceOpt: Option[EventTrace], error: Option[String]) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution resulted in Unknown exception!\n" +
           (executedTraceOpt match { case None => "" ; case Some(eventTrace) => s"Executed Traces: $eventTrace\n" }) +
           (error match { case None => "" ; case Some(err) => s"Exception throw: ${ChimpOutcome.prettyErr(err)}" })
  }
}
case class ChimpDriverExceptChimpOutcome(executedTraceOpt: Option[EventTrace], error: Option[String]) extends ChimpOutcome {
  override def toString: String = {
    return "Chimp execution resulted in Driver exception!\n" +
      (executedTraceOpt match { case None => "" ; case Some(eventTrace) => s"Executed Traces: $eventTrace\n" }) +
      (error match { case None => "" ; case Some(err) => s"Exception throw: ${ChimpOutcome.prettyErr(err)}" })
  }
}

// MissionControl outcomes
object TimedoutChimpOutcome$ extends ChimpOutcome {
  override def toString: String = "Chimp execution timed out!"
}
case class ChimpLoaderFailedChimpOutcome(error: String) extends ChimpOutcome {
  override def toString: String = {
    return "An exception occurred while initializing Chimp\n" + s"Exception: $error"
  }
}
case class ParseFailChimpOutcome(error: String) extends ChimpOutcome {
  override def toString: String = {
    return "An exception occurred while parsing outcome!\n" + s"Exception: $error"
  }
}

/*
case class ChimpTraceResults(executedTrace:EventTrace, outcome: Outcome) {
  override def toString: String = s"<$executedTrace,$outcome>"
}
*/
