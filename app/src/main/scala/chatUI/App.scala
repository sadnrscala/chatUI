package chatUI

import akka.actor.Address
import akka.actor.typed.{Behavior}
import akka.actor.typed.scaladsl.Behaviors

import akka.cluster.typed.{Cluster, Join}
import chatUI.view.ChatController
import javafx.application.Application.launch

object App {


  def main(args: Array[String]): Unit = {

    launch(classOf[MainApp])
  }
}

object ChatGuardian {
  sealed trait ChatCommand
  case class Connect(peer: String) extends ChatCommand


  def apply(controller: ChatController, seed: Address = null): Behavior[ChatCommand] =
    Behaviors.setup { context =>

      context.spawn(ClusterListener(controller), "ActorCluster")

      val cluster = Cluster(context.system)

      cluster.manager ! Join({
        if (seed != null) seed else cluster.selfMember.address
      })


      Behaviors.receive { (context, message) =>
        message match {
          case Connect(m) =>
            val cluster = Cluster(context.system)
            cluster.manager ! Join(cluster.selfMember.address)
        }

        Behaviors.same
      }
    }
}
