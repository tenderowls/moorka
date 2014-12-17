package moorka.ui.element

import moorka.rx.Mortal
import moorka.ui.event.{EventProcessor, EventTarget}
import moorka.ui.Ref

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait ElementBase extends ElementEntry with Mortal with EventTarget {

  private[moorka] var parent: EventTarget = null

  val ref: Ref

  def kill(): Unit = {
    EventProcessor.deregisterElement(this)
    ref.kill()
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends ElementEntry
