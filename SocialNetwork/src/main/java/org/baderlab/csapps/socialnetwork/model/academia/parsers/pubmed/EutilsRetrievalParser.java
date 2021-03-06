package org.baderlab.csapps.socialnetwork.model.academia.parsers.pubmed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.baderlab.csapps.socialnetwork.CytoscapeUtilities;
import org.baderlab.csapps.socialnetwork.model.Category;
import org.baderlab.csapps.socialnetwork.model.SocialNetwork;
import org.baderlab.csapps.socialnetwork.model.academia.Author;
import org.baderlab.csapps.socialnetwork.model.academia.Publication;
import org.baderlab.csapps.socialnetwork.model.academia.Tag;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ??
 * 
 * @author Victor Kofia
 */
// TODO: Write class description
public class EutilsRetrievalParser extends AbstractTask{

    private static final Logger logger = Logger.getLogger(EutilsRetrievalParser.class.getName());

    private int publication_counter = 0;
    private TaskMonitor taskMonitor = null;

    /**
     * The author of a specific publication.
     */
    private Author author = null;
    /**
     * Raw text of an author of a specific publication
     */
    private StringBuilder rawAuthorText = null;
    /**
     * A publication's journal
     */
    private StringBuilder journal = null;
    /**
     * A list containing all authors found in a particular publication
     */
    private ArrayList<Author> pubAuthorList = new ArrayList<Author>();
    /**
     * A publication's date
     */
    private StringBuilder pubDate = null;
    /**
     * A list containing all the results that search session has yielded
     */
    private ArrayList<Publication> pubList = new ArrayList<Publication>();
    /**
     * A publication's unique identifier
     */
    private StringBuilder pmid = null;
    /**
     * A publication's total number of citations
     */
    private StringBuilder timesCited = null;
    /**
     * A publication's title
     */
    private StringBuilder title = null;

    private String queryKey = "";
    private String webEnv = "";
    private int retStart = 0;
    private int retMax = 0;
    private int totalPubs = 0;
    private SocialNetwork socialNetwork = null;
    
    /**
     * Create a new eUtils retrieval parser
     * 
     * @param String queryKey
     * @param String webEnv
     * @param int retStart
     * @param int retMax
     * @param int totalPubs
     */
    public EutilsRetrievalParser(SocialNetwork socialNetwork) {
        this.rawAuthorText = new StringBuilder();
        this.journal = new StringBuilder();
        this.pubDate = new StringBuilder();
        this.pmid = new StringBuilder();
        this.timesCited = new StringBuilder();
        this.title = new StringBuilder();
        this.socialNetwork = socialNetwork;
        
        
       
    }


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		 try {
		        taskMonitor.setStatusMessage("Retrieving records from PubMed ...");  
		        
		        if(socialNetwork.getEutilsResults() != null){
		        	this.queryKey = socialNetwork.getEutilsResults().getQueryKey();
		        	this.webEnv = socialNetwork.getEutilsResults().getWebEnv();
		        	this.retStart = socialNetwork.getEutilsResults().getRetStart();
		        	this.retMax = socialNetwork.getEutilsResults().getRetMax();
		        	this.totalPubs = socialNetwork.getEutilsResults().getTotalPubs();
		        }	        
		        
		        taskMonitor.setProgress(0);
			 SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
	            EutilsParser parser = new EutilsParser();
	            this.taskMonitor = taskMonitor;
	            while (retStart < totalPubs) {
	                // Use newly discovered queryKey and webEnv to build a tag
	                Tag tag = new Tag(queryKey, webEnv, retStart, retMax);
	                // Load all publications at once
	                String url = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed%s", tag);
	                saxParser.parse(url, parser);
	                retStart += retMax;
	                taskMonitor.setProgress((int) (((double) retStart/totalPubs) * 100));
	                
	            }
	            socialNetwork.setPublications(parser.getPubList());
	        } catch (ParserConfigurationException e) {
	            logger.log(Level.SEVERE, "Exception occurred", e);
	            CytoscapeUtilities.notifyUser("Encountered temporary server issues. Please " + "try again some other time.");
	        } catch (SAXException e) {
	            logger.log(Level.SEVERE, "Exception occurred", e);
	            CytoscapeUtilities.notifyUser("Encountered temporary server issues. Please " + "try again some other time.");
	        } catch (IOException e) {
	            logger.log(Level.SEVERE, "Exception occurred", e);
	            CytoscapeUtilities.notifyUser("Unable to connect to PubMed. Please check your " + "internet connection.");
	        }
		
	}

    
    
    private class EutilsParser extends DefaultHandler{
    	/**
         * XML Parsing variables. Used to temporarily store data.
         */
        boolean isAuthor = false;
        boolean isJournal = false;
        boolean isPMID = false;
        boolean isPubDate = false;
        boolean isTimesCited = false;
        boolean isTitle = false;
        
	    // Collect tag contents (if applicable)
	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	     */
	    @Override
	    
	    
	    public void characters(char ch[], int start, int length) throws SAXException {
	        if (this.isPubDate) {
	            pubDate.append(ch, start, length);
	        }
	        if (this.isAuthor) {
	            rawAuthorText.append(ch, start, length);
	        }
	        if (this.isJournal) {
	            journal.append(ch, start, length);
	        }
	        if (this.isTitle) {
	            title.append(ch, start, length);
	        }
	        if (this.isTimesCited) {
	           timesCited.append(ch, start, length);
	        }
	        if (this.isPMID) {
	            pmid.append(ch, start, length);
	        }
	    }
	
	    /**
	     * Returns true iff attributes contains the specified text
	     *
	     * @param Attribute attributes
	     * @param String text
	     * @return Boolean bool
	     */
	    public boolean contains(Attributes attributes, String text) {
	        for (int i = 0; i < attributes.getLength(); i++) {
	            if (attributes.getValue(i).equalsIgnoreCase(text)) {
	                return true;
	            }
	        }
	        return false;
	    }
	
	    // Create new publication and add it to overall publist
	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	     */
	    @Override
	    public void endElement(String uri, String localName, String qName) throws SAXException {
	        if (qName.equals("Item") && this.isAuthor) {
	            this.isAuthor = false;
	            author = new Author(rawAuthorText.toString(), Category.PUBMED);
	            // Add author to publication author list
	            if (!pubAuthorList.contains(author)) {
	                pubAuthorList.add(author);
	            }
	        }
	        if (qName.equals("Item") && this.isJournal) {
	            this.isJournal = false;
	        }
	        if (qName.equals("Item") && this.isPubDate) {
	            this.isPubDate = false;
	        }
	        if (qName.equals("Item") && this.isTitle) {
	            this.isTitle = false;
	        }
	        if (qName.equals("Item") && this.isTimesCited) {
	            this.isTimesCited = false;
	        }
	        if (qName.equals("Id")) {
	            this.isPMID = false;
	        }
	        if (qName.equalsIgnoreCase("DocSum")) {
	            Publication publication = new Publication(title.toString(), pubDate.toString(), 
	                    journal.toString(), timesCited.toString(), null, pubAuthorList);
	            publication.setPMID(pmid.toString()); // TODO: pass this value
	                                                       // through the
	                                                       // constructor?
	            pubList.add(publication);
	            pubAuthorList.clear();
	            publication_counter++;
	         // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) publication_counter / totalPubs) * 100);
                //System.out.println(percentComplete);
                if (taskMonitor != null) {
                        taskMonitor.setProgress(percentComplete);
                        
                        
                    }
	            
	        }
	    }
	
	    /**
	     * Get publication list
	     * 
	     * @return ArrayList pubList
	     */
	    public ArrayList<Publication> getPubList() {
	        return pubList;
	    }
	
	    /**
	     * Get total pubs
	     * 
	     * @return int totalPubs
	     */
	    public int getTotalPubs() {
	        return pubList.size();
	    }
	    
	    /* (non-Javadoc)
	     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	     * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	     */
	    @Override
	    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	        if (contains(attributes, "Author")) {
	            this.isAuthor = true;
	            rawAuthorText.setLength(0);
	        }
	        if (contains(attributes, "FullJournalName")) {
	            this.isJournal = true;
	            journal.setLength(0);
	        }
	        if (contains(attributes, "PubDate")) {
	            this.isPubDate = true;
	            pubDate.setLength(0);
	        }
	        if (contains(attributes, "Title")) {
	            this.isTitle = true;
	            title.setLength(0);
	        }
	        if (contains(attributes, "PmcRefCount")) {
	            this.isTimesCited = true;
	            timesCited.setLength(0);
	        }
	        if (qName.equals("Id")) {
	            this.isPMID = true;
	            pmid.setLength(0);
	        }
	    }
    }


}
