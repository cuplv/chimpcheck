package edu.colorado.plv.chimp.generator

import edu.colorado.plv.chimp.combinator.{Assert, EventTrace, Prop, UIEvent}

/**
  * Created by edmund on 4/18/17.
  */

object Implicits {

  implicit class UIEventGen(event: UIEvent) {
    def *>> (next: UIEvent): TraceGen     = Path(EventTrace.trace(event)) *>> next
    def *>> (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) *>> trace
    def *>> (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) *>> gen
    def <+> (next: UIEvent): TraceGen     = Path(EventTrace.trace(event)) <+> next
    def <+> (trace: EventTrace): TraceGen = Path(EventTrace.trace(event)) <+> trace
    def <+> (gen: TraceGen): TraceGen     = Path(EventTrace.trace(event)) <+> gen
  }

  implicit  class EventTraceGen(trace: EventTrace) {
    def *>>(event: UIEvent): TraceGen   = Path(trace) *>> event
    def *>>(next: EventTrace): TraceGen = Path(trace) *>> next
    def *>>(gen: TraceGen): TraceGen    = Path(trace) *>> gen
    def <+> (event: UIEvent): TraceGen   = Path(trace) <+> event
    def <+> (next: EventTrace): TraceGen = Path(trace) <+> next
    def <+> (gen: TraceGen): TraceGen    = Path(trace) <+> gen
  }

  implicit class PropertyBaseGen(prop: Prop) {
    def survives (gen: TraceGen): TraceGen = {
      Assert(prop) :>> gen :>> Assert(prop)
    }
  }

}