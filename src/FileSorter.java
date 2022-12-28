/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sehall
 */
public class FileSorter implements Runnable
{
    private Queue<File> queue;
    private int maxCount;
    private File input, output;
    private int fileCount;
    private long lineCount;
    private long splitCount;
    private int numSplitFiles;
    private boolean splitDone, mergeDone;
    
    
    
    public FileSorter(File input, File output, int maxCount)
    {   this.output = output;
        this.input = input;
        this.maxCount = maxCount;
        queue = new LinkedList<>();
        Path path = Paths.get(input.getName());
        try {
            lineCount = Files.lines(path).count();
            System.out.println("Lines in inputfile "+lineCount);
        } catch (IOException ex) {
            Logger.getLogger(FileSorter.class.getName()).log(Level.SEVERE, null, ex);
        }
        splitDone = false;
        mergeDone = false;
        splitCount = 0;
        numSplitFiles = 0;
    }
    
    public File getInputFile()
    {
        return input;
    }
    public File getOutputFile()
    {
        return output;
    }
    public boolean isMergeDone()
    {
        return mergeDone;
    }
    public boolean isSplitDone()
    {
        return splitDone;
    }
    @Override
    public void run() {
        
        splitStage();
        splitDone = true;
        try{
            mergeStage();
        }catch(IOException e)
        {
            System.out.println("UNABLE TO MERGE!!! "+e);
        }
        mergeDone = true;
    }
    
    public int getSplitProgress()       
    {   System.out.println("splitCount "+(int)(splitCount/lineCount * 100));
        if(lineCount == 0) return 0;
        else return (int)((double)splitCount/(double)lineCount * 100.0);
    }
    public int getMergeProgress()
    {   if(numSplitFiles == 0) return 0;
        else{
            return (int)(100.0 - ((double)queue.size()/(double)numSplitFiles * 100.0));
        }
    }
    public void splitStage()
    {
        try
        {
            FileReader reader = new FileReader(input);
            BufferedReader br = new BufferedReader(reader);
            
            String line = br.readLine();
            
            List<String> list = new ArrayList<>();
            
            int count = 0;
            
            while(line != null)
            {   line = line.trim();
                if(count < maxCount && !line.equals(""))
                {    list.add(line);
                     count++;
                     splitCount++;
                     line = br.readLine();  
                }
                else
                {   count = 0;
                    sortAndOutputContent(list);
                }
            }
            if(list.size() > 0)
                sortAndOutputContent(list);
            
            numSplitFiles = queue.size();
            br.close();
            reader.close();
            
        }
        catch(IOException e)
        {
            System.out.println("OHHHH NOOOOO SOMETHING WENT WRONT "+e);
        }
    }
    private void sortAndOutputContent(List<String> list) throws FileNotFoundException
    {
        Collections.sort(list);
        File file = new File("TEMP"+fileCount+".txt");
        fileCount++;
        PrintWriter pw = new PrintWriter(file);
        
        for(String s : list)
            pw.println(s);
       
        pw.flush();
        queue.offer(file);
        pw.close();
        list.clear();
    }
    public void mergeStage() throws IOException
    {
        while(queue.size() >= 2)
        {
            File tempIn1 = queue.poll();
            File tempIn2 = queue.poll();
            File tempOutput = new File("TEMP"+fileCount+".txt");
            fileCount++;
            
            PrintWriter pw = new PrintWriter(tempOutput);
            BufferedReader br1 = new BufferedReader(new FileReader(tempIn1));
            BufferedReader br2 = new BufferedReader(new FileReader(tempIn2));
            
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            
            while(line1 != null || line2 != null)
            {
                //System.out.println("COMPARING "+line1+" WITH "+line2);
                
                if(line2 != null && (line1==null || line2.compareTo(line1) <= 0))
                {
                    pw.println(line2);
                    //System.out.println("OUTPUTTING  "+line2);
                    line2 = br2.readLine();
                }
                else if(line1 != null && (line2==null || line2.compareTo(line1) > 0))
                {
                    pw.println(line1);
                    //System.out.println("OUTPUTTING  "+line1);
                    line1 = br1.readLine();
                }
            }
            br1.close();
            br2.close();
            tempIn1.delete();
            tempIn2.delete();
            pw.flush();
            pw.close();
            queue.offer(tempOutput);
        }
        File f = queue.poll();
        f.renameTo(output);
        f.delete();
        
    }

    public static void main(String[] args)
    {
        System.out.println("====================FILE SORTER===========================");
        File input = new File("cities.txt");
        File output = new File("citiesSorted.txt");
        //scrambler(input);
        
        FileSorter sorter = new FileSorter(input, output, 1000);
        
        Thread t = new Thread(sorter);
        t.start();
        
        
    }
 
    public static void scrambler(File input)
    {   try{
            BufferedReader br = new BufferedReader(new FileReader(input));
            List<String> list = new ArrayList<>();
            String line = "";
            do{
                line = br.readLine();
                
                if(line != null)
                    list.add(line);
                
            }while(line != null);
            
            br.close();
            Collections.shuffle(list);
            
            
            PrintWriter pw = new PrintWriter(new File("cities.txt"));
            for(String s : list)
                pw.println(s);
            pw.flush();
            pw.close();
        }
        catch(Exception e)
        {
            System.out.println("SCRAMBLE PROBLEM: "+e);
        }
    }
            
}
