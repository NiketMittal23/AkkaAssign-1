package Akka_Ping_Pong

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

case object PingMsg
case object PongMsg
case object Start

object MessagingAndMessagingBack extends App {

  class Ping extends Actor with ActorLogging {
    override def receive: Receive = {
      case Start =>
        val childPong: ActorRef = context.actorOf(Props[Pong], "PongChild")
        childPong ! PingMsg
      case PongMsg => log.info("Pong")
    }
  }

  class Pong extends Actor with ActorLogging {
    override def receive: Receive = {
      case PingMsg =>
        log.info("ping")
        sender ! PongMsg
    }
  }

  val PingPong: ActorSystem = ActorSystem("PingPong")
  val Ping = PingPong.actorOf(Props[Ping], "Ping")
  Ping ! Start

}
