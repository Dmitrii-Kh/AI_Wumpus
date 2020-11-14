package agents


data class Point(var x : Int, var y : Int, var value:Int) : Comparable<Point> {
    override fun compareTo(other: Point): Int {
        return this.value.compareTo(other.value)
    }
}