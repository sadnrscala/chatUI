package chatUI.view

import chatUI.model.{ChatTab, User, WHISPER}
import javafx.collections.ObservableSet
import javafx.scene.control.{ListView, TabPane}

class UserListView extends ListView[User] {

  setCellFactory(new UserCellFactory())

  def pinToTabPane(tabsData: ObservableSet[ChatTab], tabsContainer:TabPane): Unit = {
    getSelectionModel.selectedItemProperty.addListener((_, _, newValue) => {
      if (newValue != null) {
        val newChatTab = new ChatTab(newValue.name, WHISPER)

        tabsData.add(newChatTab)
        tabsContainer.getTabs.filtered(_.getText == newChatTab.name).forEach(tab => {
          tabsContainer.getSelectionModel.select(tab)
        })
      }
    })
  }
}