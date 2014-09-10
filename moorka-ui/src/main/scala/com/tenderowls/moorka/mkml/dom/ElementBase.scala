package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core.Mortal
import org.scalajs.dom

import scala.scalajs.js

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class ElementBase extends Node with Mortal {

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
      DomOperation.UpdateProperty(nativeElement, name.name, value)
    )
  }

  def kill():Unit
}

class ElementSequence(val value: Seq[ElementBase]) extends Node