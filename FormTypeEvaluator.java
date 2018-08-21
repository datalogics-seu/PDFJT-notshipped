/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteWriter;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFSaveIncrementalOptions;
import com.adobe.pdfjt.services.digsig.SignatureManager;
import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;
import com.adobe.pdfjt.services.xfa.XFAService;


import java.net.URL;
import java.util.logging.Logger;

/**
 * This sample takes several different PDF files and determines which type of
 * forms they contain: AcroForm, Static XFA (shell or non-shell) or Dynamic XFA
 * (shell or non-shell).
 *
 */

public final class FormTypeEvaluator {
    private static final Logger LOGGER = Logger.getLogger(FormTypeEvaluator.class.getName());

    public static final String inputDir = "src/main/resources/com/datalogics/pdf/samples/forms/";
    public static final String[] testForm = { "testNoForm.pdf",
        "testAcroform.pdf", "testXFAStatic.pdf", "testXFADynamic.pdf" };

    /**
     * This is a utility class, and won't be instantiated.
     */
    private FormTypeEvaluator() {}

    /**
     * Main program.
     *
     * @param args command line arguments. Thre are expected in order to specify:
     *             the input file
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

        URL inputUrl = null;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                inputUrl = IoUtils.createUrlFromPath(args[i]);
                evaluatePDF(inputUrl);
            }
        } else {
            for (int i = 0; i < testForm.length; i++) {
                inputUrl = IoUtils.createUrlFromPath(inputDir + testForm[i]);
                evaluatePDF(inputUrl);
            }
        }

    }

    /**
     * evaluate the type of Form
     *
     * @param inputUrl the URL to the input file
     * @throws Exception a general exception was thrown
     */

    public static void evaluatePDF(final URL inputUrl) throws Exception {
        PDFDocument pdfDoc = null;
        try {
            // Attach font set to PDF
            final PDFFontSet fontSet = FontSetLoader.newInstance().getFontSet();
            final PDFOpenOptions openOptions = PDFOpenOptions.newInstance();
            openOptions.setFontSet(fontSet);

            // Get the PDF file.
            pdfDoc = DocumentUtils.openPdfDocumentWithOptions(inputUrl, openOptions);
            System.out.println("File " + inputUrl + ": " + XFAService.getDocumentType(pdfDoc).toString());

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
