import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.util.Objects;

import javax.swing.*;
import javax.imageio.*;

import jcanny.*;

import EdgeDetection.*;

public class EdgeDetectionV1 {

    private JFrame frame;
    private JTextField txtPath;

    private int CANNY_STD_DEV = 1;
    private double CANNY_THRESHOLD_RATIO = 1;

    private boolean loaded = false;

    private String imgFileName;
    private String imgExt;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    EdgeDetectionV1 window = new EdgeDetectionV1();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public EdgeDetectionV1() {
        initialize();
    }

    private static String getFileExtension(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    public ImageIcon scaleImage(ImageIcon icon, int w, int h) {
        int nw = icon.getIconWidth();
        int nh = icon.getIconHeight();

        if(icon.getIconWidth() > w) {
            nw = w;
            nh = (nw * icon.getIconHeight()) / icon.getIconWidth();
        }

        if(nh > h) {
            nh = h;
            nw = (icon.getIconWidth() * nh) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(nw, nh, Image.SCALE_DEFAULT));
    }


    private void initialize() {
        // main frame
        frame = new JFrame("Edge Detector");
        frame.setBounds(100, 100, 1650, 970);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // text field to show path to image
        txtPath = new JTextField();
        txtPath.setBounds(10, 10, 414, 21);
        frame.getContentPane().add(txtPath);
        txtPath.setColumns(10);

        // label to display images using ists icon
        JLabel imagePreview = new JLabel();
        imagePreview.setBounds(10, 80, 800, 800);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(Color.black);
        frame.add(imagePreview);

        // label to display output image
        JLabel imageOutput = new JLabel();
        imageOutput.setBounds(820, 80, 800,800);
        imageOutput.setOpaque(true);
        imageOutput.setBackground(Color.black);
        frame.add(imageOutput);

        // drop down for STD_DEV
        String[] devVals = {"1", "2", "3"};
        JLabel stdComboLabel = new JLabel("STD. DEVIATION: ");
        stdComboLabel.setBounds(10, 890, 150, 23);
        frame.getContentPane().add(stdComboLabel);
        JComboBox<String> stdCombo = new JComboBox<> (devVals);
        stdCombo.setBounds(130, 890, 100, 23);
        frame.getContentPane().add(stdCombo);
        stdCombo.setVisible(false);
        stdComboLabel.setVisible(false);

        // drop down for THRESHOLD
        String[] threshVals = {"1", "0.1", "0.5"};
        JLabel threshComboLabel = new JLabel("THRESHOLD RATIO: ");
        threshComboLabel.setBounds(250, 890, 150, 23);
        frame.getContentPane().add(threshComboLabel);
        JComboBox<String> threshCombo = new JComboBox<> (threshVals);
        threshCombo.setBounds(400, 890, 100, 23);
        frame.getContentPane().add(threshCombo);
        threshCombo.setVisible(false);
        threshComboLabel.setVisible(false);

        // drop down for filter
        String[] filterVals = {"Horizontal Filter", "Vertical Filter", "Sobel Vertical Filter", "Sobel Horizontal Filter",
                "Scharr Vertical Filter", "Scharr Horizontal Filter", "Canny Operator"};
        JLabel filterComboLabel = new JLabel("Filter: ");
        filterComboLabel.setBounds(250, 41, 250, 23);
        frame.getContentPane().add(filterComboLabel);
        JComboBox<String> filterCombo = new JComboBox<> (filterVals);
        filterCombo.setBounds(300, 41, 200, 23);
        frame.getContentPane().add(filterCombo);

        // event listener for filter combo box
        filterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox comboBox = (JComboBox) actionEvent.getSource();
                int selectedFilter = comboBox.getSelectedIndex();
                if (selectedFilter == 6){
                    stdCombo.setVisible(true);
                    stdComboLabel.setVisible(true);
                    threshCombo.setVisible(true);
                    threshComboLabel.setVisible(true);
                }
                else{
                    stdCombo.setVisible(false);
                    stdComboLabel.setVisible(false);
                    threshCombo.setVisible(false);
                    threshComboLabel.setVisible(false);
                }
            }
        });

        // browse button
        JButton btnBrowse = new JButton("Browse");
        btnBrowse.setBounds(10, 41, 100, 23);
        frame.getContentPane().add(btnBrowse);

        btnBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                // For File
                //fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                fileChooser.setAcceptAllFileFilterUsed(false);

                int rVal = fileChooser.showOpenDialog(null);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    imgFileName = fileChooser.getSelectedFile().toString();
                    imgExt = getFileExtension(imgFileName);
                    txtPath.setText(imgFileName);

                    // set preview image
                    ImageIcon icon = new ImageIcon(imgFileName);
                    icon = scaleImage(icon, 800, 800);
                    imagePreview.setIcon(null);
                    imagePreview.setIcon(icon);

                    loaded = true;
                }
            }
        });

        // convert button
        JButton btnConvert = new JButton("Edge Detect");
        btnConvert.setBounds(120, 41, 120, 23);
        frame.getContentPane().add(btnConvert);

        btnConvert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loaded) {

                    // convert image
                    try {
                        BufferedImage input = ImageIO.read(new File(imgFileName));
                        String imgOutFile = "CONVERTED." + imgExt;
                        BufferedImage output;
                        if (filterCombo.getSelectedIndex() <= 5){
                            EdgeDetection edgeDetection = new EdgeDetection();
                            File out = edgeDetection.detectEdges(input, Objects.requireNonNull(filterCombo.getSelectedItem()).toString());
                            output = ImageIO.read(out);
                        }
                        else {
                            CANNY_STD_DEV = stdCombo.getSelectedIndex() + 1;
                            switch (threshCombo.getSelectedIndex()) {
                                case 1 -> CANNY_THRESHOLD_RATIO = 0.2;
                                case 2 -> CANNY_THRESHOLD_RATIO = 0.5;
                                default -> CANNY_THRESHOLD_RATIO = 1.0;
                            }
                            output = JCanny.CannyEdges(input, CANNY_STD_DEV, CANNY_THRESHOLD_RATIO);
                        }

                        // set output image
                        ImageIcon icon = new ImageIcon(output);
                        icon = scaleImage(icon, 800, 800);
                        imageOutput.setIcon(null);
                        imageOutput.setIcon(icon);

                        ImageIO.write(output,imgExt,new File(imgOutFile));
                    } catch (Exception ex) {
                        System.out.println("ERROR ACCESING IMAGE FILE:\n" + ex.getMessage());
                    }
                }
            }
        });
    }
}