# preactile
A scalajs UI library utilizing Preact

To run the example app:

```
sbt ~example/ascentPreview
```

No Node or npm needed for bundling. [sbt-splice](https://github.com/early-effect/sbt-splice)
inlines a pinned Preact into the Scala.js output. The example is served at
`http://localhost:8766` and live-reloads on every re-splice.

Docs: `sbt ~docs/specularPreview` at `http://localhost:8765`.

For a production bundle, `sbt example/spliceFull` writes a Closure-optimized
`example/target/splice/full.js`.

## installation
[![Maven Central](https://img.shields.io/maven-central/v/rocks.earlyeffect/preactile_sjs1_3.svg)](https://mvnrepository.com/artifact/rocks.earlyeffect/preactile)

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile" % "<version>"
```

For conduit component support:

```scala
libraryDependencies += "rocks.earlyeffect" %% "preactile-conduit" % "<version>"
```

Copyright Russell White

Licensed under the Apache License, Version 2.0 (the "License");
you may not use the code in this repository except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.