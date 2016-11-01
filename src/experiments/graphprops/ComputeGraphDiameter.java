package experiments;

import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class ComputeGraphDiameter {
    
    private final DirectedGraph graph;
    
    public ComputeGraphDiameter(DirectedGraph webGraph){
        this.graph = webGraph;
    }
    
    public double getDiameter(){
        FloydWarshallShortestPaths<Integer, DefaultEdge> fwsp = new FloydWarshallShortestPaths<>(graph);
        double diameter = fwsp.getDiameter();
        
        return diameter;
    }
    
    public void getConnectedComponents(){
        ConnectivityInspector ci = new ConnectivityInspector(graph);
        
        System.out.println("Connectivity : " + ci.isGraphConnected());
        
        List<Set<Integer>> connectedSets = ci.connectedSets();
        
        System.out.println("Number of connected components : " + connectedSets.size());
        
        int idx = 0;
        for (Set<Integer> connectedSet : connectedSets) {
            System.out.println(idx++ + ") " + connectedSet.size());
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        String graphFile = (args.length > 0)? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2004.gz";
                
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);
        
        DirectedGraph webGraph = loader.getWebGraph();
        
        ComputeGraphDiameter g = new ComputeGraphDiameter(webGraph);
        
        //System.out.println("Diameter : " + g.getDiameter());
        g.getConnectedComponents();
    }
    
}
