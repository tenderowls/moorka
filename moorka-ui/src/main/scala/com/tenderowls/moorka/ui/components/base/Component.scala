package com.tenderowls.moorka.ui.components.base

import com.tenderowls.moorka.core.RxState
import com.tenderowls.moorka.ui.Ref
import com.tenderowls.moorka.ui.element.ElementBase

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait Component[State] extends Block {
  val state: RxState[State]
}
