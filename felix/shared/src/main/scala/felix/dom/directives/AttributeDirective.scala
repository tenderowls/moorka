package felix.dom.directives

import felix.dom.{Directive, Element}
import moorka.rx._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object AttributeDirective {

  @inline val SetAttribute = "setAttribute"

  final class Simple(name: String, value: Option[String]) extends Directive {

    def affect(element: Element): Unit = value match {
      case Some(v) ⇒ element.ref.call[Unit](SetAttribute, name, v)
      case None ⇒ element.ref.call[Unit](SetAttribute, name)
    }

    def kill(): Unit = ()
  }

  final class Reactive(name: String, value: Rx[Option[String]]) extends Directive {

    var mortal = Option.empty[Mortal]

    def affect(element: Element): Unit = {
      mortal = Some {
        value foreach {
          case Some(x) ⇒ element.ref.call[Unit](SetAttribute, name, x)
          case None ⇒ element.ref.call[Unit](SetAttribute, name)
        }
      }
    }

    def kill(): Unit = mortal.foreach(_.kill())
  }

}
