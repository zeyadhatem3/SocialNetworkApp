/**
 **                       SocialNetwork Cytoscape App
 **
 ** Copyright (c) 2013-2015 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Victor Kofia, Ruth Isserlin
 ** Authors: Victor Kofia, Ruth Isserlin, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates,
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

package org.baderlab.csapps.socialnetwork.model.academia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.baderlab.csapps.socialnetwork.model.AbstractEdge;
import org.baderlab.csapps.socialnetwork.model.AbstractNode;
import org.baderlab.csapps.socialnetwork.model.Collaboration;
import org.baderlab.csapps.socialnetwork.model.SocialNetworkAppManager;
import org.baderlab.csapps.socialnetwork.model.academia.visualstyles.EdgeAttribute;
import org.cytoscape.model.CyEdge;

/**
 * A co-publication. Used primarily in Academia networks.
 *
 * @author Victor Kofia
 */
public class Copublication extends AbstractEdge {
    
    private static final Logger logger = Logger.getLogger(Copublication.class.getName());

    /**
     * List of co-publications
     */
    private ArrayList<Publication> pubList = null;
    /**
     * An edge attribute map <br>
     * Keys: <i>Attribute type (i.e. name)</i> <br>
     * Value: <i>Attribute value (i.e. Cytoscape app store)</i>
     */
    private Map<String, Object> edgeAttrMap = null;

    /**
     * Create new {@link Copublication} tracker
     *
     * @param Collaboration collaboration
     * @param Publication publication
     */
    public Copublication(Collaboration collaboration, Publication publication) {
        this.setPubList(new ArrayList<Publication>());
        this.getPubList().add(publication);
        this.constructEdgeAttrMap();
    }

    /**
     * Add a publication
     *
     * @param Publication publication
     */
    @SuppressWarnings("unchecked")
    public void addPublication(Publication publication) {
        this.getPubList().add(publication);
        this.getEdgeAttrMap().put(EdgeAttribute.COPUBLICATION_COUNT.toString(), this.getPubList().size());
        ((ArrayList<String>) this.getEdgeAttrMap().get(EdgeAttribute.PUBLICATIONS.toString())).add(publication.getTitle());
        updateYearlyIntervalAttr();
    }

    /**
     * Construct an edge attribute map
     */
    @Override
    public void constructEdgeAttrMap() {
        this.setEdgeAttrMap(new HashMap<String, Object>());
        this.getEdgeAttrMap().put(EdgeAttribute.COPUBLICATION_COUNT.toString(), this.getPubList().size());
        ArrayList<String> titles = new ArrayList<String>();
        titles.add(this.getPubList().get(0).getTitle());
        this.getEdgeAttrMap().put(EdgeAttribute.PUBLICATIONS.toString(), titles);
        int startYear = SocialNetworkAppManager.getStartYear(), endYear = SocialNetworkAppManager.getEndYear();
        int size = endYear - startYear + 1;
        ArrayList<Integer> boolList = new ArrayList<Integer>(size);
        Publication pub = this.pubList.get(0);
        int pubYear = -1;
        try {
            pubYear = pub.getPubYear();
        } catch (UnableToParseYearException e) {
            logger.log(Level.WARNING, String.format("Year could not be parsed from raw text: %s", pub.getRawPubDateTxt()));
        }
        for (int year = startYear; year <= endYear; year++) {
            boolList.add(pubYear == year ? 1 : 0);
        }
        this.edgeAttrMap.put(EdgeAttribute.PUBS_PER_YEAR.toString(), boolList);   
    }
    
    /**
     * 
     */
    private void updateYearlyIntervalAttr() {
        int startYear = SocialNetworkAppManager.getStartYear(), endYear = SocialNetworkAppManager.getEndYear();
        @SuppressWarnings("unchecked")
        ArrayList<Integer> boolList = (ArrayList<Integer>) this.getEdgeAttrMap().get(EdgeAttribute.PUBS_PER_YEAR.toString());
        Publication pub = this.getPubList().get(this.getPubList().size() - 1);
        int pubYear = -1;
        try {
            pubYear = pub.getPubYear();
        } catch (UnableToParseYearException e) {
            logger.log(Level.WARNING, String.format("Year could not be parsed from raw text: %s", pub.getRawPubDateTxt()));
        }
        for (int year = startYear; year <= endYear; year++) {
            if (pubYear == year) {
                int index = year - startYear;
                boolList.set(index, boolList.get(index) + 1);                
            }
        }
        this.edgeAttrMap.put(EdgeAttribute.PUBS_PER_YEAR.toString(), boolList);            
    }

    /* (non-Javadoc)
     * @see org.baderlab.csapps.socialnetwork.model.AbstractEdge#getCyEdge()
     */
    @Override
    public CyEdge getCyEdge() {
        return this.cyEdge;
    }

    /**
     * Get the edge attribute map
     *
     * @return Map edgeAttrMap
     */
    @Override
    public Map<String, Object> getEdgeAttrMap() {
        return this.edgeAttrMap;
    }

    /* (non-Javadoc)
     * @see org.baderlab.csapps.socialnetwork.model.AbstractEdge#getNodes()
     */
    @Override
    public List<? extends AbstractNode> getNodes() {
        return null;
    }

    /**
     * Get publist
     *
     * @return ArrayList pubList
     */
    private ArrayList<Publication> getPubList() {
        return this.pubList;
    }

    /**
     * Get total # of co-publications
     *
     * @return int totalPubs
     */
    public int getTotalPubs() {
        return this.getPubList().size();
    }

    /* (non-Javadoc)
     * @see org.baderlab.csapps.socialnetwork.model.AbstractEdge#setCyEdge(org.cytoscape.model.CyEdge)
     */
    @Override
    public void setCyEdge(CyEdge cyEdge) {
        this.cyEdge = cyEdge;
    }

    /**
     * Set the edge attribute map
     *
     * @param Map edgeAttrMap
     */
    public void setEdgeAttrMap(Map<String, Object> edgeAttrMap) {
        this.edgeAttrMap = edgeAttrMap;
    }

    /**
     * Set publist
     *
     * @param ArrayList pubList
     */
    private void setPubList(ArrayList<Publication> pubList) {
        this.pubList = pubList;
    }

}