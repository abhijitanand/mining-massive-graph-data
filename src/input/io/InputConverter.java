package input.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Converts Adjacency list to relational form and vice versa
 * @author avishekanand
 */
public class InputConverter {
    
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String filename = (args.length > 0) ? args[0] : "/Users/avishekanand/research/data/aster/wiki-edit-graphs/jan";
        
        BufferedReader br;
        
        if (filename.endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader (
                                         new GZIPInputStream(new FileInputStream(filename))));
        }else{
            br = new BufferedReader(new FileReader(filename));
        }
        
        
        int listcounts = 0;
        int edges = 0;
        while(br.ready()){
            String read = br.readLine();
            String[] line = read.split("\t");
            if (line.length < 2) {
                continue;
            }
            
            String source = line[0];
            
            if (read.startsWith("List")) {
                //System.out.println(source);
                listcounts++;
                continue;
            }
            
            
            //System.out.println(read);
            for (int i = 1; i < line.length; i++) {
                System.out.println(source + "\t" + line[i]);
                edges++;
                
            }
    
        }
        
        System.out.println("Graph with edges: " + edges + " lists : " + listcounts);
        
        br.close();
    }
}
