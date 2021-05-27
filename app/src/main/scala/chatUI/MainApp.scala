/*
 * This Scala source file was generated by the Gradle 'init' task.
 */
package chatUI

import akka.actor.{ActorRef, ActorSystem, Props}
import chatUI.ActorUI.SetController
import chatUI.model.{ChatChannel, Message, User}
import chatUI.view.ChatController
import com.typesafe.config.ConfigFactory
import javafx.application.Application
import javafx.collections.{FXCollections, ObservableList}
import javafx.fxml.FXMLLoader
import javafx.scene.layout.HBox
import javafx.scene.Scene
import javafx.stage.Stage

import scala.collection.mutable.HashMap



class MainApp extends Application {

  private var _stage:Stage = _
  private var _appLayout:HBox = _
  private var _messagesData:HashMap[String, ObservableList[Message]] = new HashMap[String, ObservableList[Message]]()
  private val _usersData:ObservableList[User] = FXCollections.observableArrayList
  private val _channelsData:ObservableList[ChatChannel] = FXCollections.observableArrayList


  // creating Actor Systems
  private val _localActorSystem = ActorSystem("localSystem")
  private var _clusterActorSystem: ActorSystem = ActorSystem("ChatUser",
    ConfigFactory.load("application_cluster.conf"))

  def localActorSystem: ActorSystem = _localActorSystem
  def clusterActorSystem: ActorSystem = _clusterActorSystem
  def clusterActorSystem_=(actorSystem: ActorSystem): Unit = _clusterActorSystem = actorSystem

  // create actors
  private val _actorUI:ActorRef = localActorSystem.actorOf(Props(classOf[ActorUI]), "actorUI")
  def actorUI: ActorRef = _actorUI


  /**
   * start.
   * <hr>
   * GUI start here
   * */
  override def start(primaryStage: Stage): Unit = {

    stage = primaryStage
    stage.setTitle("p2p chat")

    initAppLayout()
  }

  /** stop.
   * <hr>
   * When GUI stop, terminate ActorSystems
   * */
  override def stop(): Unit = {
    localActorSystem.terminate()
    clusterActorSystem.terminate()
  }

  def initAppLayout(): Unit = {

    val loader = new FXMLLoader()
    loader.setLocation(getClass.getResource("view/ChatWindow.fxml"))

    appLayout = loader.load
    val scene = new Scene(appLayout)

    val chatController:ChatController = loader.getController
    actorUI ! SetController(chatController)
    chatController.mainApp = this

    stage.setScene(scene)
    stage.show()

    usersData.add(new User("user 1"))
    usersData.add(new User("user 2"))

    messagesData.addOne("Main", FXCollections.observableArrayList())
    messagesData("Main").add(new Message("privet", new User("asadasd")))
  }


  // setters/getters
  def usersData: ObservableList[User] = _usersData
  def messagesData: HashMap[String, ObservableList[Message]] = _messagesData
  def channelsData: ObservableList[ChatChannel] = _channelsData

  def appLayout: HBox = _appLayout
  def appLayout_=(el:HBox) = _appLayout = el

  def stage: Stage = _stage
  def stage_= (s:Stage): Unit = _stage = s
}