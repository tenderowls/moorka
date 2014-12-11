package com.tenderowls.moorka.ui.event

import com.tenderowls.moorka.ui.RefHolder

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
trait EventTarget extends RefHolder {
  private[moorka] var parent: EventTarget
}
