package chatUI.model

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}

sealed trait TabType
case object WHISPER extends TabType
case object TOPIC extends TabType

class ChatTab(val _name:String, val _tabType:TabType) {


  private val __name:StringProperty = new SimpleStringProperty(_name)
  private val __tabType:ObjectProperty[TabType] = new SimpleObjectProperty[TabType](_tabType)

  def name:String = __name.get
  def name_=(newName:String): Unit = __name.set(newName)
  def nameProperty:StringProperty = __name

  def tabType:TabType = __tabType.get
  def tabType_=(newTabType:TabType): Unit = __tabType.set(newTabType)
  def tabTypeProperty:ObjectProperty[TabType] = __tabType

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + name.hashCode
    result = prime * result + tabType.hashCode

    result
  }
  override def equals(that: Any): Boolean = this.hashCode == that.hashCode
}



