# Indexing and consuming CSV files
I started from <https://www.elastic.co/blog/indexing-csv-elasticsearch-ingest-node>.

Let's use a dataset at <https://data.ny.gov/Transportation/NYC-Transit-Subway-Entrance-And-Exit-Data/i9wp-a4ja>, ~1800 lines of data, containing 32 fields.

Each line will be turned into a simple `JSON` object

### Ingest
```
./ingest.csv.sh
```

### Search/Query
See <https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started-search.html>

```
./search.01.sh
```

#### Same data, in JSON
```
./ingest.json.sh
```
then
```
./search.02.sh
```

## TODO
- Sort
