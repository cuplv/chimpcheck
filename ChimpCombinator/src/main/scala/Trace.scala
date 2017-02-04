/**
  * Created by edmund on 2/4/17.
  */


import org.scalacheck._
import org.scalacheck.Prop.forAll
import Gen._


/*
   UIEvent = AppEvent | ExtUserEvent
   AppEvent = ClickName(String) | ClickId(Int) | TypeInName(String,String) | TypeInId(Int,String) | Sleep(Int) | Skip
   ExtUserEvent = ClickBack | ClickHome | ClickMenu | PullDownSettings | ReturnToApp | RotateLeft | RotateRight
*/

abstract class UIEvent {
  def |:| (event: UIEvent) : Trace    = Seq(Step(this),Step(event))
  def |+| (event: UIEvent) : TraceGen = ISeq( Path(Step(this)), Path(Step(event)) )
}
abstract class AppEvent extends UIEvent

case class ClickName(name: String) extends AppEvent
case class ClickId(id:Int) extends AppEvent
case class TypeInName(name:String, input:String) extends AppEvent
case class TypeInId(id: Int, input: String) extends AppEvent
case class Sleep(time: Int) extends AppEvent
case class Skip() extends AppEvent

abstract class ExtUserEvent extends UIEvent

case class ClickBack() extends ExtUserEvent
case class ClickHome() extends ExtUserEvent
case class ClickMenu() extends ExtUserEvent
case class PullDownSettings() extends ExtUserEvent
case class ReturnToApp() extends ExtUserEvent
case class RotateLeft() extends ExtUserEvent
case class RotateRight() extends ExtUserEvent

/*
  Trace = Step(UIEvent) | Trace |:| Trace | Decide (=> Bool) Trace Trace
 */

abstract class Trace {
  def |:| (trace: Trace) : Trace = Seq(this, trace)
}

case class Step(event: UIEvent) extends Trace
case class Seq(trace1:Trace, trace2:Trace) extends Trace

class Decide(test: => Boolean, trace1:Trace, trace2:Trace) extends Trace {
  def getTest = test
  def getTrace1 = trace1
  def getTrace2 = trace2
  override def hashCode = trace1.hashCode() + trace2.hashCode()
}
object Decide {
  def apply(test: => Boolean, trace1:Trace, trace2:Trace) = new Decide(test,trace1,trace2)
  def unapply(n: Decide) = Some( n.getTest -> n.getTrace1 -> n.getTrace2 )
}

/*
  TraceGen = Path(Trace) | TraceGen |:| TraceGen | TraceGen |+| TraceGen | Option(TraceGen) | Choose [TraceGen]
           | Monkey | LearnModel
 */

object TraceGen {

  val defaultUserInterrupts: List[Trace] = List(
    Step(RotateLeft()),
    Step(RotateRight()),
    RotateLeft() |:| RotateRight(),
    ClickHome() |:| ReturnToApp(),
    ClickMenu() |:| ReturnToApp(),
    PullDownSettings() |:| ReturnToApp()
  )

  def pick(n:Int): Gen[Trace] =
    if (n > 0) {
      for {
        event <- Gen.oneOf(defaultUserInterrupts)
        events <- pick(n - 1)
      } yield event |:| events
    } else {
      const(Step(Skip()))
    }
}

abstract class TraceGen {
  def |+| (gen: TraceGen): TraceGen = ISeq(this,gen)
  def |+| (trace: Trace): TraceGen = ISeq(this,Path(trace))
  def |+| (event: UIEvent): TraceGen = ISeq(this,Path(Step(event)))
  def |:| (gen: TraceGen): TraceGen = ASeq(this,gen)
  def |:| (trace: Trace): TraceGen = ASeq(this,Path(trace))
  def |:| (event: UIEvent): TraceGen = ASeq(this,Path(Step(event)))

  def generate(): Gen[Trace] = {
    this match {
      case Path(trace)     => const(trace)
      case ASeq(gen1,gen2) => for {
        tr1 <- gen1.generate()
        tr2 <- gen2.generate()
      } yield (tr1 |:| tr2)
      case ISeq(gen1,gen2) => for {
        tr1 <- gen1.generate()
        tr2 <- gen2.generate()
        n <- Gen.frequency( (1,0), (4,1), (2,2), (1,3) )
        itrs <- TraceGen.pick(n)
      } yield (tr1 |:| itrs |:| tr2)
      case Option(gen) => for {
        tr1 <- gen.generate()
        tr2 <- Gen.frequency( (1,Step(Skip())), (2,tr1) )
      } yield tr2
      case Choose(gens) => for {
        n <- Gen.choose(0,gens.length)
        tr <- gens(n).generate()
      } yield tr
    }
  }
}

case class Path(trace: Trace) extends TraceGen
case class ASeq(gen1:TraceGen, gen2:TraceGen) extends TraceGen
case class ISeq(gen1:TraceGen, gen2:TraceGen) extends TraceGen
case class Option(gen: TraceGen) extends TraceGen
case class Choose(gens: List[TraceGen]) extends TraceGen
case class Monkey() extends TraceGen
case class LearnModel() extends TraceGen

object Test {

  def main(args: Array[String]) : Unit = {
    val traces = ClickName("login") |+| TypeInName("userbox","test") |+| TypeInName("pwdbox","1234") |+| ClickName("Go")

    val prop = forAll (traces.generate()) {
      tr => {
        println(tr)
        true
      }
    }

    prop.check

  }

}
