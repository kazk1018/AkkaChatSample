package sample.server

import java.net.InetSocketAddress

import akka.actor.{Props, ActorSystem, Actor, ActorRef}
import akka.io.{Tcp, IO}

object ChatServer extends App {
  val system = ActorSystem("ChatServer")
  val server = system.actorOf(Props[ChatServerActor])
}

class ChatServerActor extends Actor {
  import context.system

  val manager = IO(Tcp)
  manager ! Tcp.Bind(self, new InetSocketAddress("localhost", 9000))

  override def receive = {
    case Tcp.Bound(local) => println("Bound")
    case Tcp.CommandFailed(_) => context.stop(self)
    case Tcp.Connected(remote, local) => {
      println("Server: Connected " + local)
      val handler = context.actorOf(Props[ConnectionHandler])
      sender ! Tcp.Register(handler)
    }
  }
}
