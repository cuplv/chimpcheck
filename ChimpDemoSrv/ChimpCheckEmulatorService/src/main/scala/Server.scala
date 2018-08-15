import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.io.StdIn

/**
  * Created by chanceroberts on 8/9/18.
  */
object Server {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  def runChimpCheck(conf: Config): server.Route = {
    extractClientIP { ip =>
      val stringIP = s"${ip.toOption.map(_.getHostAddress()).getOrElse("")}:${ip.getPort()}"
      post {
        entity(as[String]) {
          queryStr =>
            path("setUp"){
              try{
                complete(ServerLogic.setUpEmulator(queryStr, conf, stringIP))
              } catch {
                case e: Exception => complete(e.getMessage())
              }
            }~
            path("runADB") {
              try {
                complete(ServerLogic.runAnEmulator(queryStr, conf, stringIP))
              } catch {
                case e: Exception => complete(e.getMessage())
              }
            }~
            path("tearDown"){
              try{
                complete(ServerLogic.closeAnEmulator(queryStr, conf, stringIP))
              } catch {
                case e: Exception => complete(e.getMessage())
              }
            }
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    val route = runChimpCheck(conf)
    val port = if (args.length > 0){
      args(0).toInt
    } else conf.getInt("port")
    val bindingFuture = Http().bindAndHandle(route, "localhost", port)
    println(s"Chimpcheck Driver started on Port $port!")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
