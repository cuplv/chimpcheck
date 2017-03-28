package edu.colorado.plv.chimp.coordinator

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.EventTrace
import edu.colorado.plv.fixr.bash.{Cmd, Fail, Lift, Succ}
import edu.colorado.plv.fixr.bash.android.{Aapt, Adb, AmInstrument, Emulator}
import edu.colorado.plv.fixr.bash.utils.{FailTry, SuccTry, doTry, repeat}
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random

/**
  * Created by edmund on 3/11/17.
  */
object ChimpLoader {

  def extractValueWithKey(key:String, line:String): String = line.split(key)(1).drop(1).trim()

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String, packageNamesOpt:Option[(String,String)])
               (implicit bashLogger: Logger, ec: ExecutionContext): Option[ChimpTraceResults] = {
    val b64ProtoTrace = eventTrace.toBase64()

    val (appPackageName,testPackageName) = packageNamesOpt match {
      case Some(packageNames) => packageNames
      case None => {
         val packageOut = for {
           appAPKout  <- Aapt.home(aaptHomePath).apkInfo(appAPKPath) !!! ;
           testAPKout <- Aapt.home(aaptHomePath).apkInfo(chimpAPKPath) !!! ;
           appInfo  <- Aapt.parse(appAPKout) ;
           testInfo <- Aapt.parse(testAPKout)
         } yield (appInfo.packageName,testInfo.packageName)
         packageOut match {
           case SuccTry(packageNames) => packageNames
           case default => return None
         }
      }
    }

    val kickBackLockName = genLockName()

    val kickBackFut:Future[Unit] = Future { kickBack(emuID, appPackageName, kickBackLockName) }

    val instrOut = for{
      /*
      appAPKout  <- Aapt.home(aaptHomePath).apkInfo(appAPKPath) !!! ;
      testAPKout <- Aapt.home(aaptHomePath).apkInfo(chimpAPKPath) !!! ;
      appInfo  <- Aapt.parse(appAPKout) ;
      testInfo <- Aapt.parse(testAPKout) ;
      */

      uninstApp  <- doTry (Adb.target(emuID).uninstall(appPackageName)) ! ;
      uninstTest <- doTry (Adb.target(emuID).uninstall(testPackageName)) ! ;
      instApp  <- Adb.target(emuID).install(appAPKPath) ! ;
      instTest <- Adb.target(emuID).install(chimpAPKPath) ! ;

      instrOut <- AmInstrument.target(emuID).raw().sync().debug(false).extra("eventTrace",b64ProtoTrace).extra("syncFile",kickBackLockName)
        .components( appPackageName, testerClass, testPackageName,"edu.colorado.plv.chimp.driver.ChimpJUnitRunner") !!!
    } yield instrOut

    // println(s"Done! $instrOut")

    Await.result(kickBackFut, 20000 millis)

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

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String)
               (implicit bashLogger: Logger, ec: ExecutionContext): Option[ChimpTraceResults] = quickLoad(emuID, eventTrace, appAPKPath, chimpAPKPath, testerClass, aaptHomePath, None)

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String, appPackageName:String, testPackageName:String)
               (implicit bashLogger: Logger, ec: ExecutionContext): Option[ChimpTraceResults] = quickLoad(emuID, eventTrace, appAPKPath, chimpAPKPath, testerClass, aaptHomePath, Some(appPackageName,testPackageName))


  def kickBack(emuID:String, appPackageName:String, syncFilePath:String) (implicit bashLogger:Logger): Unit = {
    for {
    // Wait until the app is first started
      p <- repeat (Adb.target(emuID).shell("ps") #| Cmd(s"grep $appPackageName")) until {
        case SuccTry(Succ(c, o, e)) => c.split("\n").length > 0
        case default => false
      }
    } yield p

    Thread.sleep(2000)
    bashLogger.info("App started, KickBack routine now active...")

    var kickBackAttemptActive = false
    while(true) {
      Adb.target(emuID).shellPsGrep(appPackageName) match {
        case SuccTry(ls) => {
          if (ls.length == 0) {
            bashLogger.info("App has terminated, KickBack routine terminating..")
            return
          } else {
            bashLogger.info("App is still active, KickBack routine proceeding..")
          }
        }
        case default => {
          bashLogger.info("App has terminated, KickBack routine terminating..")
          return
        }
      }
      Adb.target(emuID).getForeGroundAppName match {
        case SuccTry(appName) => {
          if (appName != appPackageName) {
            kickBackAttemptActive = true
            bashLogger.info("App exited! Kicking back now!")
            for {
              p0 <- Lift !!! Thread.sleep(1500) ;
              p1 <- Adb.target(emuID).shell("input keyevent KEYCODE_APP_SWITCH") ! ;
              p2 <- Lift !!! Thread.sleep(2000) ;
              p3 <- Adb.target(emuID).shell("input keyevent KEYCODE_DPAD_DOWN") ! ;
              // p3 <- Adb.target(emuID).shell("input keyevent 20") ! ;
              p4 <- Lift !!! Thread.sleep(2000) ;
              p5 <- Adb.target(emuID).shell("input keyevent KEYCODE_ENTER") !
            } yield p5
          } else {
            if (kickBackAttemptActive) {
              bashLogger.info(s"Kick back successful, unlocking Chimp driver on $syncFilePath")
              Adb.target(emuID).shell(s"touch $syncFilePath") !;
              kickBackAttemptActive = false
            }
          }
        }
        case default =>
      }
      Thread.sleep(2000)
    }

  }

  def genLockName(): String = {
     val rand = Random
     s"kickBack_${rand.nextInt()}_${System.currentTimeMillis/1000}.lock"
  }

}

object TestKickBack {

  def main(args:Array[String]): Unit = {
    implicit val logger = Logger(LoggerFactory.getLogger("kick-back-tester"))
    ChimpLoader.kickBack("emulator-5554", "com.peilunzhang.contractiontimerdistilled", "/data/local/tmp/kickBack.lock")
  }

}