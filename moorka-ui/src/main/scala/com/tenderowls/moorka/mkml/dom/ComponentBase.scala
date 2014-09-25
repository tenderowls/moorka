package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core.Mortal
import com.tenderowls.moorka.mkml.engine._

import scala.scalajs.js

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class ComponentBase extends SyntheticDomNode with Mortal {

  private[mkml] var parent:ComponentBase = null

  val ref: Ref

  def kill(): Unit = {
    SyntheticEventProcessor.deregisterElement(this)
    ref.kill()
  }
}

class ElementSequence(val value: Seq[ComponentBase]) extends SyntheticDomNode