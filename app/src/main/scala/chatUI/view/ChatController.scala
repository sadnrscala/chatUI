package chatUI.view

import chatUI.MainApp
import chatUI.model.{Message, User}
import javafx.collections.ObservableList
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, ListView, Tab, TabPane, TextArea, TextField}


abstract class ChatController extends Initializable {

  protected var _mainApp:MainApp = _


  @FXML protected var usersContainer:ListView[User] = _
  @FXML protected var messagesContainer:ListView[Message] = _
  @FXML protected var messageArea:TextArea = _

  @FXML protected var connectButton:Button = _
  @FXML protected var connectAddressTextField:TextField = _
  @FXML protected var connectPortTextField:TextField = _
  @FXML var connectionErrorLabel:Label = _
  @FXML var myAddress:Label = _
  @FXML var tabsContainer:TabPane = _

  def mainApp: MainApp
  def mainApp_=(c: MainApp): Unit

  def createWhisperTab(nickNameWithWho: String,
    realName: String,
    messagesData: ObservableList[Message]): Tab

}
