import felix.dsl.{EntriesGenerator, HtmlHelpers}
import felix.vdom._
import moorka.flow.Flow

import scala.concurrent.Future
import scala.language.implicitConversions

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

  implicit def toElements(xs: Seq[Element]): Elements = Elements(xs)

  implicit def toTextEntry(s: String): TextEntry = TextEntry(s)

  implicit def toFlowNode(rx: Flow[NodeLike])(implicit system: FelixSystem): FlowNode = {
    new FlowNode(rx, system)
  }

  implicit def toFutureNode(future: Future[NodeLike])(implicit system: FelixSystem): FlowNode = {
    val rx = moorka.flow.converters.future(future).toFlow(system.executionContext) recover {
      case exception: Throwable ⇒ 'span('style /= "color: red", exception.toString)
    }
    new FlowNode(rx, system)
  }
}
