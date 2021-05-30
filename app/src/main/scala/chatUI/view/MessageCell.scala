package chatUI.view

import chatUI.model.{Message, SystemUser}
import javafx.scene.control.{Label, ListCell, ListView}
import javafx.util.Callback


class MessageCell extends ListCell[Message] {
  override def updateItem(item: Message, empty: Boolean): Unit = {
    super.updateItem(item, empty)

    var messageContent:String = null
    var messageCss:String = null
    var prompt:Label = null

    if (!empty && item != null) {
      messageContent = item.content

      if (item.direct) {
        prompt = new Label(s"You have new whisper from ${item.owner.name}: ")
        prompt.getStyleClass.add("message-whisper")
      } else if (item.owner.isInstanceOf[SystemUser]) {
        messageCss = "message-system"
      } else {
        prompt = new Label(s"${item.owner.name}: ")
        prompt.getStyleClass.add("message-owner")
      }
    }

    setText(messageContent)
    getStyleClass.removeAll(getStyleClass)
    getStyleClass.add(messageCss)
    setGraphic(prompt)
  }
}

class MessageCellFactory extends Callback[ListView[Message], ListCell[Message]] {
  override def call(param: ListView[Message]): ListCell[Message] = {
    new MessageCell
  }
}