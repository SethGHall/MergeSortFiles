
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Seth
 */
public class FileSorterGUI extends JPanel implements ActionListener{
    
    private Queue<FileSorter> queue;
    private FileSorter current;
    private JLabel label;
    private JButton queueFiles, process;
    private Timer timer;
    private JProgressBar mergeProgress, splitProgress;
    private JTextField inputField, outputField;
    private JTextField maxStrings;
    
    public  FileSorterGUI()
    {
        super(new BorderLayout());
        
        
        queue = new LinkedList<FileSorter>();
        current = null;
        
        label = new JLabel("Number of items in queue: "+queue.size());
        
        timer = new  Timer(100,this);
        
        queueFiles = new JButton("Enqueue Task");
        process = new JButton("Process Task");
        
        queueFiles.addActionListener(this);
        process.addActionListener(this);
        
        JPanel southPanel = new JPanel();
       // southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
        southPanel.add(queueFiles,SwingConstants.CENTER);
        southPanel.add(process,SwingConstants.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        
        add(label, BorderLayout.NORTH);
       
        inputField = new JTextField("N/A",30);
        inputField.setEditable(false);
        inputField.setBorder(BorderFactory.createTitledBorder("Input File:"));
        
        outputField = new JTextField("N/A",30);
        outputField.setEditable(false);
        outputField.setBorder(BorderFactory.createTitledBorder("Output File:"));
        
        maxStrings = new JTextField("100",30);
        maxStrings.setBorder(BorderFactory.createTitledBorder("Max Strings in memory:"));
        
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.Y_AXIS));
        centrePanel.add(inputField);
        centrePanel.add(outputField);
        centrePanel.add(maxStrings);
        
        mergeProgress = new JProgressBar(0, 100);
        //mergeProgress.setBorder(BorderFactory.createTitledBorder("Merge Progress:"));
        splitProgress = new JProgressBar(0, 100);
        //splitProgress.setBorder(BorderFactory.createTitledBorder("Split Progress:"));
        centrePanel.add(new JLabel("Merge Progress:"));
        centrePanel.add(mergeProgress);
        centrePanel.add(new JLabel("Split Progress:"));
        centrePanel.add(splitProgress);
        
        add(centrePanel,BorderLayout.CENTER);

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(queueFiles == source)
        {
            JFileChooser chooser = new JFileChooser(new File("."));
            if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                File openFile = chooser.getSelectedFile();
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    File saveFile = chooser.getSelectedFile();
                    queue.offer(new FileSorter(openFile, saveFile, Integer.parseInt(maxStrings.getText())));
                }
            }
            label.setText("Number of items in queue: "+queue.size());
            if(!queue.isEmpty() && current == null)
            {   current = queue.peek();
                inputField.setText(current.getInputFile().getName());
                outputField.setText(current.getOutputFile().getName());
            }
        }
        if(process == source && !queue.isEmpty())
        {   
            current = queue.poll();
            Thread t = new Thread(current);
            t.start();
            timer.start();
            process.setEnabled(false);
            label.setText("Number of items in queue: "+queue.size());
        }
        if(source == timer)
        {   
            mergeProgress.setValue(current.getMergeProgress());
            splitProgress.setValue(current.getSplitProgress());
            if(current.isMergeDone() && current.isSplitDone())
            {
                JOptionPane.showMessageDialog(this, "Merge Sort Complete.. ", "info", JOptionPane.INFORMATION_MESSAGE);
                current = null;
                process.setEnabled(true);
                timer.stop();
                
                mergeProgress.setValue(0);
                splitProgress.setValue(0);
                if(queue.isEmpty())
                {
                    inputField.setText("N/A");
                    outputField.setText("N/A");
                }
                else{
                    inputField.setText(queue.peek().getInputFile().getName());
                    outputField.setText(queue.peek().getOutputFile().getName());
                }
            }
        }
    }    

    public static void main(String[] args)
    {
       FileSorterGUI gui = new FileSorterGUI();
      // p.loadImage("seth.jpg");
       
       JFrame frame = new JFrame("FileSorter");
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.getContentPane().add(gui);
       Toolkit toolkit = Toolkit.getDefaultToolkit();
       Dimension dimension = toolkit.getScreenSize();
       int screenHeight = dimension.height;
       int screenWidth = dimension.width;
       frame.pack();             //resize frame apropriately for its content
       frame.setResizable(false);
//positions frame in center of screen
       frame.setLocation(new Point((screenWidth/2)-(frame.getWidth()/2),
         (screenHeight/2)-(frame.getHeight()/2)));
       frame.setVisible(true);
    } 

}
