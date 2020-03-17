package fr.simple.edm.crawler;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import fr.simple.edm.crawler.filesystem.FilesystemCrawler;

public class FileSystemCrawlerTest {

    @Test
    public void emptyPatternShouldNotExcludeDoc() throws Exception {
        final String exclusionRegex = "";
        final String filePath = "/data/project/.git/config";

        final boolean isExcluded = FilesystemCrawler.isExcluded(filePath, exclusionRegex);

        assertThat(isExcluded).isFalse();
    }

    @Test
    public void pathWithExcludedRegexShouldBeIgnored() throws Exception {
        final String exclusionRegex = "\\.git";
        final String filePath = "/data/project/.git/config";

        final boolean isExcluded = FilesystemCrawler.isExcluded(filePath, exclusionRegex);

        assertThat(isExcluded).isTrue();
    }

    @Test
    public void pathWithoutExcludedRegexShouldBeIgnored() throws Exception {
        final String exclusionRegex = "\\.svn";
        final String filePath = "/data/project/.git/config";

        final boolean isExcluded = FilesystemCrawler.isExcluded(filePath, exclusionRegex);

        assertThat(isExcluded).isFalse();
    }

    @Test
    public void generateTF() throws Exception {
        final String filePath = "D:/devCode/generate/template.doc";

        FilesystemCrawler.generateTF(filePath, 30);

        assertThat(filePath).isNotEmpty();
    }

}
