package experiments.wikipediagraphs;

import experiments.DcoreAnalysis;
import experiments.ExamineStructuralGraphProperties;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author avishekanand
 */
public class WikiEditGraphAnalysis {
    
    public static void main(String[] args) throws IOException {
        
        String graphFile  = (args.length> 0) ? args[0] : "/Users/avishekanand/research/data/graphlog/wikipedia-edits/day-edits.tsv.gz";
        
//        UndirectedGraph<String, DefaultEdge> g =
//            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        DirectedGraph<String, DefaultEdge> g
                = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        
        BufferedReader _input = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(graphFile))));
        
        long time = System.currentTimeMillis();
        int edgeCount = 0;
        int vertexCount = 0;
        
        try {
            // add vertices
            while (_input.ready()) {
                String[] list = _input.readLine().split("\t");

                String source = list[0];
                String dest = list[1];
                
                if (!g.containsVertex(source)){
                    g.addVertex(source);                    
                    vertexCount++;
                }
                
                if (!g.containsVertex(dest)){
                    g.addVertex(dest);                    
                    vertexCount++;
                }
                    
                    //maintain left set
                    g.addEdge(source, dest);
                    edgeCount++;
                }
                
            } catch(Exception e){
                System.out.println(e.getStackTrace());
            }
        
        System.out.println("Graph Constructed with " + vertexCount + 
            " vertices and " + edgeCount + " edges in " + (System.currentTimeMillis() - time)/1000 + " secs");
        
        DcoreAnalysis dcores = new DcoreAnalysis();
        ExamineStructuralGraphProperties ex = new ExamineStructuralGraphProperties(g);
        

        //dcores.
    }
    
}
