#!/bin/sh

export PORT=6666
lein uberjar
java -cp target/pronouns-standalone.jar clojure.main -m pronouns.web
