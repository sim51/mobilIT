package utils

import models.Address

/**
 * User: andy
 */

trait GazetteerResolver {

  def findGazetteer(address:Address): Option[Gazetteer]

}

object GazetteerResolver {

  //todo remove this
  implicit object dummy extends GazetteerResolver {
    def findGazetteer(address: Address) = Some(Gazetteer.dummy)
  } 
  
} 

trait Gazetteer {
  
  def definedFor(address:Address):Boolean
  
  def latLong(address:Address):Option[LatLong]
  
}

object Gazetteer {
  import LatLong._
  
  //todo remove this
  implicit object dummy extends Gazetteer {
    def definedFor(address: Address) = true

    def latLong(address: Address) = Some(50 N -5 E)
  }

}
