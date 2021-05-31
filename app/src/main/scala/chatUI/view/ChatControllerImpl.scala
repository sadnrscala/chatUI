package chatUI.view

import akka.actor.Address
import akka.actor.typed.ActorSystem
import chatUI.ChatPeerGuardian.{LoadChatHistory, SendPrivateMessage, SendTopicMessage}
import chatUI.model.{Message, PrivateTab, SystemUser, User}
import chatUI.{ChatPeerGuardian, MainApp}
import com.typesafe.config.ConfigFactory
import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{ListView, Tab}
import javafx.scene.input.{KeyCode, KeyEvent}
import org.w3c.dom.events.MouseEvent

import java.net.URL
import java.util.ResourceBundle



class ChatControllerImpl extends ChatController {

  private val _connectionAddress:StringProperty = new SimpleStringProperty("")
  def connectionAddress: String = _connectionAddress.get
  def connectionAddress_=(newValue: String): Unit = _connectionAddress.set(newValue)

  private val _connectionPort:StringProperty = new SimpleStringProperty("")
  def connectionPort: String = _connectionPort.get
  def connectionPort_=(newValue: String): Unit = _connectionPort.set(newValue)


  @FXML
  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    connectAddressTextField.textProperty().bindBidirectional(_connectionAddress)
    connectPortTextField.textProperty().bindBidirectional(_connectionPort)
    usersContainer.setCellFactory(new UserCellFactory)
    messagesContainer.setCellFactory(new MessageCellFactory)
  }


  /**
   * connectButtonOnAction.
   * Handler for pressed connect button
   * */
  @FXML
  def connectButtonOnAction(e: ActionEvent): Unit = {
    connectionErrorLabel.setVisible(false)
    var formValid = true
    val host = connectionAddress.trim
    val port = connectionPort.trim

    if (!host.matches(
      "^" +
              "((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
              "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
              "$")) {
      formValid = false
    }

    if (!port.matches("^\\d+$") || port.toInt < 0 || port.toInt > 65535) {
      formValid = false
    }

    if (formValid) {

      if (myAddress.getText == s"${host}:${port}") {
        connectionErrorLabel.setText("You already connected to this network")
        connectionErrorLabel.setVisible(true)
      } else {
        val address: Address = new Address("akka", "ChatPeer", host, port.toInt)
        mainApp.actorSystem.terminate()
        connectionErrorLabel.setText("Connection...")
        connectionErrorLabel.setVisible(true)

        val config = ConfigFactory.parseString(s"""
      akka.cluster.roles=[${mainApp.myNickName}]
      """).withFallback(ConfigFactory.load("application.cluster.conf"))

        mainApp.actorSystem = ActorSystem(ChatPeerGuardian(this, address, mainApp.myNickName), "ChatPeer",
          config)
      }


    } else {
      connectionErrorLabel.setText("Invalid host/port format")
      connectionErrorLabel.setVisible(true)
    }

  }


  /**
   * messageAreaKeyPressed.
   * Handler for user message area input<br>
   * send message to recipients if pressed SHIFT + ENTER
   * */
  @FXML
  def messageAreaKeyPressed(e: KeyEvent): Unit = {
    if (e.getCode == KeyCode.ENTER && e.isShiftDown) {

      val userMessage = messageArea.getText.trim
      messageArea.setText("") // clear user text area

      val selectedTab = tabsContainer.getSelectionModel.getSelectedItem
      val recipient = selectedTab.getUserData
      if (recipient == null) {
        // message to main channel
        mainApp.actorSystem ! SendTopicMessage(userMessage)
      } else {
        // private message

        val messagesData = mainApp.allMessagesData.getOrElse(recipient.asInstanceOf[PrivateTab].name, {
          mainApp.allMessagesData.addOne(recipient.asInstanceOf[PrivateTab].name, FXCollections.observableArrayList[Message])
          mainApp.allMessagesData(recipient.asInstanceOf[PrivateTab].name)
        })

        messagesData.add(new Message(s"You send private message: ${userMessage}", new SystemUser))
        mainApp.actorSystem ! SendPrivateMessage(userMessage, recipient.asInstanceOf[PrivateTab].name)
      }

    }
  }

  override def mainApp: MainApp = _mainApp
  override def mainApp_=(c:MainApp): Unit = {
    _mainApp = c

    usersContainer.setItems(mainApp.usersData)
    //messagesContainer.setItems(mainApp.messagesData)
    messagesContainer.setItems(mainApp.allMessagesData("main"))

    // При выборе пользователя в списке пользователей,
    // устанавливает в поле ввода команду для написания ему личного сообщения


    usersContainer.getSelectionModel.selectedItemProperty.addListener((_, oldValue, newValue) => {

      if (newValue != null) {
        val whisperTab = tabsContainer.getTabs
          .filtered(_.getUserData != null)
          .filtered(_.getUserData.asInstanceOf[PrivateTab].name == newValue.name)
        val isTabExist = whisperTab.size() > 0
        if (!isTabExist) {

          val messagesData = mainApp.allMessagesData.getOrElse(newValue.name, {
            mainApp.allMessagesData.addOne(newValue.name,FXCollections.observableArrayList[Message])
            mainApp.allMessagesData(newValue.name)
          })
          //create tab
          val newTab = createWhisperTab(newValue.nickName, newValue.name, messagesData)

          tabsContainer.getTabs.add(newTab)
          tabsContainer.getSelectionModel.select(newTab)
        } else {
          tabsContainer.getSelectionModel.select(whisperTab.get(0))
        }
        //messageArea.setText(s"/w ${newValue.name} ${messageArea.getText}")
      }
    })

    tabsContainer.getSelectionModel.selectedItemProperty()addListener((_, oldValue, newValue) => {
      if (newValue != null) {
        if (newValue.getUserData != null) {
          val tabData = newValue.getUserData.asInstanceOf[PrivateTab]
          tabData.unread = 0
          newValue.setText(tabData.nickWithWho)
        }
      }
    })
  }
  override def createWhisperTab(nickNameWithWho: String,
                               realName: String,
                               messagesData: ObservableList[Message]): Tab = {
    //create tab
    val newTab:Tab = new Tab(nickNameWithWho)

    newTab.setUserData(new PrivateTab(nickNameWithWho, realName))


    //create messages container
    val newMessagesContainer = new ListView[Message]
    newMessagesContainer.setCellFactory(new MessageCellFactory)

    // pin messages data to listview in tab
    newMessagesContainer.setItems(messagesData)
    newTab.setContent(newMessagesContainer)

    newTab
  }
}
