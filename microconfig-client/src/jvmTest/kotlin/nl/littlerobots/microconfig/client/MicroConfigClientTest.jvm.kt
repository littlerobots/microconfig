package nl.littlerobots.microconfig.client

import kotlinx.io.files.Path

actual fun createTempFile(): Path {
    return Path(kotlin.io.path.createTempFile().toAbsolutePath().toString())
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreNative actual constructor()