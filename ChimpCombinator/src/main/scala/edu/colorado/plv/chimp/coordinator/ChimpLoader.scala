package edu.colorado.plv.chimp.coordinator

import chimp.{protobuf => pb}
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.fixr.bash.{Fail, Succ}
import edu.colorado.plv.fixr.bash.android.{Aapt, Adb, AmInstrument, Emulator}
import edu.colorado.plv.fixr.bash.utils.{FailTry, SuccTry, doTry}

/**
  * Created by edmund on 3/11/17.
  */
object ChimpLoader {

  def extractValueWithKey(key:String, line:String): String = line.split(key)(1).drop(1).trim()

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String)
               (implicit bashLogger: Logger): Option[ChimpTraceResults] = {
    val b64ProtoTrace = eventTrace.toBase64()

    val instrOut = for{
      appAPKout  <- Aapt.home(aaptHomePath).apkInfo(appAPKPath) !!! ;
      testAPKout <- Aapt.home(aaptHomePath).apkInfo(chimpAPKPath) !!! ;
      appInfo  <- Aapt.parse(appAPKout) ;
      testInfo <- Aapt.parse(testAPKout) ;

      uninstApp  <- doTry (Adb.target(emuID).uninstall(appInfo.packageName)) ! ;
      uninstTest <- doTry (Adb.target(emuID).uninstall(testInfo.packageName)) ! ;
      instApp  <- Adb.target(emuID).install(appAPKPath) ! ;
      instTest <- Adb.target(emuID).install(chimpAPKPath) ! ;

      instrOut <- AmInstrument.target(emuID).raw().sync().debug(false).extra("eventTrace",b64ProtoTrace)
        .components( appInfo.packageName, testerClass, testInfo.packageName,"edu.colorado.plv.chimp.driver.ChimpJUnitRunner") !!!
    } yield instrOut

    // println(s"Done! $instrOut")

    instrOut match {
      case SuccTry(o) => {
         var results:Option[Boolean] = None
         var trace:Option[EventTrace] = None
         for(seg <- o.replace("\n","").split("INSTRUMENTATION")) {
           if(seg contains "ChimpTraceResult") {
             extractValueWithKey("ChimpTraceResult", seg) match {
               case "Success" => results = Some(true)
               case "Failed"  => results = Some(false)
               case default   =>
             }
           }
           if(seg contains "ChimpTraceCompleted") {
             // val trstr = extractValueWithKey("ChimpTraceCompleted", seg)
             // println(s"Here it is: $trstr")
             trace = Some( EventTrace.fromBase64( extractValueWithKey("ChimpTraceCompleted", seg) ) )
           }
         }
        (trace,results) match {
          case (Some(tr),Some(res)) => Some( ChimpTraceResults(tr,res) )
          case default => None
        }
      }
      case FailTry(Fail(c, ec, o, e)) => None
    }

  }

}
