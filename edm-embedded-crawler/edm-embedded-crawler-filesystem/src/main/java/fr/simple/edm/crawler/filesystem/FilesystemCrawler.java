package fr.simple.edm.crawler.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import fr.simple.edm.crawler.bridge.EdmConnector;
import fr.simple.edm.domain.EdmDocumentFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FilesystemCrawler {

    private static final EdmConnector edmConnector = new EdmConnector();

    /**
     * @param filePath              The path of the directory to crawl For example :
     *                              /media/raid/documents
     * @param edmServerHttpAddress  The address of the EDM webapp HTTP address For
     *                              example : 127.0.0.1:8053
     * @param sourceName            A unique name for this source of documents For
     *                              example :
     * @param exclusionRegex        Documents names which match with this regex will
     *                              be ignored
     * @param exploreSubdirectories Means the crawler should explore directories
     *                              recursively
     * @throws IOException
     */
    public static void importFilesInDir(String filePath, final String edmServerHttpAddress, final String sourceName,
            final String categoryName, final String exclusionRegex, final boolean exploreSubdirectories)
            throws IOException {
        // create parents
        String categoryId = edmConnector.getIdFromCategoryByCategoryName(edmServerHttpAddress, categoryName);
        String sourceId = edmConnector.getIdFromSourceBySourceName(edmServerHttpAddress, sourceName, categoryId);
        edmConnector.notifyStartCrawling(edmServerHttpAddress, sourceName);
        _importFilesInDir(filePath, edmServerHttpAddress, sourceId, categoryId, exclusionRegex, exploreSubdirectories,
                true);
        edmConnector.notifyEndOfCrawling(edmServerHttpAddress, sourceName);
    }

    public static void importFilesInDir(String filePath, final String edmServerHttpAddress, final String sourceName,
            final String categoryName, final String exclusionRegex) throws IOException {
        importFilesInDir(filePath, edmServerHttpAddress, sourceName, categoryName, exclusionRegex, true);
    }

    public static boolean isExcluded(String filePath, String exclusionPattern) {
        boolean toExclude = !exclusionPattern.isEmpty() && Pattern.compile(exclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath, exclusionPattern, toExclude);
        return toExclude;
    }

    /**
     * Generates a pseudo-random integer in the range [min, max]
     * 
     * @param min : the starting value of the range (inclusive)
     * @param max : the ending value of the range (inclusive)
     */
    public static int rand(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Invalid range");
        }

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean isIncluded(String filePath, String inclusionPattern) {
        boolean toInclude = !inclusionPattern.isEmpty() && Pattern.compile(inclusionPattern).matcher(filePath).find();
        log.debug("Check if '{}' match with '{}' : {}", filePath, inclusionPattern, toInclude);
        return toInclude;
    }

    private static String[] noms = "HAMA|AMADOU|SOULEYMANE|SAMAILA|SAIDOU|MOURTALA|MAHAMADOU|AMADOU|IBRAHIM|BOUBACAR|IBRAHIM|TAHIROU|ISSAKA|HAMANI|MAHAMANE|MOUNKAILA|ABDOU|OUSEINI|SOULEYMANE|AMADOU|SALEY|ABOUBACAR|SEYDOU|HASSANE|OUMAROU|MOUSSA|ADAMOU|ALI|ISSOUFOU|SOUMANA"
            .split("\\|");

    private static String[] quartiers = "Koira Kano|Yantala|Maourey|Plateau|Bani Fandou|Gaweye".split("\\|");
    private static String[] prenoms = "Seydou|Aichatou|Abass|Kadri|KarimOU|Hadiza|Abdoulaye|Aboubacar|Aboubacar|Adamou|Alfari|Ali|Ali|Amadou|Balkissa|Fatouma|Djibo|Bachir|Kiari|Boubacar|Boureima|Chefou|Daouda|Djibo|Djibrilla|Ramatou|Garba|Hamadou|Hamani|Hamidou|Bachir"
            .split("\\|");
    private static String quartier;
    private static String tfnum;
    private static String fullname;
    private static String strDate;
    private static String strDateV;
    private static String ilot;
    private static String prcl;

    public static void generateTF(String filePath, int count) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        // add files
        if (file.isFile()) {
            String templateName = "YYYY_Bordereaux Analytiques";
            String ext = FilenameUtils.getExtension(filePath).toLowerCase();
            // index
            try {
                // String cont = FileUtils.readFileToString(file);
                // log.debug("... is a file !"+cont);
                File tfFile;
                for (int kk = 0; kk < count; kk++) {
                    int year = rand(1990, 2019);
                    quartier = quartiers[rand(0, quartiers.length - 1)];
                    String nom = noms[rand(0, noms.length - 1)];
                    String prenom = prenoms[rand(0, prenoms.length - 1)];
                    fullname = prenom + " " + nom;
                    tfnum = "" + rand(16200, 98000);

                    Calendar cal2 = new GregorianCalendar(year, Calendar.DECEMBER, 15);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    strDateV = formatter.format(cal2.getTime());
                    int days = rand(0, 30);
                    cal2.add(Calendar.DAY_OF_MONTH, days);
                    Date fileDate = cal2.getTime();
                    strDate = formatter.format(fileDate);
                    ilot = "" + rand(1, 9);
                    prcl = "" + (char) rand(65, 75);
                    FileTime fileTime = FileTime.fromMillis(fileDate.getTime());
                    String tfName = "TF_" + tfnum + "_" + fullname + "_" + ilot + "_"
                            + templateName.replace("YYYY", "" + year);

                    log.debug(quartier + " " + strDate + " " + tfnum + " " + fullname + " " + ilot + " " + prcl + " ");
                    // #TF_QUARTIER#., îlot #ILOT# parcelle #PRCL#. .
                    File dossier = new File(parent, quartier);
                    if (!dossier.exists()) {
                        dossier.mkdirs();
                    }
                    tfFile = new File(dossier, tfName + "." + ext);
                    HWPFDocument doc = new HWPFDocument(new POIFSFileSystem(file));
                    if (doc != null) {
                        replaceText(doc);
                        FileOutputStream out = new FileOutputStream(tfFile) ;
                        doc.write(out);
                    }
                    // FileUtils.writeStringToFile(tfFile, content);
                    Path path = Paths.get(tfFile.getPath());
                    Files.setLastModifiedTime(path, fileTime);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            // release memory
            file = null;
        }
    }

    public static String stringReplaceFirst(String text) {
        text = text.replace("#TF_NUM#", tfnum);
        text = text.replace("#REQUERANT#", fullname);
        text = text.replace("#TF_YEAR_V#", strDateV);
        text = text.replace("#TF_YEAR#", strDate);
        text = text.replace("#ILOT#", ilot);
        text = text.replace("#PRCL#", prcl);
        text = text.replace("#TF_QUARTIER#", quartier);
        return text;
    }

    private static HWPFDocument replaceText(HWPFDocument doc) {
        Range r = doc.getRange();
        for (int i = 0; i < r.numSections(); ++i) {
            Section s = r.getSection(i);
            for (int j = 0; j < s.numParagraphs(); j++) {
                Paragraph p = s.getParagraph(j);
                String text = p.text();
                if (text.contains("#")) {
                    p.replaceText("#TF_NUM#", tfnum);
                    p.replaceText("#REQUERANT#", fullname);
                    p.replaceText("#TF_YEAR_V#", strDateV);
                    p.replaceText("#TF_YEAR#", strDate);
                    p.replaceText("#ILOT#", ilot);
                    p.replaceText("#PRCL#", prcl);
                    p.replaceText("#TF_QUARTIER#", quartier);
                }
            }
        }
        return doc;
    }


    private static void _importFilesInDir(String filePath, final String edmServerHttpAddress, final String sourceId,
            final String categoryId, final String exclusionRegex, final boolean exploreSubdirectories,
            final boolean isRoot) {

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
                    _importFilesInDir(filePath + "/" + subFile.getName(), edmServerHttpAddress, sourceId, categoryId,
                            exclusionRegex, exploreSubdirectories, false);
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
 
            String mySourceId = edmConnector.getIdFromSourceBySourceName(edmServerHttpAddress, sourceName, categoryId);
            // index
            log.debug("The source ID is {}", mySourceId);
            double bytes = file.length();
            double kilobytes = bytes / 1024;
            double megabytes = kilobytes / 1024;

            if (megabytes > 100) {
                log.warn("Skipping too big file ({})", filePath);
            } else {
                Date fileDate = new Date(file.lastModified()); 
                int idx = fName.indexOf("_Bordereaux Analytiques.pdf");
                if(idx > 0){
                    String year = fName.substring(idx-4, idx);
                    Calendar cal2 = new GregorianCalendar(Integer.parseInt(year) , Calendar.JULY, 15);
                    log.warn("Got year" + year + " from file{}", fName);
                    int days = rand(0, 30);
                    cal2.add(Calendar.DAY_OF_MONTH, days);
                    fileDate = cal2.getTime();
                }
                // construct DTO
                EdmDocumentFile document = new EdmDocumentFile();
                document.setFileDate(fileDate);
                String nodePath = filePath.replaceAll("\\\\", "/");
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
