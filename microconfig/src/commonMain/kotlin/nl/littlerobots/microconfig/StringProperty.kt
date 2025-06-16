package nl.littlerobots.microconfig

open class StringProperty(
    private val value: String,
) : RuntimeProperty {
    override fun matches(s: String): Boolean {
        return s == value
    }
}