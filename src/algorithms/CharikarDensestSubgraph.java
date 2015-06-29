package algorithms;

import input.io.GraphLoaderToJGraphT;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UndirectedSubgraph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * Implementation of Charikar's greedy Densest-subgraph algorithm for
 * unweighted, undirected graphs.
 *
 * @author avishekanand
 *
 * @param <V> type of vertex
 * @param <E> type of edge
 */
public class CharikarDensestSubgraph<V, E> {

    protected UndirectedGraph<V, E> graph;
    protected UndirectedSubgraph<V, E> bestSubGraph;
    protected FibonacciHeap<V> heap = new FibonacciHeap<V>();

    /**
     * Compute the densest subgraph of a graph.
     *
     * @param graph the graph.
     */
    public CharikarDensestSubgraph(UndirectedGraph<V, E> graph) {
        this.graph = graph;

        for (V vertex : graph.vertexSet()) {
            heap.insert(new FibonacciHeapNode<V>(vertex), graph.degreeOf(vertex));
        }

        calculateDensestSubgraph();
    }

    protected V getMinDegreeVertexBruteForce(UndirectedGraph<V, E> graph) {
        int minDegree = Integer.MAX_VALUE;
        V minDegreeVertex = null;

        for (V vertex : graph.vertexSet()) {
            int degree = graph.degreeOf(vertex);
            if (degree < minDegree) {
                minDegreeVertex = vertex;
                break;
            }
        }

        return minDegreeVertex;
    }

    protected V getMinDegreeVertex(UndirectedGraph<V, E> graph) {
        if(heap.isEmpty()) {
            //System.out.println("heap empty");
            return null;
        }
        return heap.removeMin().getData();
    }

    protected void calculateDensestSubgraph() {
        UndirectedSubgraph<V, E> currentSubGraph = new UndirectedSubgraph<V, E>(graph, graph.vertexSet(), null);
        double bestDensity = calculateDensity(graph);

        while (currentSubGraph.vertexSet().size() > 0) {
            currentSubGraph = new UndirectedSubgraph<V, E>(graph, currentSubGraph.vertexSet(), null);
            
            V minDegreeVertex = getMinDegreeVertex(currentSubGraph);
            if (minDegreeVertex == null){
                break;
            }
            currentSubGraph.removeVertex(minDegreeVertex);
            double density = calculateDensity(currentSubGraph);

            if (density > bestDensity) {
                bestDensity = density;
                bestSubGraph = currentSubGraph;
            }
        }
    }

    /**
     * Calculate the density of the graph as the number of edges divided by the
     * number of vertices
     *
     * @param g the graph
     * @return the density
     */
    public static double calculateDensity(Graph<?, ?> g) {
        return (double) g.edgeSet().size() / (double) g.vertexSet().size();
    }

    /**
     * @return The densest subgraph
     */
    public UndirectedSubgraph<V, E> getDensestSubgraph() {
        return bestSubGraph;
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String graphFile = (args.length > 0) ? args[0]
            : "/Users/avishekanand/research/data/delicious/deli-wiki-head.tsv";
        int headerSpan = (args.length > 1) ? Integer.parseInt(args[1]): 0;
//        String graphFile = (args.length > 0) ? args[0] 
//                        : "/Users/avishekanand/research/data/delicious/sample.tsv";
        GraphLoaderToJGraphT graphConstructor = new GraphLoaderToJGraphT();
        BufferedReader br = new BufferedReader(new FileReader(graphFile));

        HashSet<String> left = new HashSet<>();
        HashSet<String> right = new HashSet<>();

        UndirectedGraph<String, DefaultEdge> bipartiteGraph
            = graphConstructor.constructBipartiteUndirectedUnweightedGraph(br, 2, 4, left, right, headerSpan);
        
        
        CharikarDensestSubgraph<String, DefaultEdge> charikarDensestSubgraph = new CharikarDensestSubgraph<>(bipartiteGraph);
        charikarDensestSubgraph.calculateDensestSubgraph();
        
        UndirectedSubgraph<String, DefaultEdge> densestSubgraph = charikarDensestSubgraph.getDensestSubgraph();
        
        System.out.println("" + densestSubgraph);

    }
}
