# [pronoun.is](http://pronoun.is) is a www site for showing people how to use pronouns

## For users

You can use any pronouns you like simply by filling them into the
url path. For example, http://pronoun.is/ze/zir/zir/zirs/zirself

That's pretty unwieldy! Fortunately you can also give it only the
first pronoun or two: http://pronoun.is/she/her or http://pronoun.is/they

Automatically filling in the rest from only one or two forms only
works for pronouns in the [database](resources/pronouns.tab). If the
pronouns you or a friend uses aren't supported, please let us know and
we'll add them. Alternatively you could add them yourself and submit a
pull request (see the next section for details)

## For developers

### The database

The pronouns "database" is a tab-delimited file with fields and
example values as follows:

subject|object|possessive-determiner|possessive-pronoun|reflexive
-------|------|---------------------|------------------|---------
they   | them | their               | theirs           | themselves

If you edit it with a text editor, make sure your editor inputs real
tab characters in that file (a thing your editor might normally be
configured not to do!) In Emacs, you can input real tabs by doing
Ctrl+q <tab>

### Running the app in a dev environment

You can launch the app on your own computer by running the following
commands:

```
$ lein uberjar
$ java -cp target/pronouns-standalone.jar clojure.main -m pronouns.web
```

Then browse to localhost:5000

You can also just run quickstart.sh which will do all of these things for you. Do it like this to get an example running on port 666

```
$ ./quickstart.sh 666
```

## License
Copyright © 2014-2015 Morgan Astra <m@morganastra.me>
