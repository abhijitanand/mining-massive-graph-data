package experiments;

import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import org.jgrapht.DirectedGraph;

/**
 *
 * @author avishekanand
 */
public class DcoreAnalysis {
    public static void main(String[] args) throws IOException {
        String graphFile = (args.length > 0)? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2010.gz";
                
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);        
        DirectedGraph webGraph = loader.getWebGraph();
        
        ExamineStructuralGraphProperties g = new ExamineStructuralGraphProperties(webGraph);
//      
        findCoreWiseSizeDistribution(g);
    }

    private static void findCoreWiseSizeDistribution(ExamineStructuralGraphProperties g) {
        
        for (int k = 6; k < 1000; k++) {
            if (g.graph.vertexSet().isEmpty()) {
                break;
            }
            g.findCores(k, k);
            
            g.computeConnectedComponents();
        }
    }
}
