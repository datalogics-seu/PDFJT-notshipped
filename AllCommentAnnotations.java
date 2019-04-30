/*
 * Copyright Datalogics, Inc. 2015
 */

package com.datalogics.pdf.samples.manipulation;
// package pdfjt.cookbook.annotations; //seu

import com.adobe.fontengine.font.Font;
import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.internal.io.LazyRandomAccessFileByteReader;
import com.adobe.pdfjt.core.fontset.PDFFontSet;
import com.adobe.pdfjt.core.types.ASDate;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.core.types.ASString;
import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFEmbeddedFile;
import com.adobe.pdfjt.pdf.document.PDFEmbeddedFileInfo;
import com.adobe.pdfjt.pdf.document.PDFFileSpecification;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.filters.PDFFilterFlate;
import com.adobe.pdfjt.pdf.graphics.PDFRectangle;
import com.adobe.pdfjt.pdf.graphics.font.PDFFont;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectForm;
import com.adobe.pdfjt.pdf.interactive.PDFViewerPreferences;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationCaret;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationCircle;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationEnum;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationFileAttachment;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationFreeText;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationHighlight;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationInk;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationLine;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationPolyline;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationSquare;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationStamp;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationStrikeOut;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationText;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationUnderline;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAppearance;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFBorder;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFBorderStyle;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFBorderStyle.Style;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFInkList;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.pdf.page.PDFPageLayout;
import com.adobe.pdfjt.services.ap.AppearanceService;
import com.adobe.pdfjt.services.ap.TextFormatterImpl;
import com.adobe.pdfjt.services.ap.spi.APContext;
import com.adobe.pdfjt.services.ap.spi.APResources;
import com.adobe.pdfjt.services.fontresources.PDFFontSetUtil;
import com.adobe.pdfjt.services.manipulations.PMMTemplates;
import com.adobe.pdfjt.services.readingorder.ReadingOrderTextExtractor;
import com.adobe.pdfjt.services.textextraction.TextExtractionOptions;
import com.adobe.pdfjt.services.textextraction.Word;
import com.adobe.pdfjt.services.textextraction.WordsIterator;
import com.adobe.pdfjt.services.xobjhandler.PageContentXObject;
import com.adobe.pdfjt.services.xobjhandler.XObjectContentType;
import com.adobe.pdfjt.services.xobjhandler.XObjectUseOptions;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.document.FontSetLoader;

// import pdfjt.util.SampleFontLoaderUtil; //seu

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * This sample draws two stars on a PDF Page using two different techniques.
 */
public class AllCommentAnnotations {

    private static final String inputPDFURL = "http://dev.datalogics.com/cookbook/annotations/AllAnnotations.pdf";
    // private static final String outputDir = "cookbook/Annotations/output/";
    private static final String outputDir = "";
    private static final double INCH = 72;

    static public void main(final String[] args) throws Exception {

        // First read in the PDF file
        URLConnection connection = new URL(inputPDFURL).openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        connection.connect();
        InputStream fis = connection.getInputStream();
        ByteReader byteReader = new InputStreamByteReader(fis);
        final PDFDocument pdfDocument = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
        /*
         * Set the default view to single page and fit in window. This isn't
         * absolutely necessary but will allow you to see all of the annotations
         * without needing to scroll.
         */
        final PDFCatalog pdfCatalog = pdfDocument.requireCatalog();
        pdfCatalog.setPageLayout(PDFPageLayout.SinglePage);
        final PDFViewerPreferences pdfViewerPreferences = PDFViewerPreferences.newInstance(pdfDocument);
        pdfViewerPreferences.setFitWindow(true);
        pdfCatalog.setViewerPreferences(pdfViewerPreferences);
        /*
         * Then get the first (and only) page in the file. We'll need this
         * object in order to add annotations to it.
         */
        final PDFPage pdfPageOne = pdfDocument.requirePages().getPage(0);

        /*
         * We'll need the resources and context to create the annotation
         * appearances so let's create those here.
         */
        final APResources apResources = new APResources(pdfDocument.getCosDocument().getOptions().getFontSet(),
                pdfDocument.getCosDocument().getOptions().getDocLocale(), new HashMap<Font, PDFFont>());
        final APContext apContext = new APContext(apResources, true, null);
        /*
         * First we'll add a few note annotations. because the viewer should
         * supply the appearance based on the icon name, we don't need to
         * generate appearances. We can also use a single point for the
         * rectangle since different viewers may have different sized icons for
         * these annotation types..
         */

        // Text Annotations: Sticky note in Acrobat / Note Icon
        final PDFRectangle annotLocation_Note = PDFRectangle.newInstance(pdfDocument, .5 * INCH, 10.5 * INCH, .5 * INCH, 10.5 * INCH);
        final String popupContent_Note = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
        final PDFAnnotationText pdfAnnotationText_Note = PDFAnnotationText.newInstance(pdfDocument, annotLocation_Note, popupContent_Note);
        pdfAnnotationText_Note.setIconName("Note");
        pdfAnnotationText_Note.setColor(new double[] { 1, 1, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationText_Note);

        // Text Annotations: Sticky note in Acrobat / Help Icon
        final PDFRectangle annotLocation_Help = PDFRectangle.newInstance(pdfDocument, 1 * INCH, 10.5 * INCH, 1 * INCH, 10.5 * INCH);
        final String popupContent_Help = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
        final PDFAnnotationText pdfAnnotationText_Help = PDFAnnotationText.newInstance(pdfDocument, annotLocation_Help, popupContent_Help);
        pdfAnnotationText_Help.setIconName("Help");
        pdfAnnotationText_Help.setColor(new double[] { 1, 1, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationText_Help);

        // Text Annotations: Sticky note in Acrobat / Key Icon
        final PDFRectangle annotLocation_Key = PDFRectangle.newInstance(pdfDocument, 1.5 * INCH, 10.5 * INCH, 1.5 * INCH, 10.5 * INCH);
        final String popupContent_Key = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.";
        final PDFAnnotationText pdfAnnotationText_Key = PDFAnnotationText.newInstance(pdfDocument, annotLocation_Key, popupContent_Key);
        pdfAnnotationText_Key.setIconName("Key");
        pdfAnnotationText_Key.setColor(new double[] { 0, 0, 1 });
        pdfPageOne.addAnnotation(pdfAnnotationText_Key);

        /*
         * Now we're going to add some of the text markup annotations so we'll
         * need to get some words from the document so we can use their quads to
         * derive the annotation quads. However, the quad order for an
         * annotation is different from the quad order for a word so I've the
         * method wordsQuadsToAnnotQuads() is used to create the correct order
         * based on the first and last word that we want to add our comment to.
         *
         * We add the words to an array so we can easily access just the ones we
         * need.
         */

        // final PDFFontSet sysFontSet = SampleFontLoaderUtil.loadSampleFontSet(); //seu
        final PDFFontSet sysFontSet = FontSetLoader.newInstance().getFontSet();
        final PDFFontSet fontset = PDFFontSetUtil.buildWorkingFontSet(pdfDocument, sysFontSet, pdfDocument.getDocumentLocale(), null);
        final TextExtractionOptions textExtractionOptions = TextExtractionOptions.newInstance();
        textExtractionOptions.setUseStructure(true);
        textExtractionOptions.setIgnoreArtifacts(true);
        final ReadingOrderTextExtractor readingOrderTextExtractor = ReadingOrderTextExtractor.newInstance(pdfDocument, fontset,
                textExtractionOptions);
        final WordsIterator wordsIterator = readingOrderTextExtractor.getWordsIterator();
        final ArrayList<Word> wordsArrayList = new ArrayList<>();
        while (wordsIterator.hasNext()) {
            final Word word = wordsIterator.next();
            if (word.getBoundingQuads() != null) {
                wordsArrayList.add(word);
            }
        }
        // Create a Highlight annotation
        final PDFAnnotationHighlight pdfAnnotationHighlight = PDFAnnotationHighlight.newInstance(pdfDocument);
        // Set the properties
        double[] quadPoints = wordsQuadsToAnnotQuads(wordsArrayList.get(2), wordsArrayList.get(7));
        pdfAnnotationHighlight.setQuadPoints(quadPoints);
        pdfAnnotationHighlight.setColor(new double[] { 1, 0.8, 0 });
        // Add it to the page
        pdfPageOne.addAnnotation(pdfAnnotationHighlight);
        // Generate it's appearance
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Highlight));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        /*
         * The steps for adding the text markup annotations are all the same
         */

        // Create an Underline annotation
        final PDFAnnotationUnderline pdfAnnotationUnderline = PDFAnnotationUnderline.newInstance(pdfDocument);
        quadPoints = wordsQuadsToAnnotQuads(wordsArrayList.get(9), wordsArrayList.get(13));
        pdfAnnotationUnderline.setQuadPoints(quadPoints);
        pdfAnnotationUnderline.setColor(new double[] { 0.40, 0.85, 0.15 }); // Matches
                                                                            // the
                                                                            // Acrobat
                                                                            // default
        pdfPageOne.addAnnotation(pdfAnnotationUnderline);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Underline));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        // Create a Strikeout annotation
        final PDFAnnotationStrikeOut pdfAnnotationStrikeOut = PDFAnnotationStrikeOut.newInstance(pdfDocument);
        quadPoints = wordsQuadsToAnnotQuads(wordsArrayList.get(14), wordsArrayList.get(18));
        pdfAnnotationStrikeOut.setQuadPoints(quadPoints);
        pdfAnnotationStrikeOut.setColor(new double[] { 1, 0, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationStrikeOut);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.StrikeOut));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        /*
         * Add Caret - Insert Text at Cursor in Acrobat (approximates Acrobat
         * positioning) The caret annotation is positioned using a rectangle
         * that with the top left roughly 1/2 the font height up from the base
         * line and 1/2 a character width to the left of the end of the word.
         */
        final PDFAnnotationCaret pdfAnnotationCaret = PDFAnnotationCaret.newInstance(pdfDocument);
        final Word word = wordsArrayList.get(20);
        final double wordHeight = word.topLeft().y() - word.bottomRight().y();
        final double llx = word.bottomRight().x() - (word.charWidth() / 2);
        final double lly = word.bottomRight().y() - (wordHeight / 4);
        final double urx = word.bottomRight().x() + (word.charWidth() / 2);// (wordHeight/4);
        final double ury = lly + (wordHeight / 2);
        pdfAnnotationCaret.setRect(PDFRectangle.newInstance(pdfDocument, llx, lly, urx, ury));
        pdfAnnotationCaret.setColor(new double[] { 0, 0, 1 });
        pdfPageOne.addAnnotation(pdfAnnotationCaret);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Caret));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        /*
         * In Acrobat, there are three types of "Free Text" annotation tools.
         * One without a border, sometimes referred to as the typewriter tool,
         * one with a border called the text box tool, and one with an arrow
         * attached to it referred to as a Callout. These are all the same
         * annotation type in JT.
         *
         * When creating these types of annotations, you start with the basic
         * PDFAnnotationFreeText and then set the appropriate properties that
         * mimic the appropriate tool in Acrobat.
         *
         * Both the default Style and default Appearance (different from the
         * annotation AP dictionary) must be set in order for a proper
         * annotation appearance (the AP dictionary) to be generated.
         */

        // Add Free Text / Typewriter Tool in Acrobat
        final PDFAnnotationFreeText pdfAnnotationFreeText_Comment = PDFAnnotationFreeText.newInstance(pdfDocument);
        pdfAnnotationFreeText_Comment.setRect(PDFRectangle.newInstance(pdfDocument, 2 * INCH, 6 * INCH, 4 * INCH, 6.5 * INCH));
        pdfAnnotationFreeText_Comment.setContents("Et harum quidem rerum facilis est et expedita distinctio.");
        pdfAnnotationFreeText_Comment
                .setDefaultStyle("font: Helvetica,sans-serif 10.0pt;font-stretch:Normal; text-align:left; color:#000000");
        pdfAnnotationFreeText_Comment.setDefaultAppearance("13.75 TL /Helvetica 10 Tf"); // Helvetica
                                                                                         // 10/13.75
        pdfAnnotationFreeText_Comment.setBorderStyle(PDFBorderStyle.newInstance(pdfDocument, 0, null, null));
        pdfAnnotationFreeText_Comment.setIntent("FreeTextTypeWriter");
        pdfPageOne.addAnnotation(pdfAnnotationFreeText_Comment);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.FreeText));
        AppearanceService.generateAppearances(pdfDocument, apContext, new TextFormatterImpl(pdfDocument));

        // Add Free Text / Text Box tool in Acrobat
        final PDFAnnotationFreeText pdfAnnotationFreeText_TextBox = PDFAnnotationFreeText.newInstance(pdfDocument);
        pdfAnnotationFreeText_TextBox.setRect(PDFRectangle.newInstance(pdfDocument, 2 * INCH, 5 * INCH, 4 * INCH, 5.5 * INCH));
        pdfAnnotationFreeText_TextBox.setContents("Et harum quidem rerum facilis est et expedita distinctio.");
        pdfAnnotationFreeText_TextBox.setDefaultStyle("font: Helvetica,sans-serif 10.0pt; text-align:left; color:#FF0000 ");
        pdfAnnotationFreeText_TextBox.setDefaultAppearance("1 0 0 rg 13.75 TL /Helvetica 10 Tf");
        final PDFBorderStyle pdfBorderStyle = PDFBorderStyle.newInstance(pdfDocument, 1, Style.Solid, null);
        pdfAnnotationFreeText_TextBox.setBorderStyle(pdfBorderStyle);
        pdfAnnotationFreeText_TextBox.setIntent("FreeText");
        pdfPageOne.addAnnotation(pdfAnnotationFreeText_TextBox);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.FreeText));
        AppearanceService.generateAppearances(pdfDocument, apContext, new TextFormatterImpl(pdfDocument));

        // Add Text Callout / Arrow with Knee
        final PDFAnnotationFreeText pdfAnnotationFreeText_Callout1 = PDFAnnotationFreeText.newInstance(pdfDocument);
        pdfAnnotationFreeText_Callout1.setRect(PDFRectangle.newInstance(pdfDocument, 2 * INCH, 4 * INCH, 4 * INCH, 4.5 * INCH));
        pdfAnnotationFreeText_Callout1.setContents("Et harum quidem rerum facilis est et expedita distinctio.");
        pdfAnnotationFreeText_Callout1.setDefaultStyle("font: Helvetica,sans-serif 10.0pt; text-align:left; color:#00FF00 ");
        pdfAnnotationFreeText_Callout1.setDefaultAppearance("0 1 0 rg 13.75 TL /Helvetica 10 Tf");
        pdfAnnotationFreeText_Callout1.setLineEnds(new PDFAnnotationLine.LineEnding[] { PDFAnnotationLine.LineEnding.OpenArrow });
        final double arrowHead_x = 1 * INCH;
        final double arrowHead_y = 5 * INCH;
        final double knee_x = 1.5 * INCH;
        final double knee_y = 4.25 * INCH;
        final double attachPoint_x = 2 * INCH;
        final double attachpoint_y = 4.25 * INCH;
        pdfAnnotationFreeText_Callout1.setCalloutLine(arrowHead_x, arrowHead_y, knee_x, knee_y, attachPoint_x, attachpoint_y);
        pdfAnnotationFreeText_Callout1.setIntent("FreeTextCallout");
        pdfAnnotationFreeText_Callout1.setBorderStyle(pdfBorderStyle);
        pdfPageOne.addAnnotation(pdfAnnotationFreeText_Callout1);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.FreeText));
        AppearanceService.generateAppearances(pdfDocument, apContext, new TextFormatterImpl(pdfDocument));

        /*
         * Ink annotations, polyline annotations, and polygon annotations all
         * use an array of vertices to describe the path that they form. An ink
         * annotation will join the points using curves whereas polyline
         * annotations are joined by straight lines.
         *
         * Polygon annotations are described in this Gist
         * https://gist.github.com/JoelGeraci-Datalogics/
         * fc4170b11492b7de4e972e7623a3db43
         */

        // Add Ink annotation / Draw Free Form in Acrobat
        final PDFAnnotationInk pdfAnnotationInk = PDFAnnotationInk.newInstance(pdfDocument);
        final List<Number> inkList = Arrays.asList(new Number[] { 410, 365, 515, 370, 513, 450, 450, 450, 450, 400, 490, 400 });
        final PDFInkList pdfInkList = PDFInkList.newInstance(pdfDocument);
        pdfInkList.addPath(inkList);
        pdfAnnotationInk.setInkList(pdfInkList);
        pdfAnnotationInk.setColor(new double[] { 1, 0, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationInk);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Ink));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        final PDFAnnotationPolyline pdfAnnotationPolyline = PDFAnnotationPolyline.newInstance(pdfDocument);
        final double[] vertices_Polyline = new double[] { 359.9, 661.3 - (6.5 * INCH), 341.9, 630 - (6.5 * INCH), 359.9, 598.7 - (6.5 * INCH),
                396.1, 598.7 - (6.5 * INCH), 414.1, 630 - (6.5 * INCH) };
        pdfAnnotationPolyline.setDictionaryArrayValue(ASName.k_Vertices, vertices_Polyline);
        pdfAnnotationPolyline.setColor(new double[] { 1, 0, 0 });
        final PDFBorder pdfBorderPolyLine = PDFBorder.newInstance(pdfDocument);
        pdfBorderPolyLine.setWidth(.5);
        pdfAnnotationPolyline.setBorder(pdfBorderPolyLine);
        pdfPageOne.addAnnotation(pdfAnnotationPolyline);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.PolyLine));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        /*
         * To add a stamp to a PDF file, you must first open the stamp file,
         * find the page that represents the stamp you want, wrap it in an
         * XObject and then use that xObject as the appearance of the Stamp
         * annotation.
         */

        // Add Stamp / Static
        String standardStampsPDF = "http://dev.datalogics.com/cookbook/stamps/StandardBusiness.pdf";
        connection = new URL(standardStampsPDF).openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        connection.connect();
        fis = connection.getInputStream();
        byteReader = new InputStreamByteReader(fis);
        final PDFDocument stampsPDF = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
        /*
         * Get the page with the template name that matches the "Confidential"
         * stamp in the Acrobat UI.
         */
        final PMMTemplates pmmTemplates = new PMMTemplates(stampsPDF);
        final PDFPage confidentialStampPage = pmmTemplates.templateExistsForName(stampsPDF, "SBConfidential=Confidential");

        /*
         * We are going to use pages from the "StandardBusiness.pdf" that ships
         * with Adobe Acrobat as the Stamp Appearance. We can pull content from
         * other PDF files by encapsulating the page content stream (with the
         * associated Resources dictionary) in an XObject. For Stamps we don't
         * need any special kind of XObject type so we just use "General".
         */
        final XObjectUseOptions useOptions = new XObjectUseOptions();
        final XObjectContentType xObjectContentType = XObjectContentType.General;
        useOptions.setContentType(xObjectContentType);

        // Create the Stamp Annotation Object.
        final PDFAnnotationStamp pdfAnnotationStamp = PDFAnnotationStamp.newInstance(pdfDocument);
        final PDFAppearance pdfAppearance = PDFAppearance.newInstance(pdfDocument);

        /*
         * Create an XObject from the page that has the stamp that we want, and
         * then set it as the normal appearance for the stamp annotation.
         */
        final PDFXObjectForm approvedStampXObjectForm = PageContentXObject.generateContentXObject(pdfDocument, confidentialStampPage, useOptions);
        pdfAppearance.setNormalAppearance(approvedStampXObjectForm);
        pdfAnnotationStamp.setAppearance(pdfAppearance);

        /*
         * Add a Title and Content for the Stamp Pop-Up. These strings will
         * appear in the Acrobat Comments panel and when you roll over the
         * Stamp.
         */
        pdfAnnotationStamp.setTitle("Datalogics");
        pdfAnnotationStamp.setContents("My Confidential Stamp");

        final double stampWidth = approvedStampXObjectForm.getBBox().width();
        final double stampHeight = approvedStampXObjectForm.getBBox().height();
        final double stamp_llx = 4.5 * INCH;
        final double stamp_lly = 4.5 * INCH - stampHeight;
        final double stamp_urx = llx + stampWidth;
        final double stamp_ury = 4.5 * INCH;
        pdfAnnotationStamp.setRect(stamp_llx, stamp_lly, stamp_urx, stamp_ury);

        // Add the Stamp Annotation to the page
        pdfPageOne.addAnnotation(pdfAnnotationStamp);

        /*
         * Attachment annotations require that you embed the file you want to
         * use as the attachment and then use that object when instantiating the
         * new PDFAnnotationFileAttachment object.
         */
        // Attachment Annotation
        standardStampsPDF = "http://dev.datalogics.com/cookbook/stamps/StandardBusiness.pdf";
        connection = new URL(standardStampsPDF).openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        connection.connect();
        fis = connection.getInputStream();

        final File attachmentFile = new File("StandardBusiness.pdf");
        final OutputStream outputStream = new FileOutputStream(attachmentFile);
        IOUtils.copy(fis, outputStream);
        outputStream.close();
        final LazyRandomAccessFileByteReader attachment = new LazyRandomAccessFileByteReader(attachmentFile);

        final Calendar modCalendar = Calendar.getInstance();
        modCalendar.setTimeInMillis(attachmentFile.lastModified());
        final ASDate modTime = new ASDate(modCalendar.getTime());
        final PDFEmbeddedFileInfo info = PDFEmbeddedFileInfo.newInstance(pdfDocument, (int) attachment.length(), modTime, modTime);
        final PDFEmbeddedFile pdfEmbeddedFile = PDFEmbeddedFile.newInstance(pdfDocument, info, attachment);
        pdfEmbeddedFile.setFilter(PDFFilterFlate.newInstance(pdfDocument, null));
        final PDFFileSpecification fileSpecification = PDFFileSpecification.newInstance(pdfDocument, new ASString("StandardBusiness.pdf"),
                pdfEmbeddedFile);
        final PDFRectangle annotLocation_Attachment = PDFRectangle.newInstance(pdfDocument, (2 * INCH) - 8, (10.5 * INCH) - 10, 2 * INCH,
                10.5 * INCH);
        final PDFAnnotationFileAttachment pdfAnnotationFileAttachment = PDFAnnotationFileAttachment.newInstance(pdfDocument,
                annotLocation_Attachment, fileSpecification);
        pdfAnnotationFileAttachment.setColor(new double[] { 0.25, 0.33, 1 });
        pdfAnnotationFileAttachment.setIconName("PaperclipTag");
        pdfPageOne.addAnnotation(pdfAnnotationFileAttachment);

        /*
         * Line, arrow, rectangle, and circle annotations all use a lower left
         * and upper right set of coordinates to draw their annotations. The
         * specifics are in each set of properties.
         */

        // Line / no arrows
        final double llx_line = 2 * INCH;
        final double lly_line = 3 * INCH;
        final double urx_line = 4 * INCH;
        final double ury_line = 3.5 * INCH;
        final PDFAnnotationLine pdfAnnotationLine = PDFAnnotationLine.newInstance(pdfDocument);
        pdfAnnotationLine.setLineCoords(llx_line, lly_line, urx_line, ury_line);
        pdfAnnotationLine.setColor(new double[] { 1, 0, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationLine);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Line));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        // Line / Arrow 1
        final double llx_arrow_1 = 4.5 * INCH;
        final double lly_arrow_1 = 3 * INCH;
        final double urx_arrow_1 = 6.5 * INCH;
        final double ury_arrow_1 = 3.5 * INCH;
        final PDFAnnotationLine pdfAnnotationArrow_1 = PDFAnnotationLine.newInstance(pdfDocument);
        pdfAnnotationArrow_1.setLineCoords(llx_arrow_1, lly_arrow_1, urx_arrow_1, ury_arrow_1);
        pdfAnnotationArrow_1.setColor(new double[] { 1, 0, 0 });
        // Change the order of the line ending array to set which end gets the
        // arrow.
        pdfAnnotationArrow_1.setLineEnds(
                new PDFAnnotationLine.LineEnding[] { PDFAnnotationLine.LineEnding.OpenArrow, PDFAnnotationLine.LineEnding.None });
        pdfPageOne.addAnnotation(pdfAnnotationArrow_1);

        // Line / Arrow 2
        final double llx_arrow_2 = 5 * INCH;
        final double lly_arrow_2 = 3 * INCH;
        final double urx_arrow_2 = 7 * INCH;
        final double ury_arrow_2 = 3.5 * INCH;
        final PDFAnnotationLine pdfAnnotationArrow_2 = PDFAnnotationLine.newInstance(pdfDocument);
        pdfAnnotationArrow_2.setLineCoords(llx_arrow_2, lly_arrow_2, urx_arrow_2, ury_arrow_2);
        pdfAnnotationArrow_2.setColor(new double[] { 1, 0, 0 });
        // Change the order of the line ending array to set which end gets the
        // arrow.
        pdfAnnotationArrow_2.setLineEnds(
                new PDFAnnotationLine.LineEnding[] { PDFAnnotationLine.LineEnding.None, PDFAnnotationLine.LineEnding.OpenArrow });
        pdfPageOne.addAnnotation(pdfAnnotationArrow_2);

        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Line));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        // Rectangle
        final double llx_rect = 2 * INCH;
        final double lly_rect = 2 * INCH;
        final double urx_rect = 2.5 * INCH;
        final double ury_rect = 2.5 * INCH;
        final PDFAnnotationSquare pdfAnnotationSquare = PDFAnnotationSquare.newInstance(pdfDocument);
        pdfAnnotationSquare.setRect(llx_rect, lly_rect, urx_rect, ury_rect);
        final PDFBorderStyle pdfBorderStyle_Square = PDFBorderStyle.newInstance(pdfDocument, 1, Style.Solid, null);
        pdfAnnotationSquare.setBorderStyle(pdfBorderStyle_Square);
        pdfAnnotationSquare.setColor(new double[] { 1, 0, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationSquare);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Square));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        // Circle
        final double llx_oval = 2.75 * INCH;
        final double lly_oval = 2 * INCH;
        final double urx_oval = 3.25 * INCH;
        final double ury_oval = 2.5 * INCH;
        final PDFAnnotationCircle pdfAnnotationCircle = PDFAnnotationCircle.newInstance(pdfDocument);
        pdfAnnotationCircle.setRect(llx_oval, lly_oval, urx_oval, ury_oval);
        final PDFBorderStyle pdfBorderStyle_Oval = PDFBorderStyle.newInstance(pdfDocument, 1, Style.Solid, null);
        pdfAnnotationCircle.setBorderStyle(pdfBorderStyle_Oval);
        pdfAnnotationCircle.setColor(new double[] { 1, 0, 0 });
        pdfPageOne.addAnnotation(pdfAnnotationCircle);
        apContext.setAnnotationsToBeProcessed(EnumSet.of(PDFAnnotationEnum.Circle));
        AppearanceService.generateAppearances(pdfDocument, apContext, null);

        // Save and close
        DocumentHelper.saveFullAndClose(pdfDocument, outputDir + "AllAnnotations.pdf");

        // Save the file.
        System.out.println("Done!");
    }

    public static double[] wordsQuadsToAnnotQuads(final Word wordA, final Word wordB) throws Exception {
        final double[] quadPoints = new double[8];
        // Order is not the same for an annotation as it is for a word
        quadPoints[0] = wordA.getBoundingQuads().get(0).p4().x();
        quadPoints[1] = wordA.getBoundingQuads().get(0).p4().y();
        quadPoints[2] = wordB.getBoundingQuads().get(0).p3().x();
        quadPoints[3] = wordB.getBoundingQuads().get(0).p3().y();
        quadPoints[4] = wordA.getBoundingQuads().get(0).p1().x();
        quadPoints[5] = wordA.getBoundingQuads().get(0).p1().y();
        quadPoints[6] = wordB.getBoundingQuads().get(0).p2().x();
        quadPoints[7] = wordB.getBoundingQuads().get(0).p2().y();
        return quadPoints;

    }
}
