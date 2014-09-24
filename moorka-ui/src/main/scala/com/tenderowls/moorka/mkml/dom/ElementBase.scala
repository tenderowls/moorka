package com.tenderowls.moorka.mkml.dom

import com.tenderowls.moorka.core.Mortal
import com.tenderowls.moorka.mkml.engine._

import scala.scalajs.js

/**
 * DOM element representation
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class ElementBase extends Node with Mortal {

  private[mkml] var parent:ElementBase = null

  val ref: Ref

  def kill():Unit = {
    SyntheticEventProcessor.deregisterElement(this)
  }
}

class ElementSequence(val value: Seq[ElementBase]) extends Node