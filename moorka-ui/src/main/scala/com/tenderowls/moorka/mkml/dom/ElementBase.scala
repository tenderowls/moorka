package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core.Mortal
import com.tenderowls.moorka.mkml.engine._
import org.scalajs.dom

import scala.scalajs.js

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class ElementBase extends Node with Mortal {

  private[mkml] var parent:ElementBase = null

  val nativeElement: dom.Element

  /**
   * Read property from DOM element
   * @return property value
   */
  def extractProperty[A](name: ElementPropertyName[A]): A = {
    nativeElement
      .asInstanceOf[js.Dynamic]
      .selectDynamic(name.name)
      .asInstanceOf[A]
  }

  /**
   * Write property to DOM element
   * @return property value
   */
  def setProperty[A](name: ElementPropertyName[A], value: A) = {
    RenderContext.appendOperation(
      UpdateProperty(nativeElement, name.name, value)
    )
  }

  def kill():Unit = {
    SyntheticEventProcessor.deregisterElement(this)
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends Node