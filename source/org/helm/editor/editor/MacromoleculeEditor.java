/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.helm.editor.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;
import org.jdom.JDOMException;

import y.anim.AnimationFactory;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.layout.BufferedLayouter;
import y.layout.GraphLayout;
import y.layout.transformer.GraphTransformer;
import y.util.GraphCopier;
import y.util.GraphHider;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LayoutMorpher;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ViewMode;
import y.view.hierarchy.HierarchyManager;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NotationException;
import org.helm.notation.NucleotideFactory;
import org.helm.notation.StructureException;
import org.helm.editor.action.ShowMolecularStructureAction;
import org.helm.editor.action.ConvertNotationAction;
import org.helm.editor.action.FileMenuAction;
import org.helm.editor.action.TextMenuAction;
import org.helm.editor.action.MoleculePropertyAction;
import org.helm.editor.action.MonomerManagerAction;
import org.helm.editor.action.NucleotideManagerAction;
import org.helm.editor.action.OligonucleotideFragmentAction;
import org.helm.editor.action.ProteinEditorAction;
import org.helm.editor.action.ReplaceMonomerAction;
import org.helm.editor.action.ShowChooseUIDialog;
import org.helm.editor.componentPanel.LoadPanel;
import org.helm.editor.controller.ModelController;
import org.helm.editor.data.Annotator;
import org.helm.editor.data.DataListener;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.GraphData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.GraphManagerInfo;
import org.helm.editor.data.GraphPair;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.data.NotationUpdateEvent;
import org.helm.editor.monomerui.PropertyManager;
import org.helm.editor.monomerui.UIConstructor;
import org.helm.editor.monomerui.tabui.PolymerUI;
import org.helm.editor.realizer.MonomerNodeRealizer;
import org.helm.editor.utility.ClipBoardProcessor;
import org.helm.editor.utility.ExceptionHandler;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.GraphUtils;
import org.helm.editor.utility.ListNotComparableException;
import org.helm.editor.utility.MonomerInfoUtils;
import org.helm.editor.utility.NotationParser;
import org.helm.editor.utility.SaveAsPNG;
import org.helm.editor.utility.SequenceGraphTools;
import org.helm.editor.utility.SequenceLayout;
import org.helm.editor.utility.xmlparser.data.Template.UIType;
import org.helm.editor.utility.xmlparser.parser.TemplateParsingException;
import org.helm.editor.utility.xmlparser.validator.ValidationTemplateExcaption;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.model.Nucleotide;
import org.helm.notation.tools.ComplexNotationParser;
import org.helm.notation.tools.NucleotideSequenceParser;

public class MacromoleculeEditor extends GUIBase implements DataListener, NotationProducer, IMacromoleculeEditor {

    private JTabbedPane tabbedPane;
    private String[] tabTitles;
    private String[] boldTabTitles;
    public final static byte DROP_COMPLETE_EVENT = (byte) 110;
    protected BufferedImage bufferedImage;
    private boolean regularConnection = true;
    private JToggleButton regularConnectionButton;
    private JToggleButton pairConnectionButton;
    private double zoom = 0.7;
    private Point2D zoomingAreaCenter;
    public static String SENSE = "ss";
    public static String ANTISENSE = "as";
    public static String HEAVY_CHAIN = "hc";
    public static String LIGHT_CHAIN = "lc";
    private static final int DIVIDER_LOCATION = 250;
    private UIConstructor uiConstructor;
    /**
     * general pop up menu
     */
    private JPopupMenu popup;
    /**
     * generate the complementary strand for selected base node
     */
    private JMenuItem complementItem;
    /**
     * generate the complete complementary strand for a sequence with a
     * specified starting node
     */
    private JMenuItem completeComplementaryStrandItem;
    private JMenuItem flipHorizontalItem;
    private JMenuItem flipVerticalItem;
    private JMenuItem rotateItem;
    private JMenuItem editChemicalModifierItem;
    /**
     * popup menu for user to anotate a nuceitide sequence as sense or anti
     * sense
     */
    private JPopupMenu nucleotideAnnotationNodePopup;
    /**
     * popup menu for user to anotate a peptide sequence as sense or anti sense
     */
    private JPopupMenu peptideAnnotationNodePopup;
    /**
     * popup menu allow user insert a nucleotide node
     */
    private JPopupMenu insertNCNodePopup;
    /**
     * popup for append nc node at the end
     */
    private JPopupMenu appendEndNCNodPopup;
    private Node currentAnotateStartingNode = null;
    // the edge that insertion will occur
    private Edge insertEdge;
    private Node nucleotideAppendingNode;
    private JMenuBar menuBar;    //for structure view
//    private MViewPane mvPane;
//    private JFrame substructureView;
    // actions for append A C G U T and phosphate
    private AbstractAction append_A_Action;
    private AbstractAction append_T_Action;
    private AbstractAction append_C_Action;
    private AbstractAction append_U_Action;
    private AbstractAction append_G_Action;
    private AbstractAction append_P_Action;
    private AbstractAction append_dT_Action;
    private JPopupMenu firstNCNodePopup;
    /**
     * append at the front
     */
    private JMenu firstNCSubMenu;
    /**
     * append at the end
     */
    private JMenu appendEndSubMenu;
    private SequenceLayout sequenceLayout;
    private MonomerInfoDialog monomerInfoDialog;
    private PropertyManager userProperty;
    private UIType type;
//	private JMenuItem showSelectedStructItem;

    public MacromoleculeEditor() throws MonomerException, IOException, JDOMException, NotationException {
        NucleotideFactory.getInstance().getNucleotideTemplates();

        userProperty = PropertyManager.getInstance();
        try {
            uiConstructor = UIConstructor.getInstance();
        } catch (Exception ex) {
            throw new NotationException("Unable to initialize monomer UI template manager", ex);
        }
        uiConstructor.bind(view, this);
        uiConstructor.initUIInstances();


        init();
        sequenceLayout = new SequenceLayout(view, graphManager);

        monomerInfoDialog = MonomerInfoDialog.getDialog(getFrame());//new MonomerInfoDialog(getFrame());

        ModelController.getInstance().registerListener(this);
        ModelController.getInstance().registerListener(tabbedSequenceViewPanel);
//        zoom = 0.7;
    }

    public MacromoleculeEditor(String notation)
            throws MonomerException, JDOMException, IOException, NotationException {
        this();
        ModelController.notationUpdated(notation, _ownerCode);
    }

    public void setupUIType(UIType type, String uiXmlPath) throws ValidationTemplateExcaption, TemplateParsingException {

        uiConstructor.setupUIXml(uiXmlPath);

        setupUIType(type);
    }

    public void setupUIType(UIType type) {

        if (contentPane.getComponentCount() > 1 && contentPane.getComponent(1) != null) {
            contentPane.remove(1);
        }


        JSplitPane viewSpiltPane = getViewSplitPane();
        if (type.equals(UIType.TAB)) {
            JTabbedPane tabbedPane = constructTabbedPane();

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, viewSpiltPane);
            splitPane.setDividerLocation(DIVIDER_LOCATION);
            tabbedPane.setMinimumSize(new Dimension(DIVIDER_LOCATION - 5, 0));
            contentPane.add(splitPane);
            this.type = UIType.TAB;
        } else if (type.equals(UIType.TREE)) {
            try {
                JTree treePane = constructTreePane();

                JScrollPane treeScrollPane = new JScrollPane(treePane);
                treeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, viewSpiltPane);
                splitPane.setDividerLocation(DIVIDER_LOCATION);

                contentPane.add(splitPane);
                this.type = UIType.TREE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        contentPane.validate();

        if (frame != null) {
            frame.validate();
            frame.pack();
        }

    }

    private void runLayout() throws MonomerException, JDOMException, IOException, NotationException {
        if (view.getGraph2D().isEmpty()) {
            return;
        }

        Graph2NotationTranslator.updateHyperGraph(view.getGraph2D(), graphManager);

        sequenceLayout = new SequenceLayout(view, graphManager);
        sequenceLayout.doLayout();
    }

    private void init() {

        Graph2D graph = view.getGraph2D();
        graph.setDefaultNodeRealizer(new MonomerNodeRealizer());

        if (HierarchyManager.getInstance(graph) == null) {
            new HierarchyManager(graph);
        }

        //NodeMap for monomer data properties
        NodeMap monoPropertiesNodeMap = graph.createNodeMap();
        graph.addDataProvider(NodeMapKeys.MONOMER_REF, monoPropertiesNodeMap);

        //the node and its associated parent hyper node
        graph.addDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE, graph.createNodeMap());

        //add monomerMap for new monomers only
        EdgeMap edgeMap = graph.createEdgeMap();
        graph.addDataProvider(EdgeMapKeys.EDGE_INFO, edgeMap);

        UIType type = userProperty.getUIType();
        setupUIType(type);

        initNodePopUp();

        //----------------------------------------------------------------
        initInsertNucleotideMenu();

        initAppendActionsMenu();

        initNucleotideAnnotationMenu();
        initPeptideAnnotationMenu();

        completeComplementaryStrandItem = new JMenuItem("Get Complementary Strand");
        completeComplementaryStrandItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateCompSequence(currentAnotateStartingNode);
                currentAnotateStartingNode = null;
            }
        });



        initFirstNodePopup();


        //////////////////////////////////////////////////////////////////////

        //add the node property editor
        view.addViewMode(new ViewMode() {
            @Override
            public void mousePressed(MouseEvent ev) {
                if (ev.getButton() == MouseEvent.BUTTON3) {
                    try {
                        currentAnotateStartingNode = null;
                        Node v = getHitInfo(ev).getHitNode();
                        NodeLabel label = getHitInfo(ev).getHitNodeLabel();
                        Edge edge = getHitInfo(ev).getHitEdge();
                        final Graph2D graph = view.getGraph2D();

                        if (label != null && (label.getText().contains("5'") || label.getText().contains("n"))) {
                            currentAnotateStartingNode = label.getNode();

                            if (!SequenceGraphTools.isFirstNucleicacidBackbone(currentAnotateStartingNode)) {
                                NodeCursor predecessor = currentAnotateStartingNode.predecessors();
                                Node preNode = null;
                                for (; predecessor.ok(); predecessor.next()) {
                                    preNode = predecessor.node();
                                    if (SequenceGraphTools.isFirstNucleicacidBackbone(preNode)) {
                                        currentAnotateStartingNode = preNode;
                                        break;
                                    }
                                }
                            }
                            if (label.getText().contains("5'")) {
                                nucleotideAnnotationNodePopup.show(ev.getComponent(), ev.getX(), ev.getY());
                            } else { // contains "n"
                                peptideAnnotationNodePopup.show(ev.getComponent(), ev.getX(), ev.getY());
                            }
                        } else if (v != null && SequenceGraphTools.isFirstNucleicacidBackbone(v)) {
                            currentAnotateStartingNode = v;
                            if (!hasComplementary(currentAnotateStartingNode)) {
                                completeComplementaryStrandItem.setEnabled(false);
                            } else {
                                completeComplementaryStrandItem.setEnabled(true);
                            }
                            allowAppendNucleotide(v);
                            firstNCNodePopup.show(ev.getComponent(), ev.getX(), ev.getY());
                        } else if (v != null && SequenceGraphTools.isLastNucleicacidBackbone(v)) {
                            allowAppendNucleotide(v);
                            appendEndNCNodPopup.setEnabled(true);
                            appendEndNCNodPopup.show(ev.getComponent(), ev.getX(), ev.getY());
                        } else if (edge != null) {
                            if (allowInsertNucleotide(edge)) {
                                insertNCNodePopup.setEnabled(true);
                                insertNCNodePopup.show(ev.getComponent(), ev.getX(), ev.getY());
                            }
                        } else {
                            if (hasComplementary()) {
                                complementItem.setEnabled(true);
                            } else {
                                complementItem.setEnabled(false);
                            }
                            if (view.getGraph2D().selectedNodes().size() == 0) {
                                flipHorizontalItem.setEnabled(false);
                                flipVerticalItem.setEnabled(false);
                                rotateItem.setEnabled(false);
//                                showSelectedStructItem.setEnabled(false);
                            } else {
                                flipHorizontalItem.setEnabled(true);
                                flipVerticalItem.setEnabled(true);
                                rotateItem.setEnabled(true);
//                                showSelectedStructItem.setEnabled(true);
                            }
                            popup.show(ev.getComponent(), ev.getX(), ev.getY());

                            if (SequenceGraphTools.isChemicalModifier(v)) {
                                editChemicalModifierItem.setEnabled(true);
                            } else {
                                editChemicalModifierItem.setEnabled(false);
                            }
                            if (v != null) {
                                graph.setSelected(v, true);
                            }

                            graph.updateViews();
                        }
                    } catch (MonomerException ex) {
                        Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JDOMException ex) {
                        Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
                if (ev.getClickCount() == 2) {
                    Node v = getHitInfo(ev).getHitNode();
                    final Graph2D graph = view.getGraph2D();
                    NodeCursor nodes = graph.selectedNodes();
                    //clear current selections
                    for (; nodes.ok(); nodes.next()) {
                        graph.setSelected(nodes.node(), false);
                    }
                    graph.updateViews();

                    if (v != null) {
                        try {
                            graph.setSelected(v, true);

                            final NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
                            final MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(v);
                            MonomerFactory monomerFactory = MonomerFactory.getInstance();
                            Map<String, Map<String, Monomer>> monomerDB = monomerFactory.getMonomerDB();

                            Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
                            monomerInfoDialog.setMonomer(monomer);
                            monomerInfoDialog.setVisible(true);
                        } catch (Exception ex) {
                            Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.INFO, "Unable to display monomer info");
                        }
                    }
                }
            }
        });
    }

    private void initFirstNodePopup() {
        firstNCNodePopup = new JPopupMenu("First Node Menu");

//        annotationNodePopup.addSeparator();
        firstNCNodePopup.add(completeComplementaryStrandItem);

        firstNCNodePopup.addSeparator();
        firstNCSubMenu = new JMenu("Append ...");
        firstNCSubMenu.add(append_A_Action);
        firstNCSubMenu.add(append_C_Action);
        firstNCSubMenu.add(append_G_Action);
        firstNCSubMenu.add(append_U_Action);
        firstNCSubMenu.add(append_T_Action);
        firstNCSubMenu.add(append_dT_Action);
        firstNCSubMenu.addSeparator();
        firstNCSubMenu.add(append_P_Action);

        firstNCNodePopup.add(firstNCSubMenu);
    }

    private void initNodePopUp() {
        popup = new JPopupMenu();

        complementItem = new JMenuItem("Get Complementary");
        complementItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateCompSequence();
            }
        });

        flipHorizontalItem = new JMenuItem("Flip Horizontally");
        flipHorizontalItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flipHorizontal(view);
            }
        });

        flipVerticalItem = new JMenuItem("Flip Vertically");
        flipVerticalItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flipVertical();
            }
        });

        rotateItem = new JMenuItem("Rotate 180");
        rotateItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rotate180();
            }
        });

        JMenuItem clearHydrogenBondItem = new JMenuItem("Clear all Hydrogen Bond");
        clearHydrogenBondItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearHydrogenBond();
            }
        });

        JMenuItem removeAllLastPNodeItem = new JMenuItem("Remove all 3' Phosphate");
        removeAllLastPNodeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeAllLastPhosphate();
            }
        });

        editChemicalModifierItem = new JMenuItem("Edit Chemical Modifier");
        editChemicalModifierItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editChemicalStructure();
            }
        });

        final JMenu copyMenu = buildCopyMenu();
        final JMenu viewMenu = buildViewMenu();
        final JMenu saveMenu = buildSaveMenu();
        final JMenu molStructureMenu = buildMolecularStructureMenu();

        popup.add(complementItem);
        popup.addSeparator();
        popup.add(flipHorizontalItem);
        popup.add(flipVerticalItem);
        popup.add(rotateItem);
        popup.addSeparator();
        popup.add(clearHydrogenBondItem);

        popup.add(removeAllLastPNodeItem);
        popup.addSeparator();


        popup.add(viewMenu);
        popup.addSeparator();
        popup.add(copyMenu);
        popup.addSeparator();
        popup.add(saveMenu);
        popup.addSeparator();
        popup.add(molStructureMenu);
        popup.addSeparator();
        popup.add(editChemicalModifierItem);
    }

    private void initInsertNucleotideMenu() {
        insertNCNodePopup = new JPopupMenu("Insert a nucleotide ...");
        JMenuItem nc_AItem = new JMenuItem("A");
        nc_AItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("R(A)P");
            }
        });

        JMenuItem nc_CItem = new JMenuItem("C");
        nc_CItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("R(C)P");
            }
        });
        JMenuItem nc_GItem = new JMenuItem("G");
        nc_GItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("R(G)P");
            }
        });
        JMenuItem nc_UItem = new JMenuItem("U");
        nc_UItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("R(U)P");
            }
        });

        JMenuItem nc_TItem = new JMenuItem("T");

        nc_TItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("R(T)P");
            }
        });

        JMenuItem nc_dTItem = new JMenuItem("dT");
        nc_dTItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertNucleotide("[dR](T)P");
            }
        });

        JMenu insertNCSubMenu = new JMenu("Insert a Nuclotide ...");
        insertNCSubMenu.add(nc_AItem);
        insertNCSubMenu.add(nc_CItem);
        insertNCSubMenu.add(nc_GItem);
        insertNCSubMenu.add(nc_UItem);
        insertNCSubMenu.add(nc_TItem);
        insertNCSubMenu.add(nc_dTItem);

        insertNCNodePopup.add(insertNCSubMenu);
    }

    private void initAppendActionsMenu() {
        //---- append actions -----------------------------------------------
        append_A_Action = new AbstractAction("A") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("R(A)P");
            }
        };

        append_C_Action = new AbstractAction("C") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("R(C)P");
            }
        };

        append_G_Action = new AbstractAction("G") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("R(G)P");
            }
        };

        append_U_Action = new AbstractAction("U") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("R(U)P");
            }
        };

        append_T_Action = new AbstractAction("T") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("R(T)P");
            }
        };

        append_P_Action = new AbstractAction("Phosphate") {
            public void actionPerformed(ActionEvent e) {
                appendPhosphate();
                ModelController.notationUpdated(getNotation(), _ownerCode);
            }
        };

        append_dT_Action = new AbstractAction("dT") {
            public void actionPerformed(ActionEvent e) {
                appendNucleotide("[dR](T)P");
            }
        };
        //--------- append at the end popup --------------------------------------------
        appendEndNCNodPopup = new JPopupMenu("Append a nucleotide ...");
//        appendNCNodPopup.a

        appendEndSubMenu = new JMenu("Append ...");
        appendEndSubMenu.add(append_A_Action);
        appendEndSubMenu.add(append_C_Action);
        appendEndSubMenu.add(append_G_Action);
        appendEndSubMenu.add(append_U_Action);
        appendEndSubMenu.add(append_T_Action);
        appendEndSubMenu.add(append_dT_Action);
        appendEndSubMenu.addSeparator();
        appendEndSubMenu.add(append_P_Action);
        appendEndNCNodPopup.add(appendEndSubMenu);
    }

    private void initNucleotideAnnotationMenu() {
        nucleotideAnnotationNodePopup = new JPopupMenu("Anotate a nucleotide sequence");

        JMenuItem senseItem = new JMenuItem("Set As Sense");
        senseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, SENSE);
                currentAnotateStartingNode = null;
            }
        });


        JMenuItem antisenseItem = new JMenuItem("Set As Antisense");
        antisenseItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, ANTISENSE);
                currentAnotateStartingNode = null;
            }
        });

        JMenuItem clearItem = new JMenuItem("Clear Annotation");
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, "");
                currentAnotateStartingNode = null;
            }
        });

        nucleotideAnnotationNodePopup.add(senseItem);
        nucleotideAnnotationNodePopup.add(antisenseItem);
        nucleotideAnnotationNodePopup.add(clearItem);
    }

    private void initPeptideAnnotationMenu() {
        peptideAnnotationNodePopup = new JPopupMenu("Anotate a peptide sequence");

        JMenuItem heavyChainItem = new JMenuItem("Set As Heavy Chain");
        heavyChainItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, HEAVY_CHAIN);
                currentAnotateStartingNode = null;
            }
        });


        JMenuItem lightChainItem = new JMenuItem("Set As Light Chain");
        lightChainItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, LIGHT_CHAIN);
                currentAnotateStartingNode = null;
            }
        });

        JMenuItem clearItem = new JMenuItem("Clear Annotation");
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                annotate(currentAnotateStartingNode, "");
                currentAnotateStartingNode = null;
            }
        });

        peptideAnnotationNodePopup.add(heavyChainItem);
        peptideAnnotationNodePopup.add(lightChainItem);
        peptideAnnotationNodePopup.add(clearItem);
    }

    //
//	Set As Heavy Chain, will add 'hc' to notation as PEPTIDE#{hc}
//
//	b. Set As Light Chain, will add 'lc' to notation as PEPTIDE#{lc}
//
//	c. Clear Annotation, will remove it from notation
    private JTree constructTreePane() throws MonomerException, IOException, JDOMException {
        JTree xmlTree = uiConstructor.getTree();
        JScrollPane verticalTreeScrollPane = new JScrollPane(xmlTree);
        verticalTreeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return xmlTree;
    }

    private JTabbedPane constructTabbedPane() {
        List<PolymerUI> panels = null;
        try {
            panels = uiConstructor.getPanels();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        int tabCount = panels.size();
        tabTitles = new String[tabCount];
        boldTabTitles = new String[tabCount];
        tabbedPane = new JTabbedPane();
        for (int i = 0; i < tabCount; i++) {
            PolymerUI currPanel = panels.get(i);
            tabTitles[i] = currPanel.getTitle();
            boldTabTitles[i] = "<html><b>" + tabTitles[i] + "</b></html>";

            tabbedPane.add(tabTitles[i], currPanel);
            tabbedPane.setForegroundAt(i, currPanel.getTitleColor());
        }

        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                int selectedIndex = pane.getSelectedIndex();
                for (int i = 0; i < pane.getTabCount(); i++) {
                    if (i == selectedIndex) {
                        pane.setTitleAt(i, boldTabTitles[i]);
                    } else {
                        pane.setTitleAt(i, tabTitles[i]);
                    }
                }

            }
        });

        int selected = tabbedPane.getSelectedIndex();
        tabbedPane.setTitleAt(selected, boldTabTitles[selected]);

        return tabbedPane;
    }

    private JMenu buildCopyMenu() {
        JMenu menu = new JMenu("Copy ...");

        TextMenuAction notationAtction = new TextMenuAction(this, TextMenuAction.NOTATION_TEXT_TYPE, TextMenuAction.COPY_ACTION_TYPE);
        menu.add(notationAtction);

        TextMenuAction smiAtction = new TextMenuAction(this, TextMenuAction.SMILES_TEXT_TYPE, TextMenuAction.COPY_ACTION_TYPE);
        menu.add(smiAtction);

        TextMenuAction molAtction = new TextMenuAction(this, TextMenuAction.MOLFILE_TEXT_TYPE, TextMenuAction.COPY_ACTION_TYPE);
        menu.add(molAtction);

        TextMenuAction pdbAtction = new TextMenuAction(this, TextMenuAction.PDB_TEXT_TYPE, TextMenuAction.COPY_ACTION_TYPE);
        menu.add(pdbAtction);

        AbstractAction imageAtction = new AbstractAction("Image") {
            public void actionPerformed(ActionEvent e) {
                ClipBoardProcessor.copy(SaveAsPNG.getBufferedImageFromImage(view.getImage()));
            }
        };
        menu.add(imageAtction);

        return menu;
    }

    private JMenu buildViewMenu() {
        JMenu menu = new JMenu("View ...");

        TextMenuAction notationAtction = new TextMenuAction(this, TextMenuAction.NOTATION_TEXT_TYPE, TextMenuAction.SHOW_ACTION_TYPE);
        menu.add(notationAtction);

        TextMenuAction smiAtction = new TextMenuAction(this, TextMenuAction.SMILES_TEXT_TYPE, TextMenuAction.SHOW_ACTION_TYPE);
        menu.add(smiAtction);

        TextMenuAction molAtction = new TextMenuAction(this, TextMenuAction.MOLFILE_TEXT_TYPE, TextMenuAction.SHOW_ACTION_TYPE);
        menu.add(molAtction);

        TextMenuAction pdbAtction = new TextMenuAction(this, TextMenuAction.PDB_TEXT_TYPE, TextMenuAction.SHOW_ACTION_TYPE);
        menu.add(pdbAtction);

        MoleculePropertyAction propertyAction = new MoleculePropertyAction(this);
        menu.add(propertyAction);

        return menu;
    }

    private JMenu buildSaveMenu() {
        JMenu menu = new JMenu("Save ...");

        TextMenuAction notationAtction = new TextMenuAction(this, TextMenuAction.NOTATION_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(notationAtction);

        TextMenuAction smiAtction = new TextMenuAction(this, TextMenuAction.SMILES_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(smiAtction);

        TextMenuAction molAtction = new TextMenuAction(this, TextMenuAction.MOLFILE_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(molAtction);

        TextMenuAction pdbAtction = new TextMenuAction(this, TextMenuAction.PDB_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(pdbAtction);

        return menu;
    }

    private JMenu buildFileSaveMenu() {
        JMenu menu = new JMenu("Save ...");

        FileMenuAction notationAtction = new FileMenuAction(this, TextMenuAction.NOTATION_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(notationAtction);

        FileMenuAction smiAtction = new FileMenuAction(this, TextMenuAction.SMILES_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(smiAtction);

        FileMenuAction molAtction = new FileMenuAction(this, TextMenuAction.MOLFILE_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(molAtction);

        FileMenuAction pdbAtction = new FileMenuAction(this, TextMenuAction.PDB_TEXT_TYPE, TextMenuAction.SAVE_ACTION_TYPE);
        menu.add(pdbAtction);

        return menu;
    }

    private JMenu buildMolecularStructureMenu() {
        JMenu menu = new JMenu("Show Molecular Structure");

        ChemicalStructureViewer csViewer = ChemicalStructureViewer.getInstance(getFrame());

        ShowMolecularStructureAction allAction = new ShowMolecularStructureAction(this, csViewer, ShowMolecularStructureAction.ALL_STRUCTURE_TYPE);
        menu.add(allAction);
        ShowMolecularStructureAction selectedAction = new ShowMolecularStructureAction(this, csViewer, ShowMolecularStructureAction.SELECTED_STRUCTURE_TYPE);
        menu.add(selectedAction);

        return menu;
    }

    public String getSelectedNotation() throws IOException, MonomerException, JDOMException, NotationException, ClassNotFoundException, StructureException {
        Graph2D graph = view.getGraph2D();
        GraphData graphData = GraphSelection.getSelectedGraph(graph);
        Graph2D selectedStructure = graphData.getGraph();

        GraphManagerInfo selectedGraphManagerData = null;
        selectedGraphManagerData = GraphSelection.getSelectedGraphManager(graphData);

        GraphManager selectedGraphManager = selectedGraphManagerData.getGraphManager();

        Graph2NotationTranslator.updateHyperGraph(selectedStructure, selectedGraphManager);

        return Graph2NotationTranslator.getNewNotation(selectedGraphManager);
    }

    /**
     * appending a phosphate node at the end/beginning of a sequence
     *
     * @return the new phosphate node
     */
    private Node appendPhosphate() {
        try {
            String annotation = null;
            boolean isStarting = false;
            if (SequenceGraphTools.isFirstNucleicacidBackbone(nucleotideAppendingNode) && !SequenceGraphTools.isLastNucleicacidBackbone(nucleotideAppendingNode)) {
                isStarting = true;
                annotation = graphManager.getAnnotation(nucleotideAppendingNode);
            }

            Node targetNode = SequenceGraphTools.appendPhosphate(nucleotideAppendingNode);
            int index = -1;
            //if append at the begining, we need to update the graph manager and annotation
            if (isStarting && targetNode != null && annotation != null) {
                index = graphManager.getIndex(nucleotideAppendingNode);
                graphManager.removeStartingNode(nucleotideAppendingNode);
                graphManager.addStartingNode(index, targetNode);
                graphManager.annotate(targetNode, annotation);
            }

//            ModelController.notationUpdated(getNotation(), _ownerCode);

            return targetNode;
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }

        return null;
    }

    /**
     * append a nucleotide to the end/begining of a nucleotide
     *
     * @param notation: the polymer notation
     *
     */
    private void appendNucleotide(String notation) {
        try {
            Graph2D graph = view.getGraph2D();
            GraphCopier copier = SequenceGraphTools.getGraphCopier(graph);
            NodeMap monomerInfoMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
            MonomerInfo oldNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(nucleotideAppendingNode);
            EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
            //add the new nucleotide
            NodeCursor newNodes = copier.copy(NotationParser.createNucleotideGraph(notation), graph).nodes();
            Node newRNode = null;
            Node newPNode = null;
            MonomerInfo monomerInfo = null;
            Monomer monomer = null;
            //test if the nucleotideAppendingNode is a R node
            Monomer oldNodeMonomer = GraphUtils.getMonomerDB().get(oldNodeMonomerInfo.getPolymerType()).get(oldNodeMonomerInfo.getMonomerID());
            for (; newNodes.ok(); newNodes.next()) {
                monomerInfo = (MonomerInfo) monomerInfoMap.get(newNodes.node());
                monomer = GraphUtils.getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
                if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
                    newRNode = newNodes.node();
                } else if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    newPNode = newNodes.node();
                }
            }
            if (SequenceGraphTools.isLastNucleicacidBackbone(nucleotideAppendingNode)) {
                if (oldNodeMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
                    nucleotideAppendingNode = appendPhosphate();
                    oldNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(nucleotideAppendingNode);
                }
                MonomerInfo newRNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(newRNode);
                Attachment sourceAttachment = oldNodeMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
                Attachment targetAttachment = newRNodeMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
                if (!oldNodeMonomerInfo.isConnected(sourceAttachment)) {
                    Edge newEdge = graph.createEdge(nucleotideAppendingNode, newRNode);
                    oldNodeMonomerInfo.setConnection(sourceAttachment, true);
                    newRNodeMonomerInfo.setConnection(targetAttachment, true);
                    edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
                }
                nucleotideAppendingNode = null;
                //remove the last phosphate
                if (newPNode != null) {
                    Edge edge = newPNode.inEdges().edge();
//                Node pNode = edge.source();
                    oldNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(edge.source());
                    newRNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(edge.target());
                    EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap.get(edge);
                    oldNodeMonomerInfo.setConnection(edgeInfo.getSourceNodeAttachment(), false);
                    newRNodeMonomerInfo.setConnection(edgeInfo.getTargetNodeAttachment(), false);
                    graph.removeNode(newPNode);
                }
            } else if (SequenceGraphTools.isFirstNucleicacidBackbone(nucleotideAppendingNode)) {
                String annotation = null;
                int index = -1;
                if (oldNodeMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    annotation = graphManager.getAnnotation(nucleotideAppendingNode);
                    Node newStarting = null;

                    //we need to remove the first P node
                    NodeCursor successor = nucleotideAppendingNode.successors();
                    for (; successor.ok(); successor.next()) {
                        oldNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(successor.node());
                        if (oldNodeMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                            oldNodeMonomer = GraphUtils.getMonomerDB().get(Monomer.NUCLIEC_ACID_POLYMER_TYPE).get(oldNodeMonomerInfo.getMonomerID());
                            if (oldNodeMonomer.getMonomerType().equalsIgnoreCase(Monomer.BACKBONE_MOMONER_TYPE) && oldNodeMonomer.getNaturalAnalog().equals(Monomer.ID_R)) {
                                newStarting = successor.node();
                                break;
                            }
                        }
                    }

                    index = graphManager.getIndex(nucleotideAppendingNode);
                    graphManager.removeStartingNode(nucleotideAppendingNode);
                    deleteNode(nucleotideAppendingNode);
                    graphManager.addStartingNode(index, newStarting);
                    graphManager.annotate(newStarting, annotation);

                    nucleotideAppendingNode = newStarting;
                }
                MonomerInfo newPNodeMonomerInfo = (MonomerInfo) monomerInfoMap.get(newPNode);
                Attachment sourceAttachment = newPNodeMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
                Attachment targetAttachment = oldNodeMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
                if (sourceAttachment != null && !oldNodeMonomerInfo.isConnected(targetAttachment)) {
                    Edge newEdge = graph.createEdge(newPNode, nucleotideAppendingNode);
                    oldNodeMonomerInfo.setConnection(targetAttachment, true);
                    newPNodeMonomerInfo.setConnection(sourceAttachment, true);
                    edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));
                }

                annotation = graphManager.getAnnotation(nucleotideAppendingNode);
                index = graphManager.getIndex(nucleotideAppendingNode);
                graphManager.removeStartingNode(nucleotideAppendingNode);
                graphManager.addStartingNode(index, newRNode);
                graphManager.annotate(newRNode, annotation);
                nucleotideAppendingNode = null;
            }

            Graph2NotationTranslator.updateHyperGraph(graph, graphManager);
            String notationForUpdate = Graph2NotationTranslator.getNewNotation(graphManager);

            ModelController.notationUpdated(notationForUpdate, _ownerCode);
        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }

    }

    /**
     * delete a given node from the display, update the connection attachment.
     * This function does not update the graph manager and annotation
     *
     * @param node
     */
    private void deleteNode(Node node) {
        Graph2D graph = view.getGraph2D();
        EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);

        EdgeCursor edges = node.inEdges();
        EditorEdgeInfoData edgeInfo = null;
        Node neighborNode;
        MonomerInfo neighborMonomerInfo = null;

        Attachment att = null;
        for (; edges.ok(); edges.next()) {
            edgeInfo = (EditorEdgeInfoData) edgeMap.get(edges.edge());
            neighborNode = edges.edge().source();
            neighborMonomerInfo = (MonomerInfo) nodeMap.get(neighborNode);
            att = edgeInfo.getSourceNodeAttachment();
            neighborMonomerInfo.setConnection(att, false);
        }

        edges = node.outEdges();
        for (; edges.ok(); edges.next()) {
            edgeInfo = (EditorEdgeInfoData) edgeMap.get(edges.edge());
            neighborNode = edges.edge().target();
            neighborMonomerInfo = (MonomerInfo) nodeMap.get(neighborNode);
            att = edgeInfo.getTargetNodeAttachment();
            neighborMonomerInfo.setConnection(att, false);

        }
        graph.removeNode(node);

    }

    /**
     * test if append action is allowed. If current node is a Phosphate node,
     * then the append phosphate item will be disabled
     *
     * @param node
     * @return
     */
    private boolean allowAppendNucleotide(Node node) throws MonomerException, IOException, JDOMException {
        final Graph2D graph = view.getGraph2D();
        NodeMap monomerInfoMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(node);
        Monomer monomer = GraphUtils.getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());


        if (SequenceGraphTools.isLastNucleicacidBackbone(node)) {
            if (!monomerInfo.isConnected(monomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT))) {
                nucleotideAppendingNode = node;
                appendEndSubMenu.setEnabled(true);
                if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    append_P_Action.setEnabled(false);
                } else {
                    append_P_Action.setEnabled(true);
                }

                return true;
            } else {
                appendEndSubMenu.setEnabled(false);
            }

        }

        if (SequenceGraphTools.isFirstNucleicacidBackbone(node)) {
            if (!monomerInfo.isConnected(monomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT))) {
                nucleotideAppendingNode = node;
                firstNCSubMenu.setEnabled(true);
                if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    append_P_Action.setEnabled(false);
                } else {
                    append_P_Action.setEnabled(true);
                }

                return true;
            } else {
                firstNCSubMenu.setEnabled(false);
            }

        }
        return false;
    }

    /**
     * if the insertion is allowed between source and target node of this edge
     *
     * @param edge
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws MonomerException
     */
    private boolean allowInsertNucleotide(Edge edge) throws MonomerException, IOException, JDOMException {

        Node sourceNode = edge.source();
        Node targetNode = edge.target();
        final Graph2D graph = view.getGraph2D();
        NodeMap monomerInfoMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);

        MonomerInfo sourceMonomerInfo = (MonomerInfo) monomerInfoMap.get(sourceNode);
        MonomerInfo targetMonomerInfo = (MonomerInfo) monomerInfoMap.get(targetNode);

        Map<String, Map<String, Monomer>> monomerDB = GraphUtils.getMonomerDB();
        Monomer sourceMonomer = monomerDB.get(sourceMonomerInfo.getPolymerType()).get(sourceMonomerInfo.getMonomerID());
        Monomer targetMonomer = monomerDB.get(targetMonomerInfo.getPolymerType()).get(targetMonomerInfo.getMonomerID());
        if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE) && targetMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
            if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P) && targetMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
                insertEdge = edge;
                return true;
            } else if (sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R) && targetMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                NodeCursor successors = targetNode.successors();
                Node nextR = null;
                if (successors.ok()) {
                    nextR = successors.node();
                    MonomerInfo monomerInfo = (MonomerInfo) monomerInfoMap.get(nextR);
                    if (monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                        Monomer monomer = monomerDB.get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
                        if (monomer != null && monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
                            insertEdge = targetNode.getEdgeTo(nextR);
                            return true;
                        }
                    }
                }
            }
        } else if (sourceMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)
                && targetMonomerInfo.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)) {
            if (!sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                insertEdge = edge;
                return true;
            }
        }

        insertEdge = null;
        return false;

    }

    private void insertNucleotide(String notation) {
        try {
            if (insertEdge == null) {
                return;
            }

            Graph2D graph = view.getGraph2D();
            NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
            EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
            EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap.get(insertEdge);

            Node sourceNode = insertEdge.source();
            Node targetNode = insertEdge.target();

            MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
            MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);
            Monomer sourceMonomer = GraphUtils.getMonomerDB().get(sourceMonomerInfo.getPolymerType()).get(sourceMonomerInfo.getMonomerID());

            //if the source node is a R node and also the last backbone node in this nucleotide sequence
            boolean isLastRNode = SequenceGraphTools.isLastNucleicacidBackbone(sourceNode) && sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R);
            // if the source node is the first nucleoitide backbone node in a sequence
            boolean isFirstRNode = SequenceGraphTools.isFirstNucleicacidBackbone(sourceNode) && sourceMonomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R);
            //default is insert to the end of the node
            if (isFirstRNode && isLastRNode) {
                isFirstRNode = false;
            }

            if (isLastRNode) {
                sourceMonomerInfo.setConnection(edgeInfo.getSourceNodeAttachment(), false);
                targetMonomerInfo.setConnection(edgeInfo.getTargetNodeAttachment(), false);
                graph.removeEdge(insertEdge);
                //set up edge
                Node pNode = SequenceGraphTools.appendPhosphate(sourceNode);

                sourceNode = pNode;

                sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
                insertEdge = graph.createEdge(sourceNode, targetNode);

                EditorEdgeInfoData newEdgeInfo = new EditorEdgeInfoData(sourceMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT), edgeInfo.getTargetNodeAttachment());
                edgeMap.set(insertEdge, newEdgeInfo);
                edgeInfo = newEdgeInfo;
                isLastRNode = true;
            }


            GraphCopier copier = SequenceGraphTools.getGraphCopier(graph);
            //add the new nucleotide
            NodeCursor newNodes = copier.copy(NotationParser.createNucleotideGraph(notation), graph).nodes();
            sourceMonomerInfo.setConnection(edgeInfo.getSourceNodeAttachment(), false);
            targetMonomerInfo.setConnection(edgeInfo.getTargetNodeAttachment(), false);

            graph.removeEdge(insertEdge);

            MonomerInfo currentMonomerInfo = null;
            Edge newEdge = null;
            Attachment newAttchment = null;
            Monomer monomer = null;

            for (; newNodes.ok(); newNodes.next()) {
                currentMonomerInfo = (MonomerInfo) nodeMap.get(newNodes.node());
                monomer = GraphUtils.getMonomerDB().get(currentMonomerInfo.getPolymerType()).get(currentMonomerInfo.getMonomerID());
                if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_R)) {
                    newAttchment = currentMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT);
                    if (newAttchment != null && !currentMonomerInfo.isConnected(newAttchment)) {
                        if (isFirstRNode) {
                            newEdge = graph.createEdge(newNodes.node(), targetNode);
                            currentMonomerInfo.setConnection(newAttchment, true);
                            edgeMap.set(newEdge, new EditorEdgeInfoData(newAttchment, edgeInfo.getTargetNodeAttachment()));
                            String annotation = graphManager.getAnnotation(sourceNode);
                            int index = graphManager.getIndex(sourceNode);
                            graphManager.removeStartingNode(sourceNode);
                            graphManager.addStartingNode(index, newNodes.node());
                            graphManager.annotate(newNodes.node(), annotation);
                        } else {
                            newEdge = graph.createEdge(sourceNode, newNodes.node());
                            currentMonomerInfo.setConnection(newAttchment, true);
                            edgeMap.set(newEdge, new EditorEdgeInfoData(edgeInfo.getSourceNodeAttachment(), newAttchment));
                        }
                    }
                    if (isLastRNode) {
                        Node rNode = newNodes.node();
                        SequenceGraphTools.removeLastPhosphate(rNode);

                        newEdge = graph.createEdge(rNode, targetNode);
                        newAttchment = currentMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);
                        currentMonomerInfo.setConnection(newAttchment, true);
                        edgeMap.set(newEdge, new EditorEdgeInfoData(newAttchment, edgeInfo.getTargetNodeAttachment()));
                        break;

                    }

                } else if (monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    newAttchment = currentMonomerInfo.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT);

                    if (newAttchment != null && !currentMonomerInfo.isConnected(newAttchment)) {
                        if (isFirstRNode) {
                            newEdge = graph.createEdge(newNodes.node(), sourceNode);
                            currentMonomerInfo.setConnection(newAttchment, true);
                            edgeMap.set(newEdge, new EditorEdgeInfoData(newAttchment, edgeInfo.getSourceNodeAttachment()));
                        } else {
                            newEdge = graph.createEdge(newNodes.node(), targetNode);
                            currentMonomerInfo.setConnection(newAttchment, true);
                            edgeMap.set(newEdge, new EditorEdgeInfoData(newAttchment, edgeInfo.getTargetNodeAttachment()));
                        }
                    }
                }
            }

            insertEdge = null;

            Graph2NotationTranslator.updateHyperGraph(graph, graphManager);
            String updatedNotation = Graph2NotationTranslator.getNewNotation(graphManager);

            ModelController.notationUpdated(updatedNotation, _ownerCode);

        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }
    }

    /**
     * remove all 3' phosphate
     */
    private void removeAllLastPhosphate() {
        List<Node> startingNodeList = graphManager.getStartingNodeList();
        Node startingNode = null;

        Monomer monomer = null;
        int i = 0;
        try {
            while (i < startingNodeList.size()) {
                startingNode = startingNodeList.get(i);
                SequenceGraphTools.removeLastPhosphate(startingNode);
                if (monomer != null && monomer.getNaturalAnalog().equalsIgnoreCase(Monomer.ID_P)) {
                    graphManager.removeStartingNode(startingNode);

                }

                i++;
            }

            insertEdge = null;

            ModelController.notationUpdated(getNotation(), _ownerCode);
        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }

    }

    private void editChemicalStructure() {
        Graph2D graph = view.getGraph2D();
        NodeCursor selectedNodes = graph.selectedNodes();
        if (selectedNodes.size() != 1) {
            return;
        }

        try {
            NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
            MonomerInfo monomerInfo = (MonomerInfo) nodeMap.get(selectedNodes.node());
            StructureFrame.showDialog(this, monomerInfo);
        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }
    }

    /**
     * anotate a nucleotide to be sense or antisense
     *
     * @param anotation
     */
    private void annotate(Node startingNode, String anotation) {
        try {
            if (!anotation.equalsIgnoreCase(graphManager.getAnnotation(startingNode))) {
                graphManager.annotate(startingNode, anotation);

                ModelController.notationUpdated(getNotation(), _ownerCode);
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
     * flip the selected sequence horizontally
     */
    public static void flipHorizontal(Graph2DView view) { // the graph to rotate
        AnimationPlayer animationPlayer = new AnimationPlayer();
        Graph2D graph = view.getGraph2D();
        if (graph.selectedNodes().size() == 0) {
            return;
        }

        GraphHider graphHider = new GraphHider(graph);
        preFlip(graphHider, graph);

        GraphTransformer graphTransformer = new GraphTransformer();
        graphTransformer.setOperation(GraphTransformer.MIRROR_YAXIS);

        GraphLayout layout = (new BufferedLayouter(graphTransformer)).calcLayout(view.getGraph2D());

        // apply the geometric info in an animated fashion
        LayoutMorpher morpher = new LayoutMorpher(view, layout);
        morpher.setPreferredDuration(25);
        morpher.setKeepZoomFactor(true);
        morpher.execute();

        AnimationObject easedLM = AnimationFactory.createEasedAnimation(morpher);

        animationPlayer.animate(easedLM);
        postFlip(graphHider, view);
    }

    private void flipVertical() {
        // the graph to rotate
        Graph2D graph = view.getGraph2D();
        if (graph.selectedNodes().size() == 0) {
            return;
        }

        GraphHider graphHider = new GraphHider(graph);
        preFlip(graphHider, graph);

        GraphTransformer graphTransformer = new GraphTransformer();
        graphTransformer.setOperation(GraphTransformer.MIRROR_XAXIS);

        GraphLayout layout = (new BufferedLayouter(graphTransformer)).calcLayout(view.getGraph2D());

        // apply the geometric info in an animated fashion
        LayoutMorpher morpher = new LayoutMorpher(view, layout);
        morpher.setPreferredDuration(25);
        morpher.setKeepZoomFactor(true);
        morpher.execute();

        AnimationObject easedLM = AnimationFactory.createEasedAnimation(morpher);

        animationPlayer.animate(easedLM);
        postFlip(graphHider, view);
    }

    private void rotate180() {
        Graph2D graph = view.getGraph2D();
        if (graph.selectedNodes().size() == 0) {
            return;
        }

// calculate the geometric info of the rotated graph
        GraphTransformer graphTransformer = new GraphTransformer();
        graphTransformer.setOperation(GraphTransformer.ROTATE);
        graphTransformer.setRotationAngle(180);

        GraphHider graphHider = new GraphHider(graph);
        preFlip(graphHider, graph);

        GraphLayout layout = (new BufferedLayouter(graphTransformer)).calcLayout(view.getGraph2D());

        // apply the geometric info in an animated fashion
        LayoutMorpher morpher = new LayoutMorpher(view, layout);
        AnimationObject easedLM = AnimationFactory.createEasedAnimation(morpher);

        animationPlayer.animate(easedLM);

        postFlip(graphHider, view);
    }

    /**
     * clear all hydrogen bond
     */
    private void clearHydrogenBond() {
        Graph2D graph = view.getGraph2D();
        EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);

        EdgeCursor edges = graph.edges();
        Edge edge = null;
        EditorEdgeInfoData edgeInfo = null;

        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        MonomerInfo monomerInfo = null;

        for (; edges.ok(); edges.next()) {
            edge = edges.edge();
            edgeInfo =
                    (EditorEdgeInfoData) edgeMap.get(edge);
            if (edgeInfo.isPair()) {
                monomerInfo = (MonomerInfo) nodeMap.get(edge.source());
                monomerInfo.setConnection(edgeInfo.getSourceNodeAttachment(), false);

                monomerInfo =
                        (MonomerInfo) nodeMap.get(edge.target());
                monomerInfo.setConnection(edgeInfo.getTargetNodeAttachment(), false);

                graph.removeEdge(edge);
            }

        }

        try {
            Graph2NotationTranslator.updateHyperGraph(graph, graphManager);
            String notation = Graph2NotationTranslator.getNewNotation(graphManager);
            ModelController.notationUpdated(notation, _ownerCode);
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }

        view.updateView();
    }

    private static void preFlip(GraphHider graphHider, Graph2D graph) {
        NodeCursor nodes = graph.nodes();
        EdgeCursor edges = null;
        for (; nodes.ok(); nodes.next()) {
            if (!graph.isSelected(nodes.node())) {
                edges = nodes.node().edges();
                for (; edges.ok(); edges.next()) {
                    //if both the source and target node is not selected, hide it
                    if (!graph.isSelected(edges.edge().source())
                            && !graph.isSelected(edges.edge().target())) {
                        graphHider.hide(edges.edge());
                    }

                }
                graphHider.hide(nodes.node());
            }

        }
    }

    private static void postFlip(GraphHider graphHider, Graph2DView view) {
        graphHider.unhideAll();
        view.updateView();
        view.fitContent();
    }

    /**
     * test if the selected nodes are nucleic acid sequence and also if they
     * belongs to one sequence and they haven't pair up with someone else
     *
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws MonomerException
     */
    private boolean hasComplementary() throws MonomerException, IOException, JDOMException {
        final Graph2D graph = view.getGraph2D();
        NodeCursor selectedNodes = graph.selectedNodes();
        if (selectedNodes.size() == 0) {
            return false;
        }

        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        MonomerInfo monomerInfo = null;
        Monomer monomer = null;

        final NodeMap parentMap = (NodeMap) graph.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);
        Node hyperParentNode = null;
        boolean hasBaseNode = false;
        for (; selectedNodes.ok(); selectedNodes.next()) {
            monomerInfo = (MonomerInfo) nodeMap.get(selectedNodes.node());
            monomer = GraphUtils.getMonomerDB().get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
            if (!monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                return false;
            } else if (monomer != null && monomer.getMonomerType().equalsIgnoreCase(Monomer.BRANCH_MOMONER_TYPE)) {
                //if the base node has already paired up with some other base node
                if (monomerInfo.isConnected(monomerInfo.getAttachment(Attachment.PAIR_ATTACHMENT))) {
                    return false;
                }
                hasBaseNode = true;
            }

            if (hyperParentNode == null) {
                hyperParentNode = (Node) parentMap.get(selectedNodes.node());
            } else {
                //every selected nodes should belong to the same parent hyper node
                if (!hyperParentNode.equals((Node) parentMap.get(selectedNodes.node()))) {
                    return false;
                }
            }
        }
        if (hyperParentNode == null) {
            return false;
        }
        if (hasBaseNode) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * test if a strand starting with the given starting node CAN pair up with a
     * new complementary strand
     *
     * @param startingNode
     * @return true: if each of the base node has free hydrogen bond connection,
     * otherwise false.
     * @throws org.helm.notation.MonomerException
     * @throws org.jdom.JDOMException
     * @throws java.io.IOException
     */
    private boolean hasComplementary(Node startingNode) throws MonomerException, JDOMException, IOException {
        NodeList baseList = SequenceGraphTools.getBaseList(startingNode, view.getGraph2D(), false);
        Node base = null;
        final Graph graph = view.getGraph2D();
        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        MonomerInfo monomerInfo = null;

        while (!baseList.isEmpty()) {
            base = baseList.popNode();
            monomerInfo = (MonomerInfo) nodeMap.get(base);
            if (monomerInfo.isConnected(monomerInfo.getAttachment(Attachment.PAIR_ATTACHMENT))) {
                return false;
            }
        }

        return true;

    }

    /**
     * generate the complementary sequence for the selected sequence
     */
    private void generateCompSequence() {
        try {
            Graph2D graph = view.getGraph2D();

            final NodeMap parentMap = (NodeMap) graph.getDataProvider(NodeMapKeys.NODE2PARENT_HYPERNODE);

            NodeCursor selectedNodes = graph.selectedNodes();
            //use any selected node to get the hyper parent node. since it already been tested in other function
            Node hyperParentNode = (Node) parentMap.get(selectedNodes.node());
            Node oldSequenceStarting = null;

            //get the original sequence's starting node
            for (Node startingNode : graphManager.getStartingNodeList()) {
                if (hyperParentNode.equals((Node) parentMap.get(startingNode))) {
                    oldSequenceStarting = startingNode;
                    break;
                }
            }

            getComplementary(oldSequenceStarting, true);

        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    private void getComplementary(Node oldSequenceStarting, boolean selectedOnly) throws MonomerException, IOException,
            JDOMException, NotationException, ClassNotFoundException {

        Graph2D graph = view.getGraph2D();
        String annotationText = graphManager.getAnnotation(oldSequenceStarting);

        //get all bases in the current sequence in order
        NodeList baseList = SequenceGraphTools.getBaseList(oldSequenceStarting, graph, false);
        StringBuilder sb = new StringBuilder(baseList.size());
        Node currentNode = null;
        boolean start = false;
        NodeList pairingBaseList = new NodeList();
        //only consider the first set of consequent base node
        while (!baseList.isEmpty()) {
            currentNode = baseList.popNode();
            if (!selectedOnly || graph.isSelected(currentNode)) {
                if (MonomerInfoUtils.notModifiedMonomer(currentNode)) {
                    sb.append(MonomerInfoUtils.getNaturalAnalog(currentNode));
                    pairingBaseList.add(currentNode);
                    start = true;
                }

            } else {
                if (start) {
                    break;
                }

            }
        }

        loadFromNotation(annotationText, sb.toString(), pairingBaseList);
    }

    private void loadFromNotation(String annotationText,
            String notation, NodeList pairingBaseList)
            throws NotationException, IOException, JDOMException,
            MonomerException, ClassNotFoundException {
        String complementaryStrand;
        if (notation.length() > 0) {
            complementaryStrand = NucleotideSequenceParser.getNormalComplementSequence(notation);
            complementaryStrand =
                    complementaryStrand.replaceAll("-", "");
            complementaryStrand =
                    complementaryStrand.replaceAll("[0-9]", "");
            complementaryStrand =
                    complementaryStrand.replace("'", "");

            Graph2D graph = view.getGraph2D();
            GraphCopier copier = SequenceGraphTools.getGraphCopier(graph);

            // generating the complementary sequence
            Graph2D sequenceGraph = loadNucleiotideSequence(complementaryStrand);
            NodeCursor newNodes = copier.copy(sequenceGraph, view.getGraph2D()).nodes();
            newNodes.toFirst();

            Node newSequenceStarting = newNodes.node();
            SequenceGraphTools.removeLastPhosphate(newSequenceStarting);
            graphManager.addStartingNode(newSequenceStarting);
            pairSequences(pairingBaseList, SequenceGraphTools.getBaseList(newSequenceStarting, graph, true), graph);

            graph.unselectAll();

            Graph2NotationTranslator.updateHyperGraph(graph, graphManager);
            String commonNotation = Graph2NotationTranslator.getNewNotation(graphManager);

            if (annotationText != null) {
                if (annotationText.contains(ANTISENSE)) {
                    annotate(newSequenceStarting, SENSE);
                } else if (annotationText.contains(SENSE)) {
                    annotate(newSequenceStarting, ANTISENSE);
                }
            }

            //synchronizeZoom();
            ModelController.notationUpdated(commonNotation, _ownerCode);
        }
    }

    /**
     * generate the complementary strand for a sequence starting with
     * startingNode
     *
     * @param startingNode
     */
    private void generateCompSequence(Node startingNode) {
        try {
            getComplementary(startingNode, false);

        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    private void pairSequences(final NodeList sequence1NodeList,
            final NodeList sequence2NodeList,
            final Graph2D graph) {
        //the following two sequences should have the same size
        Node[] sequence1Nodes = sequence1NodeList.toNodeArray();
        Node[] sequence2Nodes = sequence2NodeList.toNodeArray();

        int size = sequence1Nodes.length < sequence2Nodes.length ? sequence1Nodes.length : sequence2Nodes.length;
        //find the starting point
        for (int i = 0; i < size; i++) {
            pairEdge(sequence1Nodes[i], sequence2Nodes[i]);
        }

    }

    private Edge pairEdge(Node sourceNode, Node targetNode) {
        final Graph2D graph = (Graph2D) sourceNode.getGraph();
        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
        final MonomerInfo sourceMonomerInfo = (MonomerInfo) nodeMap.get(sourceNode);
        final MonomerInfo targetMonomerInfo = (MonomerInfo) nodeMap.get(targetNode);

        Attachment sourceAttachment = sourceMonomerInfo.getAttachment(Attachment.PAIR_ATTACHMENT);
        Attachment targetAttachment = targetMonomerInfo.getAttachment(Attachment.PAIR_ATTACHMENT);

        if (!sourceMonomerInfo.isConnected(sourceAttachment) && !targetMonomerInfo.isConnected(targetAttachment)) {
            sourceMonomerInfo.setConnection(sourceAttachment, true);
            targetMonomerInfo.setConnection(targetAttachment, true);
            Edge newEdge = graph.createEdge(sourceNode, targetNode);
            edgeMap.set(newEdge, new EditorEdgeInfoData(sourceAttachment, targetAttachment));

            //set up the realizer
            EdgeRealizer edgeRealizer = graph.getRealizer(newEdge);
            edgeRealizer.setLineType(LineType.DOTTED_3);
            edgeRealizer.setLineColor(Color.BLUE);

            return newEdge;
        }

        return null;
    }

    /**
     * Called by the load sequence button to create a NC sequence
     *
     * @param sequence: String
     * @return A graph that contains the sequence
     */
    private Graph2D loadNucleiotideSequence(String sequence) {
        try {
            //translate the regular string notation like "AAAA" to a polymer notation "R(A)P.R(A)P.R(A)P.R(A)P"
            ArrayList<Nucleotide> nucleotideList = (ArrayList<Nucleotide>) NucleotideSequenceParser.getNormalList(sequence);
            StringBuilder polymerNotation = new StringBuilder();
            for (int i = 0; i < nucleotideList.size(); i++) {
                if (i > 0) {
                    polymerNotation.append(".");
                }

                polymerNotation.append(nucleotideList.get(i).getNotation());
            }

            Graph2D currentGraph = NotationParser.loadNucleicAcidMonomerSequence(polymerNotation.toString());

            return currentGraph;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Invalid Sequence!",
                    JOptionPane.WARNING_MESSAGE);
            Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, ex.getMessage());

            return null;
        }

    }

    @Override
    protected JMenuBar createMenuBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu openMenu = new JMenu("Open...");
        openMenu.add(new FileMenuAction(this, FileMenuAction.NOTATION_TEXT_TYPE, FileMenuAction.OPEN_ACTION_TYPE));
        fileMenu.add(openMenu);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenu saveMenu = buildFileSaveMenu();
        fileMenu.add(saveMenu);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(new ExitAction());
        menuBar.add(fileMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(new MonomerManagerAction(this));
        toolsMenu.add(new NucleotideManagerAction(this));
        toolsMenu.add(new ProteinEditorAction(this));
        toolsMenu.add(new org.helm.editor.action.ADCEditorAction(this));
        toolsMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        toolsMenu.add(new ConvertNotationAction(this));
        toolsMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        toolsMenu.add(new OligonucleotideFragmentAction(this));
        toolsMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        toolsMenu.add(new ShowChooseUIDialog(this));
        menuBar.add(toolsMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenu viewMenu = buildViewMenu();
        editMenu.add(viewMenu);
        editMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenu copyMenu = buildCopyMenu();
        editMenu.add(copyMenu);
        editMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        editMenu.add(new ReplaceMonomerAction(this, _ownerCode));
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Help");
        //helpMenu.add(new SendFeedback());
        //helpMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        helpMenu.add(new ShowAboutHELM());
        menuBar.add(helpMenu);

        return menuBar;
    }

    @Override
    protected JToolBar createCustomerToolBar() {
        JToolBar toolBar = super.createCustomerToolBar();

        regularConnectionButton = new JToggleButton(new ImageIcon(MacromoleculeEditor.class.getResource("resource/regularConnection16.gif")));
        regularConnectionButton.setToolTipText("connect two monomers");
        regularConnectionButton.setSelected(true);
        regularConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setRegularConnection(true);
            }
        });

        pairConnectionButton = new JToggleButton(new ImageIcon(MacromoleculeEditor.class.getResource("resource/hydrogenConnection16.gif")));
        pairConnectionButton.setToolTipText("Make a hydrogen bond");
        pairConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setRegularConnection(false);
            }
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(pairConnectionButton);
        buttonGroup.add(regularConnectionButton);

        toolBar.add(regularConnectionButton);
        toolBar.add(pairConnectionButton);

        JButton layoutButton = new JButton(new ImageIcon(MacromoleculeEditor.class.getResource("resource/rnaLayout16.gif")));
        layoutButton.setToolTipText("Clean up layout");
        layoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cleanUpLayout();
            }
        });
        toolBar.add(layoutButton);

        JButton hybridizeButton = new JButton(new ImageIcon(MacromoleculeEditor.class.getResource("resource/hybridize16.gif")));
        hybridizeButton.setToolTipText("Hybridize");
        hybridizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String notation = getNotation();
                try {
                    notation = ComplexNotationParser.hybridize(notation);
                    ModelController.notationUpdated(notation, _ownerCode);
                } catch (Exception ex) {
                    Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, ex.getMessage());
                    JOptionPane.showMessageDialog(getContentComponent(), ex.getMessage(), "Hybridization Error!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        toolBar.add(hybridizeButton);

        LoadPanel loadPanel = new LoadPanel(this, _ownerCode);
        toolBar.add(loadPanel);

        return toolBar;
    }

    /**
     * Overwritten to create an
     * <code>EditMode</code> suitable for nested graphs.
     */
    @Override
    protected EditMode createEditMode() {


        EditMode editMode = new HierarchyEditMode();
        editMode.setCreateEdgeMode(new EditorCreateEdgeMode(this));

        editMode.showNodeTips(true);
        editMode.showEdgeTips(true);
        return editMode;
    }

    @Override
    public void loadGraph(String name) {
        graphManager.reset();
        Graph2D graph = view.getGraph2D();
        graph.clear();

        super.loadGraph(name);

        NodeCursor nodes = graph.nodes();
        NodeMap nodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
        MonomerInfo monomerInfo = null;
        Node currentNode;

        Monomer monomer;

        NodeRealizer nodeRealizer = null;
        try {
            for (; nodes.ok(); nodes.next()) {
                currentNode = nodes.node();
                monomerInfo =
                        (MonomerInfo) nodeMap.get(currentNode);
                if (monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE)) {
                    graphManager.addStartingNode(currentNode);
                } else if (currentNode.inDegree() == 0) {
                    graphManager.addStartingNode(currentNode);
                    monomer = GraphUtils.getMonomerDB().
                            get(monomerInfo.getPolymerType()).get(monomerInfo.getMonomerID());
                    //only R node could have anotation label
                    if (MonomerInfoUtils.isRMonomer(currentNode) || MonomerInfoUtils.isPMonomer(currentNode)) {
                        nodeRealizer = graph.getRealizer(currentNode);
                        NodeLabel annotateLabel;

                        if (nodeRealizer.labelCount() >= 2) {
                            annotateLabel = nodeRealizer.getLabel(1);

                            if (annotateLabel.getText().contains(ANTISENSE)) {
                                graphManager.annotate(currentNode, ANTISENSE);
                            } else if (annotateLabel.getText().contains(SENSE)) {
                                graphManager.annotate(currentNode, SENSE);
                            }

                        }
                    }
                }
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            createMenuBar();
        }

        return menuBar;
    }

    /**
     * update the sequence view window when drop is complete
     *
     * @param nodes
     */
    public void onDropCompleteEvent(NodeCursor nodes) {
        try {
            ModelController.notationUpdated(getNotation(), _ownerCode);
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public String getStructureInfoXML() throws NotationException, MonomerException, IOException,
            JDOMException, StructureException, ListNotComparableException, ClassNotFoundException {
        return Graph2NotationTranslator.getStructureInfoXML(view.getGraph2D(), graphManager);
    }

    public Element getStructureInfoElement()
            throws NotationException, MonomerException, IOException, JDOMException, StructureException,
            ListNotComparableException, ClassNotFoundException {
        return Graph2NotationTranslator.getStructureInfoElement(view.getGraph2D(), graphManager);
    }

    /**
     * translate a string graph notation (do not need to be a canonical graph
     * notation) to a graph
     *
     * @param notation
     * @throws org.helm.notation.NotationException
     */
    public void setNotation(String notation) {
        try {
            if (notation == null || notation.equals("")) {
                reset();
                return;
            }

            if (notation.equalsIgnoreCase(getNotation())) {
                return;
            }

            ModelController.notationUpdated(notation, _ownerCode);

        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public void updateNotation(String notation) {
        try {
            if (notation == null || notation.equals("") || notation.equals("$$$$")) {
                reset();
                return;
            }

            //if (notation.equalsIgnoreCase(getNotation())) {
            //    return;
            //}

            ComplexNotationParser.validateComplexNotation(notation);
            GraphPair pair = NotationParser.getGraphPair(notation);

            //set the graph and the graph manager
            view.setGraph2D(pair.getGraph());
            graphManager = pair.getGraphManager();
            graphManager.getAnnotator().setGraph2D(view.getGraph2D());
            view.getGraph2D().setDefaultNodeRealizer(new MonomerNodeRealizer());


        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    /**
     * return the string notation of current graph this notation is not
     * necessary the canonical notation
     *
     * @return notation
     */
    public String getNotation() {
        if (view.getGraph2D().isEmpty() || graphManager == null) {
            return null;
        } else {
            try {
                Graph2NotationTranslator.updateHyperGraph(view.getGraph2D(), graphManager);
                return Graph2NotationTranslator.getNewNotation(graphManager);
            } catch (Exception e) {
                ExceptionHandler.handleException(e);
            }

            return null;
        }

    }

    /**
     * produce the canonical notation for this graph
     *
     * @return canonical notation for the graph
     * @throws StructureException
     */
    public String getCanonicalNotation() throws ClassNotFoundException, NotationException, IOException, MonomerException, JDOMException, StructureException {
        if (view.getGraph2D().isEmpty() || graphManager == null) {
            return null;
        } else {
            return ComplexNotationParser.getCanonicalNotation(Graph2NotationTranslator.getNewNotation(graphManager));
        }

    }

    public boolean isRegularConnection() {
        return regularConnection;
    }

    public void setRegularConnection(boolean regularConnection) {
        this.regularConnection = regularConnection;
    }

    public Map<String, Map<String, Monomer>> getMonomerDB() throws MonomerException, IOException, JDOMException {
        return GraphUtils.getMonomerDB();
    }

    /**
     * The graph view component
     *
     * @return graph view component
     */
    public JComponent getGraphViewComponent() {
        return view;
    }

    /**
     * The sequence display component.
     *
     * @return sequence view component
     */
    public JComponent getSequenceViewComponent() {
//        return sequenceViewPanel;
        return tabbedSequenceViewPanel;
    }

    /**
     * Content component is the editor without the menu bar
     *
     * @return content component
     */
    public JComponent getContentComponent() {
        return contentPane;
    }

    public JFrame getFrame() {
        Container container = getContentComponent().getParent();
        while (container != null) {
            if (container instanceof JFrame) {
                return (JFrame) container;
            }

            container = container.getParent();
        }

        return new JFrame();
    }

    public void updatePolymerPanels() {
        uiConstructor.updatePanels();
        setupUIType(type);
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public void onUpdate(NotationUpdateEvent event) {
        if (!event.getOwner().equals(_ownerCode)) {
            return;
        }

        graphManager.reset();
        synchronizeZoom();

        try {

            //always update graph in the editor..
            updateNotation(event.getData());

            updateAnnotator();
            graphManager.getAnnotator().annotateAllBasePosition();

            runLayout();
            double zoom = view.getZoom();

            boolean keepzoom = false;
            // keep zoom
            if ((Double.compare(zoom, this.zoom) < 0) && (Double.compare(this.zoom, 1.0) != 0)) {
                keepzoom = true;
            }
            //else {
            view.fitContent();

            if (keepzoom) {
                view.setZoom(this.zoom);
                view.setCenter(zoomingAreaCenter.getX(), zoomingAreaCenter.getY());
            }
            //}

            view.updateView();
        } catch (Exception e) {
            ExceptionHandler.handleException(e);
        }
    }

    public void synchronizeZoom() {
        zoom = view.getZoom();
        zoomingAreaCenter = view.getCenter();
    }

    private void updateAnnotator() {
        Annotator currAnnotator = graphManager.getAnnotator();
        currAnnotator.setGraph2D(view.getGraph2D());
        currAnnotator.setManager(graphManager);
    }

    private void cleanUpLayout() {
        try {
            runLayout();
            view.updateView();
            view.fitContent();
        } catch (Exception ex) {
            ExceptionHandler.handleException(ex);
        }
    }
}
