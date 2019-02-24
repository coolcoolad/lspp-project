package generator2;

import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Timer;

import client.CooperateEnumerator;
import drawables.*;
import poi.Poi;
import poi.PoiGenerator;
import router.RouterGenerator;
import router.RouterGraph;
import routing.*;
import server.ServerMain;
import showmap.*;
import spatial.*;
import statistics.StatisticTool;
import util.*;

import javax.swing.*;

/**
 * Abstract controller applet for the computation of network-based spatiotemporal datasets.
 * The abstract methods allow to use user-defined classes determining the bahavior of the generator.
 * Non-abtract subclasses are the classes generator2.DefaultDataGenerator, generator2.OracleDataGenerator,
 * and generator2.OracleSpatialDataGenerator.
 * It is also possible to run these subclasses as Java applications.
 * See the additional documentation for the supported properties in the property file.
 *
 * @author FH Oldenburg
 * @version 2.10    19.08.2003	considering null routes, tuned
 * @version 2.00    04.09.2001	complete revision
 * @version 1.22    15.06.2001	report of external objects added
 * @version 1.21    07.06.2001	compute() and compute(Reporter) separated, mode protected
 * @version 1.20    07.05.2001	textfields for msd & report probability added
 * @version 1.12    11.04.2001	waittime, maxSpeedDivisor introduced, adapted to changed classes
 * @version 1.11    01.06.2000	changed creation of container with drawable objects
 * @version 1.10    30.04.2000	support of external objects, reporting improved
 * @version 1.00    16.01.2000	first version
 */
public abstract class DataGenerator extends ShowNetworkMap implements java.awt.event.AdjustmentListener {

    /**
     * Properties of the data generator
     */
    protected Properties properties = new Properties();
    /**
     * Name of the property file
     */
    protected static String propFilename = "properties.txt";

    /**
     * Current displayed time (0 = all times)
     */
    protected int actTime = 0;

    /**
     * The time object.
     */
    protected Time time = null;
    /**
     * The data space.
     */
    protected DataSpace dataspace = null;
    /**
     * The edge classes.
     */
    protected EdgeClasses edgeClasses = null;
    /**
     * The classes of the moving objects.
     */
    protected ObjectClasses objClasses = null;
    /**
     * The external objects.
     */
    protected ExternalObjects extObjects = null;
    /**
     * The classes of external objects.
     */
    protected ExternalObjectClasses extObjClasses = null;
    /**
     * The reporter.
     */
    protected Reporter reporter = null;

    /**
     * Property file error
     */
    public static final int PROPERTY_FILE_ERROR = -1;
    /**
     * Application start error
     */
    public static final int APPLICATION_START_ERROR = -2;
    /**
     * Network file error
     */
    public static final int NETWORKFILE_ERROR = -3;

    /**
     * Maximum value for number of external objects at the beginning
     */
    public static int MAX_EXTOBJBEGIN = 100;
    /**
     * Maximum value for number of external object classes
     */
    public static int MAX_EXTOBJCLASSES = 10;
    /**
     * Maximum value for number of external objects per time
     */
    public static int MAX_EXTOBJPERTIME = 10;
    /**
     * Maximum value for maximum time
     */
    public static int MAX_MAXTIME = 64000;
    /**
     * Maximum value for number of moving objects at the beginning
     */
    public static int MAX_OBJBEGIN = 1000;
    /**
     * Maximum value for number of moving object classes
     */
    public static int MAX_OBJCLASSES = 20;
    /**
     * Maximum value for number of moving objects per time
     */
    public static int MAX_OBJPERTIME = 800;
    /**
     * Minimum value for maximum time
     */
    public static int MIN_MAXTIME = 5;

    /**
     * Waiting period between two time stamps in msec (a value larger 0 is required for painting the objects while the computation)
     */
    protected int waitingPeriod = 0;

    private static final String ROUTER_NUM = "400";
    private static final String POI_NUM = "100";
    private static final String USER_NUM = "1000";
    private static final String MALICIOUS_NUM = "1";
    private static final String PROTECT_RADIUS = "500";

    /**
     * The frame containing the applet if it is running as application.
     */
    protected static Frame frame = null;
    /**
     * Compute button
     */
    private JButton computeButton = null;
    /**
     * Add time button
     */
    private JButton startButton = null;
    /**
     * Scrollbar
     */
    private Scrollbar timeScrollbar = null;
    /**
     * Maximum time label
     */
    private JLabel maxTimeLabel = null;
    /**
     * Maximum time text field
     */
    private TextField maxTimeText = null;
    /**
     * Number of object classes label
     */
    private JLabel numObjClassesLabel = null;
    /**
     * Number of moving object classes text field
     */
    private TextField numObjClassesText = null;
    /**
     * Number of external object classes text field
     */
    private TextField numExtObjClassesText = null;
    /**
     * Objects per time label
     */
    private JLabel objPerTimeLabel = null;
    /**
     * Moving objects per time text field
     */
    private TextField objPerTimeText = null;
    /**
     * Objects at beginning label
     */
    private JLabel objBeginLabel = null;
    /**
     * External objects per time text field
     */
    private TextField extobjPerTimeText = null;
    /**
     * External objects at the beginning text field
     */
    private TextField extobjBeginText = null;
    /**
     * Delete button
     */
    private JButton deleteButton = null;
    /**
     * Maximum speed divisor label
     */
    private JLabel msdLabel = null;
    /**
     * Maximum speed divisor text field
     */
    private TextField msdText = null;
    /**
     * Moving objects at the beginning text field
     */
    private TextField objBeginText = null;
    /**
     * Report probability label
     */
    private JLabel reportProbLabel = null;
    /**
     * Report probability text field
     */
    private TextField reportProbText = null;

    private Timer timer = null;
    private JButton generateRouterButton = null;
    private TextField routerNumField = null;
    private JButton generatePoiButton = null;
    private TextField poiNumField = null;
    private JLabel anonymousParaLabel = null;
    private TextField anonymousParaField = null;
    private JLabel protectRadiusLabel = null;
    private TextField protectRadiusField = null;
    private JLabel requestParaLabel = null;
    private TextField requestParaField = null;
    private JLabel maliciousUserLabel = null;
    private TextField maliciousUserField = null;
    private JLabel logLabel = null;
    private List logListView = null;
    private JLabel[] poiLabelArr = null;
    private TextField[] poiFieldArr = null;
    private JLabel KLabel = null;
    private TextField KField = null;
    private JLabel LLabel = null;
    private TextField LField = null;
    private JLabel CRMaxLabel = null;
    private TextField CRMaxField = null;
    private JLabel sigmaLabel = null;
    private TextField sigmaField = null;

    /**
     * main entrypoint - starts the part when it is run as an application
     *
     * @param nameOfApplet complete name of the calling subclass
     */
    static void main(String nameOfApplet) {
        try {
            frame = new Frame("Network Generator");
            final DataGenerator aDataGenerator;
            Class iiCls = Class.forName(nameOfApplet);
            ClassLoader iiClsLoader = iiCls.getClassLoader();
            aDataGenerator = (DataGenerator) java.beans.Beans.instantiate(iiClsLoader, nameOfApplet);
            frame.add("Center", aDataGenerator);
            Dimension size = aDataGenerator.getSize();
            size.setSize(size.width + 400, size.height + 50); //改窗口大小
            frame.setSize(size);
            // add a windowListener for the windowClosedEvent
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.setVisible(true);

            ServerMain server = new ServerMain();
            server.start();
        } catch (Throwable exception) {
            System.err.println("Exception occurred in main() of generator2.DataGenerator " + exception);
            System.exit(APPLICATION_START_ERROR);
        }
    }

    void timeFlowAndRepaint() {
        int newTime = actTime + 1;
        if (newTime <= time.getMaxTime()) {
            setTime(newTime);
            setTimeScrollbar(newTime);
            repaint();
        }
    }

    public void addToLogView(String info){
        getLogListView().add(info,0);
    }

    private void initCooperateEnumerator(int time,int maliciousNum,double mapWidth,double mapHeight) {
        DrawableObjects objects = reporter.getDrawableObjects();
        ArrayList<DrawableSymbol> arr = objects.getAllDrawableSymbols();
        CooperateEnumerator.init(arr,time,maliciousNum,mapWidth,mapHeight);
        System.out.println("size:"+arr.size());
    }

    private JButton getGenerateRouterButton(){
        if (generateRouterButton == null) {
            generateRouterButton = new JButton();
            generateRouterButton.setName("GenerateRouterButton");
            generateRouterButton.setFont(new Font("Dialog", 0, 12));
            generateRouterButton.setLabel("路由数量");
        }
        return generateRouterButton;
    }

    private TextField getRouterNumField() {
        if(routerNumField == null) {
            routerNumField = new TextField();
            routerNumField.setName("RouterNumField");
            routerNumField.setText(ROUTER_NUM);
        }
        return routerNumField;
    }

    private JButton getGeneratePoiButton() {
        if(generatePoiButton == null) {
            generatePoiButton = new JButton();
            generatePoiButton.setName("GeneratePoiButton");
            generatePoiButton.setFont(new Font("Dialog", 0, 12));
            generatePoiButton.setLabel("每种语义位置数量");
        }
        return generatePoiButton;
    }

    private TextField getPoiNumField() {
        if(poiNumField == null) {
            poiNumField = new TextField();
            poiNumField.setName("PoiNumField");
            poiNumField.setText(POI_NUM);
        }
        return poiNumField;
    }

    private JLabel getAnonymousParaLabel() {
        if(anonymousParaLabel == null) {
            anonymousParaLabel = new JLabel();
            anonymousParaLabel.setName("AnonymousParaLabel");
            anonymousParaLabel.setText("匿名组大小");
        }
        return anonymousParaLabel;
    }

    private TextField getAnonymousParaField() {
        if(anonymousParaField == null) {
            anonymousParaField = new TextField();
            anonymousParaField.setName("AnonymousParaField");
            anonymousParaField.setText("10");
        }
        return anonymousParaField;
    }

    private JLabel getProtectRadiusLabel() {
        if(protectRadiusLabel == null) {
            protectRadiusLabel = new JLabel();
            protectRadiusLabel.setName("ProtectRadiusLabel");
            protectRadiusLabel.setText("保护距离");
        }
        return protectRadiusLabel;
    }

    private TextField getProtectRadiusField() {
        if(protectRadiusField == null) {
            protectRadiusField = new TextField();
            protectRadiusField.setName("ProtectRadiusField");
            protectRadiusField.setText(PROTECT_RADIUS);
        }
        return protectRadiusField;
    }

    private JLabel getRequestParaLabel() {
        if(requestParaLabel == null) {
            requestParaLabel = new JLabel();
            requestParaLabel.setName("RequestParaLabel");
            requestParaLabel.setText("单次请求返回POI数量");
        }
        return requestParaLabel;
    }

    private TextField getRequestParaField() {
        if(requestParaField == null) {
            requestParaField = new TextField();
            requestParaField.setName("RequestParaField");
            requestParaField.setText("10");
        }
        return requestParaField;
    }

    private JLabel getMaliciousUserLabel() {
        if(maliciousUserLabel == null) {
            maliciousUserLabel = new JLabel();
            maliciousUserLabel.setName("MaliciousUserLabel");
            maliciousUserLabel.setText("不诚信用户数量");
        }
        return maliciousUserLabel;
    }

    private TextField getMaliciousUserField() {
        if(maliciousUserField == null) {
            maliciousUserField = new TextField();
            maliciousUserField.setName("MaliciousUserField");
            maliciousUserField.setText(MALICIOUS_NUM);
        }
        return maliciousUserField;
    }

    private JLabel getLogLabel() {
        if(logLabel == null) {
            logLabel = new JLabel();
            logLabel.setName("LogLabel");
            logLabel.setText("运行日志");
        }
        return logLabel;
    }

    private List getLogListView() {
        if(logListView == null) {
            logListView = new List();
            logListView.setName("LogListView");
        }
        return logListView;
    }

    private JLabel[] getPoiLabelArr() {
        if (poiLabelArr == null) {
            poiLabelArr = new JLabel[5];
            for (int i=0; i < poiLabelArr.length; i++) {
                poiLabelArr[i] = new JLabel();
                poiLabelArr[i].setName("PoiLabel"+i);
                poiLabelArr[i].setText("poi"+i);
            }
        }
        return poiLabelArr;
    }

    private TextField[] getPoiFieldArr() {
        if (poiFieldArr == null) {
            poiFieldArr = new TextField[5];
            for (int i=0; i < poiFieldArr.length; i++) {
                poiFieldArr[i] = new TextField();
                poiFieldArr[i].setName("PoiField"+i);
                poiFieldArr[i].setText("10");
            }
        }
        return poiFieldArr;
    }

    private JLabel getKLabel() {
        if(KLabel == null) {
            KLabel = new JLabel();
            KLabel.setName("MaliciousUserLabel");
            KLabel.setText("K");
        }
        return KLabel;
    }

    private TextField getKField() {
        if(KField == null) {
            KField = new TextField();
            KField.setName("MaliciousUserField");
            KField.setText("1");
        }
        return KField;
    }

    private JLabel getLLabel() {
        if(LLabel == null) {
            LLabel = new JLabel();
            LLabel.setName("MaliciousUserLabel");
            LLabel.setText("L");
        }
        return LLabel;
    }

    private TextField getLField() {
        if(LField == null) {
            LField = new TextField();
            LField.setName("MaliciousUserField");
            LField.setText("1");
        }
        return LField;
    }

    private JLabel getCRMaxLabel() {
        if(CRMaxLabel == null) {
            CRMaxLabel = new JLabel();
            CRMaxLabel.setName("MaliciousUserLabel");
            CRMaxLabel.setText("CRmax");
        }
        return CRMaxLabel;
    }

    private TextField getCRMaxField() {
        if(CRMaxField == null) {
            CRMaxField = new TextField();
            CRMaxField.setName("MaliciousUserField");
            CRMaxField.setText("1");
        }
        return CRMaxField;
    }

    private JLabel getSigmaLabel() {
        if(sigmaLabel == null) {
            sigmaLabel = new JLabel();
            sigmaLabel.setName("MaliciousUserLabel");
            sigmaLabel.setText("σ");
        }
        return sigmaLabel;
    }

    private TextField getSigmaField() {
        if(sigmaField == null) {
            sigmaField = new TextField();
            sigmaField.setName("MaliciousUserField");
            sigmaField.setText("1");
        }
        return sigmaField;
    }

    /**
     * Evaluates action events.
     *
     * @param e action event
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == getComputeButton())
            compute();
        if ((e.getSource() == getStartButton()) && (time != null)) {
            if(getStartButton().getLabel().equals("开始")) {
                timer = new Timer();
                time.reset();
                actTime = time.getCurrTime();
                setTime(0);
                StatisticTool.reset();
                StatisticTool.getInstance().anonymousNum = Integer.valueOf(anonymousParaField.getText());
                StatisticTool.getInstance().userNum = Integer.valueOf(objBeginText.getText());
                timer.schedule(new TimeFlowTask(this), 100, 500);
                getDeleteButton().setEnabled(false);
                getStartButton().setLabel("停止");
            } else {
                timer.cancel();
                getDeleteButton().setEnabled(true);
                getStartButton().setLabel("开始");
            }
        }
        if (e.getSource() == getDeleteButton()) {
            deleteObjects();
            getDeleteButton().setEnabled(false);
            getStartButton().setEnabled(false);
            getComputeButton().setEnabled(true);
        }

        if (e.getSource() == getGenerateRouterButton())
            RouterGenerator.main(new String[]{routerNumField.getText()});

        if (e.getSource() == getGeneratePoiButton())
            PoiGenerator.main(new String[]{poiNumField.getText()});
    }

    /**
     * Adds components to the applet.
     */
    protected void addComponentsToApplet() {
        super.addComponentsToApplet();
        // remove unnecessary components of the super class
        remove(getTagLabel());
        remove(getValueLabel());
        remove(getClickInfoLabel());
        remove(getShiftClickInfoLabel());
        remove(getPressInfoLabel());
        // add additional components
        add(getComputeButton(), getComputeButton().getName());
        add(getStartButton(), getStartButton().getName());
        add(getTimeScrollbar(), getTimeScrollbar().getName());
        add(getMaxTimeLabel(), getMaxTimeLabel().getName());
        add(getMaxTimeTextField(), getMaxTimeTextField().getName());
        add(getReportProbLabel(), getReportProbLabel().getName());
        add(getReportProbTextField(), getReportProbTextField().getName());
        add(getMsdLabel(), getMsdLabel().getName());
        add(getMsdTextField(), getMsdTextField().getName());
        add(getNumObjClassesLabel(), getNumObjClassesLabel().getName());
        add(getNumObjClassesTextField(), getNumObjClassesTextField().getName());
        add(getNumExtObjClassesTextField(), getNumExtObjClassesTextField().getName());
        add(getObjPerTimeLabel(), getObjPerTimeLabel().getName());
        add(getObjPerTimeTextField(), getObjPerTimeTextField().getName());
        add(getObjBeginLabel(), getObjBeginLabel().getName());
        add(getObjBeginTextField(), getObjBeginTextField().getName());
        add(getExtObjPerTimeTextField(), getExtObjPerTimeTextField().getName());
        add(getExtObjBeginTextField(), getExtObjBeginTextField().getName());
        add(getDeleteButton(), getDeleteButton().getName());

        add(getGenerateRouterButton(), getGenerateRouterButton().getName());
        add(getRouterNumField(), getRouterNumField().getName());
        add(getGeneratePoiButton(), getGeneratePoiButton().getName());
        add(getPoiNumField(), getPoiNumField().getName());
        add(getAnonymousParaLabel(), getAnonymousParaLabel().getName());
        add(getAnonymousParaField(), getAnonymousParaField().getName());
        add(getProtectRadiusLabel(), getProtectRadiusLabel().getName());
        add(getProtectRadiusField(), getProtectRadiusField().getName());
        add(getRequestParaLabel(), getRequestParaField().getName());
        add(getRequestParaField(), getRequestParaField().getName());
        add(getMaliciousUserLabel(), getMaliciousUserLabel().getName());
        add(getMaliciousUserField(), getMaliciousUserField().getName());
        add(getLogLabel(), getLogLabel().getName());
        add(getLogListView(), getLogListView().getName());
        for (JLabel label: getPoiLabelArr())
            add(label, label.getName());
        for (TextField field: getPoiFieldArr())
            add(field, field.getName());
        add(getKLabel(), getKLabel().getName());
        add(getKField(), getKField().getName());
        add(getLLabel(), getLLabel().getName());
        add(getLField(), getLField().getName());
        add(getCRMaxLabel(), getCRMaxLabel().getName());
        add(getCRMaxField(), getCRMaxField().getName());
        add(getSigmaLabel(), getSigmaLabel().getName());
        add(getSigmaField(), getSigmaField().getName());
    }

    /**
     * Adds the components to the listeners.
     */
    protected void addComponentsToListeners() {
        super.addComponentsToListeners();
        getComputeButton().addActionListener(this);
        getStartButton().addActionListener(this);
        getTimeScrollbar().addAdjustmentListener(this);
        getDeleteButton().addActionListener(this);

        getGenerateRouterButton().addActionListener(this);
        getGeneratePoiButton().addActionListener(this);
    }

    /**
     * Reacts on an adjustment event.
     *
     * @param e adjustment event
     */
    public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
        if ((e.getSource() == getTimeScrollbar()) && (time != null)) {
            int newTime = e.getValue() * time.getMaxTime() / (timeScrollbar.getMaximum() - timeScrollbar.getVisibleAmount());
            if (newTime != actTime) ;
            {
                setTime(newTime);
                repaint();
            }
        }
    }

    /**
     * Computes the position of the components.
     */
    public void changeComponentPositions() {
        super.changeComponentPositions();
        getScaleLabel().setBounds(viewX, viewY + viewHeight + 4, 70, 23);
        getComputeButton().setBounds(viewX, viewY + viewHeight + 48, 76, 29);
        getStartButton().setBounds(viewX + viewWidth - 76, viewY + viewHeight + 48, 76, 29);
        getTimeScrollbar().setBounds(viewX, viewY + viewHeight + 140, viewWidth, 18);
        getNameLabel().setBounds(viewX + 90, viewY + viewHeight + 4, 60, 23);
        getMaxTimeLabel().setBounds(viewX, viewY + viewHeight + 82, 140, 23);
        getMaxTimeTextField().setBounds(viewX + 150, viewY + viewHeight + 82, 40, 23);
        getNumObjClassesLabel().setBounds(viewX + viewWidth / 2 + 25, viewY + viewHeight + 82, 155, 23);
        getNumObjClassesTextField().setBounds(viewX + viewWidth / 2 + 185, viewY + viewHeight + 82, 30, 23);
        getNumExtObjClassesTextField().setBounds(viewX + viewWidth / 2 + 220, viewY + viewHeight + 82, 30, 23);
        getObjBeginLabel().setBounds(viewX + viewWidth / 2 + 25, viewY + viewHeight + 1, 155, 23);
        getObjBeginTextField().setBounds(viewX + viewWidth / 2 + 185, viewY + viewHeight + 1, 30, 21);
        getExtObjBeginTextField().setBounds(viewX + viewWidth / 2 + 220, viewY + viewHeight + 1, 30, 21);
        getObjPerTimeLabel().setBounds(viewX + viewWidth / 2 + 25, viewY + viewHeight + 23, 155, 23);
        getObjPerTimeTextField().setBounds(viewX + viewWidth / 2 + 185, viewY + viewHeight + 23, 30, 21);
        getExtObjPerTimeTextField().setBounds(viewX + viewWidth / 2 + 220, viewY + viewHeight + 23, 30, 21);
        getCopyrightLabel().setBounds(viewX, viewY + viewHeight + 180, viewWidth - 100, 19);
        getReportProbLabel().setBounds(viewX, viewY + viewHeight + 112, 148, 23);
        getReportProbTextField().setBounds(viewX + 150, viewY + viewHeight + 112, 40, 23);
        getMsdLabel().setBounds(viewX + 195, viewY + viewHeight + 112, 260, 23);
        getMsdTextField().setBounds(viewX + viewWidth / 2 + 220, viewY + viewHeight + 112, 30, 23);
        getDeleteButton().setBounds(viewX + 150, viewY + viewHeight + 6, 75, 29);

        int gapY = 3;
        int offsetY = viewY;
        int offsetX = viewX + viewWidth + 6;
        int width = 155;
        getGenerateRouterButton().setBounds(offsetX,offsetY,width,29);
        offsetY += getGenerateRouterButton().getHeight() + gapY;
        getRouterNumField().setBounds(offsetX, offsetY, width,23);
        offsetY += getRouterNumField().getHeight() + gapY;
        getGeneratePoiButton().setBounds(offsetX, offsetY, width, 29);
        offsetY += getGeneratePoiButton().getHeight() + gapY;
        getPoiNumField().setBounds(offsetX, offsetY, width, 23);

        offsetY += 100;
        getAnonymousParaLabel().setBounds(offsetX, offsetY, width, 23);
        offsetY += getAnonymousParaLabel().getHeight() + gapY;
        getAnonymousParaField().setBounds(offsetX, offsetY, width, 23);
        offsetY += getAnonymousParaField().getHeight() + gapY;
        getProtectRadiusLabel().setBounds(offsetX, offsetY, width, 23);
        offsetY += getProtectRadiusLabel().getHeight() + gapY;
        getProtectRadiusField().setBounds(offsetX, offsetY, width, 23);
        offsetY += getProtectRadiusField().getHeight() + gapY;
        getRequestParaLabel().setBounds(offsetX, offsetY, width, 23);
        offsetY += getRequestParaLabel().getHeight() + gapY;
        getRequestParaField().setBounds(offsetX, offsetY, width, 23);
        offsetY += getRequestParaField().getHeight() + gapY;
        getMaliciousUserLabel().setBounds(offsetX, offsetY, width, 23);
        offsetY += getMaliciousUserLabel().getHeight() + gapY;
        getMaliciousUserField().setBounds(offsetX, offsetY, width, 23);

        offsetY += 104;
        getLogLabel().setBounds(offsetX, offsetY, width, 23);
        offsetY += getLogLabel().getHeight() + gapY;
        getLogListView().setBounds(offsetX, offsetY, width, 155);

        offsetY = viewY;
        offsetX = viewX + viewWidth + 6 + 230;
//        for(int i=0; i < getPoiLabelArr().length; i++) {
//            JLabel label = getPoiLabelArr()[i];
//            TextField field = getPoiFieldArr()[i];
//            label.setBounds(offsetX, offsetY, 105, 23);
//            offsetY += label.getHeight() + gapY;
//            field.setBounds(offsetX, offsetY, 105, 23);
//            offsetY += label.getHeight() + gapY;
//        }
//        getKLabel().setBounds(offsetX, offsetY, 105, 23);
//        offsetY += getKLabel().getHeight() + gapY;
//        getKField().setBounds(offsetX, offsetY, 105, 23);
//        offsetY += getKField().getHeight() + gapY;
        getLLabel().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getLLabel().getHeight() + gapY;
        getLField().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getLField().getHeight() + gapY;
        getCRMaxLabel().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getCRMaxLabel().getHeight() + gapY;
        getCRMaxField().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getCRMaxField().getHeight() + gapY;
        getSigmaLabel().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getSigmaLabel().getHeight() + gapY;
        getSigmaField().setBounds(offsetX, offsetY, 105, 23);
        offsetY += getSigmaField().getHeight() + gapY;
    }

    class NodeEdgeWrapper {
        public Nodes nodes;
        public Edges edges;
    }

    private NodeEdgeWrapper GetNewNodeEdge(Nodes nodes, Edges edges){
        Edges newEdges = new Edges();
        Nodes newNodes = new Nodes(edges.getNumOfClasses());
        HashMap<Node, LinkedList<Node>> graph = new HashMap<>();
        HashMap<Long, Integer> classMap = new HashMap<>();
        for(Enumeration e = edges.elements(); e.hasMoreElements();) {
            Edge edge = (Edge)e.nextElement();
            Node node1 = edge.getNode1();
            Node node2 = edge.getNode2();
            if (!graph.containsKey(node1.getID()))
                graph.put(node1, new LinkedList<>());
            graph.get(node1).add(node2);
            if (!graph.containsKey(node2.getID()))
                graph.put(node2, new LinkedList<>());
            graph.get(node2).add(node1);

            classMap.put(node1.getID(), edge.getEdgeClass());
            classMap.put(node2.getID(), edge.getEdgeClass());
        }
        HashMap<Long, Node> idMap = new HashMap<>();
        for(Iterator<Map.Entry<Node, LinkedList<Node>>> itr = graph.entrySet().iterator(); itr.hasNext();) {
            Map.Entry<Node, LinkedList<Node>> e = itr.next();
            Node node1 = e.getKey();
            LinkedList<Node> val = e.getValue();
            Node firstNode = null;
            for(Node node2: val) {
                Long newId = Math.min(node1.getID(), node2.getID());
                if (!idMap.containsKey(newId)){
                    Node newNode = new Node(newId, (int)((node1.getX() + node2.getX()) * 0.5), (int)((node1.getY() + node2.getY()) * 0.5));
                    newNodes.newNode(newNode.getID(), newNode.getX(), newNode.getY(), newNode.getName());
                    idMap.put(newId, newNode);
                }
                Node newNode = idMap.get(newId);
                if (firstNode == null)
                    firstNode = newNode;
                else {
                    newEdges.newEdge(newId, classMap.get(newNode.getID()), firstNode, newNode, newNode.getName());
                }
            }
        }
        NodeEdgeWrapper rlt = new NodeEdgeWrapper();
        rlt.nodes = newNodes;
        rlt.edges = newEdges;
        return rlt;
    }

    /**
     * Computes the dataset.
     * Order of initialization: <ol>
     * <li>Time (only first call)
     * <li>ObjectClasses (only first call)
     * <li>ExternalObjectClasses (only first call)
     * <li>ExternalObjectGenerator
     * <li>ExternalObjects (only first call)
     * <li>Reporter
     * <li>WeightManagerForDataGenerator
     * <li>ReRoute
     * <li>ObjectGenerator
     * <li>MovingObjects
     * </ol>
     */
    public synchronized void compute() {
        getComputeButton().setEnabled(false);
        showStatus("initialize generation...");
        // access to the network
        Network net = getNetwork();
        Nodes nodes = net.getNodes();
        Edges edges = net.getEdges();
//        NodeEdgeWrapper newNodeEdge = GetNewNodeEdge(nodes, edges);
//        nodes = newNodeEdge.nodes;
//        edges = newNodeEdge.edges;
        // initialization of the necessary classes
        if (time == null) {
            time = new Time(properties, getValueOfTextField(getMaxTimeTextField(), MIN_MAXTIME, MAX_MAXTIME, false));
            dataspace = new DataSpace(drawableObjects);
            objClasses = createObjectClasses(properties, time, dataspace, getValueOfTextField(getNumObjClassesTextField(), 1, MAX_OBJCLASSES, false),
                    getValueOfTextField(getReportProbTextField(), 0, 1000, false), getValueOfTextField(getMsdTextField(), 1, 1000, true));
            extObjClasses = createExternalObjectClasses(properties, time, dataspace, getValueOfTextField(getNumExtObjClassesTextField(), 1, MAX_EXTOBJCLASSES, false));
            if (properties.getProperty(Reporter.VIZ) != null) {
                for (int c = 0; c < objClasses.getNumber(); c++)
                    for (int i = 0; i <= time.getMaxTime(); i++)
                        DrawablePresentation.newDrawablePresentation("Point" + c + "-" + i, false, objClasses.getColor(c), Color.red, DrawableSymbol.CIRCLE, 8);
                for (int c = 0; c < extObjClasses.getNumber(); c++)
                    for (int i = 0; i <= time.getMaxTime(); i++)
                        DrawablePresentation.newDrawablePresentation("Rectangle" + c + "-" + i, false, extObjClasses.getColor(c), Color.red);
            }
        }
        time.reset();
        edgeClasses.announce(time, dataspace, getValueOfTextField(getMsdTextField(), 1, 1000, true));
        ExternalObjectGenerator extObjGen = createExternalObjectGenerator(properties, time, dataspace, extObjClasses,
                getValueOfTextField(getExtObjPerTimeTextField(), 0, MAX_EXTOBJPERTIME, true), getValueOfTextField(getExtObjBeginTextField(), 0, MAX_EXTOBJBEGIN, true));
        boolean extObjectsExist = extObjGen.externalObjectsExist();
        if (extObjectsExist && (extObjects == null))
            extObjects = new ExternalObjects(properties, time, extObjClasses);
        reporter = createReporter(properties, drawableObjects);
        deleteButton.setEnabled(true);
        WeightManagerForDataGenerator wm = null;
        if (extObjectsExist)
            wm = new WeightManagerForDataGenerator(edgeClasses, objClasses, extObjects);
        else
            wm = new WeightManagerForDataGenerator(edgeClasses, objClasses, null);
        edges.setWeightManager(wm);
        ReRoute reroute = createReRoute(properties, time, dataspace);
        ObjectGenerator objGen = createObjectGenerator(properties, time, dataspace, nodes, objClasses, getValueOfTextField(getObjPerTimeTextField(), 0, MAX_OBJPERTIME, true), getValueOfTextField(getObjBeginTextField(), 0, MAX_OBJBEGIN, true));
        MovingObjects movingObjects = new MovingObjects(wm, net, objGen, reporter, reroute);
        // the time starts
        showStatus("generate data, please wait...");
        util.Timer.reset(1);
        util.Timer.reset(2);
        util.Timer.start(1);
        actTime = time.getCurrTime();
        // traverse the time
        while (!time.isMaximumTimeExceeded()) {
            // move and report all external objects, remove the desd objects
            if (extObjectsExist)
                extObjects.moveAndResizeAndRemoveObjects(actTime, extObjGen, reporter);
            // move and report all moving objects, remove the objects reaching the destination
            movingObjects.move(actTime);
            // generate new external objects
            int numOfNewExtObjects = extObjGen.numberOfNewObjects(actTime);
            for (int i = 0; i < numOfNewExtObjects; i++) {
                ExternalObject extObj = extObjGen.computeExternalObject(actTime);
                extObj.addToContainer(extObjects);
                extObj.reportNewObject(reporter);
            }
            // generate new moving objects
            int numOfNewObjects = objGen.numberOfNewObjects(actTime);
            for (int i = 0; i < numOfNewObjects; i++) {
                // for each new moving object, determine its properties and create it, ...
                int id = objGen.computeId(actTime);
                int objClass = objGen.computeObjectClass(actTime);
                Node start = objGen.computeStartingNode(actTime, objClass);
                //System.out.println(start.getX()+","+start.getY());
                Node dest = objGen.computeDestinationNode(actTime, start, objGen.computeLengthOfRoute(actTime, objClass), objClass);
                MovingObject obj = new MovingObject(id, objClass, start, dest, actTime);
                obj.addToContainer(movingObjects);
                // and compute the (first) route
                while (!obj.computeRoute()) {
                    Node a = objGen.computeStartingNode(actTime, objClass);
                    //System.out.println(a.getX()+","+a.getY());
                    obj.setStart(a);
                    obj.setDestination(objGen.computeDestinationNode(actTime, start, objGen.computeLengthOfRoute(actTime, objClass), objClass));
                }
                obj.reportNewObject(reporter);
            }
            // show object if there is enough time
            if (waitingPeriod > 0) {
                if (!Time.isFirstTimeStamp(actTime)) {
                    for (int c = 0; c < objClasses.getNumber(); c++)
                        DrawablePresentation.get("Point" + c + "-" + (this.actTime - 1)).setVisibility(false);
                    for (int c = 0; c < extObjClasses.getNumber(); c++)
                        DrawablePresentation.get("Rectangle" + c + "-" + (this.actTime - 1)).setVisibility(false);
                }
                for (int c = 0; c < objClasses.getNumber(); c++)
                    DrawablePresentation.get("Point" + c + "-" + this.actTime).setVisibility(true);
                for (int c = 0; c < extObjClasses.getNumber(); c++)
                    DrawablePresentation.get("Rectangle" + c + "-" + this.actTime).setVisibility(true);
                update(getGraphics());
            }
            // to the next time stamp
            time.increaseCurrTime();
            actTime = time.getCurrTime();
            if (actTime % 25 == 0)
                System.gc();
            reportProgress(actTime);
            // wait
            if (waitingPeriod > 0)
                try {
                    wait(waitingPeriod);
                } catch (Exception e) {
                    System.err.println("wait: " + e);
                }
        }
        util.Timer.stop(1);
        // report and remove all still existing objects
        showStatus("remove remaining objects and report statistics...");
        movingObjects.removeObjects();
        if (extObjectsExist)
            extObjects.removeObjects();
        // report statistics
        long totalTime = util.Timer.get(1);
        long routingTime = util.Timer.get(2);
        int numOfRoutes = movingObjects.getTotalNumOfObjects() + reroute.getNumberOfRoutesByEvent() + reroute.getNumberOfRoutesByComparison();
        int dx = dataspace.getMaxX() - dataspace.getMinX();
        int dy = dataspace.getMaxY() - dataspace.getMinY();
        reporter.reportInt("data space minX: ",dataspace.getMinX());
        addToLogView("data space minX: "+dataspace.getMinX());
        reporter.reportInt("data space maxX: ",dataspace.getMaxX());
        addToLogView("data space maxX: "+dataspace.getMaxX());
        reporter.reportInt("data space minY: ",dataspace.getMinY());
        addToLogView("data space minY: "+dataspace.getMinY());
        reporter.reportInt("data space maxY: ",dataspace.getMaxY());
        addToLogView("data space maxY: "+dataspace.getMaxY());
        reporter.reportInt("data space width: ", dx);
        addToLogView("data space width: "+ dx);
        reporter.reportInt("data space height: ", dy);
        addToLogView("data space height: "+ dy);
        reporter.reportInt("number of nodes: ", nodes.numOfNodes());
        reporter.reportInt("number of edges: ", edges.numOfEdges());
        reporter.reportInt("maximum time: ", time.getMaxTime());
        reporter.reportInt("# moving objects: ", movingObjects.getTotalNumOfObjects());
        reporter.reportInt("# points: ", reporter.getNumberOfReportedPoints());
        reporter.reportInt("# traversed nodes: ", movingObjects.getTotalNumberOfTraversedNodes());
        reporter.reportDouble("# nodes/obj: ", ((double) movingObjects.getTotalNumberOfTraversedNodes()) / movingObjects.getTotalNumOfObjects());
        reporter.reportDouble("# traversed degree: ", movingObjects.getTotalDegreeOfTraversedNodes());
        double nodeDegree = (double) (movingObjects.getTotalDegreeOfTraversedNodes() - movingObjects.getTotalNumberOfTraversedNodes()) / movingObjects.getTotalNumberOfTraversedNodes();
        reporter.reportDouble("# node degree: ", nodeDegree);
        reporter.reportInt("# all routes: ", numOfRoutes);
        reporter.reportInt("# routes by event: ", reroute.getNumberOfRoutesByEvent());
        reporter.reportInt("# routes by comparison: ", reroute.getNumberOfRoutesByComparison());
        reporter.reportInt("total time in ms: ", totalTime);
        reporter.reportDouble("total time/obj: ", ((double) totalTime / movingObjects.getTotalNumOfObjects()));
        reporter.reportDouble("total time/point: ", ((double) totalTime / reporter.getNumberOfReportedPoints()));
        reporter.reportDouble("routing time in ms: ", routingTime);
        reporter.reportDouble("insert time: ", BorderHeap.insertTimer.get());
        reporter.reportDouble("fetch time:  ", BorderHeap.fetchTimer.get());
        reporter.reportDouble("change time: ", BorderHeap.changeTimer.get());
        reporter.reportDouble("routing time/obj: ", ((double) routingTime / movingObjects.getTotalNumOfObjects()));
        reporter.reportDouble("routing time/point: ", ((double) routingTime / reporter.getNumberOfReportedPoints()));
        reporter.reportDouble("routing time/node: ", ((double) routingTime / movingObjects.getTotalNumberOfTraversedNodes()));
        reporter.reportDouble("routing time/node/nodedegr: ", ((double) routingTime / movingObjects.getTotalNumberOfTraversedNodes() / nodeDegree));
        reporter.reportDouble("routing time/routing: ", ((double) routingTime / numOfRoutes));
        if (extObjectsExist) {
            reporter.reportInt("# computed decreases: ", extObjects.getNumOfComputedDecreases());
            reporter.reportInt("# real decreases: ", extObjects.getNumOfRealDecreases());
            reporter.reportInt("time for external objects in ms: ", extObjects.getUsedTime());
        }
        long totalDistance = 0;
        for (Enumeration e = edges.elements(); e.hasMoreElements(); ) {
            Edge edge = (Edge) e.nextElement();
            totalDistance += edge.getLength();
        }
        int avDistance = (int) (totalDistance / edges.numOfEdges());
        reporter.reportInt("average edge length: ", avDistance);
        reporter.reportInt("average route length: ", objGen.getAverageRouteLength());
        reporter.close();

        setTime(0);
        setTimeScrollbar(0);
        this.startButton.setEnabled(true);
        repaint();
        showStatus("ready...");

        int maliciousNum = Integer.valueOf(getMaliciousUserField().getText());
        initCooperateEnumerator(time.getMaxTime(),maliciousNum,
                (double) dataspace.getMaxX(),(double) dataspace.getMaxY());
        StatisticTool.reset();
        StatisticTool.getInstance().anonymousNum = Integer.valueOf(anonymousParaField.getText());
        StatisticTool.getInstance().userNum = Integer.valueOf(objBeginText.getText());

        RouterGraph graph = RouterGenerator.loadFromFile();//绘制路由网络
        ArrayList<double[]> nodeArr = graph.getNodes();
        ArrayList<ArrayList<Integer>> edges_ = graph.getEdges();
        for(int a=0; a < edges_.size(); a++) {
            for(int b: edges_.get(a)){
                DrawableLine line = new DrawableLine(
                        (int)(nodeArr.get(a)[0]*CooperateEnumerator.getMapWidth()),
                        (int)(nodeArr.get(a)[1]*CooperateEnumerator.getMapHeight()),
                        (int)(nodeArr.get(b)[0]*CooperateEnumerator.getMapWidth()),
                        (int)(nodeArr.get(b)[1]*CooperateEnumerator.getMapHeight()));
                line.setLayer(5);
                drawableObjects.addDrawable(line);
                line.getPresentation().setVisibility(true);
            }
        }

        generatePoiDrawablePresentation();
        ArrayList<Poi> poiArr = Utils.loadObjectFromFile(ConstantValues.poiArrObjPath);
        double width = 0.01;
        for(Poi poi: poiArr) {
            double x1 = poi.locX * CooperateEnumerator.getMapWidth();
            double y1 = poi.locY * CooperateEnumerator.getMapHeight();
            double x2 = x1 + width * CooperateEnumerator.getMapWidth();
            double y2 = y1 + width * CooperateEnumerator.getMapHeight();
            DrawableRectangle rect = new DrawableRectangle((int)x1, (int)y1, (int)x2, (int)y2);
            rect.setLayer(5);
            rect.setPresentation(DrawablePresentation.get("poiType"+poi.type));
            drawableObjects.addDrawable(rect);
            rect.getPresentation().setVisibility(true);
        }

        //output path data
        DrawableObjects objects = reporter.getDrawableObjects();
        ArrayList<DrawableSymbol> arr = objects.getAllDrawableSymbols();
        HashMap<Long, ArrayList<DrawableSymbol>> pathMap = new HashMap<>();
        for(DrawableSymbol p: arr) {
            if (!pathMap.containsKey(p.getUid()))
                pathMap.put(p.getUid(), new ArrayList<>());
            pathMap.get(p.getUid()).add(p);
//            if (p.getUid() == 0) {
//                double x1 = p.getX();
//                double y1 = p.getY();
//                double x2 = x1 + width * CooperateEnumerator.getMapWidth();
//                double y2 = y1 + width * CooperateEnumerator.getMapHeight();
//                DrawableRectangle rect = new DrawableRectangle((int)x1, (int)y1, (int)x2, (int)y2);
//                rect.setLayer(5);
//                rect.setPresentation(DrawablePresentation.get("poiType"+p.getUid()));
//                drawableObjects.addDrawable(rect);
//                rect.getPresentation().setVisibility(true);
//                width += 0.001;
//            }
        }

        try {
            FileWriter fw = new FileWriter(ConstantValues.pathDataTxtPath,false);
            for(Long k: pathMap.keySet()) {
                fw.write(k + ",");
                arr = pathMap.get(k);
                for (DrawableSymbol p: arr)
                    fw.write("(" + p.getX() + "," + p.getY() + "),");
                fw.write("\n");
            }
            fw.close();
        } catch (Exception ex){
            LogView.addLog(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void generatePoiDrawablePresentation() {
        for (int i=0; i < 5; i++) {
            DrawablePresentation pres = DrawablePresentation.newDrawablePresentation("poiType"+i);
            pres.setColor(getPoiColor(i));
        }
    }

    private Color getPoiColor(int poiType) {
        switch (poiType) {
            case 0: return Color.red;
            case 1: return Color.black;
            case 2: return Color.yellow;
            case 3: return Color.blue;
            default: return Color.green;
        }
    }

    /**
     * Calls the constructor of EdgeClasses.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties the properties of the generator
     * @return an object of the class EdgeClasses
     */
    public abstract EdgeClasses createEdgeClasses(Properties properties);

    /**
     * Calls the constructor of ExternalObjectClasses.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties   properties of the generator
     * @param time         the time object
     * @param ds           the data space
     * @param numOfClasses number of external object classes
     * @return an object of ExternalObjectClasses
     */
    public abstract ExternalObjectClasses createExternalObjectClasses(Properties properties, Time time, DataSpace ds, int numOfClasses);

    /**
     * Calls the constructor of ExternalObjectGenerator.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties         properties of the generator
     * @param time               the time object
     * @param dataspace          the dataspace
     * @param classes            the classes of external objects
     * @param numOfExtObjPerTime number of external objects per time
     * @param numAtBeginning     number of external objects at the beginning
     * @return an external object generator
     */
    public abstract ExternalObjectGenerator createExternalObjectGenerator(Properties properties, Time time, DataSpace dataspace, ExternalObjectClasses classes, int numOfExtObjPerTime, int numAtBeginning);

    /**
     * Calls the constructor of ObjectClasses.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties      properties of the generator
     * @param time            the time object
     * @param ds              the data space
     * @param numOfClasses    number of object classes
     * @param reportProb      report probability (0-1000)
     * @param maxSpeedDivisor maximum speed divisor
     * @return an object of ObjectClasses
     */
    public abstract ObjectClasses createObjectClasses(Properties properties, Time time, DataSpace ds, int numOfClasses, int reportProb, int maxSpeedDivisor);

    /**
     * Calls the constructor of ObjectGenerator.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties          properties of the generator
     * @param time                the time object
     * @param ds                  the dataspace
     * @param nodes               the nodes of the network
     * @param objClasses          description of the object classes
     * @param numOfObjPerTime     indicator for the number of objects per time
     * @param numOfObjAtBeginning indicator for the number of objects at the beginning
     * @return an object generator
     */
    public abstract ObjectGenerator createObjectGenerator(Properties properties, Time time, DataSpace ds, Nodes nodes, ObjectClasses objClasses, int numOfObjPerTime, int numOfObjAtBeginning);

    /**
     * Calls the constructor of Reporter.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties properties of the generator
     * @param objects    container of drawable objects
     * @return the reporter
     */
    public abstract Reporter createReporter(Properties properties, DrawableObjects objects);

    /**
     * Calls the constructor of ReRoute.
     * Must be implemented by a subclass of DataGenerator.
     *
     * @param properties properties of the generator
     * @param time       the time object
     * @param ds         the data space
     * @return an object of ReRoute
     */
    public abstract ReRoute createReRoute(Properties properties, Time time, DataSpace ds);

    /**
     * Deletes the generated moving and external objects.
     */
    protected void deleteObjects() {
        if (reporter != null)
            reporter.removeReportedObjects();
        repaint();
    }

    /**
     * Creates / Returns the add time button.
     *
     * @return add time button
     */
    protected JButton getStartButton() {
        if (startButton == null) {
            startButton = new JButton();
            startButton.setName("StartButton");
            startButton.setFont(new Font("Dialog", 0, 12));
            startButton.setLabel("开始");
            startButton.setEnabled(false);
        }
        ;
        return startButton;
    }

    /**
     * Creates / Returns the compute button.
     *
     * @return compute button
     */
    protected JButton getComputeButton() {
        if (computeButton == null) {
            computeButton = new JButton();
            computeButton.setName("ComputeButton");
            computeButton.setFont(new Font("Dialog", 0, 12));
            computeButton.setLabel("计算");
            computeButton.setEnabled(false);
        }
        ;
        return computeButton;
    }

    /**
     * Creates / Returns the delete button.
     *
     * @return delete button
     */
    protected JButton getDeleteButton() {
        if (deleteButton == null) {
            deleteButton = new JButton();
            deleteButton.setName("DeleteObjectsButton");
            deleteButton.setFont(new Font("Dialog", 0, 12));
            deleteButton.setLabel("清除");
            deleteButton.setEnabled(false);
        }
        ;
        return deleteButton;
    }

    /**
     * Creates / Returns the external objects at the beginning text field.
     *
     * @return text field
     */
    protected TextField getExtObjBeginTextField() {
        if (extobjBeginText == null) {
            extobjBeginText = new TextField("0");
            extobjBeginText.setName("ExtObjBeginTextField");
            extobjBeginText.setFont(new Font("Dialog", 0, 11));
            extobjBeginText.setEnabled(false);
        }
        ;
        return extobjBeginText;
    }

    /**
     * Creates / Returns the external objects per time text field.
     *
     * @return text field
     */
    protected TextField getExtObjPerTimeTextField() {
        if (extobjPerTimeText == null) {
            extobjPerTimeText = new TextField("0");
            extobjPerTimeText.setName("ExtObjPerTimeTextField");
            extobjPerTimeText.setFont(new Font("Dialog", 0, 11));
            extobjPerTimeText.setEnabled(false);
        }
        ;
        return extobjPerTimeText;
    }

    /**
     * Returns an info text for a drawable object.
     *
     * @param obj drawable object
     * @return info text
     */
    protected String getInfoText(DrawableObject obj) {
        return null;
    }

    /**
     * Creates / returns the maximum time label.
     *
     * @return maximum time label
     */
    protected JLabel getMaxTimeLabel() {
        if (maxTimeLabel == null) {
            maxTimeLabel = new JLabel();
            maxTimeLabel.setName("MaxTimeLabel");
            maxTimeLabel.setFont(new Font("sansserif", 0, 12));
            //maxTimeLabel.setText("maximum time (" + MIN_MAXTIME + "-" + MAX_MAXTIME + "):");
            maxTimeLabel.setText("时间: ");
        }
        ;
        return maxTimeLabel;
    }

    /**
     * Creates / Returns the maximum time text field.
     *
     * @return maximum time text field
     */
    protected TextField getMaxTimeTextField() {
        if (maxTimeText == null) {
            maxTimeText = new TextField("20");
            maxTimeText.setName("MaxTimeTextField");
            maxTimeText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return maxTimeText;
    }

    /**
     * Creates / returns the maximum speed divisor label.
     *
     * @return label
     */
    protected JLabel getMsdLabel() {
        if (msdLabel == null) {
            msdLabel = new JLabel();
            msdLabel.setName("MsdLabel");
            msdLabel.setFont(new Font("sansserif", 0, 12));
            msdLabel.setText("max.speed div. (10=fast,50=middle,250=slow):");
        }
        ;
        return msdLabel;
    }

    /**
     * Creates / Returns the maximum speed divisor text field.
     *
     * @return text field
     */
    protected TextField getMsdTextField() {
        if (msdText == null) {
            msdText = new TextField("50");
            msdText.setName("ReportProbTextField");
            msdText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return msdText;
    }

    /**
     * Creates / Returns the number of object classes text field.
     *
     * @return number of object classes text field
     */
    protected TextField getNumExtObjClassesTextField() {
        if (numExtObjClassesText == null) {
            numExtObjClassesText = new TextField("3");
            numExtObjClassesText.setName("NumExtObjClassesTextField");
            numExtObjClassesText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return numExtObjClassesText;
    }

    /**
     * Creates / returns the number of object classes label.
     *
     * @return number of object classes label
     */
    protected JLabel getNumObjClassesLabel() {
        if (numObjClassesLabel == null) {
            numObjClassesLabel = new JLabel();
            numObjClassesLabel.setName("NumObjClassesLabel");
            numObjClassesLabel.setFont(new Font("sansserif", 0, 12));
            numObjClassesLabel.setText("classes (M:1-" + MAX_OBJCLASSES + "/E:1-" + MAX_EXTOBJCLASSES + "):");
        }
        ;
        return numObjClassesLabel;
    }

    /**
     * Creates / Returns the number of object classes text field.
     *
     * @return number of object classes text field
     */
    protected TextField getNumObjClassesTextField() {
        if (numObjClassesText == null) {
            numObjClassesText = new TextField("6");
            numObjClassesText.setName("NumObjClassesTextField");
            numObjClassesText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return numObjClassesText;
    }

    /**
     * Creates / returns the external objects per time label.
     *
     * @return label
     */
    protected JLabel getObjBeginLabel() {
        if (objBeginLabel == null) {
            objBeginLabel = new JLabel();
            objBeginLabel.setName("ExtObjPerTimeLabel");
            objBeginLabel.setFont(new Font("sansserif", 0, 12));
            //objBeginLabel.setText("obj./begin (M:-" + MAX_OBJBEGIN + " E:-" + MAX_EXTOBJBEGIN + "):");
            objBeginLabel.setText("总人数: ");
        }
        ;
        return objBeginLabel;
    }

    /**
     * Creates / Returns the moving objects at the beginning text field.
     *
     * @return text field
     */
    protected TextField getObjBeginTextField() {
        if (objBeginText == null) {
            objBeginText = new TextField(USER_NUM);
            objBeginText.setName("ObjBeginTextField");
            objBeginText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return objBeginText;
    }

    /**
     * Creates / returns the objects per time label.
     *
     * @return objects per time label
     */
    protected JLabel getObjPerTimeLabel() {
        if (objPerTimeLabel == null) {
            objPerTimeLabel = new JLabel();
            objPerTimeLabel.setName("ObjPerTimeLabel");
            objPerTimeLabel.setFont(new Font("sansserif", 0, 12));
            objPerTimeLabel.setText("obj./time (M:-" + MAX_OBJPERTIME + "/E:-" + MAX_EXTOBJPERTIME + "):");
        }
        ;
        return objPerTimeLabel;
    }

    /**
     * Creates / Returns the objects per time text field.
     *
     * @return objects per time text field
     */
    protected TextField getObjPerTimeTextField() {
        if (objPerTimeText == null) {
            objPerTimeText = new TextField("0");
            objPerTimeText.setName("ObjPerTimeTextField");
            objPerTimeText.setFont(new Font("Dialog", 0, 11));
            objPerTimeText.setEnabled(false);
        }
        ;
        return objPerTimeText;
    }

    /**
     * Returns an integer property.
     *
     * @param key          name of the key
     * @param defaultValue the default value
     * @return the integer value
     */
    protected int getProperty(String key, int defaultValue) {
        return getProperty(properties, key, defaultValue);
    }

    /**
     * Returns an integer property.
     *
     * @param properties   the properties
     * @param key          name of the key
     * @param defaultValue the default value
     * @return the integer value
     */
    public static int getProperty(Properties properties, String key, int defaultValue) {
        try {
            return new Integer(properties.getProperty(key)).intValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Creates / returns the report probabilty label.
     *
     * @return label
     */
    protected JLabel getReportProbLabel() {
        if (reportProbLabel == null) {
            reportProbLabel = new JLabel();
            reportProbLabel.setName("ReportProbLabel");
            reportProbLabel.setFont(new Font("sansserif", 0, 12));
            reportProbLabel.setText("report probability (0-1000):");
        }
        ;
        return reportProbLabel;
    }

    /**
     * Creates / Returns the report probabilty text field.
     *
     * @return text field
     */
    protected TextField getReportProbTextField() {
        if (reportProbText == null) {
            reportProbText = new TextField("1000");
            reportProbText.setName("ReportProbTextField");
            reportProbText.setFont(new Font("Dialog", 0, 11));
        }
        ;
        return reportProbText;
    }

    /**
     * Creates / Returns the time scrollbar.
     *
     * @return East-JButton
     */
    protected Scrollbar getTimeScrollbar() {
        if (timeScrollbar == null) {
            timeScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 0, 32, 0, viewWidth - 32);
            timeScrollbar.setName("TimeScrollbar");
        }
        ;
        return timeScrollbar;
    }

    /**
     * Computes the value of the text field and adapt it.
     *
     * @param tf           the text field
     * @param min          minimum allowed value
     * @param max          maximum allowed value
     * @param enabledAfter should the text filed be enabled after computing the value?
     * @return integer value of text field
     */
    protected int getValueOfTextField(TextField tf, int min, int max, boolean enabledAfter) {
        int intValue = new Integer(tf.getText()).intValue();
        if (intValue < min)
            intValue = min;
        else if (intValue > max)
            intValue = max;
        tf.setText(String.valueOf(intValue));
        tf.setEditable(enabledAfter);
        tf.setEnabled(enabledAfter);
        return intValue;
    }

    /**
     * Initializes the data generator.
     * Initializes the class EdgeClasses.
     */
    public void init() {
        LogView.init(this);
        String p = getParameter("propertyfile");
        if (p != null)
            propFilename = p;
        // read properties
        try {
            URL url = computeURL(propFilename);
            InputStream in = url.openStream();
            properties.load(in);
        } catch (MalformedURLException mex) {
            System.err.println("DataGenerator.init: " + mex);
            System.exit(PROPERTY_FILE_ERROR);
        } catch (IOException ioex) {
            System.err.println("DataGenerator.init: " + ioex);
            System.exit(PROPERTY_FILE_ERROR);
        }
        // init drawable objects
        edgeClasses = createEdgeClasses(properties);
        if (drawableObjects == null)
            drawableObjects = new DrawableObjectsWithSearchTree(numOfLayers, new MemoryRTree());
        super.init();
    }

    /**
     * Inits the presentation of the network.
     */
    protected void initDrawablePresentation() {
        super.initDrawablePresentation();
        int num = edgeClasses.getNumber();
        // Minimum scales for depicting edges and nodes
        int[] nodeMinScale = new int[num + 1];
        for (int i = 0; i < num + 1; i++)
            nodeMinScale[i] = 0;
        int[] edgeMinScale = new int[num];
        for (int i = 0; i < num; i++)
            edgeMinScale[i] = edgeClasses.getMinScale(i);
        // Colors of edges and nodes
        Color[] nodeColor = new Color[num + 1];
        for (int i = 0; i < num + 1; i++)
            nodeColor[i] = Color.gray;
        Color[] edgeColor = new Color[num];
        for (int i = 0; i < num; i++)
            edgeColor[i] = edgeClasses.getColor(i);
        // Setting
        net.getEdges().setNumOfClasses(num);
        net.getEdges().initPresentation(edgeColor, Color.red);
        net.getEdges().setMinScaleArray(edgeMinScale);
        net.getNodes().setNumOfClasses(num + 1);
        net.getNodes().initPresentation(nodeColor, Color.red);
        net.getNodes().setMinScaleArray(nodeMinScale);
    }

    /**
     * Evaluates the properties of the property file.
     */
    protected void interpretParameters() {
        // evaluate generator properties
        MIN_MAXTIME = getProperty("MIN_MAXTIME", MIN_MAXTIME);
        MAX_MAXTIME = getProperty("MAX_MAXTIME", MAX_MAXTIME);
        MAX_OBJCLASSES = getProperty("MAX_OBJCLASSES", MAX_OBJCLASSES);
        MAX_OBJPERTIME = getProperty("MAX_OBJPERTIME", MAX_OBJPERTIME);
        MAX_OBJBEGIN = getProperty("MAX_OBJBEGIN", MAX_OBJBEGIN);
        MAX_EXTOBJCLASSES = getProperty("MAX_EXTOBJCLASSES", MAX_EXTOBJCLASSES);
        MAX_EXTOBJPERTIME = getProperty("MAX_EXTOBJPERTIME", MAX_EXTOBJPERTIME);
        MAX_EXTOBJBEGIN = getProperty("MAX_EXTOBJBEGIN", MAX_EXTOBJBEGIN);

        waitingPeriod = getProperty("waitingPeriod", waitingPeriod);
        // evaluate showmap parameters
        baseScaleFactor = getProperty("baseScaleFactor", 1);
        minScale = getProperty("minScale", minScale);
        maxScale = getProperty("maxScale", maxScale);
        scale = getProperty("scale", maxScale);
        viewWidth = getProperty("viewWidth", viewWidth);
        viewHeight = getProperty("viewHeight", viewHeight);
        mapWidth = getProperty("mapWidth", scale * viewWidth);
        mapHeight = getProperty("mapHeight", scale * viewHeight);
        viewX = getProperty("viewX", viewX);
        viewY = getProperty("viewY", viewY);
        String p = properties.getProperty("color");
        if (p != null)
            backgroundColor = ColorDefiner.getColor(p);
        p = properties.getProperty("mapColor");
        if (p != null)
            mapColor = ColorDefiner.getColor(p);
        p = properties.getProperty("language");
        if ((p != null) && p.equals("D"))
            language = GERMAN;
    }

    /**
     * Makes the file name to an absolute file name
     *
     * @param fileName the name of the file
     */
    protected static String makeAbsolute(String fileName) {
        try {
            return new File(fileName).getAbsolutePath();
        } catch (Exception ex) {
            return fileName;
        }
    }

    /**
     * Reports the progress of the generation.
     *
     * @param time actual time
     */
    protected void reportProgress(int time) {
        setTimeScrollbar(time);
    }

    /**
     * Sets the status of the applet. If the status is COMPLETE, the compute button
     * will be enabled. If the maximum time text field has been changed, the computation
     * will automatically be started.
     *
     * @param state the new state
     */
    protected void setState(int state) {
        super.setState(state);
        if (state != COMPLETE)
            return;
        Rectangle r = drawableObjects.getDataspace();
        //System.out.println("complete: "+r);
        this.movePos(r.x + r.width / 2, r.y + r.height, scale);
        repaint();
        // Automatic computation
        if (getValueOfTextField(getMaxTimeTextField(), MIN_MAXTIME, MAX_MAXTIME, true) != 20)
            compute();
        // Compute button
        getComputeButton().setEnabled(true);
    }

    /**
     * Sets the actual time for displaying purposes.
     *
     * @param actTime int
     */
    protected void setTime(int actTime) {
        // reset old state
        if (this.actTime != 0) {
            for (int c = 0; c < objClasses.getNumber(); c++)
                DrawablePresentation.get("Point" + c + "-" + this.actTime).setVisibility(false);
            for (int c = 0; c < extObjClasses.getNumber(); c++)
                DrawablePresentation.get("Rectangle" + c + "-" + this.actTime).setVisibility(false);
        } else
            for (int t = 0; t <= time.getMaxTime(); t++) {
                for (int c = 0; c < objClasses.getNumber(); c++)
                    DrawablePresentation.get("Point" + c + "-" + t).setVisibility(false);
                for (int c = 0; c < extObjClasses.getNumber(); c++)
                    DrawablePresentation.get("Rectangle" + c + "-" + t).setVisibility(false);
            }
        // set new state
        if (actTime != 0) {
            for (int c = 0; c < objClasses.getNumber(); c++)
                DrawablePresentation.get("Point" + c + "-" + actTime).setVisibility(true);
            for (int c = 0; c < extObjClasses.getNumber(); c++)
                DrawablePresentation.get("Rectangle" + c + "-" + actTime).setVisibility(true);
        } else
            for (int t = 0; t <= time.getMaxTime(); t++) {
                for (int c = 0; c < objClasses.getNumber(); c++)
                    DrawablePresentation.get("Point" + c + "-" + t).setVisibility(true);
                for (int c = 0; c < extObjClasses.getNumber(); c++)
                    DrawablePresentation.get("Rectangle" + c + "-" + t).setVisibility(true);
            }
        this.actTime = actTime;
        time.setCurrTime(actTime);
        getNameLabel().setText("Time: " + String.valueOf(actTime));

        if(actTime > 0 && actTime <= time.getMaxTime()) {
//            ArrayList<DrawableSymbol> pointArr = CooperateEnumerator.getPointArr();
//            for (DrawableSymbol point : pointArr)
//                if (point.getPresentation().getVisibility() && CooperateEnumerator.isRequestPoint(point))
//                    System.out.println(point.getX() + "," + point.getY());
//            System.out.println("---------------");
            LogView.addLog("time:"+actTime);
            int poiNum = Integer.valueOf(getRequestParaField().getText());
            int anonymousNum = Integer.valueOf(getAnonymousParaField().getText());
            int protectRadius = Integer.valueOf(getProtectRadiusField().getText());
            CooperateEnumerator.usersSendRequests(poiNum, anonymousNum, protectRadius);
        }

        if(actTime == time.getMaxTime()){
            timer.cancel();
            getDeleteButton().setEnabled(true);
            getStartButton().setLabel("开始");
            StatisticTool.getInstance().appendToFile();
        }
//        DrawableObjects objects = reporter.getDrawableObjects();
//        ArrayList<DrawableSymbol> arr = objects.getAllDrawableSymbols();
//        int count = 0;
//        for(DrawableSymbol cc: arr){
//            Color clr = cc.getPresentation().getColor();
//            if(clr == Color.BLUE && cc.getPresentation().getVisibility())
//                count++;
//        }
//        System.out.println(count);
    }

    /**
     * Sets the position of the time scrollbar and sets the time text field.
     *
     * @param t time
     */
    protected void setTimeScrollbar(int t) {
        int value = t * (timeScrollbar.getMaximum() - timeScrollbar.getVisibleAmount()) / time.getMaxTime();
        timeScrollbar.setValue(value);
        getNameLabel().setText("Time: " + String.valueOf(t));
    }

    /**
     * Shows the status.
     *
     * @param text text to be displayed
     */
    public void showStatus(String text) {
        super.showStatus(text);
        if (frame != null)
            frame.setTitle("Network Generator: " + text);
    }

    /**
     * Interprets the properties "urlne" (base name of unzipped network files) or "urlnez" (base name of zipped network files)
     * and starts the loading thread.
     * This method must be overwritten by a superclass, which reads the network from elsewhere.
     */
    protected void startLoadingThread() {
        // determine filenames
        URL url[] = {null, null, null};
        String filename = properties.getProperty("urlne");
        if (filename != null) {
            url[1] = computeURL(filename + ".node");
            url[2] = computeURL(filename + ".edge");
        } else {
            filename = properties.getProperty("urlnez");
            if (filename == null) {
                System.err.println("no network file determined");
                System.exit(NETWORKFILE_ERROR);
            }
            url[1] = computeURL(filename + ".node.zip");
            url[2] = computeURL(filename + ".edge.zip");
        }
        // start thread
        new LoadDrawables(this, url, 0).start();
    }

    /**
     * Sets the viewpoint to the value predefined by the parameters.
     */
    public void setViewToPrefinedValue() {
        viewMapX = getProperty("posx", mapWidth / 2 / scale);
        viewMapY = getProperty("posy", mapHeight / 2 / scale);
    }
}