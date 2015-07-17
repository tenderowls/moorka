import felix.dsl.{HtmlHelpers, EntriesGenerator}
import felix.vdom._

import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object felix extends HtmlHelpers {

  implicit def toEntriesGenerator(x: Symbol): EntriesGenerator = new EntriesGenerator(x)

  implicit def toElements(xs: Seq[Element]): Elements = Elements(xs)

  implicit def toTextEntry(s: String): TextEntry = TextEntry(s)
}
