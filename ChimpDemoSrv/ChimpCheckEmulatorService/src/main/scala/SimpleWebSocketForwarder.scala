/**
  * Created by chanceroberts on 8/13/18.
  */
import Server.system
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import spray.json._

import scala.io.StdIn

object SimpleWebSocketForwarder {
  var ipDir: Map[String, String] = Map()
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  def makeWebSocket: Route = {
    get{
      extractClientIP {
        ip =>
          val stringIP = s"${ip.toOption.map(_.getHostAddress()).getOrElse("")}:${ip.getPort()}"
          handleWebSocketMessages(makeTwoWayConnection(stringIP))
      }
    } ~
    post {
      entity(as[String]){
        queryStr =>
          path("add") {
            val json = queryStr.parseJson.asJsObject
            ipDir = (json.fields.get("clientIP"), json.fields.get("streamingIP")) match {
              case (Some(JsString(cIP)), Some(JsString(sIP))) =>
                ipDir + (cIP -> sIP)
              case (_, _) => ipDir
            }
            complete("")
          }~
          path("remove") {
            val json = queryStr.parseJson.asJsObject
            ipDir = (json.fields.get("clientIP"), json.fields.get("streamingIP")) match{
              case (Some(JsString(cIP)), Some(JsString(sIP))) =>
                ipDir.filter{case (key, str) => !(key.equals(cIP) && str.equals(sIP))}
              case (Some(JsString(cIP)), _) =>
                ipDir.filter{case (key, _) => !key.equals(cIP)}
              case (_, Some(JsString(sIP))) =>
                ipDir.filter{case (_, str) => !str.equals(sIP)}
            }
            complete("")
          }
      }
    }
  }

  def makeTwoWayConnection(clientIP: String): Flow[Message, Message, Any] = {
    if (!ipDir.contains(clientIP)){
      val sink: Sink[Message, Any] = Sink.asPublisher(false)
      val flow1: Flow[Message, Message, Any] = Flow.fromSinkAndSource(sink, Source.maybe)
      val source: Source[Message, Any] = Source.fromPublisher(flow1.toProcessor.run)
      val (_, _) = Http().singleWebSocketRequest(WebSocketRequest(s"ws://${ipDir(clientIP)}"), flow1)
      Flow.fromSinkAndSource(Sink.ignore, source)
    } else {
      Flow[Message]
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    val route = makeWebSocket
    val port = if (args.length > 0){
      args(0).toInt
    } else conf.getInt("webSocketPort")
    val bindingFuture = Http().bindAndHandle(route, "localhost", port)
    println(s"Web Socket Forwarder Started on port 19002!")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
