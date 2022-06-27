package com.baeldung.lucene;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LuceneFileSearchIntegrationTest {
	
	@BeforeEach
	public void resetIndex() throws IOException {
        String indexPath = "index";

        Path path = Paths.get(indexPath);

        Directory directory = FSDirectory.open(path);
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
		luceneFileSearch.deleteAll();
	}

    @Test
    public void givenSearchQueryWhenFetchedFileNamehenCorrect() throws IOException, URISyntaxException {
        String indexPath = "index";

        Path path = Paths.get(indexPath);

        Directory directory = FSDirectory.open(path);
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        writeFiles(luceneFileSearch);

        List<Document> docs = luceneFileSearch.searchFiles("contents", "consectetur");

        Assert.assertEquals("file1.txt", docs.get(0).get("filename"));
    }
    
    @Test
    public void createIndex_thenDelete() throws IOException, URISyntaxException {
        String indexPath = "index";

        Path path = Paths.get(indexPath);

        Directory directory = FSDirectory.open(path);
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        writeFiles(luceneFileSearch);
        
        List<Document> docs = luceneFileSearch.searchFiles("contents", "consectetur");

        Assert.assertEquals("file1.txt", docs.get(0).get("filename"));
    }
    
    
    @Test 
    public void leafreader_test() throws IOException {
        String indexPath = "index";

        Directory directory = FSDirectory.open(Paths.get(indexPath));
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        writeFiles(luceneFileSearch);
       
        IndexReader indexReader = luceneFileSearch.getReader();
        
        List<LeafReaderContext> leaves = indexReader.leaves();
        for (LeafReaderContext ctx: leaves) {
        	log.info("docId={} topLevel={} id={} context={}", ctx.docBase, ctx.isTopLevel, ctx.id(), ctx);
        	
        	LeafReader leafReader = ctx.reader();
        	log.info("docCount={} reader={}", leafReader.getDocCount(indexPath), leafReader);
        
        	Terms terms = leafReader.terms("contents");
        	log.info("terms for 'contents' = {}\n\n", terms);
        }
    }
    
    @Test
    public void get_postings_test() throws IOException {
        String indexPath = "index";

        Directory directory = FSDirectory.open(Paths.get(indexPath));
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        writeFiles(luceneFileSearch);
        
        IndexReader indexReader = luceneFileSearch.getReader();
        
        // get a docId
        int docId = indexReader.leaves().get(1).docBase;
        
        Terms termVector = indexReader.getTermVector(docId, "contents");
        
        TermsEnum termIter = termVector.iterator();
        while (termIter.next() != null) {
            PostingsEnum postingsEnum = termIter.postings(null, PostingsEnum.ALL);
            while (postingsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                int freq = postingsEnum.freq();
                log.info("term: {}, freq: {},", termIter.term().utf8ToString(), freq);
                while (freq > 0) {
                	log.info(" position: {},", postingsEnum.nextPosition());
                	log.info(" startOffset: {}, endOffset: {}",
                            postingsEnum.startOffset(), postingsEnum.endOffset());
                    freq--;
                }
                System.out.println();
            }
        }
    }
    
    private void writeFiles(LuceneFileSearch luceneFileSearch) {
        
        IntStream.range(1, 4)
        	.boxed()	
        	.forEach(i -> luceneFileSearch.addFileToIndex(i, String.format("data/file%d.txt", i)));

    }

}