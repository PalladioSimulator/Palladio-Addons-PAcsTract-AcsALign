//package visualizer;
//
//import java.io.IOException;
//
//import java.util.ArrayList;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDDocumentInformation;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//
//@Deprecated
//public class PDFVisualizer {
//
//	/*
//	 * Erstellt eine PDF mit einer Tabelle darin.
//	 * Diese Tabelle passt sich weitestgehend in Spalten- und Reihenanzahl dem bergebenen Inhalt an.
//	 * Bei Drei spalten jedoch, wird die Gre der hintersten reduziert und die der mittleren Spalte entsprechend erhht.
//	 * Das resultierte aus den Anforderungen der Ausgabe von "RoleAndPerms". Denn dort ist der Inhalt der letzten Spalte
//	 * immer nur ein Wort, whrend das Datenobjekt durchaus grere Ausmae annehmen kann
//	 */
//	public void createPDFOutput(ArrayList<String[]> roleAndPermArray, String pdfName, String projectPath){
//		
//		ArrayList<String[]> content = roleAndPermArray;
//		PDDocument pdf = new PDDocument();
//
//
//		//Attribute der PDF setzen
//		PDDocumentInformation pdfInfo = pdf.getDocumentInformation();
//		pdfInfo.setAuthor("BPMNRoleModelExtractor powered by FZI Berlin");
//		pdfInfo.setCreator("FZI Berlin");
//		pdfInfo.setTitle("RoleModelTable");
//		pdfInfo.setSubject("The Rolemodel that will result from your BPMN2.0 Diagramms");
//		int nextPageStartIndex = 0;
//		/*
//		 * content.size() = anzahl der bentigten Tabellenzeilen
//		 * 42f ist die festgelegte Anzahl der Tabellenzeilen pro Seite als Float
//		 * Als float, damit die Kommastellen korrekt behandelt werden und nicht einfach abgerundet werden.
//		 * Anschlieend wird der Wert aufgerundet um so die wirklich ntige Seitenanzahl zu ermitteln
//		 */
//		float pageCounterTemp = (content.size()/42f);
//		double pageCounter = Math.ceil(pageCounterTemp);
////		System.out.println("Seitenanzahl Errechnet: "+pageCounterTemp);
////		System.out.println("SeitenAnzahl gerundet: " + pageCounter);
//
//		//so viele Seiten wie bentigt werden ("pageCounter") werden der PDF hinzugefgt sowie die
//		//weiteren Funktionen aufgerufen, welche die Seiten befllen.
//		for(int currentPage = 0; currentPage < pageCounter; currentPage++){
//			PDPage page = new PDPage();
//			pdf.addPage(page);
//			PDPageContentStream contentStream;
//			
//			try {
//
//				contentStream = new PDPageContentStream(pdf, page);
//				int[] rowsArray = getRowsPerPage(content); 
//				int currentPageRows = rowsArray[currentPage];
//				nextPageStartIndex = drawTable(page, contentStream, 700, 25, content, nextPageStartIndex, rowsArray.length, currentPageRows);
//				contentStream.close();
//			} catch (IOException e) {
//				System.out.println("-- ERROR OCCURED in createPDF(); --");
//				e.printStackTrace();
//			}
//		}
//
//		try {
//			pdf.save(projectPath + "/DataOutput/"+ pdfName +".pdf" );
//			pdf.close();
//			System.out.println("#################\nPDF erstellt!\n#################");
//		}catch (IOException e) {
//				System.out.println("-- Hinweis --\nDie zu beschreibende Datei ist geffnet, Schreibgeschtzt oder hnliches!");
//				e.printStackTrace();
//			}catch (IllegalStateException e){
//				System.out.println("-- ERROR OCCURED in createPDF(); --");
//				e.printStackTrace();
//			}
//		}
//	
//	private int[] getRowsPerPage(ArrayList<String[]> content){		//nicht kompletten content bergeben sondern nur die lnge
//		
//		float contentSize = content.size();
//		float rowsOnAFullPage = 42;
//		float numberOfPages = contentSize/rowsOnAFullPage;
//		double finalNumberOfPages = Math.ceil(numberOfPages);
//		int[] rowsArray = new int[(int)finalNumberOfPages];		//new int[(int)finalNumberOfPages] bewirkt dass das Array immer so viele Pltze hat wie es seiten gibt.
//		
////		System.out.println("Number of Pages: "+numberOfPages+"\n ----------------------");
//		
//		for (int i = 1; i <= finalNumberOfPages;i++){
//			if (i != finalNumberOfPages){
//				int rows = 42;
//				System.out.println("Seite "+ i +": "+rows+" Zeilen");
//				rowsArray[i-1] = rows;
//			}else if(i == finalNumberOfPages){
//				int rows = (int) (contentSize % rowsOnAFullPage);
//				System.out.println("Seite "+ i +": "+rows+" Zeilen");
//				rowsArray[i-1] = rows;
//			}
//		}
//		return rowsArray;
//	}
//	
//	//Funktion zum zeichnen der Tabelle
//	private int drawTable(PDPage page, PDPageContentStream contentStream, float y, float margin, ArrayList<String[]> content, int nextPageStartIndex, int rowsArrayLength, int rows) throws IOException {
//
//		//spaltenanzahl wird anhand des Headers (erstes Array in der Liste) ermittelt
//		String[] innerContentTemp = content.get(0);
//		float cols = innerContentTemp.length;
//		
//		
//		float rowHeight = 15f;
//		//die breite der Tabelle entspricht der verfgbaren MediaBox(das ganze Blatt/Seite)
//		//davon die Breite minus 2 mal die bergebene Breite des Randes
//		float tableWidth = page.getMediaBox().getWidth()-(2*margin);
//		float tableHeight = rowHeight * rows;
//		float colWidth = (tableWidth/cols);
//		float cellMargin=5f;
//
//		//die Zeilen werden gezeichnet
//		float nexty = y ;
//
//		for (float j = 0; j <= rows; j++) {	//die Menge der ArrayListen in der ArrayListe "content" bestimmen die Menge an Zeilen die gezeichnet werden
//
//			//der Header wird eingefrbt wenn wir uns in Zeile 1, auf Seite 1 befinden
//			if (j == 0 && nextPageStartIndex == 0){
//				contentStream.setStrokingColor(127,255,212);	//RGB-Farbcode zur einstellung der HeaderFarbe
//				contentStream.setLineWidth(rowHeight);
//				contentStream.moveTo(25, y-(rowHeight/2));	
//				contentStream.lineTo(587,y-(rowHeight/2));
//				contentStream.stroke();
//			}
//			//der Rest der Tabelle wird gezeichnet
//			contentStream.setStrokingColor(0,0,0);
//			contentStream.setLineWidth(0.5f);	//strichstrke der Tabelle
//			contentStream.moveTo(25, nexty);	
//			contentStream.lineTo(587, nexty);
//			contentStream.stroke();
//			nexty-= rowHeight;
//		}
//		
//
//		//die Spalten werden gezeichnet
//		float nextx = margin;
//		for (int i = 0; i <= cols; i++) {	//die Anzahl der Elemente in den Unter-ArrayListen bestimmen die Anzahl der Spalten
//			//wenn drei Spalten anzulegen sind, wird eine spezielle Form angewandt
//			if(cols == 3){
//				if(i < 2){
//					contentStream.moveTo(nextx,y);
//					contentStream.lineTo(nextx, y-tableHeight);
//					contentStream.stroke();
//					nextx += colWidth;
//				}else{
//					//die dritte Splate wird auf der X-achse um die Werte Verschoben sodass mehr Inhalt in die 2. Spalte passt.
//					contentStream.moveTo(nextx+138,y);
//					contentStream.lineTo(nextx+138, y-tableHeight);
//					contentStream.moveTo(nextx+188,y);
//					contentStream.lineTo(nextx+188, y-tableHeight);
//					contentStream.stroke();
//				}
//			}else{
//				contentStream.moveTo(nextx,y);
//				contentStream.lineTo(nextx, y-tableHeight);
//				contentStream.stroke();
//				nextx += colWidth;
//
//			}
//		}
//
//		//der Text wird in die Zeilen eingefgt
////		contentStream.setFont(PDType1Font.HELVETICA,8);
//		contentStream.setFont(PDType1Font.TIMES_ROMAN, 8);
//
//		float textx = margin+cellMargin;
//		float texty = y-12;
//
//		for(int i = nextPageStartIndex; i < content.size(); i++){
//			if(i < nextPageStartIndex + 42){
//				String[] innerContent = content.get(i);	
//				for(int j = 0 ; j < innerContent.length; j++){
//					String text = innerContent[j];
//					if (j < 2){
//						try {
//						contentStream.beginText();
//						contentStream.newLineAtOffset(textx,texty);
//						contentStream.showText(text);
//						contentStream.endText();
//						textx += colWidth;
//						}catch(IllegalArgumentException e) {
//							System.out.println("-- ERROR! --\nSomewhere in your diagramm is a manualy caused line break. This cannot be handeld by the Font!\nPlease remove the line break and try again!");
//						}
//						
//					}else{
//						try {
//						contentStream.beginText();
//						contentStream.newLineAtOffset(textx+138,texty);
//						contentStream.showText(text);
//						contentStream.endText();						
//							textx += colWidth;
//						}catch(IllegalArgumentException e) {
//							System.out.println("-- ERROR! --\nSomewhere in your diagramm is a manualy caused line break. This cannot be handeld by the Font!\nPlease remove the line break and try again!");
//						}
//					}
//				}
//				texty -= rowHeight;
//				textx = margin+cellMargin;
//			}
//		}
//		nextPageStartIndex += 42;
//		return nextPageStartIndex;
//	}
//}
//
