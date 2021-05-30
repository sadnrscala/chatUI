package chatUI.model


class User(var _name:String){

  def name:String = _name
  def name_=(newName:String) = _name = newName

  override def toString: String = {
    name
  }
}

class SystemUser extends User("system")