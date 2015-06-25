package felix.dom.directives

import felix.dom.{Directive, Element}
import moorka.rx._

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object PropertyDirective {

  final class Simple(name: String, value: Any) extends Directive {

    def affect(element: Element): Unit = element.ref.set(name, value)

    def kill(): Unit = ()
  }

  final class Reactive(name: String, value: Rx[Any]) extends Directive {

    var mortal = Option.empty[Mortal]

    def affect(element: Element): Unit = {
      mortal = Some(value.foreach(element.ref.set(name, _)))
    }

    def kill(): Unit = mortal.foreach(_.kill())
  }

}
