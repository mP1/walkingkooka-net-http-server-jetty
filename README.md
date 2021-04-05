[![Build Status](https://github.com/mP1/walkingkooka-net-http-server-jetty/actions/workflows/build.yaml/badge.svg)](https://github.com/mP1/walkingkooka-net-http-server-jetty/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/walkingkooka-net-http-server-jetty/badge.svg?branch=master)](https://coveralls.io/github/mP1/walkingkooka-net-http-server-jetty?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/walkingkooka-net-http-server-jetty.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-server-jetty/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/walkingkooka-net-http-server-jetty.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/walkingkooka-net-http-server-jetty/alerts/)

# Basic Project

A jetty powered HTTP Server that uses the rich type safe, immutable and functional abstractions for headers, request/response
and more. Accurately consume headers with abstractions that model all the RFC properties and values without nasty
Regular expressions or String scanning hacks.

The ContentRange abstractions provides a better way to support responses with ranges rather than always providing the complete body.

```java
// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Range
final ContentRange contentRange = ContentRange.parse("bytes 2-11/888");
assertEquals(RangeHeaderValueUnit.BYTES, contentRange.unit());
assertEquals(Optional.of(Range.greaterThanEquals(2L).and(Range.lessThanEquals(11L))), contentRange.range());
assertEquals(Optional.of(888L), contentRange.size());
```

This example below uses `AcceptEncoding` to examine the available encodings, and honour the quality factor.

Working with [Quality-values](https://developer.mozilla.org/en-US/docs/Glossary/Quality_values) becomes rather simple
using the provided abstractions and utility methods

```text
    text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
    indicates the order of priority:

    Value	                            Priority
    text/html and application/xhtml+xml	1.0
    application/xml	                    0.9
    */*
```

```java
// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Encoding
final AcceptEncoding acceptEncoding = AcceptEncoding.parse("a;q=0.5,b");
assertEquals(Lists.of(AcceptEncodingValue.with("b"),
        AcceptEncodingValue.with("a").setParameters(Maps.of(AcceptEncodingValueParameterName.with("q"), 0.5f))),
        acceptEncoding.qualityFactorSortedValues());
```



