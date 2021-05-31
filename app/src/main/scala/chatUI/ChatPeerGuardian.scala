package chatUI

import akka.actor.Address
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp}
import akka.cluster.MemberStatus
import akka.cluster.typed.{Cluster, Join, Subscribe}
import chatUI.model.{Message, PrivateTab, SystemUser, User}
import chatUI.view.ChatController
import javafx.application.Platform
import javafx.collections.FXCollections



object ChatPeerGuardian {
  sealed trait ChatCommand

  case class SendPrivateMessage(content: String, receiver: String) extends ChatCommand
  case class SendTopicMessage(content:String) extends ChatCommand
  case class ReceiveMessage(message: Message) extends ChatCommand with CborSerializable
  case object LoadChatHistory extends ChatCommand
  case class RequestChatHistory(replyTo: ActorRef[ChatCommand]) extends ChatCommand with CborSerializable
  case class ResponseChatHistory(history: List[Message]) extends ChatCommand with CborSerializable

  private final case class MemberChange(event: MemberEvent) extends ChatCommand

  def apply(chatController: ChatController, seed: Address = null, myNickName: String): Behavior[ChatCommand] =
    Behaviors.setup[ChatCommand] { ctx =>

      //# setup
      val cluster = Cluster(ctx.system)
      //##


      val firstSeedAddress = {
        if (seed != null) seed else cluster.selfMember.address
      }

      cluster.manager ! Join(firstSeedAddress)

      val idKey = s"${cluster.selfMember.address.host.get}:${cluster.selfMember.address.port.get}"
      Platform.runLater(() => {
        chatController.myAddress.setText(idKey)
      })

      val selfServiceKey = ServiceKey[ChatCommand](idKey)
      ctx.system.receptionist ! Receptionist.Register(selfServiceKey, ctx.self)

      val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange)
      cluster.subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

      val topic = ctx.spawn(Topic[ReceiveMessage]("default"), "topic-1")
      topic ! Topic.Subscribe(ctx.self)

      val currentUser = new User(
        s"${cluster.selfMember.address.host.get}:${cluster.selfMember.address.port.get}",
        myNickName
      )
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
                usersData.add(new User(s"${m.address.host.get}:${m.address.port.get}", m.roles.head))
              }
            })

            Behaviors.same
          case SendPrivateMessage(content, receiver) =>
            ctx.spawn(MessageSender(content, receiver, ctx.self, currentUser), "sender")
            Behaviors.same

          case ReceiveMessage(message) =>

            Platform.runLater({() =>

              if (message.direct) {
                // "private" message
                // create tab for it, if not exists

                // create messages data container
                val messagesData = chatController.mainApp.allMessagesData.getOrElse(message.owner.name, {
                  chatController.mainApp.allMessagesData.addOne(message.owner.name,FXCollections.observableArrayList[Message])
                  chatController.mainApp.allMessagesData(message.owner.name)
                })
                messagesData.add(message)

                val whisperTab = chatController.tabsContainer.getTabs
                  .filtered(_.getUserData != null)
                  .filtered(_.getUserData.asInstanceOf[PrivateTab].name == message.owner.name)
                val isTabExist = whisperTab.size() > 0


                if (!isTabExist) {

                  //create tab
                  val newTab = chatController.createWhisperTab(message.owner.nickName, message.owner.name, messagesData)
                  val tabData = newTab.getUserData.asInstanceOf[PrivateTab]
                  val newLabel = s"${tabData.nickWithWho} (${tabData.unread})"
                  newTab.setText(newLabel)
                  chatController.tabsContainer.getTabs.add(newTab)

                } else {

                  if (!whisperTab.get(0).isSelected) {
                    val tabData = whisperTab.get(0).getUserData.asInstanceOf[PrivateTab]
                    tabData.unread = tabData.unread + 1
                    val newLabel = s"${tabData.nickWithWho} (${tabData.unread})"
                    whisperTab.get(0).setText(newLabel)
                  }
                }

              } else {
                val messagesData = chatController.mainApp.allMessagesData("main")
                messagesData.add(message)
              }
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
           // println(history)
            replyTo ! ResponseChatHistory(history)
            Behaviors.same
          case ResponseChatHistory(newHistory) =>
            Platform.runLater(() => {
              val messages = chatController.mainApp.allMessagesData("main")
              messages.removeAll(messages)
              messages.addAll(newHistory.reverse:_*)
            })

            chatBehavior(newHistory)
            //Behaviors.same
          case _ =>
            println("unknown message")
            Behaviors.same
        }

      chatBehavior()
    }
}