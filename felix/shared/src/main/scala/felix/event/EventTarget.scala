package felix.event

import felix.core.RefHolder

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait EventTarget extends RefHolder {
  private[felix] var parent = Option.empty[EventTarget]
}
