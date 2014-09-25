package com.tenderowls.moorka.mkml.engine

import com.tenderowls.moorka.mkml.dom.ComponentBase

import scala.scalajs.js

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
abstract class Application extends js.JSApp {

  def start(): ComponentBase

  def main() = {
    RenderBackendApi ! js.Array(
      "append_child",
      "root",
      start().ref.id
    )
  }

}
