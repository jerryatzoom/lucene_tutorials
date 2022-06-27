package com.baeldung.lucene;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class LuceneFileSearch {

    private Directory indexDirectory;
    private StandardAnalyzer analyzer;

    public LuceneFileSearch(Directory indexDirectory, StandardAnalyzer analyzer) {
        super();
        this.indexDirectory = indexDirectory;
        this.analyzer = analyzer;
    }

    public void addFileToIndex(int id, String filepath) {

        try {
			Path path = Paths.get(getClass().getClassLoader().getResource(filepath).toURI());
			File file = path.toFile();
			
			IndexWriter indexWriter = getWriter();
			Document document = new Document();

			FileReader fileReader = new FileReader(file);
			String contextText = IOUtils.toString(fileReader);
			
			document.add(new TextField("id", ""+id, Field.Store.YES));
			document.add(new VecTextField("contents", contextText, Field.Store.YES));
			document.add(new StringField("path", file.getPath(), Field.Store.YES));
			document.add(new StringField("filename", file.getName(), Field.Store.YES));

			indexWriter.addDocument(document);

			indexWriter.close();
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public List<Document> searchFiles(String inField, String queryString) {
        try {
            Query query = new QueryParser(inField, analyzer).parse(queryString);

            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            TopDocs topDocs = searcher.search(query, 10);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }

            return documents;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
    
    public void deleteAll() throws IOException {
    	IndexWriter indexWriter = getWriter();
    	indexWriter.deleteAll();
    	indexWriter.close();
    }
    
    public IndexReader getReader() {
    	try {
			return DirectoryReader.open(indexDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    public IndexWriter getWriter() throws IOException {
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setCodec(new SimpleTextCodec());
		indexWriterConfig.setUseCompoundFile(false);
		
		IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
		return indexWriter;
    }
}


