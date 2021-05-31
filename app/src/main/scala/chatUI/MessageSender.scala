package chatUI

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import chatUI.ChatPeerGuardian.{ChatCommand, ReceiveMessage}
import chatUI.model.{Message, SystemUser, User}

object MessageSender {
  sealed trait MessageSenderCommand
  case class ListingResponse(listing: Receptionist.Listing) extends MessageSenderCommand

  def apply(contentMessage: String, recipientKey: String, parent: ActorRef[ChatCommand], currentUser: User): Behavior[MessageSenderCommand] = Behaviors.setup[MessageSenderCommand] { ctx =>

    val RecipientKey: ServiceKey[ChatCommand] = ServiceKey[ChatCommand](recipientKey)
    val listingResponseAdapter = ctx.messageAdapter[Receptionist.Listing](ListingResponse)
    ctx.system.receptionist ! Receptionist.Find(RecipientKey, listingResponseAdapter)

    Behaviors.receiveMessage[MessageSenderCommand] {
      case ListingResponse(RecipientKey.Listing(listing)) =>
        if (listing.size < 1) {
          parent ! ReceiveMessage(new Message(
            "receiver offline",
            new SystemUser(recipientKey, "offline"),
            true))
        } else {
          listing.foreach { recipient =>
            recipient ! ReceiveMessage(new Message(contentMessage, currentUser, true))
          }
        }
        Behaviors.stopped
    }
  }
}