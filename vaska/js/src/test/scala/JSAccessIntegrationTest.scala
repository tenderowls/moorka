import utest.ExecutionContext.RunNow
import utest._
import vaska.{Hook, JSArray, JSObj, NativeJSAccess}

import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
 * NativeJSAccessSuite + vaska.js integration tests 
 * and some javascript-specific cases.
 * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
 */
object JSAccessIntegrationTest extends TestSuite {

  def createEnv(testEnv: js.Object): (js.Dynamic, JSObj) = {
    val g = js.Dynamic.global
    val scope = js.Dynamic.literal()
    val jsAccess = new NativeJSAccess(scope)
    val vaska = g.Vaska.create({ x: js.Any ⇒
      val event = js.Dynamic.literal(data = x)
      scope.onmessage(event)
    }, testEnv)
    scope.postMessage = { data: js.Any ⇒ vaska.receive(data) }
    (vaska, jsAccess.obj("testEnv"))
  }

  def createRootObject(testEnv: js.Object): JSObj = {
    createEnv(testEnv)._2
  }

  val tests = TestSuite {

    "Check get" - {
      val o = createRootObject {
        js.Dynamic.literal(
          width = 100f,
          height = 200f
        )
      }
      for {
        width ← o.get[Float]("width")
        height ← o.get[Float]("height")
      } yield {
        assert(width + height == 300f)
      }
    }

    "Check get with null" - {
      val o = createRootObject {
        js.Dynamic.literal(
          width = null
        )
      }
      for (width ← o.get[String]("width")) yield {
        assert(width == null)
      }
    }

    "Check set" - {
      var calls = 0
      val scope = js.Dynamic.literal(name = "")
      val o = createRootObject(scope)
      o.set("name", "Vaska") foreach { _ ⇒
        calls += 1
        assert(scope.name.asInstanceOf[String] == "Vaska")
      }
      assert(calls == 1)
    }

    "Check call" - {
      var calls = 0
      var fnCalls = 0
      val scope = js.Dynamic.literal(fun = { (x: Int, y: String) ⇒
        fnCalls += 1
        assert(x == 1)
        assert(y == "Cow")
        "Done"
      })
      val o = createRootObject(scope)
      o.call[String]("fun", 1, "Cow") foreach { res ⇒
        calls += 1
        assert(res == "Done")
      }
      assert(calls == 1)
      assert(fnCalls == 1)
    }

    "Check hooks" - {
      var successCalls = 0
      var failureCalls = 0
      var fnCalls = 0
      val scope = js.Dynamic.literal(
        fun = { (s: js.Function1[String, Unit], f: js.Function1[String, Unit], x: Int) ⇒
          fnCalls += 1
          if (x == 1) s("Done")
          else f(":(")
          "Done"
        }
      )
      val o = createRootObject(scope)
      def completeHandler(x: Try[String]) = x match {
        case Success(res) ⇒
          successCalls += 1
          assert(res == "Done")
        case Failure(e) ⇒
          failureCalls += 1
          assert(e.getMessage == ":(")
      }
      o.call[String]("fun", Hook.Success, Hook.Failure, 1).onComplete(completeHandler)
      o.call[String]("fun", Hook.Success, Hook.Failure, 2).onComplete(completeHandler)
      assert(failureCalls == 1)
      assert(successCalls == 1)
      assert(fnCalls == 2)
    }

    "Check save" - {
      val scope = js.Dynamic.literal(
        fun = { (x: Int) ⇒ js.Array[Int](x, x + 1) }
      )
      val env = createEnv(scope)
      for {
        arr ← env._2.call[JSArray]("fun", 10)
        _ ← arr.save()
        i0 ← arr[Int](0)
        i1 ← arr[Int](1)
      } yield {
        assert(env._1.checkLinkSaved(arr.id).asInstanceOf[Boolean])
        assert(i0 == 10)
        assert(i1 == 11)
      }
    }

    "Check saveAs" - {
      val scope = js.Dynamic.literal(
        fun = { (x: Int) ⇒ js.Array[Int](x, x + 1) }
      )
      val env = createEnv(scope)
      for {
        arr ← env._2.call[JSArray]("fun", 10)
        arr ← arr.saveAs("cow")
        i0 ← arr[Int](0)
        i1 ← arr[Int](1)
      } yield {
        assert(env._1.checkLinkSaved("cow").asInstanceOf[Boolean])
        assert(i0 == 10)
        assert(i1 == 11)
      }
    }

    "Check getAndSaveAs" - {
      val scope = js.Dynamic.literal(
        myObj = js.Dynamic.literal(x = 0, y = 1)
      )
      val env = createEnv(scope)
      for {
        myObj ← env._2.getAndSaveAs("myObj", "TheMyObj")
        x ← myObj.get[Int]("x")
        y ← myObj.get[Int]("y")
      } yield {
        assert(env._1.checkLinkSaved("TheMyObj").asInstanceOf[Boolean])
        assert(myObj.id == "TheMyObj")
        assert(x == 0)
        assert(y == 1)
      }
    }

    "Check free" - {
      val scope = js.Dynamic.literal(
        fun = { (x: Int) ⇒ js.Array[Int](x, x + 1) }
      )
      val env = createEnv(scope)
      for {
        arr ← env._2.call[JSArray]("fun", 10)
        _ ← arr.save()
        i0 ← arr[Int](0)
        i1 ← arr[Int](1)
        _ ← arr.free()
      } yield {
        assert(!env._1.checkLinkSaved(arr.id).asInstanceOf[Boolean])
        assert(i0 == 10)
        assert(i1 == 11)
      }
    }

    "Check callback" - {
      var cb: js.Function1[Int, Any] = null
      val scope = js.Dynamic.literal(
        onFun = { (x: js.Function1[Int, Any]) ⇒
          cb = x
        }
      )
      val root = createRootObject(scope)
      val access = root.jsAccess
      var i = 0
      for {
        jsCb ← access.registerCallback { x: Int ⇒
          i += x
        }
        _ ← root.call[Unit]("onFun", jsCb)
      } yield {
        cb(2)
        cb(3)
        assert(i == 5)
      }
    }
    
    // todo check send array
    // todo check object in hook
    // todo check object in callback
  }
}
