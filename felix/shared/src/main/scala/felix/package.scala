import felix.dsl.{NodeLikeOps, EntriesGenerator, HtmlHelpers}
import felix.vdom._
import moorka._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{Success, Failure}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object felix extends HtmlHelpers {

  type Element = vdom.Element
  type FelixSystem = core.FelixSystem

  type Component = vdom.Component
  type DataRepeat[A] = dsl.DataRepeat[A]
  type Repeat = dsl.Repeat

  val DataRepeat = dsl.DataRepeat
  val Repeat = dsl.Repeat
  val FelixSystem = core.FelixSystem

  implicit def toEntriesGenerator(x: Symbol): EntriesGenerator = new EntriesGenerator(x)

  implicit def toNodeLikeOps(x: NodeLike): NodeLikeOps = new NodeLikeOps(x)

  implicit def toElements(xs: Seq[Element]): Elements = Elements(xs)

  implicit def toTextEntry(s: String): TextEntry = TextEntry(s)

  implicit def toRxNode(rx: Rx[NodeLike])(implicit system: FelixSystem): RxNode = {
    new RxNode(rx, system)
  }

  implicit def toFutureNode(future: Future[NodeLike])(implicit system: FelixSystem): RxNode = {
    val rx = future.toRx(system.executionContext) map {
      case Success(element) ⇒ element
      case Failure(exception) ⇒
        'span('style /= "color: red", exception.toString)
    }
    new RxNode(rx, system)
  }
}
