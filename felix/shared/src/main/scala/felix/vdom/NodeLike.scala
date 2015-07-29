package felix.vdom

import felix.core.EventTarget
import moorka.rx.death.Mortal

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait NodeLike extends Entry with RefHolder with Mortal {
  // TODO tmp
  private[felix] def setParent(value: Option[RefHolder with EventTarget]): Unit
}
