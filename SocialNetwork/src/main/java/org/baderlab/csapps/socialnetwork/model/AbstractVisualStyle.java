package org.baderlab.csapps.socialnetwork.model;

import org.cytoscape.view.vizmap.VisualStyle;


/**
 * ??
 * 
 * @author Victor Kofia
 */
// TODO: Write class description
public abstract class AbstractVisualStyle {
    
    /**
     * Apply an edge style to every edge in network.
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyEdgeStyle(VisualStyle visualStyle);
    
    /**
     * Apply a node style to every node in network.
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeStyle(VisualStyle visualStyle);
    
    /**
     * Apply a visual style to network.
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyVisualStyle(VisualStyle visualStyle);
    
    /**
     * Get the underlying Cytoscape visual style object
     * 
     * @return VisualStyle visualStyle
     */
    protected abstract VisualStyle getVisualStyle();
    
    /**
     * Specify the node label
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeLabel(VisualStyle visualStyle);
    
    /**
     * Specify the node size
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeSize(VisualStyle visualStyle);
    
    /**
     * Specify the node fill color
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeFillColor(VisualStyle visualStyle);
    
    /**
     * Specify the node shape
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeShape(VisualStyle visualStyle);
    
    /**
     * Specify the node border color
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeBorderPaint(VisualStyle visualStyle);
    
    /**
     * Specify the node border width
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeBorderWidth(VisualStyle visualStyle);
    
    /**
     * Specify the node label font
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyNodeLabelFontFace(VisualStyle visualStyle);
    
    /**
     * Specify the edge width
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyEdgeWidth(VisualStyle visualStyle);

    /**
     * Specify the edge transparency
     * 
     * @param VisualStyle visualStyle
     */
    protected abstract void applyEdgeTransparency(VisualStyle visualStyle);

}
