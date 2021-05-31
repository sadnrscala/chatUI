package chatUI.model


class User(val _name:String, val _nickName:String){

  def name:String = _name
  //def name_=(newName:String) = _name = newName
  def nickName:String = _nickName

  override def toString: String = {
    name
  }
}

class SystemUser(_name:String = "system", _nickName: String = "system") extends User(_name = _name, _nickName = _nickName)