package Akka_Ping_Pong

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object StateOfActorAndFutures extends App {

  case object PingMsg
  case object PongMsg
  case object StartMsg
  case class End(Pings: Int)
  case object FromPingGetPongSum
  case class ThrowException()
  case class GetPongSum(sum: Option[Int])

  class Ping extends Actor with ActorLogging {
    val childPong: ActorRef = context.actorOf(Props[PongActor], "pong")
    var sum = 0
    var count = 0
    override def receive: Receive = {
      case StartMsg =>
        for {
          _ <- 1 to 10000
        } yield childPong ! PingMsg
      case PongMsg => count += 1
        sum += 1
      case GetPongSum(pongSum) =>
        println(s"Pong sum: $pongSum")
        sender ! ThrowException
      case End(pingSum) =>
        println(s"Sum: $pingSum")
        println(s"count: $count")
        sender ! GetPongSum(None)
      case FromPingGetPongSum =>
        childPong ! GetPongSum(None)
    }
  }

  class PongActor extends Actor with ActorLogging {
    var sum: Int = 0
    def doWork(): Int = {
     // Thread sleep 1000
      1
    }

    override def receive: Receive = {
      case PingMsg =>
        val Sum = Future {
          sum += doWork()
        }
        Await.result(Sum, Duration.Inf)
        if (sum < 10000) {
          sender ! PongMsg
        } else if (sum == 10000) {
          sender ! End(sum)
        }
      case GetPongSum(None) =>
        val Sum2: Option[Int] = Some(sum)
        sender ! GetPongSum(Sum2)
      case ThrowException =>
        throw new Exception()
    }
  }

  val pingPong: ActorSystem = ActorSystem("PingPong")
  val Ping = pingPong.actorOf(Props[Ping], "Ping")
  Ping ! StartMsg
}
