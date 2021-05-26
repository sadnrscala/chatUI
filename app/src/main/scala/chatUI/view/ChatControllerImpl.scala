package chatUI.view

import chatUI.MainApp
import chatUI.model.{ChatChannel, ChatTab, TOPIC, WHISPER}
import javafx.collections.{FXCollections, ListChangeListener, ObservableSet, SetChangeListener}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Tab

import java.net.URL
import java.util.ResourceBundle


class ChatControllerImpl extends ChatController {

  private val _tabsData:ObservableSet[ChatTab] = FXCollections.observableSet()
  def tabsData: ObservableSet[ChatTab] = _tabsData


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

  @FXML
  override def initialize(location: URL, resources: ResourceBundle): Unit = {

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
  def testHandler(e: ActionEvent): Unit = {
    println("testing")
    mainApp.channelsData.add(new ChatChannel("testtet"))
  }



  override def mainApp: MainApp = _mainApp
  override def mainApp_=(c:MainApp): Unit = {
    _mainApp = c

    usersContainer.setItems(mainApp.usersData)
    channelsContainer.setItems(mainApp.channelsData)


    tabsData.addListener(new SetChangeListener[ChatTab] {
      override def onChanged(change: SetChangeListener.Change[_ <: ChatTab]): Unit = {
        if (change.wasAdded) {
          chatTabsContainer.getTabs.add(new Tab(change.getElementAdded.name))
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
