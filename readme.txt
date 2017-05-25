ABOUT

LANES is a Java library of tools for natural language processing and text mining for the English language. For more information and the latest updates, visit http://lanes.codeplex.com. For information about the author Wilson Wong, visit http://wilsonwong.me.

For the license, please refer to license.txt.


INSTALLATION

(1) Java 7 or higher is required for compiling and running the Java codes. If you're using the library on a Windows platform, use the build.bat and run.bat. Configure the paths in these two files accordingly.

(2) The library depends on MySQL, Solr index of Wikipedia and WordNet for providing, storing and indexing the background knowledge used by the tools. Please ensure that you have the first two software installed.

(3) Create a MySQL database called "lanes" and data from two tables need to be populated into "lanes". The first table is called "lexicon" and the SQL file containing the insert  statements is located in /data/lanes_lexicon.sql. The second (two-column) table called "category" is a mash up of links from Wikipedia articles to categories and from categories to categories. The file /data/lanes_category.sql contains only the structure of the table. These links can be downloaded from DBPedia.

(4) A Solr instance is required to index the full text of Wikipedia. The Wikipedia text dump can be downloaded from Wikipedia.

(5) As for WordNet, it's packaged together with LANES in /data/wnet/


CONFIGURATION

(1) Please configure the paths, user name and password in /src/org/lanes/utility/CommonData.java accordingly.

(2) Also, look into the /data/conf/ directory for files that require configuring.


EXAMPLES

Please refer to /src/org/lanes/Example.java


TO DO

(1) Make available the Solr index of Wikipedia
(2) Make available the Wikipedia link data in the "category" table.