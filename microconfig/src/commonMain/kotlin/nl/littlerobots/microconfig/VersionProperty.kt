package nl.littlerobots.microconfig

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.ConstraintFormatException
import io.github.z4kn4fein.semver.toVersion

/**
 * A property that matches a [Version] to an incoming [Constraint]
 * @property version the version to check
 * @property onInvalidConstraint a callback that is invoked if the incoming constraint is not valid
 */
class VersionProperty(
    private val version: Version,
    private val onInvalidConstraint: (String) -> Boolean = { false }
) : RuntimeProperty {
    constructor(
        version: String,
        onInvalidConstraint: (String) -> Boolean = { false }
    ) : this(version.toVersion(), onInvalidConstraint)

    override fun matches(s: String): Boolean {
        try {
            val constraint = Constraint.parse(s)
            return constraint.isSatisfiedBy(version)
        } catch (ex: ConstraintFormatException) {
            return onInvalidConstraint(s)
        }
    }
}