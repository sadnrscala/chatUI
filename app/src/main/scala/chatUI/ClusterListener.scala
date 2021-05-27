package chatUI

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, UnreachableMember}

class ClusterListener extends Actor with ActorLogging{
  override def receive: Receive = {

    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      //val actorUI = App.localSystem.actorSelection("user/actorUI") //akka://local/user/actorUI
      //actorUI ! ListClusterMembers(App.cluster.state.members)

    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      //val actorUI = App.localSystem.actorSelection("user/actorUI") //akka://local/user/actorUI
      //actorUI ! ListClusterMembers(App.cluster.state.members)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      //val actorUI = App.localSystem.actorSelection("user/actorUI") //akka://local/user/actorUI
      //actorUI ! ListClusterMembers(App.cluster.state.members)

    case _: MemberEvent => // ignore
  }

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    // subscribe
    // App.cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = {
    // App.cluster.unsubscribe(self)
  }
}
