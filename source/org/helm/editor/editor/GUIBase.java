/****************************************************************************
 **
 ** This file is based on demoBase of yFiles-2.5.0.4. 
 ** 
 ** 
 **
 ***************************************************************************/
package org.helm.editor.editor;

import org.helm.notation.MonomerException;
import org.helm.notation.MonomerFactory;
import org.helm.notation.NucleotideFactory;
import org.helm.editor.action.DeleteAction;
import org.helm.editor.componentPanel.SequenceViewPanes;
import org.helm.editor.controller.ModelController;
import org.helm.editor.data.EditorEdgeInfoData;
import org.helm.editor.data.GraphManager;
import org.helm.editor.data.EdgeMapKeys;
import org.helm.editor.data.MonomerInfo;
import org.helm.editor.data.NodeMapKeys;
import org.helm.editor.io.EdgeInputHandler;
import org.helm.editor.io.EdgeOutputHandler;
import org.helm.editor.io.NodeInputHandler;
import org.helm.editor.io.NodeOutputHandler;
import org.helm.editor.utility.Graph2NotationTranslator;
import org.helm.editor.utility.IconGenerator;
import org.helm.editor.utility.NumericalUtils;
import org.helm.editor.utility.SaveAsPNG;
import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.JDOMException;
import y.base.DataProvider;
import y.io.IOHandler;
import y.io.YGFIOHandler;
import y.option.OptionHandler;
import y.util.D;
import y.view.AreaZoomMode;
import y.view.AutoDragViewMode;
import y.view.EditMode;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.Selections;
import y.view.ViewMode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import org.graphdrawing.graphml.GraphMLConstants;
import y.anim.AnimationPlayer;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeList;
import y.base.NodeMap;
import y.view.Graph2D;
import y.view.Graph2DClipboard;
import y.view.Graph2DCopyFactory;
import y.view.Graph2DUndoManager;
import y.view.MagnifierViewMode;
import yext.graphml.graph2D.GraphMLIOHandler;

/**
 * Abstract base class for GUI- and <code>Graph2DView</code>-based demos.
 * Provides useful callback methods.
 * <p/>
 * To avoid problems with "calls to overriden method in constructor", do not initializeIOHandler the demo
 * within the constructor of the subclass, use the method {@link #initializeIOHandler()} instead.
 */
public abstract class GUIBase {

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;

    /**
     * The view component of this demo.
     */
    protected Graph2DView view;
    protected final JPanel contentPane;
    private Graph2DUndoManager undoManager;
    protected JPanel viewPanel; //a panel that contains the view and the sequence view panel
    protected SequenceViewPanes tabbedSequenceViewPanel;
    protected Graph2DViewActions actions;
    protected ActionMap amap;
    protected GraphManager graphManager;    //a hyper graph is a clustering of the graph being displayed. each hypernode
    
    //  is a whole sequence/chemical structure
    private GraphMLIOHandler graphMLIOHandler;
    private YGFIOHandler ygfIOHandler;
    protected AnimationPlayer animationPlayer = new AnimationPlayer();
    private MagnifierViewMode magnifierMode;
    private JToggleButton magnifierButton;
    
    protected String _ownerCode;
    
    protected JFrame frame;
    
    private JSplitPane viewSplitPane;

    /**
     * Initializes to a "nice" look and feel.
     */
//    public static void initLnF() {
//        try {
//            if (!UIManager.getSystemLookAndFeelClassName().equals(
//                    "com.sun.java.swing.plaf.motif.MotifLookAndFeel")
//                    && !UIManager.getSystemLookAndFeelClassName().equals(
//                    "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
//                    && !UIManager.getSystemLookAndFeelClassName().equals(
//                    UIManager.getLookAndFeel().getClass().getName())) {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * This constructor creates the {@link #view}
     * and calls,
     * {@link #createToolBar()}
     * {@link #registerViewModes()}, {@link #registerViewActions()},
     * and {@link #registerViewListeners()}
     */
    protected GUIBase() {
        view = new Graph2DView();
        view.setAntialiasedPainting(true);
        view.setFitContentOnResize(true);
        
        animationPlayer.addAnimationListener(view);
        animationPlayer.setFps(240);
        animationPlayer.setSpeed(0.3);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(950, 650));

        viewPanel = new JPanel();

        try {
            _ownerCode = NumericalUtils.getUniqueCode();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        //setup the sequence view panel
        tabbedSequenceViewPanel = new SequenceViewPanes(_ownerCode);

        viewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, view, tabbedSequenceViewPanel);
        viewSplitPane.setDividerLocation(300);              
        viewSplitPane.setOneTouchExpandable(false);

        initializeIOHandler();

        registerViewModes();
        registerViewActions();
        Box boxLayout = Box.createHorizontalBox();
        
        boxLayout.add(createToolBar());
        boxLayout.add(createCustomerToolBar());
        
        contentPane.add(boxLayout, BorderLayout.NORTH);
        contentPane.add(viewSplitPane);
        
        registerViewListeners();

        /////////// clipboard 
        /*
        view.getCanvasComponent().getActionMap().put("CUT", cutAction);
        view.getCanvasComponent().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK), "CUT");

        view.getCanvasComponent().getActionMap().put("COPY", copyAction);
        view.getCanvasComponent().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), "COPY");

        view.getCanvasComponent().getActionMap().put("PASTE", pasteAction);
        view.getCanvasComponent().getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK), "PASTE");
         */

        graphManager = new GraphManager();

        magnifierMode = new MagnifierViewMode();
        magnifierMode.setMagnifierRadius(80);
        magnifierMode.setMagnifierZoomFactor(3.0);

        try {
            String notation = Graph2NotationTranslator.getNewNotation(graphManager);
            ModelController.notationUpdated(notation, _ownerCode);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public JSplitPane getViewSplitPane(){
    	return viewSplitPane;
    }

    /**
     * This method is called before the view modes and actions are registered and the menu and toolbar is build.
     */
    protected void initializeIOHandler() {
        //set up io handlers
        graphMLIOHandler = new GraphMLIOHandler();
        graphMLIOHandler.getOutputHandlers(GraphMLConstants.SCOPE_NODE).add(new NodeOutputHandler());
        graphMLIOHandler.getOutputHandlers(GraphMLConstants.SCOPE_EDGE).add(new EdgeOutputHandler());
        graphMLIOHandler.getInputHandlers().add(new EdgeInputHandler());
        graphMLIOHandler.getInputHandlers().add(new NodeInputHandler());

        ygfIOHandler = new YGFIOHandler();
    }

    public void dispose() {
    }

    public void loadGraph(Class aClass, String resourceString) {
        String fqResourceName = aClass.getPackage().getName().replace('.', '/') + '/' + resourceString;

        URL resource = aClass.getResource(resourceString);
        if (resource == null) {
            String message = "Resource \"" + fqResourceName + "\" not found in classpath";
            D.showError(message);
            throw new RuntimeException(message);
        }
        try {
            IOHandler ioh;
            if (resource.getFile().endsWith("ygf")) {
                ioh = this.ygfIOHandler;
            } else {
                ioh = this.graphMLIOHandler;
            }
            ioh.read(view.getGraph2D(), resource);
        } catch (Exception e) {
            String message = "Unexpected error while loading resource \"" + fqResourceName + "\" due to " + e.getMessage();
            D.showError(message);
            throw new RuntimeException(message, e);
        }
        view.fitContent();
    }

    public void loadGraph(String name) {
        try {
            IOHandler ioh;
            if (name.endsWith(".ygf")) {
                ioh = this.ygfIOHandler;
            } else {
                ioh = this.graphMLIOHandler;
            }
            ioh.read(view.getGraph2D(), name);

        } catch (Exception e) {
            String message = "Unexpected error while loading resource \"" + name + "\" due to " + e.getMessage();
            D.showError(message);
            throw new RuntimeException(message, e);
        }
        //update display
        view.fitContent();
        view.getGraph2D().updateViews();
    }

    /**
     * save current graph into a xmlfile
     * @param xmlfile
     */
    public void saveGraph(File xmlfile) {
        try {
            graphMLIOHandler.write(view.getGraph2D(), xmlfile.getName());
        } catch (IOException ioe) {
            D.show(ioe);
        }
    }

    /**
     * return a xml string representation of the given graph
     * @param graph
     * @return graph XML string
     */
    public static String getGraphXML(Graph2D graph) {
        StringBuilder xmlStringBuilder = new StringBuilder();
        String name = "temp.graphxml";
        File outputFile = new File(name);
        //write graph to a temp file
        GraphMLIOHandler ioh = new GraphMLIOHandler();
        ioh.getInputHandlers().add(new NodeInputHandler());
        ioh.getInputHandlers().add(new EdgeInputHandler());
        try {
            ioh.write(graph, name);

        } catch (IOException ioe) {
            D.show(ioe);
        }

        try {
            FileInputStream fstream = new FileInputStream(name);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                xmlStringBuilder.append(strLine);
                xmlStringBuilder.append("\n");
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        outputFile.delete();
        return xmlStringBuilder.toString();

    }

    /**
     * get the graphxml string representation of the current graph
     * @return graph XML string
     */
    public String getGraphXML() {
        return getGraphXML(view.getGraph2D());
    }

    public void saveGraphAs(String name) {
        if (name.endsWith(".ygf")) {
            try {
                ygfIOHandler.write(view.getGraph2D(), name);
            } catch (IOException ioe) {
                D.show(ioe);
            }
        } else {
            if (!name.endsWith(".graphml")) {
                name = name + ".graphml";
            }
            try {
                graphMLIOHandler.write(view.getGraph2D(), name);
            } catch (IOException ioe) {
                D.show(ioe);
            }
        }

    }

    /**
     * Creates an application  frame for this demo
     * and displays it. The class name is the title of
     * the displayed frame.
     */
    public final void start() {
        String title = generateApplicationTitle();
        if (title != null && !title.equalsIgnoreCase("")) {
            start(title);
        } else {
            start(getClass().getName());
        }
    }

    /**
     * Creates an application  frame for this demo
     * and displays it. The given string is the title of
     * the displayed frame.
     */
    public final void start(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                try {
                    MonomerFactory.getInstance().saveMonomerCache();
                    NucleotideFactory.getInstance().saveNucleotideTemplates();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        this.addContentTo(frame.getRootPane());
        frame.setIconImage(IconGenerator.getImage(IconGenerator.HELM_APP_ICON_RESOURCE_URL));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Creates an application  frame for this demo
     * and displays it. The given string is the title of
     * the displayed frame.
     */
    public final void start(String title, Image iconImage) {

        JFrame frame = new JFrame(title);
        frame.setIconImage(iconImage);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                try {
                    MonomerFactory.getInstance().saveMonomerCache();
                    NucleotideFactory.getInstance().saveNucleotideTemplates();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
        this.addContentTo(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void addContentTo(final JRootPane rootPane) {
        rootPane.setJMenuBar(createMenuBar());
        rootPane.setContentPane(contentPane);
    }

    public void reset() {
        view.getGraph2D().clear();
        graphManager.reset();
        tabbedSequenceViewPanel.init(view.getGraph2D(), graphManager);
        view.fitContent();
        view.updateView();
    }

    protected void registerViewActions() {
        //register keyboard actions
        actions = new Graph2DViewActions(view);
        amap = actions.createActionMap();
        InputMap imap = actions.createDefaultInputMap(amap);

        //replace the default delete action with a customized one       
        amap.put(Graph2DViewActions.DELETE_SELECTION, new DeleteAction(this, _ownerCode));

        if (!isDeletionEnabled()) {
            amap.remove(Graph2DViewActions.DELETE_SELECTION);
        }

        view.getCanvasComponent().setActionMap(amap);
        view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);

        //create new clipboard.
        SequenceGraph2DClipboard clipboard = new SequenceGraph2DClipboard(view);
        clipboard.setGraphFactory(new GraphDeepCopyFactory());
        
        //get Cut action from clipboard
        cutAction = clipboard.getCutAction();
        cutAction.putValue(Action.SMALL_ICON,
                new ImageIcon(GUIBase.class.getResource("resource/Cut16.gif")));
        cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut");


//          get Copy action from clipboard
        copyAction = clipboard.getCopyAction();

        copyAction.putValue(Action.SMALL_ICON,
                new ImageIcon(GUIBase.class.getResource("resource/Copy16.gif")));
        copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy");

        //get Paste action from clipboard
        pasteAction = clipboard.getPasteAction();
        pasteAction.putValue(Action.SMALL_ICON,
                new ImageIcon(GUIBase.class.getResource("resource/Paste16.gif")));
        pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste");
    }

    /**
     * Adds the view modes to the view.
     * This implementation adds a new EditMode created by {@link #createEditMode()}
     * a new {@link AutoDragViewMode}.
     */
    protected void registerViewModes() {
        //edit mode will show tool tips over nodes
        EditMode editMode = createEditMode();
        if (editMode != null) {
            view.addViewMode(editMode);
        }
        view.addViewMode(new AutoDragViewMode());
    }

    /**
     * Callback used by {@link #registerViewModes()} to create the default EditMode
     *
     * @return an instance of {@link EditMode} with showNodeTips enabled
     */
    protected EditMode createEditMode() {
        EditMode editMode = new EditMode();
        editMode.showNodeTips(true);
        editMode.showEdgeTips(true);
        return editMode;
    }

    /**
     * Instantiates and registers the listeners for the view.
     * (e.g. {@link y.view.Graph2DViewMouseWheelZoomListener}
     */
    protected void registerViewListeners() {
        //Note that mouse wheel support requires J2SE 1.4 or higher.
        view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
    }

    /**
     * Determines whether default actions for deletions will be added to the view
     * and toolbar.
     */
    protected boolean isDeletionEnabled() {
        return true;
    }

    /**
     * Creates a toolbar for this demo.
     */
    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        if (isDeletionEnabled()) {
            toolBar.add(new AbstractAction("Clear") {

                {
                    URL imageURL = GUIBase.class.getResource("resource/New16.gif");
                    if (imageURL != null) {
                        putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
                    }
                    putValue(Action.SHORT_DESCRIPTION, "Clear Window");
                }

                public void actionPerformed(ActionEvent e) {
                    reset();
                }
            });
            toolBar.add(new DeleteAction(this, _ownerCode));
        }
        toolBar.add(new Zoom(1.2));
        toolBar.add(new Zoom(0.8));
        toolBar.add(new ZoomArea());
        toolBar.add(new FitContent());


        toolBar.addSeparator();
/*
        toolBar.add(cutAction);
        toolBar.add(copyAction);
        toolBar.add(pasteAction);
        toolBar.addSeparator();
 */

        //add undo action to toolbar
        //TODO need to customize the default undo manager
//        undoAction = getUndoManager().getUndoAction();
//        undoAction.putValue(Action.SMALL_ICON,
//                new ImageIcon(GUIBase.class.getResource("resource/Undo16.gif")));
//        undoAction.putValue(Action.SHORT_DESCRIPTION, "Undo");
//        toolBar.add(undoAction);

        //add redo action to toolbar
//        redoAction = getUndoManager().getRedoAction();
//        redoAction.putValue(Action.SMALL_ICON,
//                new ImageIcon(GUIBase.class.getResource("resource/Redo16.gif")));
//        redoAction.putValue(Action.SHORT_DESCRIPTION, "Redo");
//        toolBar.add(redoAction);
//
//        toolBar.addSeparator();

        magnifierButton = new JToggleButton(
                new ImageIcon(GUIBase.class.getResource("resource/Magnifier16.gif")));
        magnifierButton.setToolTipText("Magnifier");
        magnifierButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (magnifierButton.isSelected()) {
                    view.addViewMode(magnifierMode);
                } else {
                    view.removeViewMode(magnifierMode);
                }
            }
        });

        toolBar.add(magnifierButton);

        return toolBar;
    }

    protected JToolBar createCustomerToolBar() {
    	JToolBar toolBar = new JToolBar();
        return toolBar;
    }

    /**
     * Create a menu bar for this demo.
     */
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(createLoadAction());
        menu.add(createSaveAction());
        menu.add(new SaveSubsetAction());
        menu.addSeparator();
        menu.add(new PrintAction());
        menu.addSeparator();
        menu.add(new SaveAsImage("Save monomer graph image as ...", view));
        menu.add(new SaveAsImage("Save sequence view image as ...", tabbedSequenceViewPanel.getSequenceView()));
        menu.addSeparator();
        menu.add(new ExitAction());
        menuBar.add(menu);
        return menuBar;
    }

    protected Action createLoadAction() {
        return new LoadAction();
    }

    protected Action createSaveAction() {
        return new SaveAction();
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    public Graph2DView getView() {
        return view;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }
    
    public String getOwnerCode() {
    	return _ownerCode;
    }

    public static String generateApplicationTitle() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.helm.editor.editor.resource.GUIBase");
        String title = null;
            title = resourceBundle.getString("Application.name") + " " + resourceBundle.getString("Application.id") + "v" + resourceBundle.getString("Application.version");
        return title;
    }

    /**
     * Action that prints the contents of the view
     */
    protected class PrintAction extends AbstractAction {

        PageFormat pageFormat;
        OptionHandler printOptions;

        PrintAction() {
            super("Print");

            //setup option handler
            printOptions = new OptionHandler("Print Options");
            printOptions.addInt("Poster Rows", 1);
            printOptions.addInt("Poster Columns", 1);
            printOptions.addBool("Add Poster Coords", false);
            final String[] area = {"View", "Graph"};
            printOptions.addEnum("Clip Area", area, 1);
        }

        public void actionPerformed(ActionEvent e) {
            Graph2DPrinter gprinter = new Graph2DPrinter(view);

            //show custom print dialog and adopt values
            if (!printOptions.showEditor()) {
                return;
            }
            gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
            gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
            gprinter.setPrintPosterCoords(
                    printOptions.getBool("Add Poster Coords"));
            if (printOptions.get("Clip Area").equals("Graph")) {
                gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
            } else {
                gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
            }

            //show default print dialogs
            PrinterJob printJob = PrinterJob.getPrinterJob();
            if (pageFormat == null) {
                pageFormat = printJob.defaultPage();
            }
            PageFormat pf = printJob.pageDialog(pageFormat);
            if (pf == pageFormat) {
                return;
            } else {
                pageFormat = pf;
            }

            //setup printjob.
            //Graph2DPrinter is of type Printable
            printJob.setPrintable(gprinter, pageFormat);

            if (printJob.printDialog()) {
                try {
                    printJob.print();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Action that terminates the application
     */
    protected class ExitAction extends AbstractAction {

        ExitAction() {
            super("Exit");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                MonomerFactory.getInstance().saveMonomerCache();
//                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }
    }

    /**
     * Action that saves the current graph to a file in YGF format.
     */
    protected class SaveAction extends AbstractAction {

        JFileChooser chooser;

        SaveAction() {
            super("Save...");
            chooser = null;
        }

        public void actionPerformed(ActionEvent e) {
            if (chooser == null) {
                chooser = new JFileChooser();
                chooser.setFileFilter(new GraphFileFilter());

            }
            if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                String name = chooser.getSelectedFile().toString();
                saveGraphAs(name);

            }
        }
    }

    /**
     * Action that saves the current subset of the graph to a file in YGF format.
     */
    protected class SaveSubsetAction extends AbstractAction {

        JFileChooser chooser;

        public SaveSubsetAction() {
            super("Save selection...");
            chooser = null;
        }

        public void actionPerformed(ActionEvent e) {
            if (chooser == null) {
                chooser = new JFileChooser();
                chooser.setFileFilter(new GraphFileFilter());
            }
            if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                String name = chooser.getSelectedFile().toString();
                if (name.endsWith(".ygf")) {
                    YGFIOHandler ioh = new YGFIOHandler();
                    try {
                        DataProvider dp = Selections.createSelectionDataProvider(view.getGraph2D());
                        ioh.writeSubset(view.getGraph2D(), dp, name);
                    } catch (IOException ioe) {
                        D.show(ioe);
                    }
                } else {
                    if (!name.endsWith(".graphml")) {
                        name = name + ".graphml";

                    }

                    try {
                        DataProvider dp = Selections.createSelectionDataProvider(view.getGraph2D());
                        graphMLIOHandler.writeSubset(view.getGraph2D(), dp, name);
                    } catch (IOException ioe) {
                        D.show(ioe);
                    }
                }

            }
        }
    }

    /**
     * Action that loads the current graph from a file in YGF format.
     */
    protected class LoadAction extends AbstractAction {

        JFileChooser chooser;

        public LoadAction() {
            super("Load...");
            chooser = null;
        }

        public void actionPerformed(ActionEvent e) {
            if (chooser == null) {
                chooser = new JFileChooser();
                chooser.setFileFilter(new GraphFileFilter());
            }
            if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                String name = chooser.getSelectedFile().toString();

                view.getGraph2D().clear();
                loadGraph(name);

            }
        }
    }

    /**
     * modified Graph2DCopyFactory to include usr data copy
     */
    class GraphDeepCopyFactory extends Graph2DCopyFactory {

        private final Graph2D graph;

        public GraphDeepCopyFactory() {
            super();
            graph = view.getGraph2D();
        }

        @Override
        public Node createNode(final Graph graph, final Object node) {
            final Node newNode = super.createNode(graph, node);
            copyNodeData((Node) node, newNode);

            return newNode;
        }

        @Override
        public Edge createEdge(
                final Graph graph,
                final Node source, final Node target,
                final Object edge) {
            final Edge newEdge = super.createEdge(graph, source, target, edge);
            copyEdgeData((Edge) edge, newEdge);
            return newEdge;
        }

        void copyNodeData(final Node source, final Node target) {
            final NodeMap monomerInfoNodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);
            MonomerInfo monomerInfo = (MonomerInfo) monomerInfoNodeMap.get(source);
            MonomerInfo newMonomerInfo = new MonomerInfo(monomerInfo.getPolymerType(), monomerInfo.getMonomerID());
            monomerInfoNodeMap.set(target, newMonomerInfo);
        }

        void copyEdgeData(final Edge source, final Edge target) {
            final EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(EdgeMapKeys.EDGE_INFO);
            final NodeMap monomerInfoNodeMap = (NodeMap) graph.getDataProvider(NodeMapKeys.MONOMER_REF);

            //get the source and target node of the new edge
            Node sourceNode = target.source();
            Node targetNode = target.target();


            //set up 
            EditorEdgeInfoData edgeInfo = (EditorEdgeInfoData) edgeMap.get(source);

            MonomerInfo sourceMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(sourceNode);
            Attachment sourceAtt = sourceMonomerInfo.getAttachment(edgeInfo.getSourceNodeAttachment().getLabel());

            MonomerInfo targetMonomerInfo = (MonomerInfo) monomerInfoNodeMap.get(targetNode);
            Attachment targetAtt = targetMonomerInfo.getAttachment(edgeInfo.getTargetNodeAttachment().getLabel());

            sourceMonomerInfo.setConnection(sourceAtt, true);
            sourceMonomerInfo.setConnection(targetAtt, true);

            edgeMap.set(target, new EditorEdgeInfoData(sourceAtt, targetAtt));
        }
    }

    /**
     * Action that applies a specified zoom level to the view.
     */
    protected class Zoom extends AbstractAction {

        double factor;

        public Zoom(double factor) {
            super("Zoom " + (factor > 1.0 ? "In" : "Out"));
            URL imageURL;
            if (factor > 1.0d) {
                imageURL = GUIBase.class.getResource("resource/ZoomIn16.gif");
            } else {
                imageURL = GUIBase.class.getResource("resource/ZoomOut16.gif");
            }
            if (imageURL != null) {
                this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
            }
            this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
            this.factor = factor;
        }

        public void actionPerformed(ActionEvent e) {
            view.setZoom(view.getZoom() * factor);
            //optional code that adjusts the size of the
            //view's world rectangle. The world rectangle
            //defines the region of the canvas that is
            //accessible by using the scrollbars of the view.
            Rectangle box = view.getGraph2D().getBoundingBox();
            view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

            view.updateView();
        }
    }

    /**
     * Action that fits the content nicely inside the view.
     */
    protected class FitContent extends AbstractAction {

        public FitContent() {
            super("Fit Content");
            URL imageURL = GUIBase.class.getResource("resource/FitContent16.gif");
            if (imageURL != null) {
                this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
            }
            this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
        }

        public void actionPerformed(ActionEvent e) {
            view.fitContent();
            view.updateView();
        }
    }

    /**
     * Action that zooms the view to the bounding box of selected nodes.
     */
    public class ZoomArea extends AbstractAction {

        public ZoomArea() {
            super("Zoom Area");
            URL imageURL = GUIBase.class.getResource("resource/Zoom16.gif");
            if (imageURL != null) {
                this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
            }
            this.putValue(Action.SHORT_DESCRIPTION, "Zoom Area");
        }

        public void actionPerformed(ActionEvent e) {
            Iterator viewModes = view.getViewModes();
            while (viewModes.hasNext()) {
                ViewMode viewMode = (ViewMode) viewModes.next();
                if (viewMode instanceof EditMode) {
                    EditMode editMode = (EditMode) viewMode;
                    editMode.setChild(new AreaZoomMode(), null, null);
                }
            }
        }
    }

    /**
     * Returns the undo manager for this application. Also, if not already done - it creates 
     * and configures it.
     */
    protected Graph2DUndoManager getUndoManager() {
        if (undoManager == null) {
            undoManager = new Graph2DUndoManager();
            //make it listen to graph structure changes
            view.getGraph2D().addGraphListener(undoManager);
            //make it handle backup realizer requests. 
            view.getGraph2D().setBackupRealizersHandler(undoManager);
            //assign the graph view as view container so we get view updates
            //after undo/redo actions have been performed. 
            undoManager.setViewContainer(view);
        }
        return undoManager;
    }

    private class GraphFileFilter extends javax.swing.filechooser.FileFilter {

        //Accept all directories and all gif, jpg, tiff, or png files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String name = f.toString();
            if (name != null) {
                if (name.endsWith(".graphml") || name.endsWith(".ygf")) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        //The description of this filter
        public String getDescription() {
            return "Graph";
        }
    }

    /**
     * modify the default past action
     */
    protected class SequenceGraph2DClipboard extends Graph2DClipboard {

        public SequenceGraph2DClipboard(Graph2DView view) {
            super(view);
        }

        @Override
        public NodeList pasteFromClipBoard(Graph targetGraph) {
            try {
                NodeList nodeList = super.pasteFromClipBoard(targetGraph);
                Node node = null;
                NodeMap nodeMap = (NodeMap) targetGraph.getDataProvider(NodeMapKeys.MONOMER_REF);
                MonomerInfo monomerInfo = null;
                for (int i = 0; i < nodeList.size(); i++) {
                    node = (Node) nodeList.get(i);
                    monomerInfo = (MonomerInfo) nodeMap.get(node);
                    if (node.inDegree() == 0 || (monomerInfo != null && monomerInfo.getPolymerType().equalsIgnoreCase(Monomer.CHEMICAL_POLYMER_TYPE))) {
                        graphManager.addStartingNode(node);
                    }
                }
                tabbedSequenceViewPanel.init(view.getGraph2D(), graphManager);
                return nodeList;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        ex.getMessage(),
                        "I/O Exception!",
                        JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, ex.getMessage());
            } catch (MonomerException ex) {
                JOptionPane.showMessageDialog(null,
                        ex.getMessage(),
                        "Invalid Monomer!",
                        JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, ex.getMessage());
            } catch (JDOMException ex) {
                JOptionPane.showMessageDialog(null,
                        ex.getMessage(),
                        "JDOMException!",
                        JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(MacromoleculeEditor.class.getName()).log(Level.SEVERE, ex.getMessage());
            }
            return null;
        }
    }

    protected class SaveAsImage extends AbstractAction {

        private JFileChooser fileChooser;
        private Graph2DView view;

        public SaveAsImage(String title, Graph2DView view) {
            super(title);
            this.view = view;
            fileChooser = null;
        }

        public void actionPerformed(ActionEvent e) {
            if (fileChooser == null) {
                fileChooser = new JFileChooser();
            }
            if (fileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
                String name = fileChooser.getSelectedFile().toString();
                Image image = view.getImage();
                new SaveAsPNG(image, name + ".png");
            }
        }
    }
}
