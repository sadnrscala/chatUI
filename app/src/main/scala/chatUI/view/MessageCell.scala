package chatUI.view

import chatUI.model.{ChatChannel, Message}
import javafx.css.CssMetaData
import javafx.scene.control.{Label, ListCell, ListView}
import javafx.scene.text.Font
import javafx.util.Callback


class MessageCell extends ListCell[Message] {
  override def updateItem(item: Message, empty: Boolean): Unit = {
    super.updateItem(item, empty)

    var text:String = null
    if (item == null || empty) {

    } else {
      text = item.content
      val nickName = new Label(s"${item.owner.name}:")
      nickName.getStyleClass.add("message__owner")
      setGraphic(nickName)
    }

    setText(text)

  }
}

class MessageCellFactory extends Callback[ListView[Message], ListCell[Message]] {
  override def call(param: ListView[Message]): ListCell[Message] = {
    new MessageCell
  }
}