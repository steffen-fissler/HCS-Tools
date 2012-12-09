package de.mpicbg.tds.knime.hcstools.visualization.heatmapviewer;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import info.clearthought.layout.TableLayout;

import de.mpicbg.tds.barcodes.BarcodeParserFactory;
import de.mpicbg.tds.core.Utils;
import de.mpicbg.tds.core.model.Plate;
import de.mpicbg.tds.core.model.Well;
import de.mpicbg.tds.core.util.PanelImageExporter;

/**
 * User: Felix Meyenhofer
 * Date: 11/27/12
 * Time: 16:43
 */

public class ScreenHeatMapsPanel extends JPanel implements HeatMapModelChangeListener {

    private HeatMapModel heatMapModel = new HeatMapModel();
    public List<PlateOverviewHeatMap> heatmaps;
    private int MIN_HEATMAP_WIDTH = 200;
    private int PREFERRED_WITH = 600;
    private int PREFERRED_HEIGHT = 350;

    private HeatMapMenu menu;
    private HeatMapToolbar toolbar;
    private JPanel heatMapsContainer;
    private HeatMapColorToolBar colorbar;


    // Constructors
    public ScreenHeatMapsPanel(HeatMapMenu heatMapMenu) {
        initialize();
        setMinimumSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
        setPreferredSize(new Dimension(PREFERRED_WITH, PREFERRED_HEIGHT));
        menu = heatMapMenu;
        heatMapModel.addChangeListener(this);
        new PanelImageExporter(this, true);
    }

    public ScreenHeatMapsPanel(HeatMapMenu heatMapMenu, List<Plate> plates) {
        this(heatMapMenu);

        ToolTipManager.sharedInstance().setDismissDelay(7500);
        ToolTipManager.sharedInstance().setInitialDelay(500);
        setPlates(plates);

        // relayout the app when the window size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (getWidth() > 0) {
                    repopulatePlateGrid();
                }
            }
        });

        heatMapsContainer.setDoubleBuffered(true);
    }


    // Methods
    private void initialize() {
        toolbar = new HeatMapToolbar();

        heatMapsContainer = new JPanel();
        heatMapsContainer.setPreferredSize(new Dimension(PREFERRED_WITH-10,PREFERRED_HEIGHT-10));
        heatMapsContainer.setLayout(new TableLayout(new double[][]{{TableLayout.PREFERRED}, {TableLayout.PREFERRED}}));
        TableLayout layout = (TableLayout) heatMapsContainer.getLayout();
        layout.setHGap(5);
        layout.setVGap(5);

        JTextArea text = new JTextArea("heatmap container panel");
        text.setEditable(false);
        heatMapsContainer.add(text, "0, 0");

        colorbar = new HeatMapColorToolBar();
//        JToolBar colortoolbar = new JToolBar();
//        colortoolbar.add(colorbar);

        JScrollPane heatMapScrollPane = new JScrollPane();
        heatMapScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        heatMapScrollPane.setViewportView(heatMapsContainer);
        heatMapScrollPane.setBackground(Color.BLACK);  //TODO: This is just to better distinguish the components. Should be removed before deployment.

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(heatMapScrollPane, BorderLayout.CENTER);
        add(colorbar, BorderLayout.SOUTH);
    }

    public void setPlates(List<Plate> plates) {
        heatmaps = new ArrayList<PlateOverviewHeatMap>();
        for (Plate plate : plates) {
            heatmaps.add(new PlateOverviewHeatMap(plate, heatMapModel));
        }

        // pre-configure the heatmap configuration model
        heatMapModel.setScreen(plates);
        parsePlateBarCodes();
        colorbar.configure(heatMapModel);
        toolbar.configure(heatMapModel);
//        heatMapViewerMenu.configure(heatMapModel);
    }

    protected void zoom(double zoomFactor) {
        MIN_HEATMAP_WIDTH *= zoomFactor;
        repopulatePlateGrid();
    }

    private void repopulatePlateGrid() {
        sortHeatmaps();
        List<PlateOverviewHeatMap> heatmapSelection = getFilteredHeatMap();

        int numColumns = (int) Math.floor(getWidth() / MIN_HEATMAP_WIDTH);

        //patch the layout if only a few plates are selected
        if (numColumns > heatmapSelection.size())
            numColumns = heatmapSelection.size();

        double[] columnConfig = new double[numColumns];
        for (int i = 0; i < columnConfig.length; i++) {
            columnConfig[i] = 1. / (double) numColumns;
        }
        int numRows = (int) Math.ceil(heatmapSelection.size() / (double) numColumns);
        double[] rowConfig = new double[numRows];
        for (int i = 0; i < rowConfig.length; i++) {
            rowConfig[i] = 1. / (double) numRows;
        }

        // fix the table-layout config for 1 (which would be interpreted as a single pixel otherwise
        if (numRows == 1)
            rowConfig[0] = TableLayout.FILL;
        if (numColumns == 1)
            columnConfig[0] = TableLayout.FILL;

        // remove existing components
        heatMapsContainer.removeAll();

        // Set the grid
        TableLayout containerLayout = (TableLayout) heatMapsContainer.getLayout();
        containerLayout.setRow(rowConfig);
        containerLayout.setColumn(columnConfig);

        // track changes of the batch; this allows to alter the background color of the heatmaps
        String lastBatchName = "bubabuba";
        Color batchBcknd = Color.GRAY;

        // populate the view with plates
        int plateNameFontSize = Utils.isWindowsPlatform() ? 8 : 12;
        Font barcodeFont = new Font("Serif", Font.PLAIN, plateNameFontSize);

        for (int i = 0; i < heatmapSelection.size(); i++) {
            PlateOverviewHeatMap heatMapPanel = heatmapSelection.get(i);
            Plate plate = heatMapPanel.getPlate();

            int rowIndex = i / numColumns;
            String gridPosition = (i - rowIndex * numColumns) + ", " + (rowIndex);

            JPanel plateContainer = new JPanel();
            plateContainer.setBorder(new TitledBorder(null, plate.getBarcode(), TitledBorder.CENTER, TitledBorder.BOTTOM, barcodeFont));

            plateContainer.setLayout(new BorderLayout());

            // change the background according to the batch
            String curBatchName = plate.getBatchName();
            if (curBatchName != null) {
                if ((curBatchName == null && lastBatchName != null) || !curBatchName.equals(lastBatchName)) {
                    lastBatchName = curBatchName;
                    batchBcknd = batchBcknd.equals(Color.GRAY) ? Color.LIGHT_GRAY : Color.GRAY;
                }
            }
            plateContainer.setBackground(batchBcknd);

            heatMapsContainer.add(plateContainer, gridPosition);
            plateContainer.add(heatMapPanel, BorderLayout.CENTER);
        }

        heatMapsContainer.setPreferredSize(new Dimension(numColumns * MIN_HEATMAP_WIDTH, numRows * MIN_HEATMAP_WIDTH));
        invalidate();
        updateUI();
        repaint();
    }

    private void sortHeatmaps() {
        heatmaps.clear();
        List<Plate> plates = heatMapModel.getScreen();
        heatmaps = new ArrayList<PlateOverviewHeatMap>();
        for (Plate plate : plates) {
            heatmaps.add(new PlateOverviewHeatMap(plate, heatMapModel));
        }
    }

    private void parsePlateBarCodes() {
        for (PlateOverviewHeatMap heatmap : heatmaps) {
            Plate plate = heatmap.getPlate();
            if (plate.getScreenedAt() != null) {
                continue;
            }

            try {
                Plate.configurePlateByBarcode(plate, BarcodeParserFactory.getAssayPlateBarcodeOLDParser(plate.getBarcode()));
            } catch (Throwable t) {
                // do nothing here
//                heatMapViewerMenu.setSortingEnabled(false);
            }
        }
    }

    public List<PlateOverviewHeatMap> getFilteredHeatMap() {

        List<PlateOverviewHeatMap> heatMapSelection = new ArrayList<PlateOverviewHeatMap>();
        for (PlateOverviewHeatMap heatmap : heatmaps) {
            Plate plate = heatmap.getPlate();
            if(heatMapModel.isSelected(plate)) {
                heatMapSelection.add(heatmap);
            }
        }
        return heatMapSelection;
    }

    public void setSelection(Collection<Well> wellSelection) {
        // sort the selected wells according to plate

        Map<Plate, Collection<Well>> plateWells = new HashMap<Plate, Collection<Well>>();
        for (PlateOverviewHeatMap heatmap : heatmaps) {
            plateWells.put(heatmap.getPlate(), new ArrayList<Well>());
        }

        heatMapModel.setWellSelection(wellSelection);
        repaint();

//        heatMapModel.setWellSelection(wellSelection);
//        for (Well well : wellSelection) {
//            plateWells.get(well.getPlate()).add(well);
//        }
//
//        // propagate the selection to the different panels
//        for (PlateOverviewHeatMap heatmap : heatmaps) {
//            Collection<Well> plateSelection = plateWells.get(heatmap.getPlate());
//
//            heatmap.setSelection(plateSelection);
//        }
    }

    public void modelChanged() {
        if (isVisible() && getWidth() > 0) {
            repopulatePlateGrid();
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(200, 500));
        frame.add(new ScreenHeatMapsPanel(null));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
