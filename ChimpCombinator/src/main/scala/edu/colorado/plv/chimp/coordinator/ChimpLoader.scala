package edu.colorado.plv.chimp.coordinator

import chimp.{protobuf => pb}
import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.fixr.bash.android.{Aapt, Adb, AmInstrument, Emulator}
import edu.colorado.plv.fixr.bash.utils.doTry

/**
  * Created by edmund on 3/11/17.
  */
object ChimpLoader {

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String)
               (implicit bashLogger: Logger): Unit = {
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

    println(s"Done! $instrOut")
  }

}
