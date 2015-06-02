package moorka.ui.components

import moorka.ui.components.html.div
import moorka.ui.element.{ElementBase, ElementExtension}
import vaska.JSObj

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
class FutureElement(f: Future[ElementBase])
                   (implicit ec: ExecutionContext) extends ElementExtension {
  val tempEl = div()
  def start(target: ElementBase): Unit = {
    target.ref.call[JSObj]("appendChild", tempEl.ref)
    f onComplete {
      case Success(newEl) ⇒
        target.ref.call[JSObj]("replaceChild",
          newEl.ref,
          tempEl.ref
        )
        tempEl.kill()
      case Failure(ex) ⇒
        throw ex
    }
  }
}
