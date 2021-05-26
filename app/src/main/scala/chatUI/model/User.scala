package chatUI.model

import javafx.beans.property.{SimpleStringProperty, StringProperty}

class User(_name:String) {

  private val __name:StringProperty = new SimpleStringProperty(_name)

  def name:String = __name.get

  def name_=(newName:String) = __name.set(newName)

  def nameProperty:StringProperty = __name

}
