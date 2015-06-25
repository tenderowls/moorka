package moorka.ui.event

import moorka.ui.RefHolder

import scala.annotation.tailrec

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait EventTarget extends RefHolder {

  private[moorka] var parent: EventTarget

  /**
   * Get the closest parent that satisfies some condition
   * @param f condition
   */
  def findParent(f: EventTarget ⇒ Boolean): Option[EventTarget] = {
    @tailrec def rec(it: EventTarget): Option[EventTarget] = {
      if (it == null) {
        None
      }
      else if (f(it)) {
        Some(it)
      }
      else {
        rec(it.parent)
      }
    }
    rec(this)
  }
}
