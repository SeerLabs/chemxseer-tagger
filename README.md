ChemxSeer Tagger
============================
ChemxSeer Tagger provides a chemcial entity extractor that identifies
mentions of chemical formula and names in free text. 

# Install
## Binary
- Download the distribution from [here](http://www.personal.psu.edu/mxk479/chemxseer/chemxseer-tagger-dist.tar.gz)
- Extract the compressed file 
```bash
tar xvf chemxseer-tagger-dist.tar.gz
```
- To run the tagger on a plain text document or a folder containing multiple documents, use the *batchTag.sh* script:
```bash
./bashTag.sh indir outdir
```
where indir is path to directory containing text files, and outdir is directory into which tagged files will be written
For each input file, an output file will be created that contains in each line the entity extracted, it's beginning offset within the file, and its end offset. Values are tab separated in each line.

Remember that only text files can be processed now. If you would like to extract entities out of PDFs and other formats, please convert them to text files first using tools like [Apache Tika](http://tika.apache.org/)

## From Source
- check the code out
- run: mvn install

# License
Apache License 2.0
