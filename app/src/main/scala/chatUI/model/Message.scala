package chatUI.model

import javafx.beans.property.{ObjectProperty, SimpleObjectProperty, SimpleStringProperty, StringProperty}

class Message(_content:String, owner:User) {


  private val __content:StringProperty = new SimpleStringProperty(_content)

  def content:String = __content.get


  def content_=(newContent:String) = __content.set(newContent)


  def contentProperty:StringProperty = __content

}
