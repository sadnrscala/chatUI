package chatUI.model


class Message(var _content:String, var _owner:User, var _direct: Boolean = false) {

  def content:String = _content
  def owner:User = _owner
  def direct:Boolean = _direct

  def content_=(newContent:String): Unit = _content = newContent
  def owner_=(newOwner: User): Unit = _owner = newOwner
  def direct_=(newValue: Boolean): Unit = _direct = newValue

}
