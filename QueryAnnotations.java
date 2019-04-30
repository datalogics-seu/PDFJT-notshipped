/*
 * ****************************************************************************
 *
 *  Copyright 2009-2012 Adobe Systems Incorporated. All Rights Reserved.
 *  Portions Copyright 2012-2014 Datalogics Incorporated.
 *
 *  NOTICE: Datalogics and Adobe permit you to use, modify, and distribute
 *  this file in accordance with the terms of the license agreement
 *  accompanying it. If you have received this file from a source other
 *  than Adobe or Datalogics, then your use, modification, or distribution of it
 *  requires the prior written permission of Adobe or Datalogics.
 *
 * ***************************************************************************
 */

package com.datalogics.pdf.samples.manipulation;
// package pdfjt.core.annotations; //seu

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.RandomAccessFileByteReader;
import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnsupportedFeatureException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.interactive.action.PDFAction;
import com.adobe.pdfjt.pdf.interactive.action.PDFActionURI;
import com.adobe.pdfjt.pdf.interactive.action.PDFAdditionalActions;
import com.adobe.pdfjt.pdf.interactive.action.PDFAdditionalActionsWidget;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotation;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationIterator;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationLink;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationList;
import com.adobe.pdfjt.pdf.interactive.annotation.PDFAnnotationWidget;
import com.adobe.pdfjt.pdf.interactive.forms.PDFField;
import com.adobe.pdfjt.pdf.page.PDFPage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * Prints out a list of Annotations in a PDF document and the types of each annotation.
 * If the Annotation is a Link Annotation, the URI (web address) is printed if available.
 * If the Annotation is a Widget Annotation, the OnCursorEnter action is printed, such as moving to a different page
 * in the document.
 *
 */
public class QueryAnnotations
{
	public static void main(final String[] args) throws Exception
	{
		// If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT can find the license file.
		//
		// If you are not using an evaluation version of the product you can ignore or remove this code.
		LicenseManager.setLicensePath(".");
		run();
	}

	static void run() throws Exception
	{
		// This try block surrounds the entire usage of the PDFDocument and is done
		// to ensure that it is closed if any exception occurs.
		PDFDocument pdfDocument = null;

		// The ByteReader belongs to the PDFDocument once it has been passed
		// in during construction. After that point client code should never
		// access it again.
		ByteReader byteReader = null;

		try
		{


			// This try block surrounds the finding and opening of the PDF document file
			// before turning it over to PDF Java  Toolkit.
			//
			// If exceptions occur here it is because there is something wrong with
			// the file, the specification of the file, or the file system.
			try
			{
				final File file = new File(filePath);

				// PDF documents require random access. This code uses a ByteReader that
				// wraps a RandomAccessFile. If execution speed is more important than efficient
				// use of system memory, you could use either the ByteArrayByteReader or
				// the ByteBufferByteReader instead.
				final RandomAccessFile raf = new RandomAccessFile(file, "r");
				byteReader = new RandomAccessFileByteReader(raf);
			} catch (final FileNotFoundException e)
			{
				// The file couldn't be found.
				throw e;
			}

			// This try block surrounds the steps to create a PDFDocument object and
			// get the PDFPage and PDFAnnotationList from that document.
			//
			// If exceptions occur here it is because of a fault in the PDF document
			// (invalid structure or content), errors with the ByteReader (memory or
			// underlying file depending on the type of ByteReader), the use of
			// unsupported features in the PDF document (such as encodings or encryptions schemes),
			// configuration errors on the platform (JVM or external Jars), or other
			// unspecified reasons.
			//
			// If a failure occurs here it is unlikely that further work can be done
			// with this PDF document.

			PDFAnnotationIterator annotIterator = null;
			try
			{
				// Get a PDFDocument from an ByteReader
				// After this call the ByteReader belongs to the PDFDocument
				// and client code should NEVER access it again.
				pdfDocument = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
				byteReader = null;

				// Get the first page.
				final PDFPage pdfPage = pdfDocument.requirePages().getPage(0);

				// Get the list of annotations on the page
				final PDFAnnotationList annotations = pdfPage.getAnnotationList();

				// Get annotation iterator
				annotIterator = annotations.iterator();
			} catch (final PDFInvalidDocumentException e)
			{
				// The PDF document is invalid.
				throw e;
			} catch (final PDFIOException e)
			{
				// An error occurred in reading the PDF document data.
				throw e;
			} catch (final PDFUnsupportedFeatureException e)
			{
				// A feature required to process this document isn't currently
				// supported by the PDF Java Toolkit.
				throw e;
			} catch (final PDFSecurityException e)
			{
				// There is a security problem with the requested operation, such as an
				// encryption provider that is not configured properly or an invalid
				// password. If you want to respond to this security problem
			    // look at sub-classes for more information.
				throw e;
			}

			try
			{
				// Iterator over annotations
				while(annotIterator.hasNext())
				{
					// This try block surrounds retrieval and manipulation of the individual
					// annotations.
					//
					// If exceptions occur here it is because of a fault in the PDF document
					// (invalid structure or content), errors with the ByteReader (memory or
					// underlying file depending on the type of ByteReader), the use of
					// unsupported feature in the PDF (such as encodings or encryptions schemes),
					// configuration errors on the platform (JVM or external Jars), or other
					// unspecified reasons.
					//
					// If a failure occurs here it is possible that further work can be done
					// with this PDF document and this sample will move on to the next
					// annotation and try to work with it. However, depending on the type and
					// severity of the error it may indicate that a more serious response is required.
					try
					{
						// Get next annotation
						final PDFAnnotation pdfAnnotation = annotIterator.next();
						// print type
						final Class type = pdfAnnotation.getClass();
						if(!(pdfAnnotation instanceof PDFAnnotationWidget)){
							System.out.println("PDF Annotation type: " + type.getName());
						}

						if(pdfAnnotation instanceof PDFAnnotationWidget)
						{
							final PDFAnnotationWidget widget = (PDFAnnotationWidget)pdfAnnotation;
							if (widget.getField() != null) {
								final PDFField pdfField = widget.getField();
								if (pdfField.isTerminalField()) {
									System.out.println("PDF Field type: " + pdfField.getClass().getName());
								}
							}
							// Widget Annotation's CosDictionary can be a combined form field/widget annotation dictionary.
							// Hence, it may contain either PDFAdditionalActionsWidget or PDFAdditionalActionsField.
							final PDFAdditionalActions additionalAction = widget.getAdditionalActions();
							if ((additionalAction != null) && (additionalAction instanceof PDFAdditionalActionsWidget))
							{
								final PDFAction action = ((PDFAdditionalActionsWidget)additionalAction).getActionOnCursorEnters();
								if (action != null) {
                                    System.out.println(action.toString());
                                }
							}
						}
						else  if(pdfAnnotation instanceof PDFAnnotationLink)
						{

							final PDFAnnotationLink link = (PDFAnnotationLink)pdfAnnotation;
							final PDFAction action = link.getAction();
							if(action instanceof PDFActionURI)
							{
								final PDFActionURI uriAction = (PDFActionURI)action;
								System.out.println("URI Action value: " +  uriAction.getURI().asString());
							}
						}
					} catch (final PDFInvalidDocumentException e)
					{
						// The PDF document is invalid in some way.
						throw(e);
					} catch (final PDFIOException e)
					{
						// An error occurred in reading the PDF document data.
						throw(e);
					} catch (final PDFUnsupportedFeatureException e)
					{
						// A feature required to process this document isn't currently
						// supported by the Datalogics PDF Java Toolkit.
						throw(e);
					}
				}
			}
			catch (final PDFException pdfExcp)
			{
				// The operation requested could not be completed due to some
				// unspecified error in the PDFAnnotationIterator.hasNext().
				throw(pdfExcp);
			}
		}
		finally
		{
			// Finished using the PDFDocument so close it.
			// If not closed then system resources will be held and not
			// correctly freed.
			if (pdfDocument != null)
			{
				try
				{
					pdfDocument.close();
				} catch (final PDFException e)
				{
					// Unable to close the PDFDocument
					e.printStackTrace();
				}
			}

			if (byteReader != null) {
                byteReader.close();
            }
		}
	}

    // static final String filePath = "input/form.pdf"; //seu
    static final String filePath = "AllAnnotations.pdf";
}
