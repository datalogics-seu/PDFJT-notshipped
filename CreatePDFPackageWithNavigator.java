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
import com.adobe.pdfjt.pdf.document.PDFSaveFullOptions;
import com.adobe.pdfjt.pdf.document.PDFStream;
import com.adobe.pdfjt.pdf.document.PDFVersion;
import com.adobe.pdfjt.pdf.filters.PDFFilter;
import com.adobe.pdfjt.pdf.filters.PDFFilterFlate;
import com.adobe.pdfjt.pdf.filters.PDFFilterList;
import com.adobe.pdfjt.pdf.graphics.PDFRectangle;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionColors;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionField;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionFieldType;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionFolder;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionItem;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionItemData;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionNavigator;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionResourcesTree;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionSchema;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionSort;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFCollectionView;
import com.adobe.pdfjt.pdf.interactive.navigation.collection.PDFPortableCollection;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageTree;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.File;
import java.net.URL;
import java.sql.Date;

/**
 * This sample creates a PDF package that utilizes a custom Navigator / swf. Resources (swf files, icons) are expected
 * to be in the samples\portfolioResources folder.
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
public final class CreatePDFPackageWithNavigator {

    public static final String OUTPUT_PDF_PATH = "CollectionOut.pdf";

    private static final ASName key = ASName.create("Sub");
    private static final ASName key2 = ASName.create("Auth");
    private static final ASName keyTags = ASName.create("adobe:Tags");
    private static final String pdfFile1 = "Embed1.pdf";
    private static final String pdfFile2 = "Embed2.pdf";
    private static final String pdfFile3 = "Embed3.pdf";
    private static final String imageFile1 = "ducky_1.png";
    private static final String imageFile2 = "ducky_2.png";
    private static final String imageFile3 = "ducky_3.png";
    private static final String imageFile4 = "ducky_4.png";
    private static final String imageFile5 = "ducky_5.png";
    // private static final String pdfFile1 = "c:\\test\\input\\embed1.pdf";


    /**
     * This is a utility class, and won't be instantiated.
     */
    private CreatePDFPackageWithNavigator() {}

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
            setupFile(collectionDoc, pdfFile1, "Person1", "Subject1", "", "");
            setupFile(collectionDoc, pdfFile2, "Person2", "Subject2", "", "");
            setupFile(collectionDoc, pdfFile3, "Person3", "Subject3", "", "");
            setupFile(collectionDoc, imageFile1, "Ducky1", "ImageSubject1", "Hello Ducky Description", "Ducky Tags");
            setupFile(collectionDoc, imageFile2, "Ducky2", "ImageSubject2", "", "");
            setupFile(collectionDoc, imageFile3, "Ducky3", "ImageSubject3", "", "");
            setupFile(collectionDoc, imageFile4, "Ducky4", "ImageSubject4", "", "");
            setupFile(collectionDoc, imageFile5, "Ducky5", "ImageSubject5", "", "");

            // ---------- Set up the Collection dictionary (which controls the Properties window in Adobe Acrobat)
            // -----------
            final PDFPortableCollection collectionDict = PDFPortableCollection.newInstance(collectionDoc);
            // collectionDict.setInitialDocumentName(new ASString(new File(pdfFile1).getAbsolutePath())); // not used
            collectionDict.setColors(setupColors(collectionDoc)); // added
            collectionDict.setRootFolder(""); // added

            collectionDoc.requireCatalog().setCollection(collectionDict);
            collectionDict.setSchema(setupSchema(collectionDoc));
            collectionDict.setSort(setupSort(collectionDoc));
            collectionDict.setView(setupView(collectionDoc)); // added
            collectionDict.setNavigator(setupNavigator(collectionDoc)); // added

            final PDFSaveFullOptions saveOptionsFull = PDFSaveFullOptions.newInstance();
            saveOptionsFull.setVersion(PDFVersion.v1_7_e3);

            DocumentHelper.saveAndClose(collectionDoc, outputUrl.toURI().getPath(), saveOptionsFull);

        } finally {
            //
        }

    }

    // added
    /**
     * Set up the view type of the collection. Can be .details, .hidden, .tiles or .custom
     */
    private static PDFCollectionView setupView(final PDFDocument collection) throws Exception {
        // this is an enum
        return PDFCollectionView.custom;
    }

    /**
     * Set up the Navigator of the collection.
     */
    private static PDFCollectionNavigator setupNavigator(final PDFDocument collection) throws Exception {
        //
        final PDFCollectionNavigator nav = PDFCollectionNavigator.newInstance(collection);
        nav.setAPIVersion("9.5");
        nav.setCategory("Acrobat Layouts");
        nav.setDesc("Arrange your content in an orderly grid that can scale to accomodate large numbers of files.");
        nav.setID("http://ns.adobe.com/pdf/navigator/navigators/AdobeBasicGrid");
        nav.setIcon("AX_BasicGrid_Md_N.png");

        // setup the InitalFields dictionary
        final PDFCollectionSchema schema = PDFCollectionSchema.newInstance(collection);
        final PDFCollectionField fldFileName = PDFCollectionField.newInstance(collection,
                                                                              PDFCollectionFieldType.Desc,
                                                                              "FileName");
        fldFileName.setAllowEdit(true);
        fldFileName.setOrder(0);
        fldFileName.setName("Name");
        fldFileName.setDictionaryNameValue(ASName.k_Subtype, ASName.k_F);

        schema.set(ASName.create("FileName"), fldFileName);

        final PDFCollectionField fldDisplayName = PDFCollectionField.newInstance(collection,
                                                                                 PDFCollectionFieldType.Desc,
                                                                                 "adobe:DisplayName");
        fldDisplayName.setAllowEdit(true);
        fldDisplayName.setOrder(1);
        fldDisplayName.setName("Display Name");
        fldDisplayName.setDictionaryNameValue(ASName.k_Subtype, ASName.k_S);

        schema.set(ASName.create("adobe:DisplayName"), fldDisplayName);
        final PDFCollectionField fldAdobeTags = PDFCollectionField.newInstance(collection,
                                                                               PDFCollectionFieldType.Desc,
                                                                               "adobe:Tags");
        fldAdobeTags.setAllowEdit(true);
        fldAdobeTags.setOrder(2); // should be 6
        fldAdobeTags.setName("Tags");
        fldAdobeTags.setVisibility(false);
        fldAdobeTags.setDictionaryNameValue(ASName.k_Subtype, ASName.k_S);

        schema.set(ASName.create("adobe:Tags"), fldAdobeTags);

        nav.setInitialFields(schema);

        final ASName loc = ASName.create("Locale");
        nav.setDictionaryStringValue(loc, "en_US"); // or could use .setLocale()
        nav.setName("Grid");

        // setup Resources by importing swf and icons ToDo: could add Flate support, but not required
        final PDFCollectionResourcesTree collectRes = PDFCollectionResourcesTree.newInstance(collection);

        importStreamToResourcesTree(collection, collectRes, "portfolioResources/AX_BasicGrid_Md_N.png",
                                    "AX_BasicGrid_Md_N.png");
        importStreamToResourcesTree(collection, collectRes, "portfolioResources/BasicGrid.swf", "BasicGrid.swf");
        importStreamToResourcesTree(collection, collectRes, "portfolioResources/navigator-GreyRedSkins.swf",
                                    "navigator/GreyRedSkins.swf");
        importStreamToResourcesTree(collection, collectRes, "portfolioResources/navigator-greyRed_background.jpg",
                                    "navigator/greyRed_background.jpg");
        importStreamToResourcesTree(collection, collectRes, "portfolioResources/navigator-properties.xml",
                                    "navigator/properties.xml");
        nav.setResources(collectRes);

        nav.setSWF("BasicGrid.swf");

        // ToDo: setup Strings dictionary. Not required.
        nav.setVersion("10.1"); //
        return nav;
    }

    /**
     * Read the external file to add to the collection Resources Tree
     */
    private static void importStreamToResourcesTree(final PDFDocument collection,
                                               final PDFCollectionResourcesTree collectRes, final String fileName,
                                               final String fileDesc)
                    throws Exception {

        File inputFile = null;
        inputFile = new File(fileName);

        final LazyRandomAccessFileByteReader br = new LazyRandomAccessFileByteReader(inputFile);
        // System.out.println("stream length: " + br.length());

        final PDFStream pdfStm = PDFStream.newInstance(collection);
        pdfStm.setStreamData(br);

        // Setup a filter list containing FlateDecode only. // don't need this
        final PDFFilterList filters = PDFFilterList.newInstance(collection);
        final PDFFilter filter = PDFFilter.newInstance(collection, "FlateDecode", null);
        filters.add(filter);
        // pdfStm.setOutputFilters(filters);

        // We could retrieve a name from the file itself or pick it up from the string passed to the function
        // final ASString fileDescStr = new ASString(inputFile.getName());
        final ASString fileDescStr = new ASString(fileDesc);

        // Now add the entry to the collection Resource tree
        collectRes.addEntry(fileDescStr, pdfStm);

        // return nothing;
    }

    /**
     * Set up the collection colors.
     */
    private static PDFCollectionColors setupColors(final PDFDocument collection) throws Exception {

        final PDFCollectionColors colors = PDFCollectionColors.newInstance(collection);
        final double background[] = { 0.250977, 0.262741, 0.282349 };
        colors.setBackground(background);
        final double cardBackground[] = { 0.799988, 0.199997, 0.000000 };
        colors.setCardBackground(cardBackground);
        final double cardBorder[] = { 0.839203, 0.968613, 0.956848 };
        colors.setCardBorder(cardBorder);
        final double secText[] = { 0.799988, 0.799988, 0.799988 };
        colors.setSecondaryText(secText);

        return colors;
    }


    /**
     * Set up the collection Folders dictionary. Not currently implemented
     */
    private static PDFCollectionFolder setupFolder(final PDFDocument collection) throws Exception {
        final long id = 0;

        final PDFCollectionFolder folder = PDFCollectionFolder.newInstance(null, "Folder");
        return folder;

    }

    /**
     * Set up what columns should be used to sort the rows in the portable collection interface, shown in the Properties
     * window in Adobe Acrobat.
     */
    private static PDFCollectionSort setupSort(final PDFDocument collection) throws Exception {

        // final PDFCollectionSort sort = PDFCollectionSort.newInstance(collection, new ASName[] { key, key2 });
        // first sort the rows by key (Subject) then by key2 (Author)
        final ASName keyFileName = ASName.create("FileName");
        final PDFCollectionSort sort = PDFCollectionSort.newInstance(collection, new ASName[] { keyFileName });

        return sort;
    }

    /**
     * The schema specifies the columns in the portable collection interface, shown in the Properties window in Adobe
     * Acrobat. Set it up...
     */
    private static PDFCollectionSchema setupSchema(final PDFDocument collection) throws Exception {

        final PDFCollectionSchema schema = PDFCollectionSchema.newInstance(collection);

        // Schema field 0
        PDFCollectionField field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "FileName");
        field.setOrder(0);
        field.setAllowEdit(true);
        field.setName("Name");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_F);
        schema.set(ASName.create("FileName"), field);
        // schema.set(key, field);

        // Schema field 1
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "adobe:DisplayName");
        field.setOrder(1);
        field.setAllowEdit(true);
        field.setName("Display Name");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_S);
        schema.set(ASName.create("adobe:DisplayName"), field);

        // Schema field 2
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "Description");
        field.setOrder(2);
        field.setAllowEdit(true);
        field.setName("Description");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_Desc);
        schema.set(ASName.create("Description"), field);

        // Schema field 3
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "ModDate");
        field.setOrder(3);
        // field.setAllowEdit(true);
        field.setName("Modified");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_ModDate);
        schema.set(ASName.create("ModDate"), field);

        // Schema field 4
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "Size");
        field.setOrder(4);
        // field.setAllowEdit(true);
        field.setName("Size");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_Size);
        schema.set(ASName.create("Size"), field);

        // Schema field 5
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "CreationDate");
        field.setOrder(5);
        field.setAllowEdit(false);
        field.setName("Created");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_CreationDate);
        schema.set(ASName.create("CreationDate"), field);

        // Schema field 6
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "adobe:Tags");
        field.setOrder(6);
        field.setAllowEdit(true);
        field.setName("Tags");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_S);
        schema.set(ASName.create("adobe:Tags"), field);

        // Schema field 7
        field = PDFCollectionField.newInstance(collection, PDFCollectionFieldType.text, "adobe:Summary");
        field.setOrder(7);
        field.setAllowEdit(true);
        field.setName("Summary");
        field.setDictionaryNameValue(ASName.k_Subtype, ASName.k_S);
        schema.set(ASName.create("adobe:Summary"), field);

        // Schema field 8 -- adobe:FreeForm:x
        // Schema field 9 -- adobe:FreeForm:y
        // Schema field 10 -- adobe:FreeForm:depth
        // Schema field 11 -- adobe:FreeForm:angle

        return schema;
    }


    private static void setupFile(final PDFDocument collection, final String path, final String author,
                                  final String subject, final String descr, final String tags)
                    throws Exception {
        final File attFile = new File(path);
        final ByteReader attByteReader = new LazyRandomAccessFileByteReader(attFile);
        PDFFileSpecification fileSpec;
        PDFNamedEmbeddedFiles namedFiles;

        try {

            // First, follow the standard set up for embedded files.
            final ASDate modDate = new ASDate(new Date(attFile.lastModified()));
            final PDFEmbeddedFile embeddedFile = PDFEmbeddedFile.newInstance(collection,
                                                                             PDFEmbeddedFileInfo.newInstance(collection,
                                                                                                             (int) attByteReader.length(),
                                                                                                             modDate,
                                                                                                             modDate),
                                                                             attByteReader);
            embeddedFile.setFilter(PDFFilterFlate.newInstance(collection, null));
            fileSpec = PDFFileSpecification.newInstance(collection, attFile.getAbsolutePath().getBytes(),
                                                        embeddedFile);

            // Now, Set up the data that will be presented in the portable collection user interface about this file,
            // the Properties window in Acrobat.
            final PDFCollectionItem colItem = PDFCollectionItem.newInstance(collection);
            PDFCollectionItemData data = PDFCollectionItemData.newInstance(collection, subject, key);
            colItem.setData(key, data);
            data = PDFCollectionItemData.newInstance(collection, author, key2);
            colItem.setData(key2, data);

            if (tags != "") {
                data = PDFCollectionItemData.newInstance(collection, tags, keyTags);
                colItem.setData(keyTags, data);
            }
            // Description field has to be handled as a string in the dictionary rather than an entry in the CI dict
            if (descr != "") {
                fileSpec.setDescription(descr);
            }
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
