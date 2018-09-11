import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.io.StdIn
import scala.concurrent.duration._
//import scala.concurrent.Lock

/**
  * Created by chanceroberts on 8/9/18.
  */
object Server {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  //val lock = new Lock()

  /*
  def lockedroute(conf: Config): server.Route = {
    if (!lock.available) complete("The ChimpCheck Demo Blocked!")
    lock.acquire
    val route = runChimpCheck(conf)
    lock.release
    route
  }
  */

  def errorToMessage(err: Exception): String = {
    JsObject("color" -> JsString("#888888"), "status" -> JsString("Interal Err."),
              "stackTrace" -> JsString(""), "eventTrace" -> JsString(err.getMessage)).prettyPrint
  }

  def runChimpCheck(conf: Config): server.Route = {
    withRequestTimeout(1.hour) {
      post {
        entity(as[String]) {
          queryStr =>
            path("setUp") {
              try {
                complete(ServerLogic.setUpEmulator(queryStr, conf))
              } catch {
                case e: Exception => complete(errorToMessage(e))
              }
            } ~
            path("runADB") {
              try {
                complete(ServerLogic.runAnEmulator(queryStr, conf))
              } catch {
                case e: Exception => complete(errorToMessage(e))
              }
            } ~
            path("tearDown") {
              try {
                complete(ServerLogic.closeAnEmulator(queryStr, conf))
              } catch {
                case e: Exception => complete(errorToMessage(e))
              }
            }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    //val route = lockedroute(conf)
    val route = runChimpCheck(conf)
    val port = if (args.length > 0){
      args(0).toInt
    } else conf.getInt("port")
    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", port)
    println(s"Chimpcheck Driver started on Port $port!")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
