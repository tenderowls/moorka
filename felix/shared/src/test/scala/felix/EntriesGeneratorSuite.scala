package felix

import moorka.flow.Context
import moorka.flow.mutable.Var
import utest.{TestSuite, TestableString, assert}
import vaska.JSAccess

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object EntriesGeneratorSuite extends TestSuite {

  val buffer = mutable.Buffer.empty[Seq[Any]]

  def clean() = if (buffer.nonEmpty) {
    buffer.remove(0, buffer.length)
  }

  implicit val felixSystem = {
    val ec = utest.ExecutionContext.RunNow
    val system = new FelixSystem {
      val executionContext: ExecutionContext = ec
      val flowContext = Context()
      val jsAccess: JSAccess = new JSAccess {
        implicit val executionContext: ExecutionContext = ec

        def send(args: Seq[Any]): Unit = buffer += args
      }
    }
    clean()
    system
  }

  implicit val context = felixSystem.flowContext

  val tests = TestSuite {
    "check system initialization" - {
      felixSystem.global
      felixSystem.document
      felixSystem.utils
      val length = buffer.length
      assert(length == 2)
      utest.assertMatch(buffer.head) { case Seq(_, "getAndSaveAs", "@link:global", "document", "^document") ⇒ }
      utest.assertMatch(buffer(1)) { case Seq(_, "getAndSaveAs", "@link:global", "Felix", "^Felix") ⇒ }
    }
    "check element generation" - {
      clean()
      val _ = 'div(
        'span('name /= "firstSpan"),
        "Hello",
        'mySpan('className := "secondSpan", 'disabled attr)
      )
      type S = String
      utest.assertMatch(buffer) {
        case Seq(
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "span", span1: S),
        Seq(_, "call", setAttributeLink: S, "setAttribute", "name", "firstSpan"),
        Seq(_, "registerCallback", _),
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "my-span", span2: S),
        Seq(_, "set", setPropertyLink: S, "className", "secondSpan"),
        Seq(_, "call", _, "setAttribute", "disabled", ""),
        Seq(_, "callAndSaveAs", "@link:^Felix", "createElementAndSetId", _, "div", div: S),
        Seq(_, "call", "@link:^Felix", "appendChildren", appendLink: S, Seq(fcLink: S, "Hello", scLink:S)))
          if appendLink.endsWith(div) &&
             setAttributeLink.endsWith(span1) &&
             setPropertyLink.endsWith(span2) &&
             fcLink.endsWith(span1) &&
             scLink.endsWith(span2) ⇒
      }
    }
    "check Flow attributes" - {
      val rxName = Var("name1")
      val rxClass = Var(Option.empty[String])
      val element = 'div(
        'name /= rxName,
        'class /== rxClass
      )
      context.validate()
      clean()
      rxName.update("name2")
      rxClass.update(Some("hello"))
      rxClass.update(None)
      context.validate()
      utest.assert(buffer.length == 3)
      utest.assertMatch(buffer.head) {
        case Seq(_, "call", link: String, "setAttribute", "class", "hello")
          if link.endsWith(element.ref.id) ⇒ ()
      }
      utest.assertMatch(buffer(1)) {
        case Seq(_, "call", link: String, "removeAttribute", "class")
          if link.endsWith(element.ref.id) ⇒ ()
      }
      utest.assertMatch(buffer(2)) {
        case Seq(_, "call", link: String, "setAttribute", "name", "name2")
          if link.endsWith(element.ref.id) ⇒ ()
      }
    }
    "check Flow properties" - {
      val rxName = Var("name1")
      val element = 'div('name := rxName)
      context.validate()
      clean()
      rxName.update("name2")
      context.validate()
      utest.assert(buffer.length == 1)
      utest.assertMatch(buffer.head) {
        case Seq(_, "set", link: String, "name", "name2")
          if link.endsWith(element.ref.id) ⇒ ()
      }
    }
  }
}
