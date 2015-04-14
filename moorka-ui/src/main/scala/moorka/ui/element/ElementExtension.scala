package moorka.ui.element

import moorka.rx._
import moorka.ui.RefHolder
import moorka.ui.event._

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

case class ElementEventName[EventType <: SyntheticEvent](processor: EventProcessor[EventType]) {

  def subscribe(listener: (EventType) => Unit) = {
    SyntheticEventExtension[EventType](
      processor,
      listener,
      useCapture = false
    )
  }

  def listen(listener: => Unit) = {
    SyntheticEventExtension[EventType](
      processor,
      _ => listener,
      useCapture = false
    )
  }

  def capture(listener: (EventType) => Unit) = {
    SyntheticEventExtension[EventType](
      processor,
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
    element.ref.updateAttribute(name, value)
  }
}

case class SyntheticEventExtension[EventType <: SyntheticEvent](processor: EventProcessor[EventType],
                                                                listener: EventType => Unit,
                                                                useCapture: Boolean)
  extends ElementExtension {

  var slot:Option[Rx[_]] = None

  def start(element: ElementBase): Unit = {
    slot = useCapture match {
      case false => Some(processor.addListener(element, listener))
      case true => Some(processor.addCapture(element, listener))
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
    if (trigger) {
      element.ref.classAdd(className)
    }
    else {
      element.ref.classRemove(className)
    }
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
      case true => element.ref.classAdd(className)
      case false => element.ref.classRemove(className)
    }
  }
}

case class ElementBoundPropertyExtension[A](name: String, value: Rx[A])
  extends ElementExtension with BoundElementExtension {

  def start(element: ElementBase): Unit = {
    subscription = value foreach { x ⇒
      println(s"$name, $x")
      element.ref.set(name, x)
    }
  }
}

case class VarPropertyExtension[A](name: String, value: Var[A])
  extends ElementExtension {

  var subscriptions: List[Rx[Any]] = Nil
  var awaitForRead = false
  var awaitForWrite = true

  def listener(event: SyntheticEvent) = {
    awaitForRead = true
    val f: Future[A] = event.target.ref.get(name)
    f onSuccess {
      case x if awaitForRead =>
        awaitForWrite = false
        value.modOnce(_ ⇒ x)
    }
  }

  def start(element: ElementBase): Unit = {
    
    subscriptions =
      ChangeEventProcessor.addListener(element, listener) ::
      InputEventProcessor.addListener(element, listener) ::
      value.foreach { x ⇒
        awaitForRead = false
        if (awaitForWrite) {
          element.ref.set(name, x)
        }
        else {
          awaitForWrite = true
        }
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
      element.ref.updateAttribute(name, x)
    }
  }
}

