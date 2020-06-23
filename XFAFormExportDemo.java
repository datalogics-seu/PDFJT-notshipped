/*
 * ****************************************************************************
 *
 * Copyright 2009-2012 Adobe Systems Incorporated. All Rights Reserved. Portions Copyright 2012-2014 Datalogics
 * Incorporated.
 *
 * NOTICE: Datalogics and Adobe permit you to use, modify, and distribute this file in accordance with the terms of the
 * license agreement accompanying it. If you have received this file from a source other than Adobe or Datalogics, then
 * your use, modification, or distribution of it requires the prior written permission of Adobe or Datalogics.
 *
 * ***************************************************************************
 */

package com.datalogics.pdf.samples.forms;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.xfa.XFAService;
import com.adobe.pdfjt.services.xfa.XFAService.XFAElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * This sample illustrates how to export form field data from an XFA PDF.
 */
public class XFAFormExportDemo {
    // private static final String[] DEFAULT_ARGS = { "0000000202_(For Approval Only)_filled.pdf", };
    private static final String[] DEFAULT_ARGS = {
        "C:\\Datalogics\\Adobe-PDF-Java-Toolkit-release\\DL-PDF-Java-Toolkit-8.14.1-bin-photon\\samples\\src\\main\\resources\\com\\datalogics\\pdf\\samples\\forms\\0000000202_(For Approval Only)_filled.pdf", };

    public static void main(final String[] args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        PDFDocument pdfDoc = null;
        ByteReader byteReader = null;
        FileOutputStream outputDataStream = null;
        FileInputStream fis = null;
        File inputFile = null;
        boolean succeeded = false;

        try {
            if (args.length == 0) {
                inputFile = new File(DEFAULT_ARGS[0]);
            } else {
                inputFile = new File(args[0]);
            }

            final String outputFilePath = "output" + File.separator
                                          + XFAFormExportDemo.class.getSimpleName() + File.separator
                                          + inputFile.getName() + ".xml";

            // PDF documents require random access. This code
            // assumes it is OK to store the complete file in memory
            // to get maximum performance. If you already had the file
            // in memory, you would want to use a ByteBufferByteReader.
            fis = new FileInputStream(inputFile);
            byteReader = new InputStreamByteReader(fis);
            pdfDoc = PDFDocument.newInstance(byteReader,
                                             PDFOpenOptions.newInstance());

            final File outputFile = new File(outputFilePath);
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            }
            outputDataStream = new FileOutputStream(outputFile);

            if (XFAService.getDocumentType(pdfDoc).isXFA()) {
                succeeded = XFAService.exportElement(pdfDoc,
                                                     XFAElement.DATASETS, outputDataStream);

                if (succeeded) {
                    System.out.println("Successful output to " + outputFilePath);
                }
            } else {
                System.out.println("\nThe input PDF file is not an XFA file.");
            }

        } finally {
            if (fis != null) {
                fis.close();
            }
            if (byteReader != null) {
                byteReader.close();
            }
            if (pdfDoc != null) {
                pdfDoc.close();
            }
            if (outputDataStream != null) {
                outputDataStream.close();
            }
        }
    }
}
