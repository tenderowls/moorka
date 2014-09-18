package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import com.tenderowls.moorka.mkml.engine._
import org.scalajs.dom

import scala.scalajs.js

sealed trait ElementExtension extends Node with Mortal {

  protected var element:ElementBase = null

  def assignElement(element: ElementBase): Unit = {
    this.element = element
  }

  override def kill(): Unit = {

  }
}

case class ElementAttributeName(name: String) {

  def :=(x: String) = ElementAttributeExtension(name, x)

  def :=(x: Bindable[String]) = ElementBoundAttributeExtension(name, x)
}

case class ElementEventName[EventType <: SyntheticEvent](processor: SyntheticEventProcessor[EventType]) {

  def listenNative(listener: (dom.Event) => _) = {
    NativeEventExtension(
      processor.eventType,
      listener,
      useCapture = false
    )
  }

  def captureNative(listener: (dom.Event) => _) = {
    NativeEventExtension(
      processor.eventType,
      listener,
      useCapture = true
    )
  }

  def listen(listener: (EventType) => Unit) = {
    SyntheticEventExtension[EventType](
      processor,
      listener,
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

  def :=(x: Bindable[A]) = ElementBoundPropertyExtension(name, x)

  def extractFrom(x: ElementBase): A = x.extractProperty(this)
}

class BoundExtensionFactory[A](static: (A => ElementExtension), bound: (Bindable[A]) => BoundElementExtension) 
  extends StaticExtensionFactory[A](static) {

  def :=(x: Bindable[A]) = bound(x)
}

class StaticExtensionFactory[A](static: (A => ElementExtension)) {

  def :=(x: A) = static(x)
}

case class ElementAttributeExtension(name: String, value: String) extends ElementExtension {

  override def assignElement(element: ElementBase): Unit = {
    super.assignElement(element)
    element.nativeElement.setAttribute(name, value)
  }
}

case class NativeEventExtension(name: String, value: (dom.Event) => _, useCapture: Boolean) extends ElementExtension {

  override def assignElement(element: ElementBase): Unit = {
    super.assignElement(element)
    element.nativeElement.addEventListener(name, value, useCapture)
  }

  override def kill(): Unit = {
    element.nativeElement.removeEventListener(name, value, useCapture)
  }
}

case class SyntheticEventExtension[EventType <: SyntheticEvent](processor: SyntheticEventProcessor[EventType],
                                                                listener: EventType => Unit,
                                                                useCapture: Boolean)
  extends ElementExtension {

  var slot:Option[Slot[EventType]] = None
  
  override def assignElement(element: ElementBase): Unit = {
    super.assignElement(element)
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
  override def assignElement(element: ElementBase): Unit = {
    super.assignElement(element)
    val dyn = element.nativeElement.asInstanceOf[js.Dynamic]
    val jsVal = value.asInstanceOf[js.Any]
    dyn.updateDynamic(name)(jsVal)
  }
}

case class UseClassExtension(className: String, trigger:Boolean) extends ElementExtension {
  override def assignElement(element: ElementBase): Unit = {
    if (trigger) {
      element.nativeElement
        .classList.add(className)
    }
    else {
      element.nativeElement
        .classList.remove(className)
    }
  }
}

sealed trait BoundElementExtension extends ElementExtension {

  protected var subscription: Event[_] = null

  override def kill(): Unit = {
    if (subscription != null)
      subscription.kill()
  }
}

case class UseClassBoundExtension(className: String, trigger:Bindable[Boolean]) 
  extends ElementExtension with BoundElementExtension {
  
  override def assignElement(element: ElementBase): Unit = {
    subscription = trigger observe { _ =>
      val cl = element.nativeElement.classList
      RenderContext.appendOperation(
        trigger() match {
          case true => CustomOperation { () => cl.add(className) }
          case false => CustomOperation { () => cl.remove(className) }
        }
      )
    }
  }
}

case class ElementBoundPropertyExtension[A](name: String, value: Bindable[A])
  extends ElementExtension with BoundElementExtension {

  override def assignElement(element: ElementBase): Unit = {
    super.assignElement(element)
    subscription = value observe { _ =>
      RenderContext.appendOperation(
        UpdateProperty(element.nativeElement, name, value())
      )
    }
  }
}

case class ElementBoundAttributeExtension(name: String, value: Bindable[String])
  extends ElementExtension
  with BoundElementExtension {

  override def assignElement(component: ElementBase): Unit = {
    super.assignElement(component)
    subscription = value observe { _ =>
      RenderContext.appendOperation(
        UpdateAttribute(component.nativeElement, name, value())
      )
    }
  }
}

