package experiments;

import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

/**
 *
 * @author avishekanand
 */
public class ExamineStructuralGraphProperties {
    public DirectedGraph<Integer, DefaultEdge> graph;
    
    public ExamineStructuralGraphProperties(DirectedGraph webGraph){
        graph = webGraph;
    }
    
    public static void main(String[] args) throws IOException {
        
        String graphFile = (args.length > 0)? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2001.gz";
                
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);        
        DirectedGraph webGraph = loader.getWebGraph();
        
        ExamineStructuralGraphProperties g = new ExamineStructuralGraphProperties(webGraph);
//        DirectedSubgraph<Integer, DefaultEdge>[] connectedComponents = g.computeConnectedComponents();
        
//        double diameter = g.computeDiameter(connectedComponents);
//        System.out.println("Overal Diameter : " + diameter);
//        
        g.findCores(8, 8);
        
    }
    
    public DirectedSubgraph<Integer, DefaultEdge>[] computeConnectedComponents(){
        ConnectivityInspector<Integer, DefaultEdge> ci = new ConnectivityInspector<>(graph);
        List<Set<Integer>> connectedSets = ci.connectedSets();
        
        System.out.println("\tConnected components : " + connectedSets.size());
        DirectedSubgraph<Integer, DefaultEdge>[] sg = new DirectedSubgraph[connectedSets.size()];
        
        int idx = 0;
        for (Set<Integer> set : connectedSets) {
            sg[idx] = new DirectedSubgraph(graph,set, null);
            idx++;
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
            
            if (hasCycles(directedSubgraph)) {
                //System.out.println(" Component has Cycles..");
            }else {
                System.out.println("component is acyclic..");
            }
            FloydWarshallShortestPaths<Integer, DefaultEdge> fwsp = new FloydWarshallShortestPaths(directedSubgraph);
            
            double d = fwsp.getDiameter();
            System.out.println("Diameter of subgraph : " + d);
            diameter = (d > diameter) ? d : diameter;
        }
        
        return diameter;
    }
    
    public boolean hasCycles(DirectedGraph g){
        CycleDetector<Integer, DefaultEdge> cd = new CycleDetector<>(g);
        return cd.detectCycles();
    }
    
    public DirectedGraph<Integer, DefaultEdge> findCores(int kcore, int lcore){
        int removedVertices = 0;
        
            System.out.print ("Initial graph V : " + graph.vertexSet().size() + " , E : " + graph.edgeSet().size());
            Set<Integer> vertexSet = graph.vertexSet();
            
            HashSet<Integer> removalSet = new HashSet<Integer>();
            
            for (int vertex : vertexSet) {
                int outdegree = graph.outDegreeOf(vertex);
                int indegree = graph.inDegreeOf(vertex);
                
                if(outdegree < kcore  || indegree < lcore){
                    //retain the node along with all edges
                    removedVertices++;
                    removalSet.add(vertex);
                    //System.out.println("Removed V : " + vertex + " E : " + outdegree);
                }
            }
            
            graph.removeAllVertices(removalSet);
            
            System.out.print(" Final graph V : " + graph.vertexSet().size() + " , E : " + graph.edgeSet().size());
            System.out.print(" Removed " + removedVertices + " vertices..for k,l : " + kcore + " , " + lcore);
            System.out.println("");
            return graph;
    }
}
