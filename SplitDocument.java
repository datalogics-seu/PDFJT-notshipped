/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.interactive.action.PDFAction;
import com.adobe.pdfjt.pdf.interactive.action.PDFActionGoTo;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmark;
import com.adobe.pdfjt.pdf.interactive.navigation.PDFBookmarkRoot;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.manipulations.PMMOptions;
import com.adobe.pdfjt.services.manipulations.PMMService;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.net.URL;
import java.util.ArrayList;

/**
 * This sample shows how to split a PDF document by a specific page interval (e.g. every 3 pages) or by the bookmark
 * (outline) tree, if any. The output PDF will be saved with the linearization option
 */
public final class SplitDocument {

    public static final String OUTPUT_PDF_PATH = "MergedDocument-E.pdf"; // not used at the moment
    public static final String FIRST_DOCUMENT = "Merge1.pdf";
    public static final String TARGET_PDF_PATH_INTERVAL = "SplitInterval";
    public static final String TARGET_PDF_PATH_BKMK = "SplitBkMk";
    public static final int SPLIT_INTERVAL = 3;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private SplitDocument() {}

    /**
     * * Main program.
     *
     * @param args The path to the output file // not used at the moment
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL outputUrl = null;
        if (args.length > 0) {
            outputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_PDF_PATH);
        }

        System.out.println("Splitting by pages ... ");
        splitByInterval(outputUrl);
        System.out.println("\nSplitting by bookmarks ... ");
        splitByBookmarks(outputUrl);
    }

    /**
     * Split the input document, each interval of pages to a separate document file.
     *
     * @param outputUrl the path to the file to contain the output document
     * @throws Exception a general exception was thrown
     */
    public static void splitByInterval(final URL outputUrl) throws Exception {

        final URL sourceDocumentUrl = SplitDocument.class.getResource(FIRST_DOCUMENT);
        PDFDocument sourceDoc = null;
        sourceDoc = DocumentUtils.openPdfDocument(sourceDocumentUrl);
        final ArrayList<Integer> listOfPageNumsToSplit = new ArrayList<>();

        try {

            final int numPages = sourceDoc.requirePages().getNumPages();

            // PDF page numbers are 0 based (add 1 to get the user sequential page number).
            // Get the modulo (remainder). If the remainder is 0, then split on that page.
            // For example: 5 page document, split interval of 2, you want to split the
            // document at pages 0, 2, 4 (internal PDF page number) a.k.a pages 1, 3, 5.

            listOfPageNumsToSplit.add(0); // Always split on the first page (page 0)

            for (int i = 1; i < numPages; i++) {
                if (i % SPLIT_INTERVAL == 0) {
                    listOfPageNumsToSplit.add(i);
                }
            }
            splitPDF(sourceDoc, listOfPageNumsToSplit, TARGET_PDF_PATH_INTERVAL);


        } finally {

        }

    }

    /**
     * Split the input document based on the bookmark tree, if any.
     *
     * @param outputUrl the path to the file to contain the output document
     * @throws Exception a general exception was thrown
     */
    public static void splitByBookmarks(final URL outputUrl) throws Exception {

        final URL sourceDocumentUrl = SplitDocument.class.getResource(FIRST_DOCUMENT);
        PDFDocument sourceDocument = null;
        sourceDocument = DocumentUtils.openPdfDocument(sourceDocumentUrl);

        final PDFBookmarkRoot bkRoot = sourceDocument.requireCatalog().getBookmarkRoot();
        System.out.println("bookmark count: " + bkRoot.getCount());
        System.out.println("bookmark kids: " + bkRoot.getNumKids());

        final ArrayList<Integer> listOfPages = new ArrayList<>();

        final PDFBookmark bkMark = bkRoot.getFirstKid();

        enumerateBookmarks(bkMark, listOfPages);
        splitPDF(sourceDocument, listOfPages, TARGET_PDF_PATH_BKMK);

    }

    /*
     * Go through the bookmark, getting the page number from the direct Destination or the Action Destination
     */
    public static void enumerateBookmarks(final PDFBookmark bkMark, final ArrayList<Integer> listOfPageNumsToSplit)
                    throws Exception {

        int pageNum = -1;

        try {
            if (bkMark != null) {
                System.out.println("Bookmark Title: " + bkMark.getTitle());

                if (bkMark.hasDestination()) {
                    final PDFPage page = bkMark.getDestination().getPage();
                    pageNum = page.getPageNumber();
                } else if (bkMark.hasAction()) {
                    final PDFAction action = bkMark.getAction();
                    if (action.isValid() && action instanceof PDFActionGoTo) {
                        final PDFActionGoTo goToAction = (PDFActionGoTo) action;

                        if (goToAction.hasDestination()) {
                            final PDFPage page = goToAction.getDestination().getPage();
                            pageNum = page.getPageNumber();
                        }
                    }
                }

                System.out.println("bookmark pagenum: " + pageNum);

                // Multiple bookmarks can point to the same destination page, so skip repeats
                // and only add it to the list if we got a positive pageNum
                if (listOfPageNumsToSplit.contains(pageNum) == false && (pageNum >= 0)) {
                    listOfPageNumsToSplit.add(pageNum);
                }
                enumerateBookmarks(bkMark.getFirstKid(), listOfPageNumsToSplit);
                enumerateBookmarks(bkMark.getNext(), listOfPageNumsToSplit);
            }
        } finally {

        }
    }

    /**
     * Split the input document
     *
     * @param outputUrl the path to the file to contain the output document
     * @throws Exception a general exception was thrown
     */
    static void splitPDF(final PDFDocument sourceDoc, final ArrayList<Integer> listOfPageNumsToSplit,
                         final String outName)
                    throws Exception {

        final int numOfSplits = listOfPageNumsToSplit.size();
        final int sourceNumPages = sourceDoc.requirePages().getNumPages();
        int numPagesToSplit = 0;
        int startPage = 0;
        int endPage = 0;
        URL targetUrl = null;
        String targetPath = "";

        System.out.println("\nSplitting into " + numOfSplits + " files.");
        try {
            for (int j = 0; j < numOfSplits; j++) {

                if (j < numOfSplits - 1) {
                    startPage = listOfPageNumsToSplit.get(j);
                    endPage = listOfPageNumsToSplit.get(j + 1) - 1;
                    numPagesToSplit = endPage - startPage + 1;
                } else {
                    // the last split, up to the end of the document
                    startPage = listOfPageNumsToSplit.get(j);
                    endPage = sourceNumPages - 1;
                    numPagesToSplit = endPage - startPage + 1;
                }
                System.out.println("startPage=" + startPage + " endPage=" + endPage + " numPagesToSplit="
                                   + numPagesToSplit);

                // Create a new PDF document that will be used to insert pages into. The new document
                // will contain a single blank page but we'll remove this just before saving the file.
                targetPath = outName + j + ".pdf";
                targetUrl = IoUtils.createUrlFromPath(targetPath);
                final PDFDocument targetDoc = PDFDocument.newInstance(new ASRectangle(ASRectangle.US_LETTER),
                                                                      PDFOpenOptions.newInstance());
                // Set up pages to be extracted
                PDFDocument extractedPDF = null;
                final PDFPage[] extractedPages = new PDFPage[numPagesToSplit];
                for (int k = 0; k < numPagesToSplit; k++) {
                    extractedPages[k] = sourceDoc.requirePages().getPage(startPage + k);
                }

                // Use all page extraction options enabled
                final PMMOptions options = PMMOptions.newInstanceAll();
                // Extract pages in the list into a separate PDF document
                // Create PMMService for page extractions
                final PMMService extractService = new PMMService(sourceDoc);
                // Use this service to extract pages
                extractedPDF = extractService.extractPages(extractedPages, options, PDFOpenOptions.newInstance());
                // Now create service to insert pages
                final PMMService insertService = new PMMService(targetDoc);
                // Use this service to insert previously extracted pages into the target document.
                // We can use the same options since they are not specific to the PDF document.
                final PDFPage lastPage = targetDoc.requirePages()
                                                  .getPage(targetDoc.requirePages().getCount() - 1);
                insertService.insertPages(extractedPDF, lastPage, "", options);

                // Remove the first page. We don't need it anymore.
                targetDoc.requirePages().removePage(targetDoc.requirePages().getPage(0));

                DocumentHelper.saveLinearAndClose(targetDoc, targetUrl.toURI().getPath());

            }
        } finally {

        }

    }

}
