package moorka.ui.components.html

import moorka.rx._
import moorka.ui.element.ElementBase
import moorka.ui.Ref
import moorka.ui.event.EventProcessor

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class BoundComponentContainer(element: RxState[ElementBase]) extends ElementBase {

  var previous:ElementBase = null

  val ref = Ref("div")

  val observer = element observe {

    previous.parent = null
    ref.removeChild(previous.ref)
    previous.kill()

    // Swap it
    val current = element()
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
