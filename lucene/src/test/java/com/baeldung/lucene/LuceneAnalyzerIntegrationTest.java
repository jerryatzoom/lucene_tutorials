package com.baeldung.lucene;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.document.Field;

@Slf4j
public class LuceneAnalyzerIntegrationTest {

    private static final String SAMPLE_TEXT = "This is baeldung.com Lucene Analyzers test";
    private static final String FIELD_NAME = "sampleName";
    
    private static final String SAMPLE_TITLE = "Moby Dick";
    
    @Test
    public void simple_index() throws IOException, ParseException {
    	Directory memoryIndex = new RAMDirectory();
    	StandardAnalyzer analyzer = new StandardAnalyzer();
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
    	
    	IndexWriter writter = new IndexWriter(memoryIndex, indexWriterConfig);
    	Document document = new Document();

    	document.add(new TextField("title", SAMPLE_TITLE, Field.Store.YES));
    	document.add(new TextField("body", SAMPLE_TEXT, Field.Store.YES));

    	writter.addDocument(document);
    	writter.close();

        Query query = new QueryParser("body", analyzer)
          .parse("test");

        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 10);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        log.info("found {} documents", documents.size());
    }
    
    
    @Test
    public void givenSearchQueryWhenFetchedDocumentThenCorrect() {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("B Hello world", "Some hello world");
        inMemoryLuceneIndex.indexDocument("A Hello ", "Some hello ");
        inMemoryLuceneIndex.indexDocument("Hello world", "Some hello world");
        
        List<Document> documents 
          = inMemoryLuceneIndex.searchIndex("body", "world");
        
//        assertEquals(
//          "Hello world", 
//          documents.get(0).get("title"));
        
        documents.stream().forEach(d -> log.info(d.get("title")));
    }
    
    @Test
    public void givenTermQueryWhenFetchedDocumentThenCorrect() {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("activity", "running in track");
        inMemoryLuceneIndex.indexDocument("activity", "Cars are running on road");

        Term term = new Term("body", "running");
        Query query = new TermQuery(term);

        List<Document> documents = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(2, documents.size());
    }
    
    @Test
    public void givenPrefixQueryWhenFetchedDocumentThenCorrect() {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("article", "Lucene introduction");
        inMemoryLuceneIndex.indexDocument("article", "Theory Introduction to Lucene");

        Term term = new Term("body", "theory");
        Query query = new PrefixQuery(term);

        List<Document> documents = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(2, documents.size());
    }
    
    @Test
    public void givenSortFieldWhenSortedThenCorrect() {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("Ganges", "River in India");
        inMemoryLuceneIndex.indexDocument("Mekong", "This river flows in south Asia");
        inMemoryLuceneIndex.indexDocument("Amazon", "Rain forest river");
        inMemoryLuceneIndex.indexDocument("Rhine", "Belongs to Europe");
        inMemoryLuceneIndex.indexDocument("Nile", "Longest River");

        Term term = new Term("body", "river");
        Query query = new WildcardQuery(term);

        SortField sortField 
          = new SortField("title", SortField.Type.STRING_VAL, false);
        Sort sortByTitle = new Sort(sortField);

        List<Document> documents 
          = inMemoryLuceneIndex.searchIndex(query, sortByTitle);
        assertEquals(4, documents.size());
        assertEquals("Amazon", documents.get(0).getField("title").stringValue());
        
        documents.stream().forEach(d -> log.info(d.get("title")));
    }
    
    
    @Test
    public void given_thensearch_wildcard() throws ParseException {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("Ganges", "River in India");
        inMemoryLuceneIndex.indexDocument("Mekong", "This river flows in south Asia");
        inMemoryLuceneIndex.indexDocument("Amazon", "Rain forest river");
        inMemoryLuceneIndex.indexDocument("Rhine", "Belongs to Europe");
        inMemoryLuceneIndex.indexDocument("Nile", "Longest River");

        Query query = new QueryParser("body", new StandardAnalyzer()).parse("riv*");


        List<Document> documents 
          = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(4, documents.size());
        
        documents.stream().forEach(d -> log.info(d.get("title")));
    }
    
    @Test
    public void given_thendelete_id() throws ParseException {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("Ganges", "River in India");
        inMemoryLuceneIndex.indexDocument("Mekong", "This river flows in south Asia");
        inMemoryLuceneIndex.indexDocument("Amazon", "Rain forest river");
        inMemoryLuceneIndex.indexDocument("Rhine", "Belongs to Europe");
        inMemoryLuceneIndex.indexDocument("Nile", "Longest River");
        
        long cnt;
        cnt = inMemoryLuceneIndex.deleteIndex(1);
        assertEquals(1, cnt);

        assertEquals(4, inMemoryLuceneIndex.numDocs());
        
        Query query = new QueryParser("id", new StandardAnalyzer()).parse("1");

        List<Document> documents 
          = inMemoryLuceneIndex.searchIndex(query);

        assertEquals(0, documents.size());
        
        query = new QueryParser("title", new StandardAnalyzer()).parse("Ganges");
        documents = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(0, documents.size());

        
        query = new QueryParser("title", new StandardAnalyzer()).parse("Nile");
        documents = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(1, documents.size());

    }
    
    @Test
    public void whenDocumentDeletedThenCorrect() {
        InMemoryLuceneIndex inMemoryLuceneIndex 
          = new InMemoryLuceneIndex(new RAMDirectory(), new StandardAnalyzer());
        inMemoryLuceneIndex.indexDocument("Ganges", "River in India");
        inMemoryLuceneIndex.indexDocument("Mekong", "This river flows in south Asia");

        Term term = new Term("title", "ganges");
        inMemoryLuceneIndex.deleteDocument(term);

        Query query = new TermQuery(term);

        List<Document> documents = inMemoryLuceneIndex.searchIndex(query);
        assertEquals(0, documents.size());
    }
    
    

    @Test
    public void whenUseStandardAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new StandardAnalyzer());

        assertThat(result, contains("baeldung.com", "lucene", "analyzers", "test"));
    }

    @Test
    public void whenUseStopAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new StopAnalyzer());

        assertThat(result, contains("baeldung", "com", "lucene", "analyzers", "test"));
    }

    @Test
    public void whenUseSimpleAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new SimpleAnalyzer());

        assertThat(result, contains("this", "is", "baeldung", "com", "lucene", "analyzers", "test"));
    }

    @Test
    public void whenUseWhiteSpaceAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new WhitespaceAnalyzer());

        assertThat(result, contains("This", "is", "baeldung.com", "Lucene", "Analyzers", "test"));
    }

    @Test
    public void whenUseKeywordAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new KeywordAnalyzer());

        assertThat(result, contains("This is baeldung.com Lucene Analyzers test"));
    }

    @Test
    public void whenUseEnglishAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new EnglishAnalyzer());

        assertThat(result, contains("baeldung.com", "lucen", "analyz", "test"));
    }

    @Test
    public void whenUseCustomAnalyzerBuilder_thenAnalyzed() throws IOException {
        Analyzer analyzer = CustomAnalyzer.builder()
            .withTokenizer("standard")
            .addTokenFilter("lowercase")
            .addTokenFilter("stop")
            .addTokenFilter("porterstem")
            .addTokenFilter("capitalization")
            .build();
        List<String> result = analyze(SAMPLE_TEXT, analyzer);

        assertThat(result, contains("Baeldung.com", "Lucen", "Analyz", "Test"));
    }

    @Test
    public void whenUseCustomAnalyzer_thenAnalyzed() throws IOException {
        List<String> result = analyze(SAMPLE_TEXT, new MyCustomAnalyzer());

        assertThat(result, contains("Baeldung.com", "Lucen", "Analyz", "Test"));
    }

    // ================= usage example
    
    @Test
    public void givenTermQuery_whenUseCustomAnalyzer_thenCorrect() {
        InMemoryLuceneIndex luceneIndex = new InMemoryLuceneIndex(new RAMDirectory(), new MyCustomAnalyzer());
        luceneIndex.indexDocument("introduction", "introduction to lucene");
        luceneIndex.indexDocument("analyzers", "guide to lucene analyzers");
        Query query = new TermQuery(new Term("body", "Introduct"));

        List<Document> documents = luceneIndex.searchIndex(query);
        assertEquals(1, documents.size());
    }
    
    @Test
    public void givenTermQuery_whenUsePerFieldAnalyzerWrapper_thenCorrect() {
        Map<String,Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("title", new MyCustomAnalyzer());
        analyzerMap.put("body", new EnglishAnalyzer());

        PerFieldAnalyzerWrapper wrapper =
          new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);
        InMemoryLuceneIndex luceneIndex = new InMemoryLuceneIndex(new RAMDirectory(), wrapper);
        luceneIndex.indexDocument("introduction", "introduction to lucene");
        luceneIndex.indexDocument("analyzers", "guide to lucene analyzers");
        
        Query query = new TermQuery(new Term("body", "introduct"));
        List<Document> documents = luceneIndex.searchIndex(query);
        assertEquals(1, documents.size());
        
        query = new TermQuery(new Term("title", "Introduct"));

        documents = luceneIndex.searchIndex(query);
        assertEquals(1, documents.size());
    }

    // ===================================================================

    public List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<String>();
        TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

}
