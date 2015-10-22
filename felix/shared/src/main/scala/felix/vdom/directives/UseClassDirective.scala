package felix.vdom.directives

import felix.core.FelixSystem
import felix.vdom.{Directive, Element}
import moorka._
import moorka.death.Reaper
import moorka.flow.Flow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class UseClassDirective(className: String,
                              trigger: Flow[Boolean],
                              system: FelixSystem)
  extends Directive {

  implicit val reaper = Reaper()
  implicit val context = system.flowContext

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
