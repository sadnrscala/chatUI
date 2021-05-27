package chatUI.view

import chatUI.MainApp
import chatUI.model.{ChatChannel, ChatTab, Message, TOPIC, User}
import javafx.beans.property.{BooleanProperty, IntegerProperty, SimpleBooleanProperty, SimpleIntegerProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}
import javafx.beans.value.{ChangeListener, ObservableBooleanValue, ObservableObjectValue}
import javafx.collections.{FXCollections, ListChangeListener, ObservableSet, SetChangeListener}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{ListView, Tab}
import javafx.scene.input.{KeyCode, KeyEvent}

import java.net.URL
import java.util.ResourceBundle



class ChatControllerImpl extends ChatController {


  private val _tabsData:ObservableSet[ChatTab] = FXCollections.observableSet()
  def tabsData: ObservableSet[ChatTab] = _tabsData

  private val _connectionAddress:StringProperty = new SimpleStringProperty("")
  def connectionAddress: String = _connectionAddress.get
  def connectionAddress_=(newValue: String): Unit = _connectionAddress.set(newValue)

  private val _connectionPort:StringProperty = new SimpleStringProperty("")
  def connectionPort: String = _connectionPort.get
  def connectionPort_=(newValue: String): Unit = _connectionPort.set(newValue)


  @FXML
  def testHandler(e: ActionEvent): Unit = {
    //connectFormInvalid = false
  }


  @FXML
  override def initialize(location: URL, resources: ResourceBundle): Unit = {

    connectAddressTextField.textProperty().bindBidirectional(_connectionAddress)
    connectPortTextField.textProperty().bindBidirectional(_connectionPort)



    usersContainer.pinToTabPane(tabsData, chatTabsContainer)

    channelsContainer.setCellFactory(new ChannelCellFactory())
    channelsContainer.getSelectionModel.selectedItemProperty.addListener((_, _, newValue) => {
      val newChatTab = new ChatTab(newValue.name, TOPIC)
      tabsData.add(newChatTab)
      chatTabsContainer.getTabs.filtered(_.getText == newChatTab.name).forEach(tab => {
        chatTabsContainer.getSelectionModel.select(tab)
      })
    })

    testButton.setOnAction(testHandler)
  }


  @FXML
  def connectAddressTextFieldOnKeyReleased(e: KeyEvent): Unit = {
  }

  @FXML
  def connectPortTextFieldOnKeyReleased(e: KeyEvent): Unit = {
  }



  /**
   * connectButtonOnAction.
   * <hr>
   * Handler for pressed connect button
   * */
  @FXML
  def connectButtonOnAction(e: ActionEvent): Unit = {
    connectionErrorLabel.setVisible(false)
    var formValid = true
    val address = connectionAddress.trim
    val port = connectionPort.trim

    if ( ! address.matches(
      "^" +
              "((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
              "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" +
              "$")) {
      formValid = false
    }

    if ( ! port.matches("^\\d+$") || port.toInt < 0 || port.toInt > 65000) {
      formValid = false
    }

    if (formValid) {
      println("connection")
    } else {
      connectionErrorLabel.setVisible(true)
    }

  }


  /**
   * messageAreaKeyPressed.
   * <hr>
   * Handler for user message area input<br>
   * send message to recipients if pressed SHIFT + ENTER
   * */
  @FXML
  def messageAreaKeyPressed(e: KeyEvent): Unit = {
    if (e.getCode == KeyCode.ENTER && e.isShiftDown) {
      //actorChatClient ! NewChatMessage(DEFAULT_CHANNEL, messageInputArea.getText)

      println(s"sending message to " +
        s"${chatTabsContainer.getSelectionModel.selectedItemProperty().get().getText}" +
        s"message: ${messageArea.getText}")

      // clear user text area
      messageArea.setText("")
    }

  }


  @FXML
  def joinToChannelButtonHandler(): Unit = {
    val newChannel = new ChatChannel(joinToChannelInput.getText, 10);
    joinToChannelInput.setText(null)

    mainApp.channelsData.add(newChannel)
    tabsData.add(new ChatTab(newChannel.name, TOPIC))

    chatTabsContainer.getTabs.filtered(_.getText == newChannel.name).forEach(tab => {
      chatTabsContainer.getSelectionModel.select(tab)
    })
  }






  override def mainApp: MainApp = _mainApp
  override def mainApp_=(c:MainApp): Unit = {
    _mainApp = c

    usersContainer.setItems(mainApp.usersData)
    channelsContainer.setItems(mainApp.channelsData)


    tabsData.addListener(new SetChangeListener[ChatTab] {
      override def onChanged(change: SetChangeListener.Change[_ <: ChatTab]): Unit = {
        if (change.wasAdded) {
          val newTab = new Tab(change.getElementAdded.name)
          val newTabMessagesContainer = new ListView[Message]
          newTabMessagesContainer.setCellFactory(new MessageCellFactory())
          val newTabMessagesData = mainApp.messagesData("Main")
          newTabMessagesContainer.setItems(newTabMessagesData)
          newTab.setContent(newTabMessagesContainer)

          chatTabsContainer.getTabs.add(newTab)
        }
        if (change.wasRemoved) {
          chatTabsContainer.getTabs.removeIf(_.getText == change.getElementRemoved.name)
        }
      }
    })

    chatTabsContainer.getTabs.addListener(new ListChangeListener[Tab] {
      override def onChanged(c: ListChangeListener.Change[_ <: Tab]): Unit = {
        c.next
        if (c.wasRemoved) {
          c.getRemoved forEach(el => tabsData.removeIf(_.name == el.getText))
        }
      }
    })
  }

}
