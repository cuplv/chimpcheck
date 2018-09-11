import java.io.{BufferedWriter, File, FileWriter}

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.fixr.bash.{Cmd, Fail, Succ}
import edu.colorado.plv.fixr.bash.utils.{FailTry, SuccTry}
import org.slf4j.LoggerFactory
import spray.json.{JsBoolean, JsObject, JsString}
//import scala.sys.process._

import scala.io.Source

/**
  * Created by chanceroberts on 8/21/18.
  */
object InputTransformer {
  val correctInput: Map[String, String] = Map("trainer" -> "import plv.colorado.edu.chimptrainer.R",
  "nextcloud" -> "import com.owncloud.android.R",
  "kisten" -> "import de.d120.ophasekistenstapeln.R")
  implicit val bashLogger = Logger(LoggerFactory.getLogger(""))
  def transformInput(input: String, app: String): JsObject = {
    // Get the input into the file.
    val file = new File("../ChimpCheckStub/src/main/scala/edu/colorado/plv/chimp/stub/StubGenerator.scala")
    val fileArr = Source.fromFile(file).mkString.split("\n")
    val strList = Source.fromFile(file).mkString.split("\n").toList
    val inputList = input.split("\n").toList
    /*val (newVal, _) = strList.foldRight(List[String](), true){
      case (s, (lis, true)) => s match {

      }
      case (s, (lis, false)) =>
        if (s.contains("val traceGen =")) {
          val newLis = inputList.foldRight(lis) {
            case (x, myLis) => s"    $x" :: myLis
          }
          (s :: newLis, true)
        } else
          (lis, false)
    }
    */
    val (newVal, _) = strList.foldRight(List[String](), fileArr.length){
      case (s, (lis, x)) if x <= (fileArr.length-16) && x > 22 =>
        (lis, x-1)
      case (s, (lis, 22)) => val newLis = inputList.foldRight(lis) {
        case (x, myLis) => s"  $x" :: myLis
      }
        (newLis, 21)
      case (s, (lis, x)) => s match{
        case "import plv.colorado.edu.chimptrainer.R"
             | "import com.owncloud.android.R" | "import de.d120.ophasekistenstapeln.R" =>
          (correctInput.getOrElse(app, s) :: lis, x-1)
        case _ => (s :: lis, x-1)
      }
    }
    val writer = new BufferedWriter(new FileWriter(file, false))
    newVal.foreach(dta => writer.append(s"$dta\n"))
    writer.close()

    val res = Cmd("bash runSBT.bash").!
    val output = if (res.isSucc()){
      val out = res.asInstanceOf[SuccTry[Succ, Fail]].result.stdout.split('\n')
      out.filter{x => !x.contains("[")}.foldRight(""){
        case (x, z) => s"$x\n$z"
      }
    } else {
      val out = res.asInstanceOf[FailTry[Succ, Fail]].error.stdout.split('\n')
      out.foldRight(""){
        case (x, z) if !x.contains('[') || !x.contains("info") =>
          s"$x\n$z"
        case (_, z) => z
      }
      /*out.filter{x => !x.contains("[info]")}.foldRight(""){
        case (x, z) => s"$x\n$z"
      }*/
    }
    JsObject("status" -> JsBoolean(res.isSucc()), "output" -> JsString(output.split("\n")(0)))
  }
}
