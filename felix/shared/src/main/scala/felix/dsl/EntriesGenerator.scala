package felix.dsl

import felix.core.{EventProcessor, FelixSystem}
import felix.vdom.directives.{AttributeDirective, EventDirective, PipeToDirective, PropertyDirective}
import felix.vdom.{Directive, Element, Entry}
import moorka.death.Reaper
import moorka.rx.{Var, Channel, Rx}

import scala.collection.mutable
import scala.language.implicitConversions

private object EntriesGenerator {

  val nameCache = mutable.Map.empty[Symbol, String]

  val twoWayBindingDefaultEvents = Seq("input", "change")
  
  def htmlName(x: Symbol): String = {
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
    val element = new Element(htmlName(self), system)
    element.append(xs)
  }

  /**
   * Set property named as Symbol to tag  
   * @param value property value
   */
  def :=(value: Any): Directive = {
    new PropertyDirective.Simple(self.name, value)
  }

  /**
   * Reactive set property named as Symbol to tag
   * @param value reactive property value
   */
  def :=(value: Rx[Any]): Directive = {
    new PropertyDirective.Reactive(self.name, value)
  }

  /**
   * todo doc
   */
  def =:=[T](value: Var[T])(implicit system: FelixSystem): Directive = {
    new PropertyDirective.TwoWayBinding(
      self.name, 
      value,
      twoWayBindingDefaultEvents,
      system
    )
  }
  
  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /=(value: String): Directive = {
    new AttributeDirective.Simple(htmlName(self), value)
  }

  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /=(value: Rx[String])(implicit reaper: Reaper = Reaper.nice): Directive = {
    new AttributeDirective.Reactive(htmlName(self), value.map(Some(_)))
  }

  /**
   * Set attribute named as Symbol to tag
   * @param value property value
   */
  def /==(value: Rx[Option[String]]): Directive = {
    new AttributeDirective.Reactive(htmlName(self), value)
  }

  /**
   * Set attribute named as Symbol without value to the tag
   */
  def attr: Directive = {
    new AttributeDirective.Simple(htmlName(self), "")
  }

  def listen(f: EventProcessor.EventListener)
            (implicit system: FelixSystem): Directive = {
    new EventDirective(self.name, f, false, system)
  }

  def listen[U](f: ⇒ U)(implicit system: FelixSystem): Directive = {
    new EventDirective(self.name, (_,_,_) ⇒ f, false, system)
  }

  def capture(f: EventProcessor.EventListener)
            (implicit system: FelixSystem): Directive = {
    new EventDirective(self.name, f, true, system)
  }

  def capture[U](f: ⇒ U)(implicit system: FelixSystem): Directive = {
    new EventDirective(self.name, (_,_,_) ⇒ f, true, system)
  }
  
  def pipeTo(channel: Channel[Unit])
            (implicit system: FelixSystem): Directive = {
    new PipeToDirective(self.name, channel, system)
  }
}
