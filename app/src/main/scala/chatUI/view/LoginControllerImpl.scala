package chatUI.view
import chatUI.MainApp

import java.net.URL
import java.util.ResourceBundle

class LoginControllerImpl extends LoginController {
  override def initialize(location: URL, resources: ResourceBundle): Unit = {

  }

  def onLoginAction(): Unit = {

    loginLabelError.setText("")

    val nickName = loginInput.getText.trim
    if(nickName.length < 1) {
      loginLabelError.setText("Please pick a name")
    } else {
      //println("accept login, go next scene")
      // set nickname here
      mainApp.myNickName = nickName
      mainApp.initChatWindow()
    }
  }

  override def mainApp: MainApp = _mainApp
  override def mainApp_=(c:MainApp): Unit = {
    _mainApp = c
  }
}
