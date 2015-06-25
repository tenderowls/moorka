import felix.dom._

import scala.language.implicitConversions

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
package object felix {

  implicit def toEntriesGenerator(x: Symbol): EntriesGenerator = new EntriesGenerator(x)

  implicit def toElements(xs: Seq[Element]): Elements = Elements(xs)

  implicit def toTextEntry(s: String): TextEntry = TextEntry(s)
}
