package chatUI.model

import javafx.beans.property.{IntegerProperty, SimpleIntegerProperty, SimpleStringProperty, StringProperty}

class ChatChannel(_name:String, _countSubscribers:Int = 0) {

  private val __name:StringProperty = new SimpleStringProperty(_name)
  private val __countSubscribers:IntegerProperty = new SimpleIntegerProperty(_countSubscribers)

  def name:String = __name.get
  def countSubscribers:Int = __countSubscribers.get

  def name_=(newName: String): Unit = __name.set(newName)
  def countSubscribers_=(newCountSubscribers: Int): Unit = __countSubscribers.set(newCountSubscribers)

  def nameProperty:StringProperty = __name
  def countSubscribersProperty = __countSubscribers


}
