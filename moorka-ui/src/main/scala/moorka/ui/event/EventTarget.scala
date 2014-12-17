package moorka.ui.event

import moorka.ui.RefHolder

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait EventTarget extends RefHolder {
  private[moorka] var parent: EventTarget
}
