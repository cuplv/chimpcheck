package edu.colorado.plv.chimp.coordinator

import com.typesafe.scalalogging.Logger
import edu.colorado.plv.chimp.combinator.{EventTrace, Prop, dummy}
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

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String, packageNamesOpt:Option[(String,String)])
               (implicit bashLogger: Logger, ec: ExecutionContext): ChimpOutcome = {
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
           case FailTry(Fail(c,ec,o,e)) => return ChimpLoaderFailedChimpOutcome( s"Failed while attempting to extract package names. Exitcode: $ec ; ErrOut: $e" )
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
        .components( appPackageName, testerClass, testPackageName,"edu.colorado.plv.chimp.driver.ChimpJUnitRunner") !!! ;

      wait <- Lift !!! Thread.sleep(1000) ;
      kill <- Adb.target(emuID).shell(s"am force-stop $appPackageName") !
    } yield instrOut

    // println(s"Done! $instrOut")

    Await.result(kickBackFut, 20000 millis)

    instrOut match {
      case SuccTry(o) => parseOutcome(o)
      case FailTry(Fail(c, ec, o, e)) => {
        ChimpLoaderFailedChimpOutcome( s"Failed while bashing with Adb. Exitcode: $ec ; ErrOut: $e" )
      }
    }

  }

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String)
               (implicit bashLogger: Logger, ec: ExecutionContext): ChimpOutcome = quickLoad(emuID, eventTrace, appAPKPath, chimpAPKPath, testerClass, aaptHomePath, None)

  def quickLoad(emuID:String, eventTrace: EventTrace, appAPKPath:String, chimpAPKPath:String, testerClass:String, aaptHomePath:String, appPackageName:String, testPackageName:String)
               (implicit bashLogger: Logger, ec: ExecutionContext): ChimpOutcome = quickLoad(emuID, eventTrace, appAPKPath, chimpAPKPath, testerClass, aaptHomePath, Some(appPackageName,testPackageName))


  def extractValueWithKey(key:String, line:String): String = line.split(key)(1).drop(1).trim()

  def parseOutcome(stdout: String): ChimpOutcome = {
    var outcomeStr:String = ""
    var traceOpt:Option[String] = None
    var propertyOpt:Option[String] = None
    var stackTraceOpt: Option[String] = None
    var driverExceptOpt: Option[String] = None
    for(seg <- stdout.replace("\n","").split("INSTRUMENTATION")) {
      if(seg contains "ChimpDriver-Outcome") {
        outcomeStr = extractValueWithKey("ChimpDriver-Outcome", seg)
      }
      if(seg contains "ChimpDriver-ExecutedTrace") {
        // val trstr = extractValueWithKey("ChimpTraceCompleted", seg)
        // println(s"Here it is: $trstr")
        traceOpt = Some( extractValueWithKey("ChimpDriver-ExecutedTrace", seg) )
      }
      if(seg contains "stack") {
        stackTraceOpt = Some( extractValueWithKey("stack", seg) )
      }
      if(seg contains "ChimpDriver-ViolatedProperty") {
        propertyOpt = Some( extractValueWithKey("ChimpDriver-ViolatedProperty", seg) )
      }
      if(seg contains "ChimpDriver-Exception") {
        driverExceptOpt = Some( extractValueWithKey("ChimpDriver-Exception", seg) )
      }
    }

    traceOpt match {
      case Some(traceStr) => {
        var trace:EventTrace = null
        try {
          trace = EventTrace.fromBase64( traceStr )
        } catch {
          case e:Exception => return ParseFailChimpOutcome( s"Trace ProtoBuf is malformed: $traceStr" )
        }
        outcomeStr match {
          case "Success" => return SuccChimpOutcome(trace)
          case "Crashed" => return CrashChimpOutcome(trace, stackTraceOpt match { case Some(st) => st ; case None => "Cannot find stacktrace" } )
          case "Blocked" => return BlockChimpOutcome(trace, stackTraceOpt)
          case "Unknown" => return UnknownChimpDriverChimpOutcome(Some(trace), stackTraceOpt)
          case "AssertFailed" => {
            propertyOpt match {
              case Some(propertyStr) => {
                var prop:Prop = null
                try { prop = Prop.fromBase64( propertyStr ) }
                catch {
                  case e:Exception => return ParseFailChimpOutcome( s"Property ProtoBuf is malformed: $propertyStr" )
                }
                return AssertFailChimpOutcome(trace, prop)
              }
              case None => return ParseFailChimpOutcome( s"Cannot find violated property" )
            }
          }
          case "DriverExcept" =>return ChimpDriverExceptChimpOutcome(Some(trace), driverExceptOpt)
          case default => return ParseFailChimpOutcome( "Cannot find outcome" )
        }
      }
      case None => {
        outcomeStr match {
          case "Unknown" => return UnknownChimpDriverChimpOutcome(None, Some("<TODO: Implement this>"))
          case default   => return ParseFailChimpOutcome( "Cannot find executed trace" )
        }
      }
    }

  }

  def kickBack(emuID:String, appPackageName:String, syncFilePath:String) (implicit bashLogger:Logger): Unit = {
    for {
    // Wait until the app is first started
      p <- repeat (Adb.target(emuID).shell("ps") #| Cmd(s"grep $appPackageName")) until {
        case SuccTry(Succ(c, o, e)) => c.split("\n").length > 0
        case default => false
      }
    } yield p

    Thread.sleep(5000)
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
              p0 <- Lift !!! Thread.sleep(2000) ;
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
      Thread.sleep(3000)
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