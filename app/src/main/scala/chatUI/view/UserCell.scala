package chatUI.view

import chatUI.model.User
import javafx.scene.control.{ListCell, ListView}
import javafx.util.Callback



class UserCell extends ListCell[User] {
  override def updateItem(item: User, empty: Boolean): Unit = {
    super.updateItem(item, empty)
    var name:String = null
    if (!empty && item != null) {
      name = item.nickName
    }

    setText(name)
    setGraphic(null)
  }
}

class UserCellFactory extends Callback[ListView[User], ListCell[User]] {
  override def call(param: ListView[User]): ListCell[User] = {
    new UserCell
  }
}