# Palladio-Addons-PAcsTract-AcsALign

This Repository contains the tools developed by Roman Pilipchuk throughout his dissertation.
* **IntBIIS_LP**: Extention of IntBIIS in order to provide a minimum set of BPMN capabilities
* **PAcsTract (old name PallBPMNRME)**: Uses in IntBIIS_LP defined business processes in order to extract access control requirement
* **AcsALign**: Uses PAcsTract and a Palladio defined architecture model with Stephan Seifermanns Data Flow Extension in order to analze the architecture (Enterprise Application Architecture) for violations of access control requirements extracted from business processes

See also Dissertation (\Dokumentation\Dissertation.pdf) for further details on the approaches and especially the publication "Challenges in Aligning Enterprise Application Architectures to Business Process Access Control Requirements in Evolutional Changes" on more infos about AcsALign.

### IntBIIS_Lanes and Pool (IntBIIS_LP)

* Documentation in: \IntBIIS_LP\model\documentation\
* Eclipse Setup p2f: \IntBIIS_LP\model\documentation\
* IntBIIS and Palladio resources:  \IntBIIS_LP\model\lib\


### Palladio Access permissions Extractor (PAcsTract) & Access permissions Architecture Aligner (AcsALign)

The old name of PAcsTract is PallBPMNRME, and might be often found in namings of files or inside functions.

* Documentation in: \Dokumentation\PallBPMNRMELaunchPlugin Dokumentation.docx
  * Overview control flow PACsTract: \Dokumentation\Kontrollfluss PallBPMNRME.docx
  * Overview control flow AcsALign: \Dokumentation\Kontrollfluss AcsALign.docx
  * Dissertation (containing further details about the approaches): \Dokumentation\Dissertation.pdf
  * See also publication (on more details about AcsALign): "Challenges in Aligning Enterprise Application Architectures to Business Process Access Control Requirements in Evolutional Changes" 
* Eclipse Setup p2f: \Dokumentation\Installation\
* UML Architecture: \Dokumentation\UML-Diagramme
* JavaDoc: \Dokumentation\JavaDoc\
* Input Files from Case Study CoCoME: \CaseStudiesCoCoME\
  * Documentation in: \CaseStudiesCoCoME\Dokumentation CoCoMe-CaseStudies 9, 10 & 13 implementierung.docx
* Input Files for AcsALign from Kunsthalle Karlsruhe: \CaseStudiesKunsthalle Karlsruhe\
  * DataType Usage file from Seifermanns: \CaseStudiesKunsthalle Karlsruhe\PCM Kunsthalle Karlsruhe\dataTypeUsage_2020-11-15.json


