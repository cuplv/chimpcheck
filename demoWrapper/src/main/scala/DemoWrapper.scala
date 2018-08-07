import edu.colorado.plv.chimp.combinator.EventTrace
import java.io.File

import scala.io.Source

/**
  * Created by chanceroberts on 8/7/18.
  */


object DemoWrapper {
  def listOfCommands(): String = {
    " decode file.txt\n"
  }

  def getInfo(file: File, lookingFor: String, withNewLine: Boolean = false): String = {
    val (_, executedTrace) = Source.fromFile(file).getLines().foldLeft((false, "")){
      case ((false, x), l) if l.contains(lookingFor) =>
        val newL = x+l.substring(l.indexOf('=')+1)
        if (withNewLine) (true, s"$newL\n") else (true, newL)
      case ((true, x), l) if !l.isEmpty =>
        val newL = x+l
        if (withNewLine) (true, s"$newL\n") else (true, newL)
      case ((_, x), l) => (false, x)
    }
    executedTrace
  }

  def main(args: Array[String]): Unit = {
    args.length match{
      case 0 => throw new Exception("Expected an arguments.\nList of commands: \n"+listOfCommands())
      case _ =>
        //args(0) match{
          //case "decode" =>
        val a = new File(args(1))
        val trace = getInfo(a, "INSTRUMENTATION_RESULT: ChimpDriver-ExecutedTrace=")
        val stack = getInfo(a, "INSTRUMENTATION_STATUS: stack=", withNewLine = true)
        println(s"ChimpCheck ended with a ${args(0).replace("ed","")} with trace ${EventTrace.fromBase64(trace)}!")
        if (!stack.isEmpty) println(s"Stack:\n  $stack")
          //case _ => ()
      //}
    }
  }
}
