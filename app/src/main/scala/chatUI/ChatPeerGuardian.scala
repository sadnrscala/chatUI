package chatUI

import akka.actor.Address
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}
import akka.cluster.MemberStatus
import akka.cluster.typed.{Cluster, Join, Subscribe}
import chatUI.model.{Message, SystemUser, User}
import chatUI.view.ChatController
import javafx.application.Platform



object ChatPeerGuardian {
  sealed trait ChatCommand

  case class SendPrivateMessage(content: String, receiver: String) extends ChatCommand
  case class SendTopicMessage(content:String) extends ChatCommand
  case class ReceiveMessage(message: Message) extends ChatCommand with CborSerializable
  case object LoadChatHistory extends ChatCommand
  case class RequestChatHistory(replyTo: ActorRef[ChatCommand]) extends ChatCommand with CborSerializable
  case class ResponseChatHistory(history: List[Message]) extends ChatCommand with CborSerializable

  private final case class MemberChange(event: MemberEvent) extends ChatCommand

  def apply(chatController: ChatController, seed: Address = null): Behavior[ChatCommand] =
    Behaviors.setup[ChatCommand] { ctx =>

      //# setup
      val cluster = Cluster(ctx.system)
      val firstSeedAddress = {
        if (seed != null) seed else cluster.selfMember.address
      }

      cluster.manager ! Join(firstSeedAddress)

      val idKey = s"${cluster.selfMember.address.host.get}:${cluster.selfMember.address.port.get}"
      chatController.myAddress.setText(idKey)
      val selfServiceKey = ServiceKey[ChatCommand](idKey)
      ctx.system.receptionist ! Receptionist.Register(selfServiceKey, ctx.self)

      val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange)
      cluster.subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

      val topic = ctx.spawn(Topic[ReceiveMessage]("default"), "topic-1")
      topic ! Topic.Subscribe(ctx.self)

      val currentUser = new User(s"${cluster.selfMember.address.host.get}:${cluster.selfMember.address.port.get}")
      //# setup

      def chatBehavior(history: List[Message] = List()): Behavior[ChatCommand] =
        Behaviors.receiveMessage[ChatCommand] {
          case MemberChange(changeEvent) =>
            changeEvent match {
              case MemberUp(member) =>
                if (member.address == cluster.selfMember.address) {
                  Platform.runLater(() => {
                    chatController.connectionErrorLabel.setText("Connected")
                  })
                  ctx.self ! LoadChatHistory
                }
              case _ => //ignore
            }

            Platform.runLater({() =>
              val members = cluster.state.members
              val usersData = chatController.mainApp.usersData
              usersData.removeAll(usersData)

              members filter { member =>
                member.status == MemberStatus.Up || member.status == MemberStatus.Joining
              } foreach { m =>
                usersData.add(new User(s"${m.address.host.get}:${m.address.port.get}"))
              }
            })

            Behaviors.same
          case SendPrivateMessage(content, receiver) =>
            ctx.spawn(MessageSender(content, receiver, ctx.self, currentUser), "sender")
            Behaviors.same

          case ReceiveMessage(message) =>

            Platform.runLater({() =>
              val messagesData = chatController.mainApp.messagesData
              messagesData.add(message)
            })

            if (!message.direct && !message.owner.isInstanceOf[SystemUser]) {
              chatBehavior(message +: history)
            } else {
              Behaviors.same
            }

          case SendTopicMessage(content) =>
            topic ! Topic.Publish(ReceiveMessage(new Message(content, currentUser)))
            Behaviors.same
          case LoadChatHistory =>
            ctx.spawn(HistoryGrabber(s"${firstSeedAddress.host.get}:${firstSeedAddress.port.get}", ctx.self), "historyGrabber")
            Behaviors.same

          case RequestChatHistory(replyTo) =>
            println(history)
            replyTo ! ResponseChatHistory(history)
            Behaviors.same
          case ResponseChatHistory(history) =>
            Platform.runLater(() => {
              val messages = chatController.mainApp.messagesData
              messages.removeAll(messages)
              messages.addAll(history.reverse:_*)
            })

            Behaviors.same
          case _ =>
            println("unknown message")
            Behaviors.same
        }

      chatBehavior()
    }
}