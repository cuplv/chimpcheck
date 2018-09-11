import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.fixr.bash.android.Adb

/**
  * Created by chanceroberts on 8/17/18.
  */
object OutputTransformer {
  private def goUntilNewLine(lines: List[String], concat: String, addNewLine: Boolean = false): String = lines match{
    case Nil => concat
    case _ =>
      if (lines.head.length() == 0 || lines.head.startsWith("INSTRUMENTATION_")){
        concat
      } else {
        if (addNewLine)
          goUntilNewLine(lines.tail, s"$concat${lines.head}\n", addNewLine)
        else
          goUntilNewLine(lines.tail, s"$concat${lines.head}")
      }
  }

  private def findResult(lines: List[String], after: String, addNewLine: Boolean = false): String = lines match{
    case Nil => ""
    case head :: tail => head.indexOf(after) match{
      case -1 => findResult(tail, after, addNewLine)
      case x => goUntilNewLine(head.substring(x+after.length()) :: tail, "", addNewLine)
    }
  }

  private def findResultOneLine(lines: List[String], after: String): String = lines match{
    case Nil => ""
    case head :: tail => head.indexOf(after) match{
      case -1 => findResultOneLine(tail, after)
      case x => head.substring(x+after.length())
    }
  }

  def transformOutput(output: String): String = {
    val outputList = output.split("\n").toList
    val result = findResultOneLine(outputList, "ChimpDriver-Outcome=")
    val trace = findResult(outputList, "ChimpDriver-ExecutedTrace=")
    val realTrace = EventTrace.fromBase64(trace)
    val firstRet = result match{
      case "Success" => s"Trace $realTrace got run through successfully!"
      case "Crashed" => s"Trace $realTrace got the program to crash!"
      case "Blocked" => s"Trace $realTrace got blocked."
      case "AssertFailed" => s"Trace $realTrace caused an assertion to fail!"
      case "DriverExcept" => s"Trace $realTrace caused an exception in ChimpDriver!"
      case "Unknown" | "" => s"Trace $realTrace ended for an unknown reason."
      case _ => s"Trace $realTrace led to something weird happening. Outcome $result"
    }
    findResult(outputList, "stack=", addNewLine=true) match{
      case "" => firstRet
      case x => s"$firstRet\nStack Trace: $x"
    }
  }
}


object OutputTransformerTest {
  def main(args: Array[String]): Unit = {
    //val file = new File("successTest.txt")
    //val file = new File("failedTest.txt")
    val eventTrace = "//If we land on the \"Turm\" screen, then Click(*) won't work, so we need to go back to the previous screen.\nval checkTurm = Try((isDisplayed(\"Turm\") Then ClickBack:>>Skip).generator.sample.get)\n//This clicks randomly 500 times, unless it gets to the Turm screen, where it goes back a screen.\nval traceGen = Repeat(500, Click(*) :>> checkTurm) :>> Skip"
    //val eventTrace = "Click(R.id.skip) :>> Type(R.id.hostUrlInput, \"ncloud.zaclys.com\"):>> Type(R.id.account_username, \"22203\"):>> Type(R.id.account_password, \"12321qweqaz!\") :>> Click(R.id.buttonOK) :>> (isDisplayed(\"Allow\") Then Click(\"Allow\"):>> Sleep(1000)) :>> LongClick(\"Documents\") :>> ClickMenu :>> Click(\"Move\") :>> Rotate"
    println(InputTransformer.transformInput(eventTrace, "kisten"))
    //println(OutputTransformer.transformOutput(Source.fromFile(file).mkString))
  }
}
