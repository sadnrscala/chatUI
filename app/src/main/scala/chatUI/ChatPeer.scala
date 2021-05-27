package chatUI

import akka.actor.{Actor, ActorLogging}

class ChatPeer extends Actor with ActorLogging {
  override def receive: Receive = {
    case _ =>
      log.warning("ChatPeer Unknown message received")
  }
}
