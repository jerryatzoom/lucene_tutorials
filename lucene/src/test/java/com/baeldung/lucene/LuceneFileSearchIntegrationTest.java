package com.baeldung.lucene;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LuceneFileSearchIntegrationTest {

    @Test
    public void givenSearchQueryWhenFetchedFileNamehenCorrect() throws IOException, URISyntaxException {
        String indexPath = "index";

        Path path = Paths.get(indexPath);
        String fullpath = IntStream.range(0, path.getNameCount())
        	.boxed()
        	.map(i -> path.getName(i))
        	.collect(Collectors.toList())
        	.toString();
        
        log.info(fullpath);

        Directory directory = FSDirectory.open(path);
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        
        IntStream.range(1, 4)
        	.boxed()	
        	.forEach(i -> luceneFileSearch.addFileToIndex(i, String.format("data/file%d.txt", i)));

        List<Document> docs = luceneFileSearch.searchFiles("contents", "consectetur");

        Assert.assertEquals("file1.txt", docs.get(0).get("filename"));
    }
    
    @Test
    public void createIndex_thenDelete() throws IOException, URISyntaxException {
        String indexPath = "index";

        Path path = Paths.get(indexPath);
        String fullpath = IntStream.range(0, path.getNameCount())
        	.boxed()
        	.map(i -> path.getName(i))
        	.collect(Collectors.toList())
        	.toString();
        
        log.info(fullpath);

        Directory directory = FSDirectory.open(path);
        LuceneFileSearch luceneFileSearch = new LuceneFileSearch(directory, new StandardAnalyzer());
        
        IntStream.range(1, 4)
        	.boxed()	
        	.forEach(i -> luceneFileSearch.addFileToIndex(i, String.format("data/file%d.txt", i)));

        List<Document> docs = luceneFileSearch.searchFiles("contents", "consectetur");

        Assert.assertEquals("file1.txt", docs.get(0).get("filename"));
    }

}