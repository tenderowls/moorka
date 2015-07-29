package felix.vdom.directives

import felix.core.FelixSystem
import felix.vdom.{Directive, Element}
import moorka._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class UseClassDirective(className: String,
                              trigger: Rx[Boolean],
                              system: FelixSystem)
  extends Directive {

  val reaper = Reaper()

  def affect(element: Element): Unit = {
    reaper mark {
      trigger foreach { x ⇒
        if (x) system.utils.call[Unit]("classAdd", element.ref, className)
        else system.utils.call[Unit]("classRemove", element.ref, className)
      }
    }
  }

  def kill(): Unit = {
    reaper.sweep()
  }
}
