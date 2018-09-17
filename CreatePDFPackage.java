/*
 * Copyright 2018 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.LazyRandomAccessFileByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASDate;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.core.types.ASString;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFEmbeddedFile;
import com.adobe.pdfjt.pdf.document.PDFEmbeddedFileInfo;
import com.adobe.pdfjt.pdf.document.PDFFileSpecification;
import com.adobe.pdfjt.pdf.document.PDFNameDictionary;
import com.adobe.pdfjt.pdf.document.PDFNamedEmbeddedFiles;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.filters.PDFFilterFlate;
import com.adobe.pdfjt.pdf.graphics.PDFRectangle;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionField;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionFieldType;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionItem;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionItemData;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionSchema;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionSort;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFPortableCollection;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * This sample creates a PDF package that contains three files, and each file has two attributes, Author and Subject,
 * shown in the Properties window in Adobe Acrobat. The files are sorted first by Subject and then by Author.
 *
 * Note that the PDF package can also be referred to as the portable collection, the PDF collection, or simply, the
 * collection. But note that these terms are obsolete.
 *
 * The PDF Package or PDF Collection is now referred to as the PDF Portfolio. In the years since Adobe Systems first
 * introduced the PDF Collection feature, upgrades to Adobe Acrobat have added a new user interface and other features
 * to PDF Collection.
 *
 * With these later versions of the product Adobe Systems has referred to a PDF document with a collection dictionary as
 * a PDF Package or a PDF Portfolio.
 *
 */
public final class CreatePDFPackage {

    public static final String OUTPUT_PDF_PATH = "CollectionOut.pdf";

    private static final ASName key = ASName.create("Sub");
    private static final ASName key2 = ASName.create("Auth");

    private static final String pdfFile1 = "Embed1.pdf";
    private static final String pdfFile2 = "Embed2.pdf";
    private static final String pdfFile3 = "Embed3.pdf";
    // private static final String pdfFile1 = "c:\\test\\input\\embed1.pdf";


    /**
     * This is a utility class, and won't be instantiated.
     */
    private CreatePDFPackage() {}

    /**
     * Main program.
     *
     * @param args The path to the merged output file
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

        embedFiles(outputUrl);
    }

    /**
     * setup the collection and embed the files, with the output written to the given path.
     *
     * @param outputUrl the path to the file to contain the output document
     * @throws Exception a general exception was thrown
     */
    public static void embedFiles(final URL outputUrl) throws Exception {

        PDFDocument collectionDoc = PDFDocument.newInstance(PDFOpenOptions.newInstance());

        try {
            collectionDoc = PDFDocument.newInstance(PDFOpenOptions.newInstance());
            final PDFRectangle mediaBox = PDFRectangle.newInstance(collectionDoc, 0., 0., 612., 792.);
            final PDFPage emptyPage = PDFPage.newInstance(collectionDoc, mediaBox);
            PDFPageTree.newInstance(collectionDoc, emptyPage);

            // -------------- Set up the embedded files ---------------
            setupFile(collectionDoc, pdfFile1, "Person1", "Subject1");
            setupFile(collectionDoc, pdfFile2, "Person2", "Subject2");
            setupFile(collectionDoc, pdfFile3, "Person3", "Subject3");

            // ---------- Set up the Collection dictionary (which controls the Properties window in Adobe Acrobat)
            // -----------
            final PDFPortableCollection collectionDict = PDFPortableCollection.newInstance(collectionDoc);
            collectionDict.setInitialDocumentName(new ASString(new File(pdfFile1).getAbsolutePath()));
            collectionDoc.requireCatalog().setCollection(collectionDict);
            collectionDict.setSchema(setupSchema(collectionDoc));
            collectionDict.setSort(setupSort(collectionDoc));

            DocumentHelper.saveFullAndClose(collectionDoc, outputUrl.toURI().getPath());


        } finally {
            //
        }

    }



    /**
     * Set up what columns should be used to sort the rows in the portable collection interface, shown in the Properties
     * window in Adobe Acrobat.
     */
    private static PDFCollectionSort setupSort(final PDFDocument collection) throws Exception {

        final PDFCollectionSort sort = PDFCollectionSort.newInstance(collection, new ASName[] { key, key2 });
        // first sort the rows by key (Subject) then by key2 (Author)
        return sort;
    }


    /**
     * The schema specifies the columns in the portable collection interface, shown in the Properties window in Adobe
     * Acrobat. Set it up...
     */
    private static PDFCollectionSchema setupSchema(final PDFDocument collection) throws Exception {

        PDFCollectionField field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text
                                                                  , "Subject");
        field.setOrder(0);
        final PDFCollectionSchema schema = PDFCollectionSchema.newInstance(collection);
        schema.set(key, field);

        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "Author");
        field.setOrder(1);
        schema.set(key2, field);

        return schema;
    }
    private static void setupFile(final PDFDocument collection, final String path, final String author,
                                  final String subject) throws Exception
    {
        final File attFile = new File(path);
        final ByteReader attByteReader = new LazyRandomAccessFileByteReader(attFile);
        PDFFileSpecification fileSpec;
        PDFNamedEmbeddedFiles namedFiles;

        try {

            // First, follow the standard set up for embedded files.
            final ASDate modDate = new ASDate(new Date(attFile.lastModified()));
            final PDFEmbeddedFile embeddedFile =
                PDFEmbeddedFile.newInstance(collection,
                                            PDFEmbeddedFileInfo.newInstance(collection,
                                                                            (int) attByteReader.length(),
                                                                            modDate,
                                                                            modDate),
                                            attByteReader);
            embeddedFile.setFilter(PDFFilterFlate.newInstance(collection, null));
            fileSpec = PDFFileSpecification.newInstance(collection, attFile.getAbsolutePath().getBytes(), embeddedFile);

            // Now, Set up the data that will be presented in the portable collection user interface about this file,
            // the Properties window in Acrobat.
            final PDFCollectionItem colItem = PDFCollectionItem.newInstance(collection);
            PDFCollectionItemData data = PDFCollectionItemData.newInstance(collection, subject, key);
            colItem.setData(key, data);
            data = PDFCollectionItemData.newInstance(collection, author, key2);
            colItem.setData(key2, data);
            fileSpec.setCollectionItem(colItem);

            // Finally, the file is set up. Add it to the collection.
            final PDFNameDictionary nameDictionary = collection.requireCatalog().procureNameDictionary();
            namedFiles = nameDictionary.procureNamedEmbeddedFiles();
            namedFiles.addEntry(new ASString(attFile.getAbsolutePath()), fileSpec);
        } catch (final Exception e) {
            attByteReader.close();
            throw e;
        }
    }


}
