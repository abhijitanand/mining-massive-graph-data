package Diameter;

import com.sun.tools.javac.util.Pair;
import input.io.GraphLoaderToJGraphT;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 *
 * This class provides the BFS tree implementation for estimating the upper
 * bound for the Graph Diameter
 *
 * @author avishekanand
 */
public class DiameterBFS<K, V> {

    private final UndirectedGraph<K, V> graph;

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    public DiameterBFS(UndirectedGraph<K, V> graph) {
        this.graph = graph;
    }

    /**
     *
     * We use a couple of heuristics here : 1) We compute the diameter estimate
     * only for the largest connected component 2) we do a BFS followed by a
     * Dijkstra's between the source and the last visited vertex
     *
     *
     * In principle we can optimize to find the longest path estimate in the
     * first BFS, with careful examination we actually do 3 BFS currently, which
     * is reasonable for sparse graphs
     *
     * @return
     */
    public int findDiameterUpperBound() {

        ConnectivityInspector<K, V> ci = new ConnectivityInspector<>(graph);
        List<Set<K>> connectedSets = ci.connectedSets();

        HashSet<K> sources = new HashSet<>();
        for (Set<K> connectedSet : connectedSets) {
            Iterator<K> iter = connectedSet.iterator();
            K source = iter.next();
            sources.add(source);
        }
        
        int maxDiameter = 0;
        for (K source : sources) {
            BreadthFirstIterator<K, V> biter = new BreadthFirstIterator(graph, source);
            biter.setCrossComponentTraversal(false);

            K destination = null;
            while (biter.hasNext()) {
                destination = biter.next();
            }

            DijkstraShortestPath dsp = new DijkstraShortestPath(graph, source, destination);
            int diameter = (int) dsp.getPathLength();
            
            if (maxDiameter < diameter) {
                System.out.println("Updated Diam " + maxDiameter + " to " + diameter);
            }
            maxDiameter = maxDiameter < diameter ? diameter : maxDiameter;
            

        }

        
        log.log(Level.INFO, "Diameter Lower Bound : " + maxDiameter + " , diam. estimate : " + 2 * maxDiameter);

        //The actual diameter could be twice as large
        return 2 * maxDiameter;
    }

    public void testBFS() {
        ConnectivityInspector<K, V> ci = new ConnectivityInspector<>(graph);
        List<Set<K>> connectedSets = ci.connectedSets();

        int diameter = 0;

        for (Set<K> g : connectedSets) {
            //choose initial vertex and conduct BFS on it
            Iterator<K> iter = g.iterator();
            K initialVertex = iter.next();

            HashSet<K> seen = new HashSet();

            //conduct BFS from the seedVertex
            Deque<K> q = new ArrayDeque();
            Deque<Integer> depthCount = new ArrayDeque();
            //Queue<Pair<K,Integer>> q = new LinkedList<Pair<K,Integer>>();
            q.add(initialVertex);
            depthCount.add(0);

            while (!q.isEmpty()) {
                K node = q.remove();
                int depth = depthCount.remove();
                depth++;
                seen.add(node);

                for (K nbr : Graphs.neighborListOf(graph, node)) {
                    if (!seen.contains(nbr)) {
                        q.add(nbr);
                        depthCount.add(depth);
                    }
                }

                diameter = diameter < depth ? depth : diameter;
            }

        }

        System.out.println("Estimated Diam : " + diameter);
    }

    public int getDiameter() {
        int diameter = 0;

        //BFS routine
        ConnectivityInspector<K, V> ci = new ConnectivityInspector<>(graph);
        List<Set<K>> connectedSets = ci.connectedSets();

        for (Set<K> g : connectedSets) {
            //choose initial vertex and conduct BFS on it
            Iterator<K> iter = g.iterator();
            K initialVertex = iter.next();
            HashSet<K> seen = new HashSet();

            Queue<Pair<K, Integer>> q = new LinkedList<Pair<K, Integer>>();
            q.add(new Pair(initialVertex, 0));
            seen.add(initialVertex);

            int currentDiameterEstimate = 0;
            while (!q.isEmpty()) {
                Pair<K, Integer> head = q.remove();
                K node = head.fst;
                seen.add(node);

                //get all neighbors
                Set<V> outEdges = graph.edgesOf(node);
                int newScore = head.snd + 1;

                for (V edge : outEdges) {
                    K nbr = graph.getEdgeTarget(edge);
                    nbr = nbr.equals(node) ? graph.getEdgeSource(edge) : nbr;
                    if (!seen.contains(nbr)) {
                        q.add(new Pair(nbr, newScore));
                        currentDiameterEstimate = (currentDiameterEstimate < newScore) ? newScore : currentDiameterEstimate;
                    }
                }
            }

            diameter = currentDiameterEstimate > diameter ? currentDiameterEstimate : diameter;
        }
        return diameter;
    }

    public void BFSiteration() {
        BreadthFirstIterator<K, V> biter = new BreadthFirstIterator(graph);

        int count = 0;
        while (biter.hasNext()) {
            biter.next();
            count++;
        }

        ConnectivityInspector<K, V> ci = new ConnectivityInspector<>(graph);

        List<Set<K>> connectedSets = ci.connectedSets();

        for (Set<K> g : connectedSets) {
            //choose initial vertex and conduct BFS on it
            Iterator<K> iter = g.iterator();
            K initialVertex = iter.next();
//            
//            DijkstraShortestPath dsp = new DijkstraShortestPath(graph, initialVertex, null);
//            dsp.getPathLength();
//            
        }

        System.out.println("BFS complete : " + count);
    }

    public int getDiameter2() {

        int diameter = 0;
        HashSet<K> visitedNodes = new HashSet<>(graph.vertexSet());

        while (!visitedNodes.isEmpty()) {
            int currentDiameterEstimate = 0;

            //pick a random vertex
            Iterator<K> iterator = visitedNodes.iterator();
            K seedVertex = iterator.next();

            //conduct BFS from the seedVertex
            Deque<Pair<K, Integer>> q = new ArrayDeque();
            //Queue<Pair<K,Integer>> q = new LinkedList<Pair<K,Integer>>();
            q.add(new Pair(seedVertex, 0));

            HashSet<K> seen = new HashSet<>();
            while (!q.isEmpty()) {
                Pair<K, Integer> currentVertex = q.remove();

                K node = currentVertex.fst;
                int newScore = currentVertex.snd + 1;
                seen.add(node);

                for (K nbr : Graphs.neighborListOf(graph, node)) {
                    if (!seen.contains(nbr)) {
                        q.add(new Pair(nbr, newScore));
                        currentDiameterEstimate = (currentDiameterEstimate < newScore) ? newScore : currentDiameterEstimate;
                    }
                }
            }

            visitedNodes.removeAll(seen);
            diameter = diameter < currentDiameterEstimate ? currentDiameterEstimate : diameter;
            System.out.println("Remaining Nodes : " + visitedNodes.size());
        }

        return diameter;
    }

    public int getDiameterByDFS() {
        int diameter = 0;

        ConnectivityInspector<K, V> ci = new ConnectivityInspector<>(graph);
        List<Set<K>> connectedSets = ci.connectedSets();

        for (Set<K> g : connectedSets) {
            //choose initial vertex and conduct BFS on it
            Iterator<K> iter = g.iterator();
            K initialVertex = iter.next();

            HashSet<K> seen = new HashSet<>();

            int currentDiameterEstimate = doDFS(initialVertex, seen, 0, diameter);
            diameter = diameter < currentDiameterEstimate ? currentDiameterEstimate : diameter;
        }

        System.out.println("Diam by DFS : " + diameter);

        return diameter;
    }

    private int doDFS(K vertex, HashSet<K> seen, int i, int diameter) {
        List<K> nbrs = Graphs.neighborListOf(graph, vertex);
        seen.add(vertex);
        diameter = i + 1 > diameter ? i + 1 : diameter;

        if (nbrs == null) {
            return diameter;
        }

        nbrs.removeAll(seen);
        if (nbrs.isEmpty()) {
            return diameter;
        }

        for (K nbr : nbrs) {
            int d = doDFS(nbr, seen, i + 1, diameter);
            diameter = d > diameter ? d : diameter;
        }

        return diameter;
    }
}
