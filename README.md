# notshipped
PDFJT samples -- Derived from demos, blogs or other sources

** AllCommentAnnotations.java - Creates a PDF with various comment annotations (Sticky Notes (with different icons) , Highlight, Underline,  Strikeout, Caret, FreeText (Typewriter, Text Box, Text Callout w arrow), Ink/Free Form Draw, Stamp, Line/Arrow, Rectangle, Circle). 8.14 update from JG blog https://blogs.datalogics.com/2016/06/29/creating-comment-annotations-using-the-datalogics-pdf-java-toolkit/.  Place in \samples\manipulation. Will create AllAnnotations.pdf in top level \samples folder. 

** CreatePDFPackage.java - Creates a PDF Package / Portfolio with 3 embedded PDFs (place input files in toplevel \samples). v8.2 adaptation of v4.2 sample; Place in \samples\creation.

** CreatePDFPackageWithNavigator.java - Creates a PDF Package / Portfolio with embedded PDFs that utilizes a flash Navigator. v8.2 adaptation of v4.2 sample;  Place in \samples\creation.

** FormTypeEvaluator.java - determines which type of form: Flat, AcroForm, Static XFA (shell or non-shell) or Dynamic XFA (shell or non-shell). v8.10 adaptation of v4.2 sample; Place in \samples\forms.  

** ListUsageRights.java - Lists usage rights in a PDF. Useful for testing Acrobat enabled features. Place in \samples\signature.  

** QueryAnnotations.java - Iterates through the annotations in a PDF document, listing the subtype of each annotation.
 If the Annotation is a Link Annotation, the URI (web address) is printed if available. If the Annotation is a Widget Annotation, the OnCursorEnter action is printed, such as moving to a different page in the document.  8.14 update of the v6.3.6 sample. Place in \samples\manipulation. Looks for AllAnnotations.pdf in top level \samples folder. 

** RasterizePages.java - Converts PDF to PNG. v8.2 adaptation of v4.2 sample; Place in \samples\images

** RemoveUsageRights.java * Removes Usage Rights aka Reader Extensions. v8.10 adaptation of v4.2 sample; Place in \samples\signature.  RELite cannot process PDFs that already have usage rights

** SplitDocument.java * Demonstrates spliting a PDF document by page intervals (every n pages) and by bookmarks using the PMMservice. v8.14 sample can be placed in \samples\manipulation. 


