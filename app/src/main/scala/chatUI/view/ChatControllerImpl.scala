package chatUI.view

import akka.actor.Address
import akka.actor.typed.ActorSystem
import chatUI.ChatPeerGuardian.{LoadChatHistory, SendPrivateMessage, SendTopicMessage}
import chatUI.model.{Message, SystemUser, User}
import chatUI.{ChatPeerGuardian, MainApp}
import com.typesafe.config.ConfigFactory
import javafx.beans.property.{SimpleStringProperty, StringProperty}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.input.{KeyCode, KeyEvent}

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
        mainApp.actorSystem = ActorSystem(ChatPeerGuardian(this, address), "ChatPeer",
          ConfigFactory.load("application.cluster.conf"))
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

      if (!userMessage.startsWith("/")) {
        mainApp.actorSystem ! SendTopicMessage(userMessage)
      } else {
        // сообщения начинающиеся с / - это команды чата
        val whisperRegexp = """(?s)^/w\s([^\s]+)\s(.+)""".r
        if (whisperRegexp.matches(userMessage)) {
            // приватное сообщение
            val whisperRegexp(nick, msg) = userMessage
            mainApp.messagesData.add(new Message(s"You send private message to ${nick}: ${msg}", new SystemUser))
            mainApp.actorSystem ! SendPrivateMessage(msg, nick)
        } else {
          if (userMessage == "/?") {
            mainApp.messagesData.add(new Message(
              """List available commands:
                |/? - show this help
                |/w - send private message to user
                | Usage: /w <nickname> <message>
                | Example: /w 192.168.1.1:7777 Hello sadnr!
                |""".stripMargin, new SystemUser))
          } else {
            mainApp.messagesData.add(new Message("Unknown command, type /? for help", new SystemUser))
          }
        }
      }
    }
  }


  override def mainApp: MainApp = _mainApp
  override def mainApp_=(c:MainApp): Unit = {
    _mainApp = c

    usersContainer.setItems(mainApp.usersData)
    messagesContainer.setItems(mainApp.messagesData)

    // При выборе пользователя в списке пользователей,
    // устанавливает в поле ввода команду для написания ему личного сообщения
    usersContainer.getSelectionModel.selectedItemProperty.addListener((_, oldValue, newValue) => {
      if (newValue != null) {
        messageArea.setText(s"/w ${newValue.name} ${messageArea.getText}")
      }
    })
  }
}
