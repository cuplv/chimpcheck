import com.typesafe.config.Config
import spray.json.{JsNumber, JsObject, JsString}

import scalaj.http.Http
import spray.json._
import scala.sys.process._

/**
  * Created by chanceroberts on 8/10/18.
  */
object ServerLogic {
  def runAnEmulator(queryStr: String, conf: Config): String = {
    val json = queryStr.parseJson.asJsObject
    val test = json.fields.getOrElse("test",
      throw new Exception("Unexpected JSON Format (Requires field test for the test to run.)"))
    val packAPK = json.fields.getOrElse("appPack",
      throw new Exception("Unexpected JSON Format (Requires field appPack for the app package name."))
    val testAPK = json.fields.getOrElse("apk",
      throw new Exception("Unexpected JSON Format (Requires field apk for the package to install. " +
        "(Yes, this is different than the app package name...)")
    )
    val eventTrace = json.fields.getOrElse("eventTrace",
      throw new Exception("Unexpected JSON Format (Requires field eventTrace for the Base64-encoded version of the event trace.)")
    )
    //Get the Emulator (Returns Host:Port)
    /*val newContainer = Http("???").postData(
      JsObject(Map("id" -> JsString(conf.getString("id")), "instances" -> JsNumber(1))).prettyPrint
    ).method("PATCH").header("accept", "application/json").asString.body.parseJson*/
    val (newHost, adbPort, streamPort) = ("172.17.0.3", "5037", "9002") //Hardcoded for now.
    // Http("").asString.body
    //Yes, exprAPK and testAPK are two different things.
    val chimpCheckReturn = s"bash runCommand.sh $newHost $adbPort $test $packAPK $testAPK $eventTrace".!!
    //Close the Emulator
    /*val done = Http("???").postData(
      //I'm not convinced that this is the correct way to do it...
      JsObject(Map("id" -> JsString(conf.getString("id")), "instances" -> JsNumber(0))).prettyPrint
    )*/
    chimpCheckReturn
  }
}
