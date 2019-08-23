[![Build Status](https://api.travis-ci.org/axibase/date-processor.svg?branch=master)](https://travis-ci.org/axibase/date-processor)
[![License](https://img.shields.io/badge/License-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.axibase/date-processor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.axibase/date-processor)
[![Known Vulnerabilities](https://snyk.io/test/github/axibase/date-processor/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/axibase/date-processor?targetFile=pom.xml)

Date parsing and formatting library optimized for [Axibase date pattern syntax](https://axibase.com/docs/atsd/shared/time-pattern.html#date-and-time-patterns).

`com.axibase.date.DatetimeProcessor` performs parsing and formatting timestamps.

`com.axibase.date.PatternResolver` creates a `com.axibase.date.DatetimeProcessor` object by pattern.

Processor objects are immutable, so feel free to cache them for better performance.