package chatUI.view

import chatUI.MainApp
import chatUI.model.{ChatChannel, ChatTab, User, WHISPER}
import javafx.collections.ObservableSet
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, ListView, TabPane, TextField}


abstract class ChatController extends Initializable {

  protected var _mainApp:MainApp = _

  @FXML protected var testButton:Button = _
  @FXML protected var usersContainer:UserListView = _
  @FXML protected var chatTabsContainer:TabPane = _
  @FXML protected var channelsContainer:ListView[ChatChannel] = _
  @FXML protected var joinToChannelInput:TextField = _


  def mainApp: MainApp
  def mainApp_=(c: MainApp): Unit
}