package experiments;

import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.Subgraph;

/**
 *
 * @author avishekanand
 */
public class ExamineStructuralGraphProperties {
    private final DirectedGraph graph;
    
    public ExamineStructuralGraphProperties(DirectedGraph webGraph){
        graph = webGraph;
    }
    
    public static void main(String[] args) throws IOException {
        
        String graphFile = (args.length > 0)? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2003.gz";
                
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);        
        DirectedGraph webGraph = loader.getWebGraph();
        
        ExamineStructuralGraphProperties g = new ExamineStructuralGraphProperties(webGraph);
        DirectedSubgraph<Integer, DefaultEdge>[] connectedComponents = g.computeConnectedComponents();
        double diameter = g.computeDiameter(connectedComponents);
        
        
        System.out.println("Overal Diameter : " + diameter);
        
    }
    
    public DirectedSubgraph<Integer, DefaultEdge>[] computeConnectedComponents(){
        ConnectivityInspector<Integer, DefaultEdge> ci = new ConnectivityInspector<>(graph);
        
        List<Set<Integer>> connectedSets = ci.connectedSets();
        
        System.out.println("Connected components : " + connectedSets.size());
        
        
        DirectedSubgraph<Integer, DefaultEdge>[] sg = new DirectedSubgraph[connectedSets.size()];
        int idx = 0;
        
        for (Set<Integer> set : connectedSets) {
            sg[idx] = new DirectedSubgraph(graph,set, null);
            
            int nodes = set.size();
            int edges = sg[idx].edgeSet().size();
            
            double gfd = (edges > 0) ? edges/(double)nodes : 0.0 ;
            double loggfd = (edges > 0) ? Math.log(edges)/(double)Math.log(nodes) : 0.0 ;
            
            idx++;
            System.out.println("nodes: " + nodes + " edges: " + edges + " gfd : " + gfd + " loggfd : " + loggfd);
        }
        
        return sg;
    }
    
    public double computeDiameter(){
        FloydWarshallShortestPaths<Integer, DefaultEdge> fwsp = new FloydWarshallShortestPaths<>(graph);
        double diameter = fwsp.getDiameter();
        
        return diameter;
    }
    
    public double computeDiameter(DirectedSubgraph<Integer, DefaultEdge>[] sg){
        double diameter = 0.0;
        
        for (DirectedSubgraph<Integer, DefaultEdge> directedSubgraph : sg) {
            FloydWarshallShortestPaths<Integer, DefaultEdge> fwsp = new FloydWarshallShortestPaths(directedSubgraph);
            
            double d = fwsp.getDiameter();
            System.out.println("Diameter of subgraph : " + d);
            diameter = (d > diameter) ? d : diameter;
        }
        
        return diameter;
    }
}
