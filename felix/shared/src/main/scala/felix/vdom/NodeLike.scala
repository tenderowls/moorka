package felix.vdom

import felix.core.EventTarget
import moorka.rx.death.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait NodeLike extends Entry with RefHolder with Mortal {
  // TODO tmp
  def setParent(value: Option[EventTarget]): Unit
}
