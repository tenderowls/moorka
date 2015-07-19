import felix.dsl.{EntriesGenerator, HtmlHelpers}
import felix.vdom._

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
}
