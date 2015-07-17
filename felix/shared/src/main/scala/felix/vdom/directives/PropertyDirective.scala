package felix.vdom.directives

import felix.core.{EventProcessor, FelixSystem}
import felix.vdom.{Directive, Element}
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

  sealed trait TwoWayBindingState[+A]

  object TwoWayBindingState {

    case object Idle extends TwoWayBindingState[Nothing]

    case class Update[A](data: A) extends TwoWayBindingState[A]

    case class Writing[A](data: A) extends TwoWayBindingState[A]
    
    def empty[A]: TwoWayBindingState[A] = TwoWayBindingState.Idle
  }

  final class TwoWayBinding[T](name: String,
                               aVar: Var[T],
                               changeEvents: Seq[String],
                               system: FelixSystem) extends Directive {

    implicit val reaper = Reaper()

    def affect(element: Element): Unit = {
      import TwoWayBindingState._
      implicit val ec = system.ec
      aVar.once(element.ref.set(name, _))
      val changesFromDom = Channel[T]()
      val changesFromVar = aVar.stateless
      val listener: EventProcessor.EventListener = { (_, _, _) ⇒
        element.ref.get[T](name) foreach { data ⇒
          changesFromDom.pull(Val(data))
        }
      }
      changeEvents foreach { eventType ⇒
        system.eventProcessor.registerEventType(eventType, autoPreventDefault = false)
        system.eventProcessor.addListener(element, eventType, listener)
      }
      Var.withMod(TwoWayBindingState.empty[T]) {
        case Idle ⇒
          println("PropertyDirective in Idle")
          changesFromDom or changesFromVar map {
            case Left(x) ⇒ 
              Update(x)
            case Right(x) ⇒
              println(s"PropertyDirective move to Writing($x)")
              Writing(x)
          }
        case Writing(x) ⇒
          println(s"PropertyDirective in Writing($x)")
          element.ref.set(name, x).toRx or changesFromVar map {
            case Left(_) ⇒ Idle
            case Right(updatedX) ⇒ Writing(updatedX)
          }
        case Update(x) ⇒
          println(s"PropertyDirective in Update($x)")
          aVar.pull(Val(x))
          Val(Idle)
      }
    }

    def kill(): Unit = {
      reaper.sweep()
    }
  }

}
