trait Animal {
  def makeSound: String
}

class Dog extends Animal {
  override def makeSound: String = "Woof"
}

class Cat extends Animal {
  override def makeSound: String = "Meow"
}

object OffSpringName {
  trait OffSpringName[T <: Animal] {
    def offSpringName(t: T): String
  }

  //  implicit class OffSrpingNameOp[T : OffSpringName](t: T) {
  //    def offSpring: String = implicitly[OffSpringName[T]].offSpringName(t)
  //  }
  implicit class OffSrpingNameOp[T <: Animal](t: T)(implicit o: OffSpringName[T]) {
    def offSpring: String = o.offSpringName(t)
  }

  implicit object CatOffSpringName extends OffSpringName[Cat] {
    override def offSpringName(t: Cat): String = "Kitten"
  }
  implicit object DogOffSpringName extends OffSpringName[Dog] {
    override def offSpringName(t: Dog): String = "Puppy"
  }
  //  implicit object IntOffSpringName extends OffSpringName[Int] {
  //    override def offSpringName(t: Int): String = "AnotherNumber"
  //  }
}

import OffSpringName._
(new Cat).offSpring
(new Dog).offSpring
//1.offSpring

implicitly[OffSpringName[Cat]].offSpringName(new Cat)
implicitly[Ordering[String]]
