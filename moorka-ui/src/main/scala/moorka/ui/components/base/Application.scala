package moorka.ui.components.base

import moorka.ui.element.ElementBase
import moorka.ui.RenderAPI

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
