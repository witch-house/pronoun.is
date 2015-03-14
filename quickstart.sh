#!/bin/sh

lein uberjar
java -cp target/pronouns-standalone.jar clojure.main -m pronouns.web
open http://localhost:5000/ze/zir
