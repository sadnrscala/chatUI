package chatUI.view

import chatUI.model.{ChatChannel}
import javafx.scene.control.{ListCell, ListView}
import javafx.util.Callback


class ChannelCell extends ListCell[ChatChannel] {
  override def updateItem(item: ChatChannel, empty: Boolean): Unit = {
    super.updateItem(item, empty)
    val index:Int = getIndex
    var name:String = null
    if (item == null || empty) {

    } else {
      name = s"${item.name} (${item.countSubscribers})"
    }

    setText(name)
    setGraphic(null)
  }
}

class ChannelCellFactory extends Callback[ListView[ChatChannel], ListCell[ChatChannel]] {
  override def call(param: ListView[ChatChannel]): ListCell[ChatChannel] = {
    new ChannelCell
  }
}