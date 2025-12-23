package com.ashkite.pictureclassification.data.geo

object GeoHash {
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    fun encode(lat: Double, lon: Double, precision: Int = 6): String {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        var isEven = true
        var bit = 0
        var ch = 0
        val sb = StringBuilder()

        while (sb.length < precision) {
            if (isEven) {
                val mid = (lonMin + lonMax) / 2
                if (lon >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    lonMin = mid
                } else {
                    lonMax = mid
                }
            } else {
                val mid = (latMin + latMax) / 2
                if (lat >= mid) {
                    ch = ch or (1 shl (4 - bit))
                    latMin = mid
                } else {
                    latMax = mid
                }
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                sb.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }

        return sb.toString()
    }
}
