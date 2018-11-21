## Changelog 

### 1.2.0 (pending)

* changed default values for all reference types to null; in particular:
  * default for Strings, Files, URIs and URLs
  * default for Wrapper types (java.lang.Boolean, java.lang.Integer etc)
  Rationale: this is consistent with the other reference types, and it allows to
  detect if a boolean flags has not been set
* update sushi 3.1.1 to 3.2.0
* update lazy-foss-parent 1.0.2 to 1.0.3

### 1.1.1 (2017-07-17)

* Cli.commands is public now (to allow collecting command names for bash completion)


### 1.1.0 (2016-07-08)

* Options can be specified in a single argument now: -foo=bar.
* Added Cli.defaults for configurable default values
* Added Cli.base to inherit definitions


### 1.0.1 (2016-03-05)

* Fix setting arguments twice.


### 1.0.0 (2016-03-01)

* Extracted from Sushi and heavily reworked.
