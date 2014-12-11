package com.tenderowls.moorka.ui.element

import com.tenderowls.moorka.core.Mortal
import com.tenderowls.moorka.ui.event.{EventProcessor, EventTarget}
import com.tenderowls.moorka.ui.Ref

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class ElementBase extends ElementEntry with Mortal with EventTarget {

  private[moorka] var parent: EventTarget = null

  val ref: Ref

  def kill(): Unit = {
    EventProcessor.deregisterElement(this)
    ref.kill()
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends ElementEntry