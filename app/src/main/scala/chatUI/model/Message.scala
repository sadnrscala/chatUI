package chatUI.model

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}

class Message(_content:String, _owner:User) {

  private val __owner:ObjectProperty[User] = new SimpleObjectProperty[User](_owner)
  private val __content:StringProperty = new SimpleStringProperty(_content)

  def content:String = __content.get
  def owner:User = __owner.get

  def content_=(newContent:String) = __content.set(newContent)
  def owner_=(newOwner:User): Unit = __owner.set(newOwner)


  def contentProperty:StringProperty = __content
  def ownerProperty:ObjectProperty[User] = __owner
}
