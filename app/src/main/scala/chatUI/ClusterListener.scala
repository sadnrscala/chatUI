package chatUI

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, ReachabilityEvent, ReachableMember, UnreachableMember}
import akka.cluster.{Member, MemberStatus}
import akka.cluster.typed.{Cluster, Subscribe}
import chatUI.model.User
import chatUI.view.ChatController
import javafx.application.Platform

import scala.collection.immutable.SortedSet

object ClusterListener {

  sealed trait Event
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Event
  private final case class MemberChange(event: MemberEvent) extends Event


  def apply(controller:ChatController): Behavior[Event] = Behaviors.setup { ctx =>

    val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange)
    Cluster(ctx.system).subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])


    val reachabilityAdapter = ctx.messageAdapter(ReachabilityChange)
    Cluster(ctx.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

    def updateMembers(members: SortedSet[Member]) = {
      Platform.runLater({() =>
        val usersData = controller.mainApp.usersData
        usersData.removeAll(usersData)

        members filter { member =>
          member.status == MemberStatus.Up || member.status == MemberStatus.Joining
        } foreach { m =>
          usersData.add(new User(s"${m.address.host.get}: ${m.address.port.get}"))
        }
      })
  }

    Behaviors.receiveMessage { message =>
      message match {
        case ReachabilityChange(reachabilityEvent) =>
          reachabilityEvent match {
            case UnreachableMember(member) =>
              ctx.log.info("Member detected as unreachable: {}", member)
            case ReachableMember(member) =>
              ctx.log.info("Member back to reachable: {}", member)

          }

        case MemberChange(changeEvent) =>
          changeEvent match {
            case MemberUp(member) =>
              ctx.log.info("Member is Up: {}", member.address)

            case MemberRemoved(member, previousStatus) =>
              ctx.log.info("Member is Removed: {} after {}", member.address, previousStatus)

            case _: MemberEvent =>
              ctx.log.info("unknown event1") // ignore
          }
        case _ =>
          ctx.log.info("unknown event2")
      }
      val members = Cluster(ctx.system).state.members
      updateMembers(members)

      Behaviors.same
    }
  }
}