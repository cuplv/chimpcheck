import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.fixr.bash.android.Adb
import org.slf4j.LoggerFactory
import spray.json.{JsNumber, JsObject, JsString}

import scalaj.http.Http
import spray.json._

// import scala.sys.process._

/**
  * Created by chanceroberts on 8/10/18.
  */
object ServerLogic {
  implicit val bashLogger = Logger(LoggerFactory.getLogger(""))
  val testToAPK = Map(
    "trainer" -> ("plv.colorado.edu.chimptrainer", "plv.colorado.edu.chimptrainer"),
    "nextcloud" -> ("com.nextcloud.client", "com.owncloud.android"),
    "kisten" -> ("de.d120.ophasekistenstapeln", "de.d120.ophasekistenstapeln")
  ) //Hardcoded for now?

  def setUpEmulator(queryStr: String, conf: Config, ip: String): String = {
    val (newHost, adbPort, streamPort) = ("localhost", "5037", "9002") //Hardcoded for now.

    //Create the connection between the server and the adb client.
    Http(s"localhost:${conf.getString("webSocketPort")}").postData(
      JsObject("clientIP" -> JsString(ip), "streamingIP" -> JsString(s"$newHost:$streamPort")).prettyPrint)
    s"$newHost:$streamPort"
  }
  def runAnEmulator(queryStr: String, conf: Config, ip: String): String = {
    val (newHost, adbPort, _) = ("localhost", "5037", "9002") //Hardcoded for now.
    val chimpCheckLoc = conf.getString("chimpCheckAPKLoc")
    val json = queryStr.parseJson.asJsObject
    val test = json.fields.getOrElse("test",
      throw new Exception("Unexpected JSON Format (Requires field test for the test to run.)")) match{
      case JsString(s) => s
      case _ => throw new Exception("Unexpected JSON Format (Requires field test to be a string.")
    }
    val eventTrace = json.fields.getOrElse("eventTrace",
      throw new Exception("Unexpected JSON Format (Requires field eventTrace for the Base64-encoded version of the event trace.)")
    ) match{
      case JsString(s) => s
      case _ => throw new Exception("Unexpected JSON Format (Requires field eventTrace to be a string.")
    }
    val (testAPK, packAPK) = testToAPK(test)
    //Get the Emulator (Returns Host:Port)
    /*val newContainer = Http("???").postData(
      JsObject(Map("id" -> JsString(conf.getString("id")), "instances" -> JsNumber(1))).prettyPrint
    ).method("PATCH").header("accept", "application/json").asString.body.parseJson*/

    //Here's the bash scripts! (Now with ScalaBashing)
    Adb.extend(s"-H $newHost -P $adbPort").target("emulator-5554").uninstall(testAPK).!
    Adb.extend(s"-H $newHost -P $adbPort").target("emulator-5554").uninstall(s"$testAPK.test").!
    Adb.extend(s"-H $newHost -P $adbPort").target("emulator-5554").install(s"$chimpCheckLoc/$test/app-debug.apk").!
    Adb.extend(s"-H $newHost -P $adbPort").target("emulator-5554").install(s"$chimpCheckLoc/$test/app-debug-androidTest.apk").!
    val chimpCheckReturn = Adb.extend(s"-H $newHost -P $adbPort").target("emulator-5554").shell(s"am instrument -r -w -e debug false " +
        s"-e eventTrace $eventTrace -e appPackageName $packAPK -e class $packAPK.TestExpresso " +
        s"$testAPK.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner").!!!.toString
    /*val chimpCheckReturn =
      s"adb -H $newHost -P $adbPort -s emulator-5554 shell am instrument -r -w -e debug false " +
        s"-e eventTrace $eventTrace -e appPackageName $packAPK -e class $packAPK.TestExpresso " +
        s"$testAPK.test/edu.colorado.plv.chimp.driver.ChimpJUnitRunner".!!*/
    //val chimpCheckReturn = s"bash runCommand.sh $newHost $adbPort $test $packAPK $testAPK $eventTrace".!!
    //Close the Emulator
    /*val done = Http("???").postData(
      //I'm not convinced that this is the correct way to do it...
      JsObject(Map("id" -> JsString(conf.getString("id")), "instances" -> JsNumber(0))).prettyPrint
    )*/
    chimpCheckReturn.substring(8, chimpCheckReturn.length()-1)
  }

  def closeAnEmulator(queryStr: String, conf: Config, ip: String): String ={
    //This does nothing for now; However, this will de-allocate the Docker Container using the Marathon APIs.
    ""
  }
}
