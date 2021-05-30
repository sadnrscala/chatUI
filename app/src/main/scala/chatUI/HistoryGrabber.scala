package chatUI

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import chatUI.ChatPeerGuardian.{ChatCommand, ReceiveMessage, RequestChatHistory}
import chatUI.model.{Message, SystemUser, User}

object HistoryGrabber {
  sealed trait HistoryGrabberCommand
  case class ListingResponse(listing: Receptionist.Listing) extends HistoryGrabberCommand

  def apply(recipientKey: String, parent: ActorRef[ChatCommand]): Behavior[HistoryGrabberCommand] =
    Behaviors.setup[HistoryGrabberCommand] { ctx =>

    val RecipientKey: ServiceKey[ChatCommand] = ServiceKey[ChatCommand](recipientKey)
    val listingResponseAdapter = ctx.messageAdapter[Receptionist.Listing](ListingResponse)
    //ctx.system.receptionist ! Receptionist.Find(RecipientKey, listingResponseAdapter)
    ctx.system.receptionist ! Receptionist.Subscribe(RecipientKey, listingResponseAdapter)
    Behaviors.receiveMessage[HistoryGrabberCommand] {
      case ListingResponse(RecipientKey.Listing(listing)) =>
        listing.foreach { recipient =>

          recipient ! RequestChatHistory(parent)
          Behaviors.stopped
        }
        Behaviors.same
    }
  }
}