import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.Config
import spray.json._

/**
  * Created by chanceroberts on 8/9/18.
  */
object Server {
  def runChimpCheck(conf: Config): server.Route = {
    post{
      entity(as[String]){
        queryStr =>
          try {
            val finalString = ServerLogic.runAnEmulator(queryStr, conf)
            complete(finalString)
          } catch {
            case e: Exception => complete(e.getMessage())
          }
      }
    }
  }

  def main(args: Array[String]): Unit = {

  }
}
