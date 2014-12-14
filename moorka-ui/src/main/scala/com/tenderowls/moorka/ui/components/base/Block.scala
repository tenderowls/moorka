package com.tenderowls.moorka.ui.components.base

import com.tenderowls.moorka.ui.Ref
import com.tenderowls.moorka.ui.element.ElementBase

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Block extends ElementBase {

  val ref: Ref = start().ref

  def start(): ElementBase

}
