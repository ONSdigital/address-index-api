package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser

class CrfFeatureTest extends FlatSpec with Matchers  {

  it should "return the same type as the type which is returned from its analyser for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceBoolean.feature.analyse(input)
    actual shouldBe a [java.lang.Boolean]
  }

  it should "return the expected `CrfJniInput` for type `Boolean` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceBoolean.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceBoolean.name}:1.0"
    expected shouldBe actual
  }

  it should "return the expected `CrfJniInput` for type `String` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceStringA.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceStringA.name}\\:$input:1.0"
    expected shouldBe actual
  }

  it should "return the expected `CrfJniInput` for type `String` for input `arbitrary:arbitrary`" in {
    val input = "arbitrary:arbitrary"
    val actual = Instances.TestInstanceStringB.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceStringB.name}\\:${input.replace(":", "\\:")}:1.0"
    expected shouldBe actual
  }

  it should "return the expected `CrfJniInput` for type `Int` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceInt.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceInt.name}:0.0"
    expected shouldBe actual
  }

  it should "return the expected `CrfJniInput` for type `Double` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceDouble.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceDouble.name}:0.0"
    actual shouldBe expected
  }

  it should "qualify any feature analyser's name with a `:` in it across all supported return types" in {
    val input = "arbitrary"
    val actual = Instances.TestInstanceQualifyName.feature.toCrfJniInput(input)
    val expected = s"\t${Instances.TestInstanceQualifyName.name.replace(":", "\\:")}:0.0"
    actual shouldBe expected
  }

  it should "throw an `UnsupportedOperationException` for any type other than `Boolean`, `String`, `Int`, `Double` for input `arbitrary`" in {
    val input = "arbitrary"
    an [UnsupportedOperationException] should be thrownBy Instances.TestInstanceArbType.feature.toCrfJniInput(input)
  }

  trait TestInstance[T] {
    def output: T
    def analyser = CrfFeatureAnalyser[T](_ => output)
    def name = "name"
    def feature = CrfFeatureTestImpl[T](name)(analyser)
  }

  object Instances {
    def apply[T]()(implicit ev: TestInstance[T]): TestInstance[T] = ev

    case class ArbType()

    implicit object TestInstanceArbType extends TestInstance[ArbType] {
      def output = ArbType()
    }
    implicit object TestInstanceBoolean extends TestInstance[Boolean] {
      def output = true
    }
    implicit object TestInstanceInt extends TestInstance[Int] {
      def output = 0
    }
    implicit object TestInstanceStringA extends TestInstance[String] {
      def output = "arbitrary"
    }
    implicit object TestInstanceStringB extends TestInstance[String] {
      def output = "arbitrary:arbitrary"
    }
    implicit object TestInstanceDouble extends TestInstance[Double] {
      def output = 0d
    }
    implicit object TestInstanceQualifyName extends TestInstance[Double] {
      def output = 0d
      override def name = "name:WithSomethingToQualify"
    }
  }
}

case class CrfFeatureTestImpl[T](override val name: String)(override val analyser: CrfFeatureAnalyser[T]) extends CrfFeature[T]