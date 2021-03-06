package org.baderlab.csapps.socialnetwork.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.baderlab.csapps.socialnetwork.CytoscapeUtilities;
import org.baderlab.csapps.socialnetwork.model.SocialNetworkAppManager;
import org.baderlab.csapps.socialnetwork.tasks.ExportNthDegreeNeighborsTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;

/**
 * Allows users to export a csv file containing the nth degree neighbors of
 * every node in a currently existing Cytoscape network.
 * 
 * @author Victor Kofia
 */
@SuppressWarnings("serial")
public class ExportNthDegreeNeighborsAction extends AbstractCyAction {

    /**
     * 
     */
    // TODO: Write description
    private CyNetworkManager cyNetworkManagerServiceRef = null;
    private CySwingApplication cySwingApplicationServiceRef = null;
    private JPanel exportNeighborsPanelRef = null;
    private JTextField neighborDegreeRef = null;
    private JComboBox<String> nodeAttrComboBoxRef = null;
    private JComboBox<String> networkNameComboBoxRef = null;
    private HashMap<String, CyNetwork> cyNetworkMap = null;
    private TaskManager<?, ?> taskManagerServiceRef = null;
    private ExportNthDegreeNeighborsTaskFactory exportNthDegreeNeighborsTaskFactoryRef = null;
    private SocialNetworkAppManager appManager = null;

    /**
     * Constructor for {@link ExportNthDegreeNeighborsAction}
     * 
     * @param String name
     * @param CyApplicationManager applicationManager
     * @param CyNetworkViewManager networkViewManager
     * @param CyNetworkManager cyNetworkManagerServiceRef
     */
    public ExportNthDegreeNeighborsAction(Map<String, String> configProps, CyApplicationManager applicationManager, CyNetworkViewManager networkViewManager,
            CyNetworkManager cyNetworkManagerServiceRef, CySwingApplication cySwingApplicationServiceRef, TaskManager<?, ?> taskManager,
            ExportNthDegreeNeighborsTaskFactory exportNthDegreeNeighborsTaskFactoryRef, SocialNetworkAppManager appManager) {
        super(configProps, applicationManager, networkViewManager);
        putValue(Action.NAME, "Export Neighbors");
        this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
        this.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
        this.nodeAttrComboBoxRef = new JComboBox<String>();
        this.cyNetworkMap = new HashMap<String, CyNetwork>();
        this.networkNameComboBoxRef = createNetworkNameComboBoxRef();
        this.taskManagerServiceRef = taskManager;
        this.exportNthDegreeNeighborsTaskFactoryRef = exportNthDegreeNeighborsTaskFactoryRef;
        this.appManager = appManager;
    }

    /**
     * Export the neighborlist if a valid network, attribute and degree has been
     * provided by the user
     * 
     * @param ActionEvent e
     */
    public void actionPerformed(ActionEvent e) {
        int outcome = JOptionPane.OK_OPTION;
        while (outcome == JOptionPane.OK_OPTION) {
            updateNetworkNameComboBoxRef();
            outcome = JOptionPane.showConfirmDialog(null, getExportNeighborsPanelRef(), "Export neighbor list options", JOptionPane.OK_CANCEL_OPTION);
            if (outcome == JOptionPane.OK_OPTION) {
                String networkName = (String) this.networkNameComboBoxRef.getSelectedItem();
                CyNetwork network = this.cyNetworkMap.get(networkName);
                if (network == null) {
                    CytoscapeUtilities.notifyUser("Unable to export. No network is available");
                    return;
                }
                String text = this.neighborDegreeRef.getText().trim();
                if (!Pattern.matches("[0-9]+", text)) {
                    CytoscapeUtilities.notifyUser("Invalid input. Please enter an integer value.");
                    continue;
                }
                int degree = Integer.parseInt(text);
                if (degree == 0) {
                    return; // No neighbors to export if degree is equal to 0
                }
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setDialogTitle("Select a folder");
                fc.setApproveButtonText("Select");
                if (fc.showOpenDialog(this.cySwingApplicationServiceRef.getJFrame()) == JFileChooser.APPROVE_OPTION) {
                    this.appManager.setNeighborListDegree(degree);
                    this.appManager.setNeighborListNetwork(network);
                    this.appManager.setNeighborListFolder(fc.getSelectedFile());
                    this.appManager.setNeighborListAttribute((String)nodeAttrComboBoxRef.getSelectedItem());
                    this.getTaskManager().execute(this.exportNthDegreeNeighborsTaskFactoryRef.createTaskIterator());                
                }
                outcome = JOptionPane.CANCEL_OPTION;
            }
        }
    }

    /**
     * Create a panel that allows the user to export the nth degree neighbors of
     * a network.
     * 
     * @return JPanel exportNeighborsPanel
     */
    private JPanel createExportNeighborsPanel() {
        JPanel exportNeighborsPanel = new JPanel();
        exportNeighborsPanel.setLayout(new BoxLayout(exportNeighborsPanel, BoxLayout.Y_AXIS));

        JPanel networkNamePanel = new JPanel();
        networkNamePanel.setLayout(new BoxLayout(networkNamePanel, BoxLayout.X_AXIS));

        networkNamePanel.add(new JLabel("Select network: "));
        networkNamePanel.add(networkNameComboBoxRef);
        updateNetworkNameComboBoxRef();

        JPanel degreeInputPanel = new JPanel();
        degreeInputPanel.setLayout(new BoxLayout(degreeInputPanel, BoxLayout.X_AXIS));
        neighborDegreeRef = new JTextField(5);
        neighborDegreeRef.setText("1");
        degreeInputPanel.add(new JLabel("Please specify the degree: "));
        degreeInputPanel.add(neighborDegreeRef);

        JPanel nodeAttrPanel = new JPanel();
        nodeAttrPanel.setLayout(new BoxLayout(nodeAttrPanel, BoxLayout.X_AXIS));
        nodeAttrPanel.add(new JLabel("Select node attribute: "));
        updateNodeAttrComboBoxRef();
        nodeAttrPanel.add(nodeAttrComboBoxRef);

        exportNeighborsPanel.add(networkNamePanel);
        exportNeighborsPanel.add(nodeAttrPanel);
        exportNeighborsPanel.add(degreeInputPanel);
        return exportNeighborsPanel;
    }

    /**
     * Create and return the network name JComboBox
     * 
     * @return JComboBox networkNameComboBoxRef
     */
    private JComboBox<String> createNetworkNameComboBoxRef() {
        JComboBox<String> comboBox = new JComboBox<String>();
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateNodeAttrComboBoxRef();
            }
        });
        return comboBox;
    }

    /**
     * Get the reference to the export neighbors panel
     * 
     * @return JPanel exportNeighborsPanelRef
     */
    private JPanel getExportNeighborsPanelRef() {
        if (this.exportNeighborsPanelRef == null) {
            setExportNeighborsPanelRef(createExportNeighborsPanel());
        }
        return exportNeighborsPanelRef;
    }

    /**
     * Set the reference to the export neighbors panel
     * 
     * @param JPanel exportNeighborsPanelRef
     */
    private void setExportNeighborsPanelRef(JPanel exportNeighborsPanelRef) {
        this.exportNeighborsPanelRef = exportNeighborsPanelRef;
    }

    /**
     * Update the network name JComboBox with the info that is currently in the
     * CyNetworkMap.
     */
    private void updateNetworkNameComboBoxRef() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        Set<CyNetwork> networkSet = this.cyNetworkManagerServiceRef.getNetworkSet();
        String networkName = null;
        this.cyNetworkMap.clear();
        if (networkSet.isEmpty()) {
            networkName = "N/A";
            model.addElement(networkName);
        } else {
            Iterator<CyNetwork> it = networkSet.iterator();
            CyNetwork cyNetwork = null;
            while (it.hasNext()) {
                cyNetwork = it.next();
                networkName = SocialNetworkAppManager.getNetworkName(cyNetwork);
                model.addElement(networkName);
                this.cyNetworkMap.put(networkName, cyNetwork);
            }
        }
        this.networkNameComboBoxRef.setModel(model);
        this.networkNameComboBoxRef.setSelectedItem(networkName);
    }

    /**
     * Update the node attribute JComboBox with the attributes of the selected
     * CyNetwork
     */
    private void updateNodeAttrComboBoxRef() {
        String selectedItem = (String) this.networkNameComboBoxRef.getSelectedItem();
        CyNetwork cyNetwork = cyNetworkMap.get(selectedItem);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        if (cyNetwork == null) {
            model.addElement("N/A");
        } else {
            for (CyColumn col : cyNetwork.getDefaultNodeTable().getColumns()) {
                if (col.getType() == String.class) {
                    model.addElement(col.getName());
                }
            }
        }
        nodeAttrComboBoxRef.setModel(model);
    }

    /**
     * Get Cytoscape task manager
     *
     * @return TaskManager taskManager
     */
    public TaskManager<?, ?> getTaskManager() {
        return this.taskManagerServiceRef;
    }
    
    /**
     * Set task manager
     *
     * @param TaskManager taskManager
     */
    public void setTaskManager(TaskManager<?, ?> taskManager) {
        this.taskManagerServiceRef = taskManager;
    }

}
