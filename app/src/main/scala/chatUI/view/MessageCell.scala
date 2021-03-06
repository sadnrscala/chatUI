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
    getStyleClass.removeAll(getStyleClass)

    if (!empty && item != null) {
      messageContent = item.content
      getStyleClass.add("message")

      if(item.owner.isInstanceOf[SystemUser]) {
        messageCss = "message-system"
      } else if (item.direct) {
        prompt = new Label(s"You have new whisper from ${item.owner.nickName}: ")
        prompt.getStyleClass.add("message-whisper")

      } else {

        prompt = new Label(s"${item.owner.nickName}: ")
        prompt.getStyleClass.add("message-owner")
      }
    }

    setText(messageContent)

    getStyleClass.add(messageCss)
    setGraphic(prompt)
  }
}

class MessageCellFactory extends Callback[ListView[Message], ListCell[Message]] {
  override def call(param: ListView[Message]): ListCell[Message] = {
    new MessageCell
  }
}