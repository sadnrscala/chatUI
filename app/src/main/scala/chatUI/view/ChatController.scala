package chatUI.view

import chatUI.MainApp
import chatUI.model.ChatChannel
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, ListView, TabPane, TextArea, TextField}


abstract class ChatController extends Initializable {

  protected var _mainApp:MainApp = _

  @FXML protected var testButton:Button = _


  @FXML protected var usersContainer:UserListView = _
  @FXML protected var chatTabsContainer:TabPane = _
  @FXML protected var channelsContainer:ListView[ChatChannel] = _
  @FXML protected var joinToChannelInput:TextField = _
  @FXML protected var messageArea:TextArea = _
  @FXML protected var connectButton:Button = _
  @FXML protected var connectAddressTextField:TextField = _
  @FXML protected var connectPortTextField:TextField = _
  @FXML protected var connectionErrorLabel:Label = _

  def mainApp: MainApp
  def mainApp_=(c: MainApp): Unit
}