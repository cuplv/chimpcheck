import edu.colorado.plv.chimp.combinator.EventTrace

/**
  * Created by chanceroberts on 8/7/18.
  */


object DemoWrapper {
  def listOfCommands(): String = {
    " decode base64\n" +
    " "
  }

  def main(args: Array[String]): Unit = {
    args.length match{
      case 0 | 1 => throw new Exception("Expected two arguments.\nList of commands: \n"+listOfCommands())
      case _ => args(0) match{
        case "decode" => println(EventTrace.fromBase64(args(1)))
        case _ => throw new Exception("Unexpected command. \nList of commands: \n"+listOfCommands())
      }
    }
  }
}
