/******************************************************************************
 *  Compilation:  javac PageCounter.java
 *  Execution:   java PageCounter
 *  Dependencies:  none
 *  Prompted user input:  file path to  denote the target file/directory
 *  Function:   For a file/directory that contains Doc/Docx/Pdf files, this program counts the total number of pages.
 *
 *
 *  Remarks - external libraries
 *  -------
 *  Open source java library Apache PDFBox 2.0.22 is used to handle PDF files.
 * https://pdfbox.apache.org/
 *  Open source java library Apache  POI 5.0.0  is used to handle Windows documents, in this case, word files.
 * https://poi.apache.org/download.html#POI-5.0.0
 ******************************************************************************/

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class PageCounter {

    private File root;
    private int docFiles;
    private int pdfFiles;
    private int docPages;
    private int pdfPages;
    private Queue<File> fileList;

    // constructor
    private PageCounter(String path) {
        root = new File(path);
        docFiles = 0;
        pdfFiles = 0;
        docPages = 0;
        pdfPages = 0;
        fileList = new LinkedList<File>();
        fileList.add(root);
    }

    private void countPages() {
        // Breadth first search.
        // when the queue fileList is not empty, pop out the first item to process.
        // If the first item is a file, count pages.
        // If it is a directory, add its children items to the queue.
        while (fileList.peek() != null) {
            File f = fileList.remove();
            String filePath = f.getPath();
            if (f.isFile()) {
                if (f.isHidden()) {
                    System.out.println("File : " + f.getName() + " is hidden, page counted.");
                }

                String fileName = f.getName();
                if (fileName.endsWith(".pdf")) {
                    try {
                        PDDocument pdf = PDDocument.load(f);
                        int numberOfPages = pdf.getNumberOfPages();
                        System.out.println("File " + fileName + " has " + numberOfPages + " pages.");
                        pdfFiles = pdfFiles + 1;
                        pdfPages = pdfPages + numberOfPages;
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Could not load file : " + f.getName() + " , number and pages not counted.");
                    }

                } else if (fileName.endsWith(".docx")) {
                    // doc, docx,
                    try {
                        XWPFDocument docx = new XWPFDocument(POIXMLDocument.openPackage(filePath));
                        int numberOfPages = docx.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
                        System.out.println("File " + fileName + " has " + numberOfPages + " pages.");
                        docFiles = docFiles + 1;
                        docPages = docPages + numberOfPages;
                    } catch (IOException e) {
                        System.out.println("Failed to read file : " + f.getName());
                    }

                } else if (fileName.endsWith(".doc")) {
                    try {
                        HWPFDocument wordDoc = new HWPFDocument(new FileInputStream(filePath));
                        int numberOfPages = wordDoc.getSummaryInformation().getPageCount();
                        System.out.println("File " + fileName + " has " + numberOfPages + " pages.");
                        docFiles = docFiles + 1;
                        docPages = docPages + numberOfPages;
                    } catch (IOException e) {
                        System.out.println("Failed to read file: " + f.getName());
                    }
                }
                //} else if (fileName.endsWith(".odt")) {
                // OdfDocument odt = (OdfDocument) OdfDocument.loadDocument("filePath");
                // docFiles = docFiles + 1;
                // docPages = docPages + odt.getOfficeMetadata()
                //}
            }
            if (f.isDirectory()) {
                // For test
                System.out.println(f.getName() + " is a directory. ");

                File[] children = f.listFiles(); // if the directory is empty, the return array will be empty array.
                if (children != null) {
                    for (File child : children) {
                        //String childName = child.getName();
                        //if (childName.endsWith(".pdf") || childName.endsWith(".doc") || childName.endsWith(".docm") || childName.endsWith(".docx") || childName.endsWith(".dot") || childName.endsWith(".dotx")) {
                        fileList.add(child);
                        System.out.println("File " + child.getName() + " has been added to queue.");
                        //}
                    }
                }
            }

        }
    }


    public static void main(String[] args) {
        // To fix the two log4j warnings thrown at run time
        // log4j:WARN No appenders could be found for logger (org.apache.pdfbox.cos.COSDocument).
        // log4j:WARN Please initialize the log4j system properly.
        // BasicConfigurator.configure();

        // create a new scanner to read in user input
        Scanner scanner = new Scanner(System.in);
        String pathToFile;
        File file = null;
        // prompt for user input

        // test user input until the input path leads to a valid file
        do {
            // get user input as string
            System.out.println("Please specify a valid path to count pages:");
            pathToFile = scanner.next();
            System.out.println(pathToFile);

            try {
                file = new File(pathToFile);
            } catch (NullPointerException e) {
                // if the input pathToFile is null, prompt for anther input
                System.out.println("Path can not be null. Please specify a VALID file to count pages:");
            } catch (IllegalArgumentException e1) {
                System.out.println("Please input a VALID file path to count pages: ");
            }
        } while (!file.exists());

        // the path has been tested, is not null and file is normal
        PageCounter counter = new PageCounter(pathToFile);
        counter.countPages();
        System.out.println(counter.docFiles + " doc files -> " + counter.docPages + " pages");
        System.out.println(counter.pdfFiles + " pdf files -> " + counter.pdfPages + "pages");
        System.out.println("Totally " + (counter.docPages + counter.pdfPages) + " pages for you to read.");
    }
}

