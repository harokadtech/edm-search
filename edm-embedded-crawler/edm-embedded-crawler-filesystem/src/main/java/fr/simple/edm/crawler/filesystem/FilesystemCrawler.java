package fr.simple.edm.crawler.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import fr.simple.edm.crawler.bridge.EdmConnector;
import fr.simple.edm.domain.EdmDocumentFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilesystemCrawler {

    private static final EdmConnector edmConnector = new EdmConnector();

    /**
     * @param filePath              The path of the directory to crawl For example :
     *                              /media/raid/documents
     * @param edmServerHttpAddress  The address of the EDM webapp HTTP address For example :
     *                              127.0.0.1:8053
     * @param sourceName            A unique name for this source of documents For example :
     * @param exclusionRegex        Documents names which match with this regex will be ignored
     * @param exploreSubdirectories Means the crawler should explore directories recursively
     * @throws IOException
     */
    public static void importFilesInDir(String filePath,
                                        final String edmServerHttpAddress, final String sourceName,
                                        final String categoryName, final String exclusionRegex, final boolean exploreSubdirectories)
        throws IOException {
        // create parents
        String categoryId = edmConnector.getIdFromCategoryByCategoryName(
            edmServerHttpAddress, categoryName);
        String sourceId = sourceName;
        edmConnector.notifyStartCrawling(edmServerHttpAddress, sourceName);
        _importFilesInDir(filePath, edmServerHttpAddress, sourceId, categoryId, exclusionRegex, exploreSubdirectories, true);
        edmConnector.notifyEndOfCrawling(edmServerHttpAddress, sourceName);
    }

    public static void importFilesInDir(String filePath,
                                        final String edmServerHttpAddress, final String sourceName,
                                        final String categoryName, final String exclusionRegex)
        throws IOException {
        importFilesInDir(filePath, edmServerHttpAddress, sourceName, categoryName, exclusionRegex, true);
    }

    public static boolean isExcluded(String filePath, String exclusionPattern) {
        boolean toExclude = !exclusionPattern.isEmpty()
            && Pattern.compile(exclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath,
            exclusionPattern, toExclude);
        return toExclude;
    }

    public static boolean isIncluded(String filePath, String inclusionPattern) {
        boolean toInclude = !inclusionPattern.isEmpty()
            && Pattern.compile(inclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath,
            inclusionPattern, toInclude);
        return toInclude;
    }

    private static void _importFilesInDir(String filePath, final String edmServerHttpAddress,
                                          final String sourceId, final String categoryId, final String exclusionRegex,
                                          final boolean exploreSubdirectories, final boolean isRoot) {

        log.info("Embedded crawler looks for : " + filePath);

        // exclusion pattern
        if (isExcluded(filePath, exclusionRegex)) {
            log.info("File excluded because it matches with exclusion regex");
            return;
        }

        File file = new File(filePath);

        // recursive crawling
        if (file != null && file.isDirectory()) {
            log.debug("... is a directory !");
            if (isRoot || exploreSubdirectories) {
                for (File subFile : file.listFiles()) {
                    _importFilesInDir(filePath + "/" + subFile.getName(), edmServerHttpAddress, sourceId, categoryId, exclusionRegex, exploreSubdirectories, false);
                }
            } else {
                log.debug("I won't explore this directory");
            }

            // release memory
            file = null;
        }

        // add files
        if (file != null && file.isFile()) {
            log.debug("... is a file !");
            String fName = FilenameUtils.removeExtension(file.getName());
            String sourceName = sourceId;
            String[] split = fName.split("_");
            if(split.length==3){
                sourceName = split[2];
                log.debug("The sourceName  {}", sourceName );
            }
            String mySourceId = edmConnector.getIdFromSourceBySourceName(
                edmServerHttpAddress, sourceName, categoryId);
            // index
            log.debug("The source ID is {}", mySourceId);
            double bytes = file.length();
            double kilobytes = bytes / 1024;
            double megabytes = kilobytes / 1024;

            if (megabytes > 100) {
                log.warn("Skipping too big file ({})", filePath);
            } else {
                // construct DTO
                EdmDocumentFile document = new EdmDocumentFile();
                document.setFileDate(new Date(file.lastModified()));
                if(!sourceId.equals(mySourceId)){
                    int year = Integer.parseInt(sourceName);
                    Date fileDate = new Date(year, 1, 1);
                    document.setFileDate(fileDate);
                }
                String nodePath = filePath.replaceAll("\\\\", "/");
                nodePath = nodePath.replaceAll("/var/nc_data/", ""); 
                document.setNodePath(nodePath);
                document.setSourceId(mySourceId);
                document.setCategoryId(categoryId);
                document.setName(fName);
                document.setFileExtension(FilenameUtils.getExtension(filePath).toLowerCase());

                // save DTO
                try {
                    document.setFileContentType(Files.probeContentType(file.toPath()));
                    edmConnector.saveEdmDocument(edmServerHttpAddress, document, file);
                } catch (IOException e) {
                    log.error("failed to save edm document '{}'", filePath, e);
                }
            }

            // release memory
            file = null;
        }

        // other type
        if (file != null) {
            log.debug("... is nothing !");
        }
    }
}
