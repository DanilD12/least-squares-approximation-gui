/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package coursework;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MainForm extends javax.swing.JFrame {

    private JTextField txtA, txtB, txtC, txtXMin, txtXMax, txtN;
    private JComboBox<String> comboMethod;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel graphPanelPlaceholder;

    public MainForm() {
        initComponents();
        initGUI();
    }

    private void initGUI() {
        setTitle("Курсовая: Вариант 20");
        setSize(1000, 700);
        setLayout(new BorderLayout(5, 5));

        JPanel panelSettings = new JPanel(new GridLayout(2, 6, 5, 5));
        panelSettings.setBorder(BorderFactory.createTitledBorder("Ввод данных"));

        panelSettings.add(new JLabel("a:"));
        txtA = new JTextField("1.0");
        panelSettings.add(txtA);

        panelSettings.add(new JLabel("b:"));
        txtB = new JTextField("0.5");
        panelSettings.add(txtB);

        panelSettings.add(new JLabel("c:"));
        txtC = new JTextField("1.0");
        panelSettings.add(txtC);

        panelSettings.add(new JLabel("X min:"));
        txtXMin = new JTextField("1.0");
        panelSettings.add(txtXMin);

        panelSettings.add(new JLabel("X max:"));
        txtXMax = new JTextField("5.0");
        panelSettings.add(txtXMax);

        panelSettings.add(new JLabel("Точек N:"));
        txtN = new JTextField("20");
        panelSettings.add(txtN);

        JPanel panelControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        String[] methods = {"1. Квадратичная", "2. Экспоненциальная", "3. Степенная"};
        comboMethod = new JComboBox<>(methods);

        JButton btnCalc = new JButton("Рассчитать");
        JButton btnSave = new JButton("Сохранить отчет");
        JButton btnLoad = new JButton("Считать из файла");

        panelControls.add(comboMethod);
        panelControls.add(btnCalc);
        panelControls.add(btnSave);
        panelControls.add(btnLoad);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(panelSettings, BorderLayout.CENTER);
        topPanel.add(panelControls, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"X", "Y исх", "Y аппр"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(430, 0));
        add(tableScroll, BorderLayout.WEST);

        graphPanelPlaceholder = new JPanel(new BorderLayout());
        graphPanelPlaceholder.add(new JLabel("График будет здесь", SwingConstants.CENTER), BorderLayout.CENTER);
        add(graphPanelPlaceholder, BorderLayout.CENTER);

        btnCalc.addActionListener(e -> performCalculationFromFields());

        btnSave.addActionListener(e -> {
            try {
                FileSaver.saveTableData(tableModel, "results.txt");
                JOptionPane.showMessageDialog(this, "Файл сохранен!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + ex.getMessage());
            }
        });

        btnLoad.addActionListener(e -> loadDataFromFile());

        setLocationRelativeTo(null);
    }

    private void performCalculationFromFields() {
        try {
            double a = Double.parseDouble(txtA.getText());
            double b = Double.parseDouble(txtB.getText());
            double c = Double.parseDouble(txtC.getText());
            double xMin = Double.parseDouble(txtXMin.getText());
            double xMax = Double.parseDouble(txtXMax.getText());
            int n = Integer.parseInt(txtN.getText());

            if (n < 2) {
                JOptionPane.showMessageDialog(this, "N должно быть >= 2");
                return;
            }
            if (xMax <= xMin) {
                JOptionPane.showMessageDialog(this, "X max должен быть больше X min");
                return;
            }
            if (xMin <= 0) {
                JOptionPane.showMessageDialog(this, "Внимание! X должен быть > 0");
                return;
            }

            double[] x = new double[n];
            double[] y = new double[n];
            double dx = (xMax - xMin) / (n - 1);

            for (int i = 0; i < n; i++) {
                x[i] = xMin + i * dx;
                y[i] = Variant20.f(x[i], a, b, c);
            }

            performCalculationOnArrays(x, y);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void performCalculationOnArrays(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            JOptionPane.showMessageDialog(this, "Некорректные данные для расчёта");
            return;
        }

        int n = x.length;
        double xMin = min(x);
        double xMax = max(x);

        txtXMin.setText(String.valueOf(xMin));
        txtXMax.setText(String.valueOf(xMax));
        txtN.setText(String.valueOf(n));

        int method = comboMethod.getSelectedIndex();
        double[] coefs;
        String label;

        if (method == 0) {
            coefs = MNK.solveQuadratic(x, y, n);
            label = String.format("Парабола: %.2fx^2 + %.2fx + %.2f", coefs[2], coefs[1], coefs[0]);
        } else if (method == 1) {
            coefs = MNK.solveExponential(x, y, n);
            label = String.format("Экспонента: %.2f * e^(%.2fx)", coefs[0], coefs[1]);
        } else {
            coefs = MNK.solvePower(x, y, n);
            label = String.format("Степенная: %.2f * x^%.2f", coefs[0], coefs[1]);
        }

        updateTableAndChart(x, y, coefs, method, label, xMin, xMax, n);
    }

    private void loadDataFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Выберите файл с данными (X Y или X Y Yappr)");

        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                line = line.replace(";", " ");
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                Double xVal = tryParseDouble(parts[0]);
                Double yVal = tryParseDouble(parts[1]);
                if (xVal == null || yVal == null) continue;

                xs.add(xVal);
                ys.add(yVal);
            }

            if (xs.size() < 2) {
                JOptionPane.showMessageDialog(this, "В файле недостаточно данных (нужно минимум 2 строки с X и Y).");
                return;
            }

            double[] x = new double[xs.size()];
            double[] y = new double[ys.size()];
            for (int i = 0; i < xs.size(); i++) {
                x[i] = xs.get(i);
                y[i] = ys.get(i);
            }

            performCalculationOnArrays(x, y);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения файла: " + ex.getMessage());
        }
    }

    private Double tryParseDouble(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        // поддержка десятичной запятой
        s = s.replace(",", ".");

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double min(double[] a) {
        double m = a[0];
        for (double v : a) if (v < m) m = v;
        return m;
    }

    private double max(double[] a) {
        double m = a[0];
        for (double v : a) if (v > m) m = v;
        return m;
    }

    private void updateTableAndChart(double[] x, double[] y, double[] coefs, int method,
                                     String label, double xMin, double xMax, int n) {

        tableModel.setRowCount(0);

        XYSeries seriesFunc = new XYSeries("Исходные: " + Variant20.getFuncName());
        XYSeries seriesApprox = new XYSeries(label);

        for (int i = 0; i < n; i++) {
            double yAppr = MNK.calcApprox(x[i], coefs, method);

            tableModel.addRow(new Object[]{x[i], y[i], yAppr});
            seriesFunc.add(x[i], y[i]);
        }

        int steps = 300;
        double dx = (xMax - xMin) / (steps - 1);

        for (int i = 0; i < steps; i++) {
            double xx = xMin + dx * i;
            double yy = MNK.calcApprox(xx, coefs, method);
            seriesApprox.add(xx, yy);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesFunc);
        dataset.addSeries(seriesApprox);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Результат аппроксимации",
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);

        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);

        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);

        chart.getXYPlot().setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        graphPanelPlaceholder.removeAll();
        graphPanelPlaceholder.setLayout(new BorderLayout());
        graphPanelPlaceholder.add(chartPanel, BorderLayout.CENTER);
        graphPanelPlaceholder.revalidate();
        graphPanelPlaceholder.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
  }
