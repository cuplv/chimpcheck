/**
  * Created by edmund on 4/3/17.
  */

import org.scalacheck.Prop._
import org.scalacheck.Properties
import org.scalacheck.Test.Parameters
import edu.colorado.plv.chimp.combinator.{*, Assert, Click, Coord, EventTrace, Left, Not, Swipe, TraceGen, Type, isClickable}

import edu.colorado.plv.chimp.combinator.Generator_Implicits._

import edu.colorado.plv.chimp.combinator.UIID_Implicits._
import edu.colorado.plv.chimp.combinator.Orient_Implicits._
import edu.colorado.plv.chimp.combinator.PropArg_Implicits._
import edu.colorado.plv.chimp.combinator.BaseProp_Implicits._
import edu.colorado.plv.chimp.combinator.Prop_Implicits._
import edu.colorado.plv.chimp.combinator.ViewID_Implicits._

object MainTester extends Properties("String") {

  property("Basic generator test") = {
    val traces: TraceGen = Click("login") :>> Click(*) *>> Type("userbox","test") *>> Type("pwdbox","1234") *>> Click("Go") *>>
      Swipe("nuts",Left) *>> Swipe("crap",Coord(1,2)) *>> (Click("button1") <+> Click("button2")) *>>
      Assert( Not(isClickable("crap")) )

    val myParam = Parameters.default.withMinSuccessfulTests(10)

    forAll(traces.generator()) {
      tr => {
        println(" ============================================================================= ")
        println("Generated: " + tr.toString())
        val b64 = tr.toBase64()
        println("Base64 Encoded ProtoBuf: " + b64)
        val trB = EventTrace.fromBase64(b64)
        println("Recovered: " + trB.toString())
        println(" ============================================================================= ")
        true
      }
    }

  }

}
