import scala.language.reflectiveCalls

// ref: http://jsuereth.com/2010/07/13/monkey-patching-scala.html
trait Resource[R] {
  def close(r: R) : Unit
}

object Resource {
  implicit def genericResourceTrait[A <: { def close(): Unit }] = new Resource[A] {
    println(s"in genericResourceTrait")
    override def close(r: A) = r.close()
    override def toString = "Resource[{def close() : Unit }]"
  }
  implicit def jioResourceTrait[A <: java.io.Closeable] = new Resource[A] {
    println(s"in jioResourceTrait")
    override def close(r: A) = r.close()
    override def toString = "Resource[java.io.Closeable]"
  }
}

def withResource[A : Resource, B](resource : => A)(f : A => B) = {
  val r = resource
  try {
    f(r)
  } finally {
    println(s"c = ${implicitly[Resource[A]]}")
    implicitly[Resource[A]].close(r)
  }
}

def withResource2[A, B](resource : => A)(f : A => B)(implicit c: Resource[A]) = {
  val r = resource
  try {
    f(r)
  } finally {
    println(s"c = $c")
    c.close(r)
  }
}

withResource(new java.io.StringReader("HAI")) { input => () }
withResource2(new java.io.StringReader("HAI")) { input => () }
def showTypeTraitUsed[A : Resource](a : A) = println(implicitly[Resource[A]])

showTypeTraitUsed(new java.io.StringReader("HAI"))

implicit case object IntAsResource extends Resource[Int] {
  override def close(r: Int): Unit = println(s"closing $r")
}

showTypeTraitUsed(5)

withResource(6)(r => println(s"r = $r"))
withResource2(7)(r => println(s"r = $r"))

case object MyClosable {
  def close() {
    println(s"closing MyClosable")
  }
}
withResource(MyClosable)(r => println(s"r = $r"))
showTypeTraitUsed(MyClosable)
