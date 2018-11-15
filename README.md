**[pronoun.is](https://pronoun.is) is a website for personal pronoun usage examples**

## For users

You can use any pronouns you like simply by filling them into the
url path. For example, https://pronoun.is/ze/zir/zir/zirs/zirself

That's pretty unwieldy! Fortunately you can also give it only the
first pronoun or two: https://pronoun.is/she/her or https://pronoun.is/they

Automatically filling in the rest from only one or two forms only
works for pronouns in the [database][pronoun-database]. If the
pronouns you or a friend uses aren't supported, please let us know and
we'll add them. Alternatively you could add them yourself and submit a
pull request (see the next section for details)

## For developers

### The database

The pronouns "database" is a tab-delimited file located in [resources/pronouns.tab][pronoun-database] with fields and example values as follows:

subject|object|possessive-determiner|possessive-pronoun|reflexive
-------|------|---------------------|------------------|---------
they   | them | their               | theirs           | themselves

The top 6 pronouns are displayed on the front page. Please don't edit these
without talking to me, they've been hand-curated based on usage frequency.
Below the top 6, the remaining pronouns are sorted in alphabetical order by
subject and then in roughly frequency order for sets that have the same subject
pronoun. If you're adding a set that shares the same object pronoun as other
set(s) already in the database, please insert it immediately below those ones.

If you edit the database with a text editor, make sure your editor inputs real
tab characters in that file (a thing your editor might normally be configured 
not to do!) In Emacs, you can input real tabs by doing Ctrl+q <tab>. 
In Vi you can use Ctrl+v <tab>.

[pronoun-database]: resources/pronouns.tab

### The code

The top-level logic for running the server lives in [`pronouns.web`](src/pronouns/web.clj)

Page rendering markup is in [`pronouns.pages`](src/pronouns/pages.clj), it uses
[hiccup](https://github.com/weavejester/hiccup) for rendering HTML from Clojure
datastructures.

[`pronouns.config`](src/pronouns/config.clj) is currently used only for loading
the [pronouns database][pronoun-database]

The unfortunately-named [`pronouns.util`](src/pronouns/util.clj) includes both
actual utility functions used elsewhere in the code, but also what you might
think of as "controllers" if you're used to the MVC model of web design - code
that does the computations necessary for the `pages` (analogous to "views")
to render themselves. We should probably break up `util` into (at least) two
namespaces and be a little more deliberate about where everything currently
in that namespace should live!

### Tests

Run the suite with `lein test`

Test coverage is not great but getting better. Please run the tests and
confirm that everything passes before merging changes, and please include
tests with any new logic you introduce in a PR!

Goals for the future include setting up automated CI to run the tests for
us on every PR branch

### Running the app in a dev environment

First, install [leiningen](https://leiningen.org/). Then you can launch the app
on your own computer by running the following command:

```
$ lein ring server
```

This will launch a server running the app and open your default web browser to 
the index page. The server will automatically reload files as you edit them -
with the unfortunate exception of `pronouns.tab`, which is loaded as a resource
and requires an app restart to reload.

### The git repo

For most of this project's history we had separate `master` and `develop`
branches but that's proven to be more trouble than it's worth. Going
forward we'll be doing all development in feature branches off of `master`,
and PRs should be issued against `master`.

Please follow [this guide](https://chris.beams.io/posts/git-commit/) 
for writing good commit messages :)

## Philosophy on pronoun inclusion

Pronoun.is aims foremost and exclusively to be a useful resource for people to communicate the personal pronoun they use for themselves.

It is possible to use these example sentences to demonstrate the usage of words that are not personal pronouns, or even cleverly insert an [entire story](https://pronoun.is/she/or%20they,%20those%20ships%20who%20were%20docked%20and%20still%20equipped%20with%20ancillaries,%20arranged%20to%20share%20the%20duty%20of%20monitoring%20our%20guest%20as%20it%20fit%20into%20their%20routines;%20that%20was%20the%20agreement,%20despite%20it%20being%20less%20convenient%20for%20me%20to%20participate%20at%20all,%20on%20the%20grounds%20that%20certain%20visitors%20might%20prefer%20a%20constant%20individual%20companion%20to%20what%20might%20seem,%20depending%20on%20their%20past%20experiences,%20to%20be%20undue%20attention%20from%20every%20soldier%20they%20passed.%20As%20usual,%20then,%20I%20took%20the%20first%20shift/the%20one%20possession%20of%20hers%20that%20Station%20Security%20hadn't%20confiscated,%20a/knowingly%20left%20with%20her.%20What%20a%20Presger%20frisbee%20might%20do%20or%20even%20look%20like%20I%20couldn't%20say.%20She%20hadn't%20seemed%20the%20sort%20to%20have%20alien%20technology,%20but,%20then%20again,%20neither%20had%20I/another%20unremarkable%20stranger,%20quite%20a%20ways%20down%20the%20concourse,%20who%20caught%20it%20with%20a%20degree%20of%20coordination%20that%20most%20would%20have%20overlooked.%20It%20did%20not%20escape%20my%20notice,%20however.%20"Cousin,"%20I%20said,%20enough%20to%20convey%20-%20unless%20our%20visitor%20were%20quite%20ignorant,%20but,%20of%20course,%20at%20this%20point%20I%20was%20certain%20that%20she%20couldn't%20be%20-%20both%20that%20I%20knew%20what%20she%20was%20not%20and%20that%20I%20was%20giving%20her%20the%20benefit%20of%20the%20doubt%20as%20to%20what,%20or%20who,%20she%20was)! However, as a policy we will not include such entries in the database.


## License
Copyright © 2014-2018 Morgan Astra <m@morganastra.me>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>

