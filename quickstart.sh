#!/bin/sh

export port=$1
lein uberjar
open http://localhost:"$port"/ze/zir
java -cp target/pronouns-standalone.jar clojure.main -m pronouns.web
