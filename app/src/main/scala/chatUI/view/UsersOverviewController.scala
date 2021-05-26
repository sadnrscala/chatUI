package chatUI.view

import chatUI.MainApp
import chatUI.model.User
import javafx.collections.ListChangeListener
import javafx.fxml.{FXML, Initializable}
import javafx.scene.control.{Accordion, Cell, Label, ListCell, ListView, TitledPane}
import javafx.scene.layout.HBox

import java.net.URL
import java.util.ResourceBundle


class UsersOverviewController extends Initializable{


  protected var _mainApp:MainApp = _



  @FXML
  private var usersContainer:ListView[User] = _


  @FXML
  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    usersContainer.setCellFactory(new UserCellFactory())

  }

  def mainApp: MainApp = _mainApp

  def mainApp_=(c:MainApp): Unit = {
    _mainApp = c

    usersContainer.setItems(mainApp.usersData)

  }


}
