package chatUI

import akka.actor.{Actor, ActorLogging}
import chatUI.ActorUI.{ChatMessage, SetController}
import chatUI.model.Message
import chatUI.view.ChatController


object ActorUI {
  case class SetController(c:ChatController)
  case class ChatMessage(msg:Message)
}

class ActorUI extends Actor with ActorLogging {

  private var chatController:ChatController = _


  override def receive: Receive = {
    case ChatMessage(msg) =>
      log.info("ActorUI received message {}", msg)

    case SetController(c) =>
      chatController = c
      log.info("ActorUI received controller")

    case _ =>
      log.warning("ActorUI Unknown message received")
  }
}
