package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfFeature, CrfFeatureAnalyser}
import third_party.org.chokkan.crfsuite.Attribute
import scala.util.Try

class CrfFeatureTest extends FlatSpec with Matchers  {

  object TestInstanceBoolean {
    type tType = Boolean
    val analyser = CrfFeatureAnalyser[tType](_ => true)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  ignore should "return the same type as the type which is returned from its analyser for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceBoolean.feature.analyse(input)
    assert(actual.isInstanceOf[TestInstanceBoolean.tType])
  }

  ignore should "return the expected attribute for type `Boolean` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceBoolean.feature.toCrfJniInput(input)
    val attributesValue : Double = if(TestInstanceBoolean.feature.analyse(input)) 1d else 0d
    val expected = new Attribute(TestInstanceBoolean.feature.name, attributesValue)
//    actual.getAttr shouldBe expected.getAttr
//    actual.getValue shouldBe expected.getValue
  }


  object TestInstanceString {
    type tType = String
    val analyser = CrfFeatureAnalyser[tType](identity)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  ignore should "return the expected attribute for type `String` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceString.feature.toCrfJniInput(input)
    val attribute : String = s"${TestInstanceString.feature.name}=$input"
    val expected = new Attribute(attribute)
//    actual.getAttr shouldBe expected.getAttr
  }


  object TestInstanceInt {
    type tType = Int
    val output : tType = 0
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  ignore should "return the expected attribute for type `Int` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceInt.feature.toCrfJniInput(input)
    val expected =  new Attribute(TestInstanceInt.feature.name, Int int2double TestInstanceInt.output)
//    actual.getAttr shouldBe expected.getAttr
  }


  object TestInstanceDouble {
    type tType = Double
    val output : tType = 0
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  ignore should "return the expected attribute for type `Double` for input `arbitrary`" in {
    val input = "arbitrary"
    val actual = TestInstanceDouble.feature.toCrfJniInput(input)
    val expected =  new Attribute(TestInstanceInt.feature.name, TestInstanceInt.output)
//    actual.getAttr shouldBe expected.getAttr
  }


  object TestInstanceArbType {
    case class ArbType()
    type tType = ArbType
    val output : tType = ArbType()
    val analyser = CrfFeatureAnalyser[tType](_ => output)
    val name = "name"
    val feature = CrfFeatureTestImpl[tType](name)(analyser)
  }

  ignore should "throw an `UnsupportedOperationException` for any type other than `Boolean`, `String`, `Int`, `Double` for input `arbitrary`" in {
    val input = "arbitrary"
//    Try[Attribute](TestInstanceArbType.feature.toCrfJniInput(input)) recover {
//      case _ : UnsupportedOperationException => assert(true)
//      case _ => assert(false)
//    }
  }
}

case class CrfFeatureTestImpl[T](override val name : String)(override val analyser : CrfFeatureAnalyser[T]) extends CrfFeature[T]