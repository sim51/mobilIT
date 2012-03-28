package models

import utils.{LatLong, GazetteerResolver}


/**
 * User: andy
 */
case class Address(street: String, number: String, city: String, zipCode: String, county: String, country: String) {

  def latLong(implicit r: GazetteerResolver): Option[LatLong] = for (
    g <- r.findGazetteer(this);
    l <- g.latLong(this)
  ) yield l

}
