package graphmatching;

import com.sun.tools.javac.util.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.HopcroftKarpBipartiteMatching;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 *
 * @author avishekanand
 */
public class OptimalBipartiteMatching {
    
    
    public static void main(String[] args) {
        //PartiteRandomGraphGenerator<Integer, DefaultEdge> bipGraphGen = new PartiteRandomGraphGenerator<>(5,5,10);
        UndirectedGraph<String, DefaultEdge> graph =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        VertexFactory<String> vertexFactory = new VertexFactory<String>()
        {
            int n = 0;
            @Override
            public String createVertex()
            {
                String s = String.valueOf(n);
                n++;
                return s;
            }
        };
        int numVertices0 = 100000;
        int numVertices1 = 150000;
        int numEdges = 20000000;
        
        long time = System.currentTimeMillis();
        Pair<List<String>, List<String>> leftRightSets = generateGraph(graph, numVertices0, numVertices1, numEdges, vertexFactory);

        //Run bipartite graph matching algorithm
        Set<String> p1 = new HashSet<String>(leftRightSets.fst);
        Set<String> p2 = new HashSet<String>(leftRightSets.snd);

        System.out.println("Graph construction done in  " + (System.currentTimeMillis()-time)/1000 + " seconds");
        
        time = System.currentTimeMillis();
        HopcroftKarpBipartiteMatching<String, DefaultEdge> alg = 
            new HopcroftKarpBipartiteMatching<String, DefaultEdge>(graph, p1, p2);
        Set<DefaultEdge> match = alg.getMatching();
        System.out.println("Matching done in  " + (System.currentTimeMillis()-time)/1000 + " seconds");
        //System.out.println(graph.toString());
        
        
        //System.out.println(graph);
        System.out.println("Matching Size: " + match.size());
        
    }

    public static <V, E> Pair<List<V>,List<V>> generateGraph(Graph<V, E> graph,
        int numVertices0, int numVertices1, int numEdges,
        final VertexFactory<V> vertexFactory)
    {
        List<V> vertices0 = new ArrayList<V>();
        for (int i = 0; i < numVertices0; i++)
        {
            V v = vertexFactory.createVertex();
            graph.addVertex(v);
            vertices0.add(v);
        }
        List<V> vertices1 = new ArrayList<V>();
        for (int i = 0; i < numVertices1; i++)
        {
            V v = vertexFactory.createVertex();
            graph.addVertex(v);
            vertices1.add(v);
        }

        // Create edges between random vertices
        Random random = new Random(0);
        while (graph.edgeSet().size() < numEdges)
        {
            int i1 = random.nextInt(vertices1.size());
            V v1 = vertices1.get(i1);
            int i0 = random.nextInt(vertices0.size());
            V v0 = vertices0.get(i0);
            graph.addEdge(v0, v1);
        }

        return new Pair(vertices0, vertices1);
    }
    
}
