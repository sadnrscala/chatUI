package chatUI.view

import chatUI.MainApp
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Button, Label, TextField}


abstract class LoginController extends Initializable {

  protected var _mainApp:MainApp = _

  @FXML var loginLabelError:Label = _
  @FXML var loginButtonLogin:Button = _
  @FXML var loginInput:TextField = _

  def mainApp: MainApp
  def mainApp_=(c: MainApp): Unit
}
