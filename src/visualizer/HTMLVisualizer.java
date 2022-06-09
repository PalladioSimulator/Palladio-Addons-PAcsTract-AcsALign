package visualizer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;


public class HTMLVisualizer {

    String projectPath;
	
	public void writeToHTML(String fileName, ArrayList<String[]> everythingInsideArrayList) {

		buildTableInHTML(fileName, everythingInsideArrayList);
	}
	
	public HTMLVisualizer(String projectPath) {
		this.projectPath = projectPath;
	}
	
	/**
	 * Builds the given array list into html and writes it to a file
	 * @param fileName name of the file that will be created
	 * @param everythingInsideArrayList The data for the html table
	 */
	private void buildTableInHTML(String fileName, ArrayList<String[]> everythingInsideArrayList) {
		
		String tableContent = "";
		
		String styleFor = "";
		
		if (fileName.equals("TraceModel")) {
			styleFor = "body > table > tbody > tr > td:nth-child(6){\nborder-right: 5px solid lightgrey;\n}\n\n"
					+ "body > table > tbody > tr:nth-child(1) > th:nth-child(6) {\nborder-right: 5px solid lightgrey;\n}\n\n";
		}
		
		if (fileName.equals("ADRWithSurroundingASAndELSC")) {
			styleFor = "body > table > tbody > tr > td:nth-child(7){\nborder-right: 5px solid lightgrey;\n}\n\n"
					+ "body > table > tbody > tr:nth-child(1) > th:nth-child(7) {\nborder-right: 5px solid lightgrey;\n}\n\n";
		}
				
		String htmlStructureStart= 
				"<!DOCTYPE html>"
						+ "<html>\n"
						+ "<head>\n"
						+ "<style>\n\n"
						+ "table{\n\tborder-collapse: collapse;\n\twidth: 100%;\n\t}\n\n"
						+ styleFor
						+ "th, td {\n\tborder: 1px solid lightgrey;\n\ttext-align: left;\n\tpadding: 8px;\n\t; vertical-align:top;\n\t; white-space:nowrap;\n\t}\n\n"
						+ "tr:nth-child(even){background-color: #f2f2f2}\n\n"
						+ "th{\n\tbackground-color: #4CAF50;\n\tcolor: white;\n\t}\n\n"
						+ "</style>\n"
						+ "</head>\n"
						+ "<body>\n"
						+ "<h1 align=\"center\">" + fileName + "</h1>\n"
						+ "<p align=\"center\">If you want to export it as a PDF, print it with a PDF-reader of your choice, wich is able to print things as a PDF</p>\n\n\n"
						+ "\t<table>\n";


		for (int i = 0; i < everythingInsideArrayList.size();i++) {
			
			String[] tempContent = everythingInsideArrayList.get(i);
			
			if (i == 0) {
				String tableHeader = "";
				
				for (int j = 0; j < tempContent.length;j++) {
					if (j == 0) {
						
						tableHeader = "\t\t<tr>\n"
								+ "\t\t\t<th>"+tempContent[j]+"</th>\n";
						
					}else if (j > 0 && j <= tempContent.length) {
						
						tableHeader += "\t\t\t<th>"+ tempContent[j] +"</th>\n";
						
					}else if(j == tempContent.length) {
						
						tableHeader += "\t\t\t<th>"+ tempContent[j] +"</th>\n"
								+ "\t\t</tr>\n";
						
					}
				}
				
				tableContent += tableHeader;

			}else{
				
				for (int k = 0; k < tempContent.length;k++) {
					if (k==0) {
						
						tableContent += "\t\t<tr>\n"
								+ "\t\t\t<td>"+tempContent[k]+"</td>\n";
						
					}else if(k > 0 && k <= tempContent.length) {
						
						tableContent += "\t\t\t<td>"+ tempContent[k] +"</td>\n";
						
					}else if(k == tempContent.length) {
						
						tableContent += "\t\t\t<td>"+ tempContent[k] +"</td>\n"
								+ "\t\t</tr>\n";
						
					}
				}
			}
		}
		
		
		String HTMLStructureEnd = 
				"\t</table>\n"
				+ "</body>\n"
				+ "</html>\n";
		
		
		writeIntoHTML(fileName, htmlStructureStart, false, projectPath);
		writeIntoHTML(fileName, tableContent, true, projectPath);
		writeIntoHTML(fileName, HTMLStructureEnd, true, projectPath);
		System.out.println("HTML-Datei geschrieben");
	
	}

	/**
	 * Writes the given data to an html file in the Data outpu folder in the project path
	 * @param fileName the name for the new file
	 * @param content the html content as a string
	 * @param overwrite the boolean for overwrite the old file
	 * @param filePath the path where the file should be created
	 */
	private void writeIntoHTML(String fileName,String content, boolean overwrite, String filePath){
	
		/*
		 * um eine quasie neue Datei zu erstellen, wird der alte Inhalt der Datei (sofern schon vorhanden) berschrieben
		 * dazu existiert der bergabeparameter "overwrite"
		 * dieser sollte false sein, wenn eine neue Tabelle angelegt wird
		 * und sollte true sein, falls diese Tabelle mit Inhalten befllt werden soll.
		 */
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + "/DataOutput/" + fileName + ".html", overwrite), "UTF-8"))) {
			writer.write(content);
	
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}

