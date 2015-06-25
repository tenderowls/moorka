package felix.dom

import felix.core.FelixSystem
import felix.dom.directives.{AttributeDirective, PropertyDirective}
import moorka.rx.Rx
import moorka.rx.death.Reaper

import scala.language.implicitConversions
import scala.collection.mutable

private object EntriesGenerator {

  val nameCache = mutable.Map.empty[Symbol, String]

  implicit def symbolToEntryName(x: Symbol): String = {
    nameCache.getOrElseUpdate(x, x.name.replaceAll("([A-Z]+)", "-$1").toLowerCase
    )
  }
}

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
final class EntriesGenerator(val self: Symbol) extends AnyVal {

  import EntriesGenerator._

  /**
   * Creates tag from Symbol 
   * @param xs list of entries of the tag
   */
  def apply(xs: Entry*)(implicit system: FelixSystem): Element = {
    val element = new Element(self, system)
    element.append(xs)
  }

  /**
   * Set property named as Symbol to tag  
   * @param value property value
   */
  def :=(value: Any): Directive = {
    new PropertyDirective.Simple(self, value)
  }

  /**
   * Reactive set property named as Symbol to tag
   * @param value reactive property value
   */
  def :=(value: Rx[Any]): Directive = {
    new PropertyDirective.Reactive(self, value)
  }

  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /=(value: String): Directive = {
    new AttributeDirective.Simple(self, Some(value))
  }

  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /=(value: Rx[String])(implicit reaper: Reaper = Reaper.nice): Directive = {
    new AttributeDirective.Reactive(self, value.map(Some(_)))
  }

  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /==(value: Rx[Option[String]]): Directive = {
    new AttributeDirective.Reactive(self, value)
  }

  /**
   * Set attribute named as Symbol without value to the tag
   */
  def attr: Directive = {
    new AttributeDirective.Simple(self, None)
  }
}
