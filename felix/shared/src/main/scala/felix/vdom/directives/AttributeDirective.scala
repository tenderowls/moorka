package felix.vdom.directives

import felix.vdom.{Directive, Element}
import moorka._

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

  final class Reactive(name: String, value: Rx[Option[String]]) extends Directive {

    var mortal = Option.empty[Mortal]

    def affect(element: Element): Unit = {
      mortal = Some {
        value foreach {
          case Some(x) ⇒ element.ref.call[Unit](SetAttribute, name, x)
          case None ⇒ element.ref.call[Unit](RemoveAttribute, name)
        }
      }
    }

    def kill(): Unit = mortal.foreach(_.kill())
  }

}
