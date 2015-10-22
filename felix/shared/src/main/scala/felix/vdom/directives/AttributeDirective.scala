package felix.vdom.directives

import felix.core.FelixSystem
import felix.vdom.{Directive, Element}
import moorka.death.Reaper
import moorka.flow.Flow

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object AttributeDirective {

  @inline val SetAttribute = "setAttribute"

  @inline val RemoveAttribute = "removeAttribute"

  final class Simple(name: String, value: String) extends Directive {

    def affect(element: Element): Unit = {
      element.ref.call[Unit](SetAttribute, name, value)
    }

    def kill(): Unit = ()
  }

  final class Reactive(name: String, value: Flow[Option[String]], system: FelixSystem) extends Directive {

    implicit val reaper = Reaper()
    implicit val context = system.flowContext

    def affect(element: Element): Unit = {
      value foreach {
        case Some(x) ⇒ element.ref.call[Unit](SetAttribute, name, x)
        case None ⇒ element.ref.call[Unit](RemoveAttribute, name)
      }
    }

    def kill(): Unit = reaper.sweep()
  }

}
