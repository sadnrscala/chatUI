package chatUI.model

class PrivateTab(val _nickWithWho: String, val _name: String, var _unread:Int = 1) {
  def nickWithWho: String = _nickWithWho
  def name: String = _name
  def unread: Int = _unread
  def unread_=(newVal: Int): Unit = _unread = newVal
}
