package visualizer;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import model.RoleModelPermissionList;

public class GraphVisualizer {

	String projectPath;

	public void writeToHTML(String fileName, ArrayList<RoleModelPermissionList> everythingInsideArrayList) {

		buildGraphInHTML(fileName, everythingInsideArrayList);
	}

	/**
	 * Constructor with initializing the projectpath
	 * 
	 * @param projectPath
	 *            the path of the current working tree
	 */
	public GraphVisualizer(String projectPath) {
		this.projectPath = projectPath;
	}

	/**
	 * Builds the given array list into html and writes it to a file
	 * 
	 * @param fileName
	 *            name of the file that will be created
	 * @param everythingInsideArrayList
	 *            The data for the html table
	 */
	private void buildGraphInHTML(String fileName, ArrayList<RoleModelPermissionList> everythingInsideArrayList) {

		String nodes = "";
		String edges = "";
		String styleFor = "";

		//header and body of the html file
		String htmlStructureStart = "<!DOCTYPE html>" + "<html>\n" + "<head>\n"
				+ "<script type=\"text/javascript\" src=\"https://unpkg.com/vis-network/standalone/umd/vis-network.min.js\"></script>"
				+ "<style>\n\n" + "#mynetwork {width: 100%;height: 80vh;border: 1px solid lightgray;}" + styleFor
				+ "th, td {\n\tborder: 1px solid lightgrey;\n\ttext-align: left;\n\tpadding: 8px;\n\t; vertical-align:top;\n\t; white-space:nowrap;\n\t}\n\n"
				+ "tr:nth-child(even){background-color: #f2f2f2}\n\n"
				+ "th{\n\tbackground-color: #4CAF50;\n\tcolor: white;\n\t}\n\n" + "</style>\n" + "</head>\n"
				+ "<body>\n" + "<h1 align=\"center\">Graph der Berechtigungen und deren Abhängigkeiten von " + fileName
				+ "</h1>\n                 " + "<div id=\"mynetwork\"></div>\n";

		// extracts the data from the ArrayList and creates nodes and edges for the data
		// object
		for (int i = 0; i < everythingInsideArrayList.size(); i++) {
			String role = everythingInsideArrayList.get(i).getRole();
			
			ArrayList<String> inheritFrom = everythingInsideArrayList.get(i).getInheritFrom();

			for (int k = 0; k < inheritFrom.size(); k++) {
				for (int j = 0; j < everythingInsideArrayList.size(); j++) {
					if (everythingInsideArrayList.get(j).getRole().equals(inheritFrom.get(k))) {
						edges += "{from: " + i + ", to: " + j + ", label: \"erbt von\", arrows: \"to\"},";
					}
				}
			}
			nodes += "{id: " + i + ", label: '" + role + "',shape: \"box\"},";
		}

		//code with the javascript options for the graph
		String HTMLStructureEnd = "<script type=\"text/javascript\">\n" + "    // create an array with nodes\n"
				+ "    var nodes = new vis.DataSet([\n" + nodes + "]);\n" + "  var edges = new vis.DataSet([" + edges
				+ "  ]);" + "    var container = document.getElementById('mynetwork');\n"
				+ "    var data = {nodes: nodes,edges: edges };\n" + "var options = {\n" + "  layout: {\n"
				+ "    randomSeed: undefined,\n" + "    improvedLayout:true,\n" + "    clusterThreshold: 150,\n"
				+ "    hierarchical: {\n" + "      enabled:true,\n" + "      levelSeparation: 250,\n"
				+ "      nodeSpacing: 300,\n" + "      treeSpacing: 300,\n" + "      blockShifting: false,\n"
				+ "      edgeMinimization: true,\n" + "      parentCentralization: true,\n"
				+ "      direction: 'DU',        // UD, DU, LR, RL\n"
				+ "      sortMethod: 'directed',  // hubsize, directed\n"
				+ "      shakeTowards: 'leaves'  // roots, leaves\n" + "    }\n" + "  }\n" + "};\n"
				+ "    var network = new vis.Network(container, data, options);\n" + "</script>" + "</body>\n"
				+ "</html>\n";

		writeIntoHTML(fileName, htmlStructureStart, false, projectPath);
		writeIntoHTML(fileName, HTMLStructureEnd, true, projectPath);
		System.out.println("HTML-Datei geschrieben");

	}

	/**
	 * Writes the given content into a html file at the given fiel path
	 * 
	 * @param fileName
	 *            name of the file to create
	 * @param content
	 *            the content of the file as a string
	 * @param overwrite
	 *            boolean if the file should be overwritten
	 * @param filePath
	 *            the path of the file to write
	 */
	private void writeIntoHTML(String fileName, String content, boolean overwrite, String filePath) {

		/*
		 * um eine quasie neue Datei zu erstellen, wird der alte Inhalt der Datei
		 * (sofern schon vorhanden) berschrieben dazu existiert der bergabeparameter
		 * "overwrite" dieser sollte false sein, wenn eine neue Tabelle angelegt wird
		 * und sollte true sein, falls diese Tabelle mit Inhalten befllt werden soll.
		 */
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filePath + "/DataOutput/" + fileName + ".html", overwrite), "utf-8"))) {
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
