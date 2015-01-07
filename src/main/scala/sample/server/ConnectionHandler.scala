package sample.server

import akka.util.ByteString
import sample.protocol.{MyProtocol, DataParser}

import scala.collection.mutable.Map
import akka.actor.{ActorRef, Actor}
import akka.io.Tcp

class ConnectionHandler extends Actor {
  val connectedClients: Map[String, ActorRef] = Map()

  def registerClient(name: String, actor: ActorRef): Map[String, ActorRef] = {
    connectedClients += (name -> actor)
  }

  override def receive = {
    case Tcp.Received(data) =>
      val stringData = data.utf8String.trim
      println("Received data: " + stringData)
      val parsedData = DataParser.parse(stringData).get
      parsedData match {
        case MyProtocol("Register", value) =>
          registerClient(value, sender())
          println(value + " registerd")

        case MyProtocol("Message", value) =>
          println("Registered clients: " + connectedClients)
          connectedClients.foreach { case (_, actor) =>
            actor ! Tcp.Write(ByteString(value))
          }
      }
    case Tcp.PeerClosed =>
      context.stop(self)
  }
}