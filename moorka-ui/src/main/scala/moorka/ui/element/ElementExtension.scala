package moorka.ui.element

import moorka.rx._
import moorka.ui.RefHolder
import moorka.ui.event._
import vaska.JSObj

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

trait ElementExtension extends ElementEntry with Mortal {
  def start(element: ElementBase): Unit
  def kill(): Unit = {
  }
}

case class ElementAttributeName(name: String) {

  def :=(x: String) = ElementAttributeExtension(name, x)

  def :=(x: Rx[String]) = ElementBoundAttributeExtension(name, x)
}

case class ElementEventName(eventType: String) {

  def subscribe(listener: (SyntheticEvent) => Unit) = {
    SyntheticEventExtension(
      eventType,
      listener,
      useCapture = false
    )
  }

  def listen(listener: => Unit) = {
    SyntheticEventExtension(
      eventType,
      _ => listener,
      useCapture = false
    )
  }

  def capture(listener: (SyntheticEvent) => Unit) = {
    SyntheticEventExtension(
      eventType,
      listener,
      useCapture = true
    )
  }
}

case class ElementPropertyName[A](name: String) {

  def :=(x: A) = ElementPropertyExtension(name, x)

  def :=(x: Rx[A]) = ElementBoundPropertyExtension(name, x)

  def =:= (x: Var[A]) = VarPropertyExtension(name, x)

  def from(x: RefHolder): Future[A] = x.ref.get(name)
}

class BoundExtensionFactory[A](static: (A => ElementExtension), bound: (Rx[A]) => BoundElementExtension)
  extends StaticExtensionFactory[A](static) {

  def :=(x: Rx[A]) = bound(x)
}

class StaticExtensionFactory[A](static: (A => ElementExtension)) {

  def :=(x: A) = static(x)
}

case class ElementAttributeExtension(name: String, value: String) extends ElementExtension {
  def start(element: ElementBase): Unit = {
    element.ref.call[Any]("setAttribute", name, value)
  }
}

case class SyntheticEventExtension(eventType: String,
                                  listener: SyntheticEvent => Unit,
                                  useCapture: Boolean)
  extends ElementExtension {

  var slot:Option[Rx[_]] = None

  def start(element: ElementBase): Unit = {
    slot = useCapture match {
      case false => Some(EventProcessor.addListener(element, eventType, listener))
      case true => Some(EventProcessor.addCapture(element, eventType, listener))
    }
  }

  override def kill(): Unit = {
    slot.foreach(_.kill())
  }
}

case class ElementPropertyExtension[A](name: String, value: A) extends ElementExtension {
  def start(element: ElementBase): Unit = {
    
    element.ref.set(name, value)
  }
}

case class UseClassExtension(className: String, trigger:Boolean) extends ElementExtension {
  def start(element: ElementBase): Unit = {
    if (trigger) element.ref.call[Unit]("classAdd", className)
    else element.ref.call[Unit]("classRemove", className)
  }
}

sealed trait BoundElementExtension extends ElementExtension {

  protected var subscription: Rx[Any] = null

  override def kill(): Unit = {
    if (subscription != null)
      subscription.kill()
  }
}

case class UseClassBoundExtension(className: String, trigger:Rx[Boolean])
  extends ElementExtension with BoundElementExtension {

  def start(element: ElementBase): Unit = {
    subscription = trigger foreach {
      case true =>
        element.ref.call[Unit]("classAdd", className)
      case false =>
        element.ref.call[Unit]("classRemove", className)
    }
  }
}

case class ElementBoundPropertyExtension[A](name: String, value: Rx[A])
  extends ElementExtension with BoundElementExtension {

  def start(element: ElementBase): Unit = {
    subscription = value foreach { x ⇒
      element.ref.set(name, x)
    }
  }
}

case class VarPropertyExtension[A](name: String, value: Var[A])
  extends ElementExtension {

  var subscriptions: List[Rx[Any]] = Nil
  var awaitForWrite = true

  def listener(event: SyntheticEvent) = {
    val f: Future[A] = event.target.ref.get(name)
    f foreach { x =>
      value modOnce { old =>
        if (x != old) {
          awaitForWrite = false
          x
        }
        else {
          old
        }
      }
    }
  }

  def start(element: ElementBase): Unit = {

    subscriptions =
      EventProcessor.addListener(element, "change", listener) ::
      EventProcessor.addListener(element, "input", listener) ::
      value.foreach { x ⇒
        if (awaitForWrite) element.ref.set(name, x)
        else awaitForWrite = true
      } ::
      subscriptions
  }

  override def kill(): Unit = {
    super.kill()
    subscriptions.foreach(_.kill())
  }
}

case class ElementBoundAttributeExtension(name: String, value: Rx[String])
  extends ElementExtension
  with BoundElementExtension {

  def start(element: ElementBase): Unit = {
    subscription = value foreach { x ⇒
      element.ref.call[Unit]("setAttribute", name, x)
    }
  }
}

