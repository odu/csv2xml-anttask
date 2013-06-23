package ant;

import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;

import java.io.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



import au.com.bytecode.opencsv.CSVReader;
import java.util.ArrayList;
import java.util.List;

public class CSV2XMLTask extends Task {

    String dest;

    public void setDest(String dest) {
        this.dest = dest;
    }
    
    String root = "csv";

    public void setRoot(String root) {
        this.root = root;
    }
    
    String row = "row";

    public void setRow(String row) {
        this.row = row;
    }
    
    Boolean headerRow = true;

    public void setHeaderRow(Boolean headerRow) {
        this.headerRow = headerRow;
    }
    
    Boolean skipEmpty = true;

    public void setSkipEmpty(Boolean skipEmpty) {
        this.skipEmpty = skipEmpty;
    }
    
    Boolean lowerCaseTags = true;

    public void setLowerCaseTags(Boolean lowerCaseTags) {
        this.lowerCaseTags = lowerCaseTags;
    }      
    
    char delim;

    public void setDelim(char delim) {
        this.delim = delim;
    }

    private Vector<FileSet> filesets = new Vector<FileSet>();

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
        
    private String inencoding = "utf-8";
    
    public void setInEncoding(String str) {
        inencoding = str;
    }

    private String outencoding = "utf-8";
    
    public void setOutEncoding(String str) {
        outencoding = str;
    }

    private Vector<Mapper> fmappers = new Vector<Mapper>();
    
    public void addMapper(Mapper mapper) {
        fmappers.add(mapper);
    }

    protected String getOutputFilename(String inputfile) {
        String result = null;
        for (int i = 0, c = fmappers.size(); i < c; i++) {
            Mapper mapper = fmappers.get(i);
            String[] names = mapper.getImplementation().mapFileName(inputfile);

            if (names != null && names.length > 0) {
                result = names[0];
                break;
            }
        }

        if (result == null) {
            File f = new File(inputfile);
            String newfilename = f.getPath().replaceAll("\\.[^.]*$", "") + ".xml";
            result = newfilename;
        }

        return result;
    }

    private void fillXML(Document d, CSVReader r) throws IOException {
        
        Node csv = d.createElement(root);
        d.appendChild(csv);

        String[] line = r.readNext();
        
        List<String> headers = new ArrayList<String>();
        
        if (headerRow) {
            
            for (int i = 0, c = line.length; i < c; i++) {
                if (lowerCaseTags) {
                    headers.add(line[i].replaceAll(" ", "_").toLowerCase());
                } else {
                    headers.add(line[i].replaceAll(" ", "_"));
                }
            }
            line = r.readNext();
        }

        while (line != null) {
            Node rowNode = d.createElement(row);
            for (int i = 0, c = line.length; i < c; i++) {
                if (skipEmpty && line[i].isEmpty()) {
                    // Skipping Empty Cell
                } else {
                    Node cell;
                    if (headerRow) {
                        cell = d.createElement(headers.get(i));
                    } else {
                        cell = d.createElement("c" + Integer.toString(i));   
                    }
                    cell.setTextContent(line[i]);
                    rowNode.appendChild(cell);
                }
            }
            csv.appendChild(rowNode);
            line = r.readNext();
        }
    }

    public void execute() {
        for (Iterator<FileSet> itFSets = filesets.iterator(); itFSets.hasNext();) {      // 2
            FileSet fs = (FileSet) itFSets.next();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());         // 3
            String[] includedFiles = ds.getIncludedFiles();

            for (int i = 0; i < includedFiles.length; i++) {
                String filename = includedFiles[i];           // 4
                log("Convert file: " + filename);

                String outputfile = getOutputFilename(filename);
                log("To: " + outputfile);

                outputfile = (dest != null && dest.length() > 0) ? dest + File.separatorChar + outputfile : outputfile;
                CSVReader reader = null;
                try {
                    FileInputStream fis = new FileInputStream(ds.getBasedir().getPath() + File.separatorChar + filename);
                    InputStreamReader isr = new InputStreamReader(fis, inencoding);

                    log("Encoding: " + isr.getEncoding());

                    reader = new CSVReader(isr, delim);
                    // ---- Parse XML file ----
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.newDocument();
                    fillXML(document, reader);
                    // ---- Use a XSLT transformer for writing the new XML file ----
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    DOMSource source = new DOMSource(document);
                    FileOutputStream os = new FileOutputStream(new File(outputfile));
                    StreamResult result = new StreamResult(os);

                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, outencoding);

                    transformer.transform(source, result);


                    // ---- Error handling ----
                } catch (TransformerConfigurationException tce) {
                    log("Transformer Factory error");
                    log("   " + tce.getMessage());
                    Throwable e = (tce.getException() != null) ? tce.getException() : tce;
                    e.printStackTrace();
                } catch (TransformerException tfe) {
                    log("Transformation error");
                    log("   " + tfe.getMessage());
                    Throwable e = (tfe.getException() != null) ? tfe.getException() : tfe;
                    e.printStackTrace();
                } catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (Exception e) {
                    log(e, 0);
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }

            }
        }
    }
}
