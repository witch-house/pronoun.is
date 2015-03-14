#!/bin/sh

export port=$1
lein uberjar
java -cp target/pronouns-standalone.jar clojure.main -m pronouns.web
open http://localhost:"$port"/ze/zir
