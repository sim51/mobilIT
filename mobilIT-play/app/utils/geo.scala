package utils

/**
 * User: andy
 */

case class LatLong(latitude:Double, longitude:Double) {
  require(-90 <= latitude && latitude <= 90, "The Latitude MUST be between -90 (90°N) and 90 (90°S)")
  require(-180 <= longitude && longitude <= 180, "The Longitude MUST be between -180 (180°W) and 180 (180°E)")
  
}

case class Lat(l:Double) {
  require(-90 <= l && l <= 90, "The Latitude MUST be between -90 (90°N) and 90 (90°S)")

  def N(L:Double):LatAndOrientationWithLong = LatAndOrientationWithLong(LatAndOrientation(this, true), L)
  def S(L:Double):LatAndOrientationWithLong = LatAndOrientationWithLong(LatAndOrientation(this, false), L)
}

case class LatAndOrientation(lat:Lat, north:Boolean) {
  def apply(L:Double):LatAndOrientationWithLong = LatAndOrientationWithLong(this, L)
  
  lazy val value:Double = if (north) -lat.l else lat.l
  
}

case class LatAndOrientationWithLong(lao:LatAndOrientation, L:Double) {
  require(-180 <= L && L <= 180, "The Longitude MUST be between -180 (180°W) and 180 (180°E)")

  def W:LatLong = LatLong(lao.value, -L)
  def E:LatLong = LatLong(lao.value, L)
}

object LatLong {
  
  implicit def doubleToLat(d:Double) = new Lat(d)
  
  //val latLongExample = -2.0 N 58.2 E
  
  
}