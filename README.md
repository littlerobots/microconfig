# Micro config

This is a tiny library to manage run-time arbitrary config stored as json on a server.

## Format the json format should look like this

```json
{
  "settings": {},
  "overrides": {}
}
```

The `settings` key holds the app configuration for your application. This is an
arbitrary json object specific to your app.

The optional `overrides` key holds any override based on runtime properties you provide to the
library.

The example below shows a more complete example that as an override that is active
when the `platform` is `ios` and the current date is between the first of January and
the 10th of January 2026.

While both `from` and `to` are specified for the `schedule` property in this example,
only `from` or `to` is required. The `schedule` property itself is optional.

```json
{
  "settings": {
    "greeting": "Hello from my app",
    "should_greet": true
  },
  "overrides": [
    {
      "matching": [
        {
          "platform": "ios"
        }
      ],
      "settings": {
        "greeting": "Happy new year dear iOS user!"
      },
      "schedule": {
        "from": "2026-01-01T00:00:00Z",
        "to": "2026-01-10T00:00:00Z"
      }
    }
  ]
}
```

With in the time the override is active the effective config will be

```json
{
  "greeting": "Happy new year dear iOS user!",
  "should_greet": true
}
```

## Defining configuration

A config class is a data class with default values that is `@Serializable` with Kotlin
serialization.

```kotlin
@Serializable
data class MyConfig(
    val greeting: String = "Hello world",
    @SerialName("should_greet") val shouldGreet: Boolean = false
)
```

## Resolving configuration

To resolve the configuration, it should first be deserialized into the `Config`
data class provided by the library. The 
[`Config`](https://github.com/littlerobots/microconfig/blob/main/microconfig/src/commonMain/kotlin/nl/littlerobots/microconfig/Config.kt#L26)
data class represents the settings
with their overrides as stored on the server.

```kotlin
val config: Config = fetchConfigAndDeserialize()
val myConfig: MyConfig = config.resolve(
    MyConfig.serializer(),
    propertiesOf(
        // the property is set based on runtime conditions
        "platform" to StringProperty("android"),
        // other properties that are used for overrides
    )
)
println(myConfig) // prints MyConfig(greeting=Hello world, shouldGreet=true)
```

## Runtime properties

A `RuntimeProperty` is a property that is defined to match
`overrides` in a setting. Each override has a `matching` property with a map of keys and
values. For an override to apply *all* properties in a single override should match.

Two implementations for `RuntimeProperty` are provided, a `StringProperty` that
matches values and a `VersionProperty` that uses
the [semver](https://github.com/z4kn4fein/kotlin-semver)
library to match versions and version ranges.

## MicroConfigClient

To fetch, cache and resolve config the `client` module provides `MicroConfigClient`.

## Acknowledgements

This work was inspired on https://github.com/egeniq/app-remote-config

## License

```
/*
 * Copyright 2025 Little Robots
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```