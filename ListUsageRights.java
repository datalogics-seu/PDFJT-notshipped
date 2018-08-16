/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.signature;

import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.cos.CosObject;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.digsig.PDFURAnnots;
import com.adobe.pdfjt.pdf.document.PDFCosDictionary;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveIncrementalOptions;
import com.adobe.pdfjt.services.digsig.SignatureManager;
import com.adobe.pdfjt.pdf.digsig.PDFTransformParametersUR; //seu
import com.adobe.pdfjt.pdf.digsig.PDFURAnnots;
import com.adobe.pdfjt.pdf.digsig.PDFURDocument;
import com.adobe.pdfjt.pdf.digsig.PDFUREmbeddedFiles;
import com.adobe.pdfjt.pdf.digsig.PDFURForm;
import com.adobe.pdfjt.pdf.digsig.PDFURSignature; // seu
import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.net.URL;
import java.util.logging.Logger;
import java.util.ArrayList;  //seu

/**
 * This is a sample that demonstrates how to remove usage rights (reader extensions) from a PDF
 * 
 * <p>
 */

public final class ListUsageRights {
    private static final Logger LOGGER = Logger.getLogger(ListUsageRights.class.getName());

    public static final String INPUT_ENABLED_PDF_PATH = "ReaderEnabled.pdf";
    public static final String OUTPUT_UR_REMOVED_PDF_PATH = "ReaderEnabled-removed.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private ListUsageRights() {}

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

        // List all Usage Rights aka Reader Extensions
        listUsageRights(inputUrl);
    }

    /**
     * List usage rights in the example document.
     *
     * @param inputUrl the URL to the input file
     * @throws Exception a general exception was thrown
     */

    public static void listUsageRights(final URL inputUrl) throws Exception {
        PDFDocument pdfDoc = null;
        try {
            // Attach font set to PDF
            final PDFFontSet fontSet = FontSetLoader.newInstance().getFontSet();
            final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
            openOptions.setFontSet(fontSet);

            // Get the PDF file.
            pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);
            System.out.println("Opened file " + inputUrl);

            //ByteWriter byteWriter = null;

            // Set up a signature service
            final SignatureManager sigService = SignatureManager.newInstance(pdfDoc);
            if (sigService.hasUsageRights()) {
                //sigService.removeUsageRights();

                // transformMethod should be UB1, UR or UR3
                // UB1 is first version of Reader Extensions. See PDFSignatureUtils doc explanation
                PDFTransformParametersUR transformParams = sigService.getDocumentUsageRights("UR3");;

                /*
                ArrayList<PDFURAnnots> annotRights = new ArrayList<PDFURAnnots>();
                ArrayList<PDFURDocument> docRights = new ArrayList<PDFURDocument>();
                ArrayList<PDFURForm> formRights = new ArrayList<PDFURForm>();
                ArrayList<PDFURSignature> sigRights = new ArrayList<PDFURSignature>();
                ArrayList<PDFUREmbeddedFiles> efRights = new ArrayList<PDFUREmbeddedFiles>();
                */

                PDFURAnnots[] annotRights = transformParams.getAnnotationUsageRights();
                PDFURDocument[] docRights = transformParams.getDocumentUsageRights();
                PDFURForm[] formRights = transformParams.getFormUsageRights();
                PDFURSignature[] sigRights = transformParams.getSignatureUsageRights();
                PDFUREmbeddedFiles[] efRights = transformParams.getEFUsageRights();

                if ( annotRights != null )
                    for (PDFURAnnots aR : annotRights) {
                        System.out.println("annot Right: " + aR.name() ); // aR.toString() and aR.name and aR.getValue are all the same
                    }
                else
                    System.out.println("annot Right: null ");

                if ( docRights != null )
                    for (PDFURDocument dR : docRights) {
                        System.out.println("document Right: " + dR.name());
                    }
                else
                    System.out.println("document Right: null ");

                if ( formRights != null )
                    for (PDFURForm fR : formRights) {
                    System.out.println("form Right: " + fR.name() );
                }
                else
                    System.out.println("form Right: null ");

                if ( sigRights != null )
                    for (PDFURSignature sR : sigRights) {
                    System.out.println("signature Right: " + sR.name() );
                }
                else
                    System.out.println("signature Right: null ");

                 if ( efRights != null )
                    for (PDFUREmbeddedFiles eR : efRights) {
                    System.out.println("embedded file Right: " + eR.name() );
                }
                else
                    System.out.println("embedded file Right: null ");

            }


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
