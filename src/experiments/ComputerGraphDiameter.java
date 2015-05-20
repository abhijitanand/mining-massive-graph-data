/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experiments;

import input.io.GraphLoaderToJGraphT;
import java.io.IOException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class ComputerGraphDiameter {
    
    public static void main(String[] args) throws IOException {
        
        String graphFile = (args.length > 0)? args[0] : "/Users/avishekanand/research/data/de-yr-graphs/de-2004.gz";
                
        GraphLoaderToJGraphT loader = new GraphLoaderToJGraphT(graphFile);
        
        DirectedGraph webGraph = loader.getWebGraph();
        
        FloydWarshallShortestPaths<Integer, DefaultEdge> fwsp = new FloydWarshallShortestPaths<>(webGraph);
        
        double diameter = fwsp.getDiameter();
        
        System.out.println("Diameter : " + diameter);
    }
    
}
