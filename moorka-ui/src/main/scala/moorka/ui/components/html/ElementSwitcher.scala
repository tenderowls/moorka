package moorka.ui.components.html

import moorka.rx._
import moorka.ui.element.{ElementExtension, ElementBase}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ElementSwitcher(state: State[ElementBase]) extends ElementExtension {

  val reaper = Reaper()
  var previous:ElementBase = null

  def start(element: ElementBase): Unit = {
    state observe {
      if (previous != null) {
        previous.parent = null
        element.ref.removeChild(previous.ref)
        previous.kill()
      }
      // Swap it
      val current = state()
      previous = current
      current.parent = element
      element.ref.appendChild(current.ref)
    }
  }

  override def kill(): Unit = {
    super.kill()
    reaper.sweep()
    if (previous != null)
      previous.kill()
  }
}
