# About

A little **Linux/MAC only** command line tool to
download a work of art from
[MIA](https://new.artsmia.org/).

# Use

First of all, you'll need a link with the
following format:

> https://collections.artsmia.org/art/xxxx/xx-xxx-xxxx-xxx

Now, if you don't know what `lein` command tool
means just download the jar from the release
and execute it with the proper parameters:

```
wget https://github.com/frankitox/merge-mia-thumbnails/releases/download/v0.1-alpha/mia-work-to-png.jar
java -jar https://collections.artsmia.org/art/22753/bat-and-moon-yamada-hogyoku bat-and-moon.png
```

The alternative for Clojure aware people would be
to do:

```
git clone https://github.com/frankitox/merge-mia-thumbnails
cd merge-mia-thumbnails
lein run https://collections.artsmia.org/art/22753/bat-and-moon-yamada-hogyoku bat-and-moon.png
```

Either way, you'll end up with this
beautiful friend of the shadows:

![bat-and-moon-yamada-hogyoku](bat-and-moon-yamada-hogyoku.png)


The jar's
available here.
