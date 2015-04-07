package moorka.ui.components.html

import moorka.rx._
import moorka.ui.element.{ElementBase, ElementExtension}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ElementSwitcher(state: Rx[ElementBase]) extends ElementExtension {

  val reaper = Reaper()
  var previous: ElementBase = null

  def start(element: ElementBase): Unit = {
    reaper mark {
      state foreach { x ⇒
        if (previous != null) {
          previous.parent = null
          element.ref.removeChild(previous.ref)
          previous.kill()
        }
        // Swap it
        val current = x
        previous = current
        current.parent = element
        element.ref.appendChild(current.ref)
      }
    }
  }

  override def kill(): Unit = {
    super.kill()
    reaper.sweep()
    if (previous != null)
      previous.kill()
  }
}
