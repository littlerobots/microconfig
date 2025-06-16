package nl.littlerobots.microconfig

interface RuntimeProperty {
  fun matches(s: String): Boolean
}

fun propertiesOf(vararg properties: Pair<String, RuntimeProperty>): Map<String, RuntimeProperty> {
  return properties.toMap()
}
