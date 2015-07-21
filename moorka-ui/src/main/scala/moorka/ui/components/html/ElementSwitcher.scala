package moorka.ui.components.html

import moorka.rx._
import moorka.ui.element.{Element, ElementBase, ElementExtension}
import vaska.JSObj

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class ElementSwitcher(state: Rx[ElementBase]) extends ElementExtension {

  val reaper = Reaper()
  private var current: ElementBase = null

  def start(element: ElementBase): Unit = {
    reaper mark {
      current = new Element("span", Seq(style := "display: none;"))
      current.parent = element
      element.ref.call[JSObj]("appendChild", current.ref)

      state foreach { replacement ⇒
        replacement.parent = element

        element.ref.call[JSObj]("replaceChild", replacement.ref,  current.ref)

        current.parent = null
        current.kill()

        current = replacement
      }
    }
  }

  override def kill(): Unit = {
    super.kill()
    reaper.sweep()
    if (current != null)
      current.kill()
  }
}
