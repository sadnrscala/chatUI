package chatUI

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import chatUI.ActorUI.commandUI
import com.typesafe.config.{Config, ConfigFactory}
import javafx.application.Application.launch

object App {
  object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      // Create an actor that handles cluster domain events
      context.spawn(ClusterListener(), "ClusterListener")

      Behaviors.empty
    }
  }



  val config: Config = ConfigFactory.load("application.cluster.conf")

  val localSystem: ActorSystem[commandUI] = ActorSystem(ActorUI.behavior, "systemUI")


  def main(args: Array[String]): Unit = {

    launch(classOf[MainApp])
  }
}
