/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.rasterizer.PageRasterizer;
import com.adobe.pdfjt.services.rasterizer.RasterizationOptions;

import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * This sample shows how to rasterize a PDF page and save it as a PNG image.
 */
public class RasterizePages {

    private static final Logger LOGGER = Logger.getLogger(RasterizePages.class.getName());
    public static final String DEFAULT_INPUT = "UnicodeText.pdf";
    public static final String OUTPUT_IMAGE_PATH = "UnicodeText.png";


    private static PageRasterizer pageRasterizer;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private RasterizePages() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where
        // PDFJT can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        URL inputUrl = null;
        URL outputUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            inputUrl = RasterizePages.class.getResource(DEFAULT_INPUT);
            outputUrl = RasterizePages.class.getResource(OUTPUT_IMAGE_PATH);

        }

        ConvertToPng(inputUrl, outputUrl);
    }

    /**
     * Rasterize the specified PDF.
     *
     * @param inputUrl path to the PDF
     * @throws Exception a general exception was thrown
     */
    public static void ConvertToPng(final URL inputUrl, final URL outputURL) throws Exception {
        // Only log info messages and above
        LOGGER.setLevel(Level.INFO);

        try {
            // Read the PDF input file and detect the page size of the first page. This sample assumes all pages in
            // the document are the same size.
            final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputUrl);
            final PDFPage pdfPage = pdfDocument.requirePages().getPage(0);
            final int pdfPageWidth = (int) pdfPage.getMediaBox().width();
            final int pdfPageHeight = (int) pdfPage.getMediaBox().height();


            final int resolution = 300;
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Resolution: " + resolution + " DPI");
            }

            // Create a default FontSetLoader. This will include the Base 14 fonts, plus all fonts in the standard
            // system locations.
            final FontSetLoader fontSetLoader = FontSetLoader.newInstance();

            // Create a set of options that will be used to rasterize the pages. We use the page width, height,
            // to tell the Java Toolkit what dimensions the bitmap should be.
            final RasterizationOptions rasterizationOptions = new RasterizationOptions();
            rasterizationOptions.setFontSet(fontSetLoader.getFontSet());
            rasterizationOptions.setWidth(pdfPageWidth / 72 * resolution);
            rasterizationOptions.setHeight(pdfPageHeight / 72 * resolution);
            int pageNumber = 0;

            // Use a PageRasterizer to create a bitmap for each page.
            pageRasterizer = new PageRasterizer(pdfDocument.requirePages(), rasterizationOptions);

            while (pageRasterizer.hasNext()) {
                pageNumber++;
                final BufferedImage image = pageRasterizer.next();
                final File outputFile = new File( //OUTPUT_DIRECTORY +
                        DEFAULT_INPUT.replace(".pdf", "_" + pageNumber + ".png"));
                //Saving raster image
                ImageIO.write(image, "png", outputFile);
            }

        } catch (final IOException exp) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(exp.getMessage());
            }
        }
    }

}
