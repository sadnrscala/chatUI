package chatUI

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.Member
import chatUI.model.User
import chatUI.view.ChatController
import javafx.collections.FXCollections

import java.util
import java.util.concurrent.Executors
import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext


object ActorUI {

  sealed trait commandUI
  case class UpdateCurrentMembers(members: SortedSet[Member]) extends commandUI
  case class SetController(c:ChatController) extends commandUI

  private var chatController:ChatController = _

  val behavior: Behavior[commandUI] = Behaviors.receive { (_,message) =>
    message match {
      case UpdateCurrentMembers(members) =>
        members foreach { m =>
          chatController.mainApp.usersData.add(
            new User(s"${m.address.host.get}:${m.address.port.get}")
          )
        }

      case SetController(c) =>
        chatController = c
      case _ => println("unknown event")
    }
    Behaviors.same
  }
}