information-retrieval
=====================
Computer Science Center, Information Retrieval, autumn 2014

# Generating JARs
To generate JARs, perform: `mvn package`

# Run Indexer
To see usage, perform: `java -jar indexer.jar --h`<br>
To run the indexer, perform: `java -jar indexer.jar -indexes <output-file-with-index> -folder <folder-with-files>`

# Run Searcher
To see usage, perform: `java -jar searcher.jar --h`<br>
To run the searcher, perform: `java -jar searcher.jar -indexes <input-file-with-index>`<br>
Requests are strings consisting of terms and operators **AND** and **OR**.

# Run Metrics
To run the metrics, perform: `java -jar metrics.jar`<br>
