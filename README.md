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
command:

```
$ lein ring server
```

This will launch a server running the app and open your default web browser to the index page. The server will automatically reload files as you edit them.

## Philosophy on pronoun inclusion

Pronoun.is aims foremost and exclusively to be a useful resource for people to communicate the personal pronoun they use for themselves.

It is possible to use these example sentences to demonstrate the usage of words that are not personal pronouns, or even cleverly insert an [entire story](http://pronoun.is/she/or%20they,%20those%20ships%20who%20were%20docked%20and%20still%20equipped%20with%20ancillaries,%20arranged%20to%20share%20the%20duty%20of%20monitoring%20our%20guest%20as%20it%20fit%20into%20their%20routines;%20that%20was%20the%20agreement,%20despite%20it%20being%20less%20convenient%20for%20me%20to%20participate%20at%20all,%20on%20the%20grounds%20that%20certain%20visitors%20might%20prefer%20a%20constant%20individual%20companion%20to%20what%20might%20seem,%20depending%20on%20their%20past%20experiences,%20to%20be%20undue%20attention%20from%20every%20soldier%20they%20passed.%20As%20usual,%20then,%20I%20took%20the%20first%20shift/the%20one%20possession%20of%20hers%20that%20Station%20Security%20hadn't%20confiscated,%20a/knowingly%20left%20with%20her.%20What%20a%20Presger%20frisbee%20might%20do%20or%20even%20look%20like%20I%20couldn't%20say.%20She%20hadn't%20seemed%20the%20sort%20to%20have%20alien%20technology,%20but,%20then%20again,%20neither%20had%20I/another%20unremarkable%20stranger,%20quite%20a%20ways%20down%20the%20concourse,%20who%20caught%20it%20with%20a%20degree%20of%20coordination%20that%20most%20would%20have%20overlooked.%20It%20did%20not%20escape%20my%20notice,%20however.%20"Cousin,"%20I%20said,%20enough%20to%20convey%20-%20unless%20our%20visitor%20were%20quite%20ignorant,%20but,%20of%20course,%20at%20this%20point%20I%20was%20certain%20that%20she%20couldn't%20be%20-%20both%20that%20I%20knew%20what%20she%20was%20not%20and%20that%20I%20was%20giving%20her%20the%20benefit%20of%20the%20doubt%20as%20to%20what,%20or%20who,%20she%20was)! However, as a policy we will not include such entries in the database.


## License
Copyright © 2014-2016 Morgan Astra <m@morganastra.me>
