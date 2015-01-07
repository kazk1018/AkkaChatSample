package sample.client

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.io.{Tcp, IO}
import akka.util.ByteString

object ChatClient {
  def chat(connection: ActorRef) = {
    connection ! "Register: user1"
    Thread.sleep(1000)
    connection ! "Message: Hello"
  }

  def main(args: Array[String]) {
    val system = ActorSystem("ChatClient")
    val client = system.actorOf(Props[ChatClientActor])
    client ! "Start"
  }
}

class FinishNortifierActor extends Actor {
  override def receive = {
    case "Finish" =>
      ChatClient.chat(sender())
  }
}

class ChatClientActor extends Actor {
  import context.system
  val remote = new InetSocketAddress("localhost", 9000)

  val finishNotifier = context.actorOf(Props[FinishNortifierActor])

  val manager = IO(Tcp)
  manager ! Tcp.Connect(remote)

  override def receive = {
    case Tcp.Connected(remote, local) =>
      println("Client: Connected")
      val connection = sender()
      connection ! Tcp.Register(self)
      context become {
        case data: String =>
          connection ! Tcp.Write(ByteString(data))
        case data: ByteString =>
          connection ! Tcp.Write(data)
        case Tcp.Received(data) =>
          println("Client Received: " + data.utf8String.trim)
        case Tcp.CommandFailed(_) => println("command failed")
      }
      finishNotifier ! "Finish"
  }
}