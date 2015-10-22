package felix.vdom.directives

import felix.core.{EventProcessor, FelixSystem}
import felix.vdom.{Directive, Element}
import moorka.death.Reaper
import moorka.flow.Flow
import moorka.flow.converters.future
import moorka.flow.immutable.Val
import moorka.flow.mutable.{Channel, Fsm}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object PropertyDirective {

  final class Simple(name: String, value: Any) extends Directive {

    def affect(element: Element): Unit = element.ref.set(name, value)

    def kill(): Unit = ()
  }

  final class Reactive(name: String, value: Flow[Any], system: FelixSystem) extends Directive {

    implicit val reaper = Reaper()
    implicit val context = system.flowContext


    def affect(element: Element): Unit = {
      value.foreach(element.ref.set(name, _))
    }

    def kill(): Unit = reaper.sweep()
  }

  sealed trait TwoWayBindingState[+A]

  object TwoWayBindingState {

    case object Idle extends TwoWayBindingState[Nothing]

    case class Update[A](data: A) extends TwoWayBindingState[A]

    case class Writing[A](data: A) extends TwoWayBindingState[A]

    def empty[A]: TwoWayBindingState[A] = TwoWayBindingState.Idle
  }

  final class TwoWayBinding[T](name: String,
                               input: Flow[T],
                               output: T ⇒ _,
                               changeEvents: Seq[String],
                               system: FelixSystem) extends Directive {

    implicit val reaper = Reaper()
    implicit val context = system.flowContext

    def affect(element: Element): Unit = {
      import TwoWayBindingState._
      implicit val ec = system.executionContext
//      if (input.isInstanceOf[StatefulSource[T]]) {
//        input.once(element.ref.set(name, _))
//      }
      val changesFromDom = Channel[T]
      val listener: EventProcessor.EventListener = { (_, _, _) ⇒
        element.ref.get[T](name) foreach { data ⇒
          changesFromDom.push(data)
        }
      }
      changeEvents foreach { eventType ⇒
        system.eventProcessor.registerEventType(eventType, autoPreventDefault = false)
        system.eventProcessor.addListener(element, eventType, listener)
      }
      Fsm(TwoWayBindingState.empty[T]) {
        case Idle ⇒
          changesFromDom or input map {
            case Left(x) ⇒ Update(x)
            case Right(x) ⇒ Writing(x)
          }
        case Writing(x) ⇒
          element.ref.set(name, x).toFlow or input map {
            case Left(_) ⇒ Idle
            case Right(updatedX) ⇒ Writing(updatedX)
          }
        case Update(x) ⇒ Val(Idle)
      } andThen {
        case Update(x) ⇒
          output(x)
      }
    }

    def kill(): Unit = {
      reaper.sweep()
    }
  }

}
