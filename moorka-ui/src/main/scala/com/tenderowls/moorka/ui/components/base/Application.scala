package com.tenderowls.moorka.ui.components.base

import com.tenderowls.moorka.ui.element.ElementBase
import com.tenderowls.moorka.ui.RenderAPI

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class Application extends js.JSApp {

  def start(): ElementBase

  def main() = {
    RenderAPI ! js.Array(
      "append_child",
      "root",
      start().ref.id
    )
  }

}
