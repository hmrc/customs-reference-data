package models

import base.SpecBase
import generators.ModelArbitraryInstances
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.Arbitrary.arbitrary

class ReferenceDataListSpec extends SpecBase with ScalaCheckDrivenPropertyChecks with ModelArbitraryInstances {


  "ReferenceDataList" - {

    "must serialise" in {

      forAll(arbitrary[ReferenceDataList]) {
        referenceDataList =>
          ???
      }


    }

  }

}
