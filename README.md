OVERVIEW

Language Analysis Essentials or LANES is a suite of open source Java modules for natural language processing and text mining. The toolkit includes modules for:

- measurement of content bearingness of terms.
- measurement of surface similarity of strings.
- parsing sentences and extracting keyterms.
- clustering of terms (with measurement of semantic relatedness of terms).
- determine the stability of word sequences.
- detecting classes of entities. the detection rate of this technique is about 95%, which outperforms state-of-the-art open-source and proprietary techniques such as Alchemy (73%), Illinois (13%), Stanford (13%) and Lexalytics (26%).
- mapping terms to Wikipedia concepts*.
- extraction of textual content from html pages*.
 
 *Not available currently
 
 
INSTALLATION

- Java 7 or higher is required for compiling and running the Java codes. If you're using the library on a Windows platform, use the build.bat and run.bat. Configure the paths in these two files accordingly.
- The library depends on MySQL, Solr index of Wikipedia and WordNet for providing, storing and indexing the background knowledge used by the tools. Please ensure that you have the first two software installed.
- Create a MySQL database called "lanes" and data from two tables need to be populated into "lanes". The first table is called "lexicon" and the SQL file containing the insert  statements is located in /data/lanes_lexicon.sql. The second (two-column) table called "category" is a mash up of links from Wikipedia articles to categories and from categories to categories. The file /data/lanes_category.sql contains only the structure of the table. These links can be downloaded from DBPedia.
- A Solr instance is required to index the full text of Wikipedia. The Wikipedia text dump can be downloaded from Wikipedia.
- As for WordNet, it's packaged together with LANES in /data/wnet/


LICENSE

- The LANES source code is released under the GNU GPL v3 License. Refer to the Java docs for more information. Some of the incorporated code and data fall under different licenses, all of which are GNU GPL compatible, as listed below.
- Java WordNet Library for lemmatisation: The library and WordNet are both licensed under the BSD License.
- Illinois Part of Speech Tagger for part-of-speech tagging: Licensed under the University of Illinois/NCSA Open Source License.
- Illinois Learning Based Java Package for tokenisation: Licensed under the University of Illinois/NCSA Open Source License.
- Apache Solr for indexing and searching the Wikipedia text dump: Licensed under the Apache License 2.0.
- Wikipedia text dump: Licensed under the GNU Free Documentation License.
- Lang and Codec libraries from Apache Commons for string manipulation, coding and encoding: These libraries are licensed under the Apache License 2.0.


CONFIGURATION

- Please configure the paths, user name and password in /src/org/lanes/utility/CommonData.java accordingly.
- Also, look into the /data/conf/ directory for files that require configuring.


EXAMPLES

- Please refer to /src/org/lanes/Example.java


TO-DO

- Make available the Solr index of Wikipedia
- Make available the Wikipedia link data in the "category" table.
