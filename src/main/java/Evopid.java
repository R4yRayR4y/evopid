import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class Evopid implements Runnable {

    private JFrame frame;

    public Evopid() {
        JFrame frame = new JFrame("Evopid");
        frame.setContentPane(new GuiRoot(this).rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void run() {
        XYSeries bestPIDResults = doSimulation();

        frame = new JFrame("Evopid");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        JPanel aPanel = new JPanel();
        aPanel.setPreferredSize(new Dimension(600, 300));
        XYSeries constant = new XYSeries("Wymuszenie skokowe");
        constant.add(0, 1);
        constant.add(SettingsContainer.get().getTime(), 1);
        ChartPanel chartPanel = new ChartPanel(createChart(bestPIDResults, constant));
        chartPanel.setPreferredSize(new Dimension(600, 300));
        frame.getContentPane().add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public XYSeries doSimulation() {
        HashMap<String, Double[][]> modelMap = new HashMap();
        modelMap.put("A", SettingsContainer.get().getA());
        modelMap.put("B", SettingsContainer.get().getB());
        modelMap.put("C", SettingsContainer.get().getC());
        modelMap.put("D", SettingsContainer.get().getD());
        HashMap<String, Double> pidMap = new HashMap();
        int maxSamples = SettingsContainer.get().getSamples();
        double maxTime = SettingsContainer.get().getTime();

        Generator generator = new Generator(modelMap, maxSamples, maxTime);
        pidMap = generator.generate(SettingsContainer.get().getPopulations(), SettingsContainer.get().getSpecimens());

//        pidMap.put("P", 1.0);
//        pidMap.put("I", 0.0);
//        pidMap.put("D", 0.0);
        DynamicModel model = new DynamicModel(modelMap, pidMap);
        Simulator simulator = new Simulator(model);
        simulator.simulate(maxSamples, maxTime);
        ArrayList<Simulator.Result> results = simulator.getResults();
        Evaluator evaluator = new Evaluator(results);
//        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println("\n\n===== BEST PID FOUND =====");
        System.out.println(pidMap);
        generator.printEvaluatorStats(evaluator);
//        System.out.println("Overshoot: " + df.format(evaluator.getOvershoot()*100) + "%");
//        System.out.println("Rising time: " + evaluator.getRisingTime() + " seconds");
//        System.out.println("Settling time: " + evaluator.getSettlingTime() + " seconds");
//        System.out.println("SCORE: " + evaluator.getScore());
        return simulator.getXYSeries();
    }

    private JFreeChart createChart(XYSeries series, XYSeries constant) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(constant);
        JFreeChart timechart = ChartFactory.createXYLineChart(
                "Odpowiedz ukladu na skok jednostkowy", // Title
                "Czas (s)",         // X-axis Label
                "Wartosc odpowiedzi",       // Y-axis Label
                dataset,        // Dataset
                PlotOrientation.VERTICAL,
                true,          // Show legend
                true,          // Use tooltips
                false          // Generate URLs
        );
        return timechart;
    }

}