package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core._
import org.scalajs.dom

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

case class ElementEventName(name: String) {

  def :=(x: (dom.Event) => _) = ElementEventExtension(name, x)
}

case class ElementPropertyName[A](name: String) {

  def :=(x: A) = ElementPropertyExtension(name, x)

  def :=(x: Bindable[A]) = ElementBoundPropertyExtension(name, x)

  def extractFrom(x: ElementBase): A = x.extractProperty(this)
}

case class ExtensionFactory[A](static: (A => ElementExtension), bound: (Bindable[A]) => BoundElementExtension) {

  def :=(x: A) = static(x)

  def :=(x: Bindable[A]) = bound(x)
  
}

case class ElementAttributeExtension(name: String, value: String) extends ElementExtension {

  override def assignElement(component: ElementBase): Unit = {
    RenderContext.appendOperation(
      DomOperation.UpdateAttribute(component.nativeElement, name, value)
    )
  }
}

case class ElementEventExtension(name: String, value: (dom.Event) => _) extends ElementExtension {

  override def assignElement(component: ElementBase): Unit = {
    super.assignElement(component)
    component.nativeElement.addEventListener(name, value)
  }

  override def kill(): Unit = {
    element.nativeElement.removeEventListener(name, value)
  }
}

case class ElementPropertyExtension[A](name: String, value: A) extends ElementExtension {

  override def assignElement(e: ElementBase): Unit = {
    RenderContext.appendOperation(
      DomOperation.UpdateProperty(e.nativeElement, name, value)
    )
  }
}


case class UseClassExtension(className: String, trigger:Boolean) extends ElementExtension {
  override def assignElement(element: ElementBase): Unit = {
    RenderContext.appendOperation(
      if (trigger) {
        DomOperation.CustomOperation(
          () => element.nativeElement.classList.add(className)
        )
      }
      else {
        DomOperation.CustomOperation(
          () => element.nativeElement.classList.remove(className)
        )
      }
    )
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
      RenderContext.appendOperation(
        if (trigger()) {
          DomOperation.CustomOperation(
            () => element.nativeElement.classList.add(className)
          )
        }
        else {
          DomOperation.CustomOperation(
            () => element.nativeElement.classList.remove(className)
          )
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
        DomOperation.UpdateProperty(element.nativeElement, name, value())
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
        DomOperation.UpdateAttribute(component.nativeElement, name, value())
      )
    }
  }
}

