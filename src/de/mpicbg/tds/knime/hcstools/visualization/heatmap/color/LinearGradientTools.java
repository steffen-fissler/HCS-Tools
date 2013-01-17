package de.mpicbg.tds.knime.hcstools.visualization.heatmap.color;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * Useful manipulations for a LinearGradientPaint instance.
 *
 * @author Felix Meyenhofer
 *         12/10/12
 */
public abstract class LinearGradientTools {

    /**
     * Available default color maps.
     */
    public static final String[] MAP_GB = {"GB", "green-black"};
    public static final String[] MAP_HSV = {"HSV", "hsv"};
    public static final String[] MAP_GBR = {"GBR", "green-black-red"};
    public static final String[] MAP_JET = {"Jet", "jet"};
    public static final String[] MAP_DARK = {"Dark", "dark"};


    /**
     * Get a color at a particular fraction of the gradient.
     *
     * @param painter to fetch the color from
     * @param input fraction [0...1]
     * @return the color at the input fraction
     */
    public static Color getColorAt(LinearGradientPaint painter, float input) {
        int lowerIndex = 0;
        int upperIndex = 1;
        float lowerBound = 0;
        float upperBound = 1;
        float[] fractions = painter.getFractions();
        Color[] colors = painter.getColors();

        if ( input > 1 ) {
            input = 1f;
        }
        if ( input < 0 ) {
            input = 0f;
        }

        int index = 0;
        for (float fraction : fractions ) {

            if ( fraction < input ) {
                lowerBound = fraction;
                lowerIndex = index;
            } else if ( fraction == input ) {
                return colors[index];
            } else if ( fraction > input ) {
                upperBound = fraction;
                upperIndex = index;
            }
            index++;
        }

        float rescaled = (input - lowerBound) / (upperBound - lowerBound);

        return interpolateColor(colors[lowerIndex], colors[upperIndex], rescaled);
    }


    /**
     * Linear interpolator between two colors
     *
     * @param color1 first bound
     * @param color2 second bound
     * @param fraction [0...1]
     * @return interpolation: color2+(color2-color1) * fraction
     */
    public static Color interpolateColor(final Color color1, final Color color2, final float fraction) {
        assert(Float.compare(fraction, 0f) >= 0 && Float.compare(fraction, 1f) <= 0);

        final float INT_TO_FLOAT_CONST = 1f / 255f;

        final float RED1 = color1.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN1 = color1.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE1 = color1.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA1 = color1.getAlpha() * INT_TO_FLOAT_CONST;

        final float RED2 = color2.getRed() * INT_TO_FLOAT_CONST;
        final float GREEN2 = color2.getGreen() * INT_TO_FLOAT_CONST;
        final float BLUE2 = color2.getBlue() * INT_TO_FLOAT_CONST;
        final float ALPHA2 = color2.getAlpha() * INT_TO_FLOAT_CONST;

        final float DELTA_RED = RED2 - RED1;
        final float DELTA_GREEN = GREEN2 - GREEN1;
        final float DELTA_BLUE = BLUE2 - BLUE1;
        final float DELTA_ALPHA = ALPHA2 - ALPHA1;

        float red = RED1 + (DELTA_RED * fraction);
        float green = GREEN1 + (DELTA_GREEN * fraction);
        float blue = BLUE1 + (DELTA_BLUE * fraction);
        float alpha = ALPHA1 + (DELTA_ALPHA * fraction);

        red = red < 0f ? 0f : red;
        red = red > 1f ? 1f : red;
        green = green < 0f ? 0f : green;
        green = green > 1f ? 1f : green;
        blue = blue < 0f ? 0f : blue;
        blue = blue > 1f ? 1f : blue;
        alpha = alpha < 0f ? 0f : alpha;
        alpha = alpha > 1f ? 1f : alpha;

        return new Color(red, green, blue, alpha);
    }


    /**
     * Get a predefined color gradient
     *
     * @param str abbreviation or name of the gradient.
     * @return predefined gradient
     */
    public static LinearGradientPaint getStandardGradient(String str) {
        LinearGradientPaint gradient = null;
        if (Arrays.asList(MAP_GB).contains(str)) {
            gradient = new LinearGradientPaint(new Point2D.Double(0,0),
                                               new Point2D.Double(100, 0),
                                               new float[] {0f,1f},
                                               new Color[] {new Color(0,0,0),
                                                            new Color(0, 255, 0)});
        } else if (Arrays.asList(MAP_DARK).contains(str)) {
            gradient = new LinearGradientPaint(new Point2D.Double(0,0),
                                               new Point2D.Double(100, 0),
                                               new float[] {0f,0.5f,1f},
                                               new Color[] {new Color(0,0,0),
                                                            new Color(255, 0, 0),
                                                            new Color(255, 255, 0)});
        } else if (Arrays.asList(MAP_HSV).contains(str)) {
            gradient = new LinearGradientPaint(new Point2D.Double(0,0),
                                               new Point2D.Double(100, 0),
                                               new float[] {0f,0.2f,0.4f,0.6f,0.8f,1f},
                                               new Color[] {new Color(255,0,0),
                                                            new Color(255, 255, 0),
                                                            new Color(0, 255, 0),
                                                            new Color(0, 255, 255),
                                                            new Color(0, 0, 255),
                                                            new Color(255, 0, 255)});
        } else if (Arrays.asList(MAP_JET).contains(str)) {
            gradient = new LinearGradientPaint(new Point2D.Double(0,0),
                                               new Point2D.Double(100, 0),
                                               new float[] {0f,0.333f,0.666f,1f},
                                               new Color[] {new Color(255,0,0),
                                                            new Color(255, 255,0),
                                                            new Color(0, 255, 255),
                                                            new Color(0, 0, 255)});
        } else if (Arrays.asList(MAP_GBR).contains(str)) {
            gradient = new LinearGradientPaint(new Point2D.Double(0,0),
                                               new Point2D.Double(100, 0),
                                               new float[] {0f,0.5f,1f},
                                               new Color[] {new Color(0,255,0),
                                                            new Color(0, 0, 0),
                                                            new Color(255, 0, 0)});
        } else {
            System.err.println("Don't know the '" + str + "' color map.");
        }

        return gradient;
    }




    /**
     * Class to create a panel with a color gradient for display
     */
    public static class ColorGradientPanel extends JPanel {

        /**
         * Defaults
         */
        private static final Dimension dimension = new Dimension(400, 30);
        private LinearGradientPaint gradientPainter = getStandardGradient("GBR");


        /**
         * Constructor
         */
        public ColorGradientPanel() {
            setMinimumSize(dimension);
        }


        /**
         * Configure the gradient panel.
         *
         * @param painter color gradient to display.
         */
        public void configure(LinearGradientPaint painter) {
            this.gradientPainter = painter;
        }


        /** {@inheritDoc} */
        @Override
        public void paintComponent(Graphics graphics) {
            // Create the 2D copy
            Graphics2D graphics2D = (Graphics2D) graphics.create();

            // Create a new gradient painter with the current panel width.
            Point2D sta = new Point2D.Double(0, 0);
            Point2D sto = new Point2D.Double(getWidth(), 0);
            float[] pos = gradientPainter.getFractions();
            Color[] col = gradientPainter.getColors();
            LinearGradientPaint gradient = new LinearGradientPaint(sta, sto, pos, col);

            // Render the graphics
            graphics2D.setPaint(gradient);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            // Dispose of copy
            graphics2D.dispose();
        }


        /**
         * Quick testing
         *
         * @param args whatever
         */
        public static void main(String[] args) {
            ColorGradientPanel bar = new ColorGradientPanel();
            JFrame frame = new JFrame("ColorGradientToolBar Test");
            frame.setSize(dimension);
            frame.add(bar);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }


}
