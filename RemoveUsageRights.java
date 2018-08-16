/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.signature;

import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.digsig.*;
import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;
import org.w3c.dom.NodeList;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import java.net.URL;
import java.util.logging.Logger;
import com.adobe.pdfjt.pdf.document.PDFSaveIncrementalOptions;

/**
 * This is a sample that demonstrates how to remove usage rights (reader extensions) from a PDF
 * 
 * <p>
 */

public final class RemoveUsageRights {
    private static final Logger LOGGER = Logger.getLogger(RemoveUsageRights.class.getName());

    public static final String INPUT_ENABLED_PDF_PATH = "ReaderEnabled.pdf";
    public static final String OUTPUT_UR_REMOVED_PDF_PATH = "ReaderEnabled-removed.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private RemoveUsageRights() {}

    /**
     * Main program.
     *
     * @param args command line arguments. Thre are expected in order to specify:
     *             the input file with usage righs
     *             the output file with usage rights removed
     *             If no arguments are given, the sample will output
     *             to the root of the samples directory by default.
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        URL inputUrl, outputUrl, outputRightsAddedUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            inputUrl = IoUtils.createUrlFromPath(INPUT_ENABLED_PDF_PATH);
        }
        if (args.length > 1) {
            outputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_UR_REMOVED_PDF_PATH);
        }

        // Remove all Usage Rights aka Reader Extensions
        removeUsageRights(inputUrl, outputUrl);
    }

    /**
     * Remove usage rights in the example document.
     *
     * @param inputUrl the URL to the input file
     * @param outputUrl the path to the file with usage rights removed
     * @throws Exception a general exception was thrown
     */

    public static void removeUsageRights(final URL inputUrl, final URL outputUrl) throws Exception {
        PDFDocument pdfDoc = null;
        try {
            // Attach font set to PDF
            final PDFFontSet fontSet = FontSetLoader.newInstance().getFontSet();
            final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
            openOptions.setFontSet(fontSet);

            // Get the PDF file.
            pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);
            System.out.println("Opened file " + inputUrl);

            ByteWriter byteWriter = null;

            // Set up a signature service
            final SignatureManager sigService = SignatureManager.newInstance(pdfDoc);
            if (sigService.hasUsageRights()) {
                System.out.println("Document has usage rights -- removing");
                sigService.removeUsageRights();
            }
            byteWriter = IoUtils.newByteWriter(outputUrl);
            // Save the modified PDF to a new file.
            pdfDoc.save(byteWriter, PDFSaveIncrementalOptions.newInstance());
            System.out.println("Saving output file to " + outputUrl);

            byteWriter = null;

        } finally {
            try {
                if (pdfDoc != null) {
                    pdfDoc.close();
                }
            } catch (final PDFException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

}
