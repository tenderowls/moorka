package com.tenderowls.moorka.ui.components.html

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.ui.element.ElementBase
import com.tenderowls.moorka.ui.Ref
import com.tenderowls.moorka.ui.event.EventProcessor

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class BoundComponentContainer(element: Bindable[ElementBase]) extends ElementBase {

  var previous:ElementBase = null

  val ref = Ref("div")

  val observer = element observe { reactiveElement =>

    previous.parent = null
    ref.removeChild(previous.ref)
    previous.kill()

    // Swap it
    val current = reactiveElement()
    previous = current

    current.parent = this
    ref.appendChild(current.ref)
  }

  override def kill(): Unit = {
    super.kill()
    observer.kill()
    if (previous != null)
      previous.kill()
  }

  EventProcessor.registerElement(this)
}