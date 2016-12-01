package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfFeature, CrfFeatureAnalyser}
import scala.util.Try

class CrfFeatureTest extends FlatSpec with Matchers  {

  object TestInstanceBoolean {
    type tType = Boolean
    val analyser = CrfFeatureAnalyser[tType](_ => true)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "return the same type as the type which is returned from its analyser for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceBoolean.feature.analyse(input)
    assert(actual.isInstanceOf[TestInstanceBoolean.tType])
  }

  it should "return the expected attribute for type `Boolean` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceBoolean.feature.toCrfJniInput(input)
    val expected = s"\t${TestInstanceBoolean.name}:1.0\n"
    expected shouldBe actual
  }

  object TestInstanceStringA {
    type tType = String
    val analyser = CrfFeatureAnalyser[tType](identity)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "return the expected attribute for type `String` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceStringA.feature.toCrfJniInput(input)
    val expected = s"\t${TestInstanceStringA.name}\\:$input:1.0\n"
    expected shouldBe actual
  }

  object TestInstanceStringB {
    type tType = String
    val analyser = CrfFeatureAnalyser[tType](identity)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "return the expected attribute for type `String` for input `arbitrary:arbitrary`" in {
    val input = "arbitrary:arbitrary"
    val actual = TestInstanceStringB.feature.toCrfJniInput(input)
    val expected = s"\t${TestInstanceStringB.name}\\:${input.replace(":", "\\:")}:1.0\n"
    expected shouldBe actual
  }

  object TestInstanceInt {
    type tType = Int
    val output: tType = 0
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "return the expected attribute for type `Int` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceInt.feature.toCrfJniInput(input)
    val expected = s"\t${TestInstanceInt.name}:0.0\n"
    expected shouldBe actual
  }

  object TestInstanceDouble {
    type tType = Double
    val output: tType = 0d
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "return the expected attribute for type `Double` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceDouble.feature.toCrfJniInput(input)
    val expected = s"\t${TestInstanceDouble.name}:0.0\n"
    actual shouldBe expected
  }

  object TestInstanceArbType {
    case class ArbType()
    type tType = ArbType
    val output: tType = ArbType()
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  it should "throw an `UnsupportedOperationException` for any type other than `Boolean`, `String`, `Int`, `Double` for input `arbitrary`" in {
    val input = "arbitrary"
    Try[String](TestInstanceArbType.feature.toCrfJniInput(input)) recover {
      case _: UnsupportedOperationException => assert(true)
      case _ => assert(false)
    }
  }
}

case class CrfFeatureTestImpl[T](override val name: String)(override val analyser: CrfFeatureAnalyser[T]) extends CrfFeature[T]