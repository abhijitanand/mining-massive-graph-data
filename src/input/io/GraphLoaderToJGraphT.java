package input.io;

import gnu.trove.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class GraphLoaderToJGraphT {

    private static final Logger log = Logger.getLogger(GraphLoaderToJGraphT.class.getName());

    BufferedReader _input = null;

    DirectedGraph<Integer, DefaultEdge> webGraph;

    public GraphLoaderToJGraphT(){

    }
     
    public GraphLoaderToJGraphT(String input) throws IOException {
        _input = getReader(input);

        int nodes = 0;
        int edges = 0;

        //first line contians node and edge information
        if (_input.ready()) {
            String[] line = _input.readLine().split("\\s+");

            nodes = Integer.parseInt(line[3]);
            edges = Integer.parseInt(line[6]);
            //System.out.println("Nodes : " + nodes + " , edges : " + edges);
        }

        //_input.close();
        // create a graph based on URL objects
        //webGraph = constructDirectedWebGraph(_input, nodes);
        //webGraph = constructDirectedWebGraphArray(_input, nodes);
        webGraph = constructDirectedWebGraphUnknownVertices(_input);
    }

    public BufferedReader getReader(String inputFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(inputFile))));

        return br;
    }

    public static void main(String[] args) throws IOException {
        GraphLoaderToJGraphT graphLoader = new GraphLoaderToJGraphT("/Users/avishekanand/research/data/graphlog/out/de-2000.gz");
        //GraphLoaderToJGraphT graphLoader = new GraphLoaderToJGraphT("/Users/avishekanand/research/data/de-yr-graphs/de-2000.gz");
//
        //System.out.println(graphLoader.webGraph.toString());

    }

    public DirectedGraph getWebGraph() {
        return webGraph;
    }

    private DirectedGraph<Integer, DefaultEdge> constructDirectedWebGraph(BufferedReader _input, int nodes) throws IOException {
        DirectedGraph<Integer, DefaultEdge> g
                = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        ArrayList<Integer>[] adjacencyLists = new ArrayList[nodes];

        HashSet<Integer> uniqueNodes = new HashSet<Integer>();

        int[] nodeIDs = new int[nodes];

        int edgeCount = 0;
        try {
            int index = 0;
            // add vertices
            while (_input.ready()) {
                String[] list = _input.readLine().split("\\s+");

                int nodeID = Integer.parseInt(list[0]);

                if (uniqueNodes.contains(nodeID)) {
                    System.out.println("Error: node has more than one adjacency lists");
                }
                g.addVertex(nodeID);
                uniqueNodes.add(nodeID);

                adjacencyLists[index] = new ArrayList<>();
                nodeIDs[index] = nodeID;

                //cases where a node doesnt have edges
                if (list.length <= 1) {
                    edgeCount++;
                    continue;
                }

                for (int i = 1; i < list.length; i++) {
                    int target = Integer.parseInt(list[i]);

                    if (!uniqueNodes.contains(target)) {
                        g.addVertex(target);
                    }
                    adjacencyLists[index].add(target);
                    edgeCount++;
                }
                index++;
            }

            _input.close();
            log.log(Level.INFO, "Created Graph from Adjacency Lists for " + adjacencyLists.length + " nodes and " + edgeCount + " edges");
            // add edges to create linking structure
            int stepSize = adjacencyLists.length / 200;

            for (int i = 0; i < adjacencyLists.length; i++) {
                int source = nodeIDs[i];

                if (adjacencyLists[i] == null || adjacencyLists[i].isEmpty()) {
                    continue;
                }

                long time = System.currentTimeMillis();
                for (int target : adjacencyLists[i]) {
                    g.addEdge(source, target);
                }

                System.out.println(adjacencyLists[i].size() + " edges in " + (System.currentTimeMillis() - time) / 1000 + " seconds");
                if (i % stepSize == 0) {
                    log.log(Level.INFO, i + " nodes processed");
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return g;
    }

    private DirectedGraph<Integer, DefaultEdge> constructDirectedWebGraphArray(BufferedReader _input, int nodes) throws IOException {
        DirectedGraph<Integer, DefaultEdge> g
                = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        int[][] adjacencyLists = new int[nodes][];

        HashSet<Integer> uniqueNodes = new HashSet<Integer>();

        int[] nodeIDs = new int[nodes];

        int edgeCount = 0;
        try {
            int index = 0;
            // add vertices
            while (_input.ready()) {
                String[] list = _input.readLine().split("\\s+");

                int nodeID = Integer.parseInt(list[0]);

                if (uniqueNodes.contains(nodeID)) {
                    System.out.println("Error: node has more than one adjacency lists");
                }
                g.addVertex(nodeID);
                uniqueNodes.add(nodeID);

                //cases where a node doesnt have edges
                if (list.length <= 1) {
                    edgeCount++;
                    continue;
                }

                adjacencyLists[index] = new int[list.length - 1];
                nodeIDs[index] = nodeID;

                for (int i = 1; i < list.length; i++) {
                    int target = Integer.parseInt(list[i]);

                    if (!uniqueNodes.contains(target)) {
                        g.addVertex(target);
                    }
                    adjacencyLists[index][i - 1] = target;
                    edgeCount++;
                }
                index++;
            }

            _input.close();
            log.log(Level.INFO, "Creating Graph from Adjacency Lists for " + adjacencyLists.length + " nodes and " + edgeCount + " edges");
            // add edges to create linking structure
            int stepSize = adjacencyLists.length / 200;

            for (int i = 0; i < adjacencyLists.length; i++) {
                int source = nodeIDs[i];

                if (adjacencyLists[i] == null) {
                    continue;
                }

                long time = System.currentTimeMillis();
                for (int target : adjacencyLists[i]) {
                    g.addEdge(source, target);
                }

                System.out.println(adjacencyLists[i].length + " edges in " + (System.currentTimeMillis() - time) / 1000 + " seconds");
                if (i % stepSize == 0) {
                    log.log(Level.INFO, i + " nodes processed");
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return g;
    }
    
    
    private DirectedGraph<Integer, DefaultEdge> constructDirectedWebGraphUnknownVertices(BufferedReader _input) throws IOException {
        DirectedGraph<Integer, DefaultEdge> g
                = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        int edgeCount = 0;
        try {
            // add vertices
            while (_input.ready()) {
                String[] list = _input.readLine().split("\\s+");

                int nodeID = Integer.parseInt(list[0]);

                if (!g.containsVertex(nodeID)) {
                    g.addVertex(nodeID);
                }
                
                //cases where a node doesnt have edges
                if (list.length <= 1) {
                    edgeCount++;
                    continue;
                }

                for (int i = 1; i < list.length; i++) {
                    int target = Integer.parseInt(list[i]);

                    if (!g.containsVertex(target)) {
                        g.addVertex(target);
                    }
                    
                    g.addEdge(nodeID, target);
                    edgeCount++;
                }
            }

            _input.close();
            log.log(Level.INFO, "Created Graph from Adjacency Lists for " + g.vertexSet().size() + " nodes and " + edgeCount + " edges");
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return g;
    }

    /**
     * 
     * Constructs a bi-partite graph from the input files.
     * 
     * Here is the list of following ignores:
     * 
     * 1) Ignores edges which have source = destination
     * 2) Ignore edges if the destination is already present in the source set
     * 
     * @param _input
     * @param nodeOffsetInFile
     * @param edgeOffset
     * @return
     * @throws IOException 
     */
    public UndirectedGraph<String, DefaultEdge> constructBipartiteUndirectedUnweightedGraph(BufferedReader _input, int nodeOffsetInFile, 
                                                        int edgeOffset, HashSet<String> leftSet, HashSet<String> rightSet) throws IOException {
        UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

        int edgeCount = 0;
        int vertexCount = 0;
        
        String[] list = {"nukll"};
        try {
            // add vertices
            while (_input.ready()) {
                list = _input.readLine().split("\\s+");

                String source = list[nodeOffsetInFile];
                
                if (!g.containsVertex(source) && !rightSet.contains(source)){
                    g.addVertex(source);                    
                    vertexCount++;
                    
                    //maintain left set
                    leftSet.add(source);
                }
                
                //cases where a node doesnt have edges
                if (list.length <= nodeOffsetInFile+1) {
                    edgeCount++;
                    continue;
                }

                for (int i = edgeOffset; i < list.length; i++) {
                    String target = list[i];

                    if (!g.containsVertex(target)) {
                        g.addVertex(target);
                        vertexCount++;
                    }
                    
                    if (!g.containsEdge(source,target) && !source.equals(target) && !leftSet.contains(target)){
                        g.addEdge(source, target);
                        rightSet.add(target);
                        edgeCount++;
                    }
                }
            }

            _input.close();
            log.log(Level.INFO, "Creating Graph from Adjacency Lists for " + vertexCount + " nodes and " + edgeCount + " edges");
        } catch (MalformedURLException e) {
            e.printStackTrace();            
        }
        catch(Exception e){
            for (String string : list) {
                    System.out.println(string + "");
                }
            
        }
        return g;
    }
    
    
    public TIntObjectHashMap<String> createIdToLabelMappings(String mapFile) throws FileNotFoundException, IOException {
        TIntObjectHashMap<String> nodeToIdMappings = new TIntObjectHashMap<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(mapFile))));

        while (br.ready()) {
            String[] line = br.readLine().split("\\s+");

            String node = line[0];
            int id = Integer.parseInt(line[1]);

            //System.out.println(node + "\t" + id);
            nodeToIdMappings.put(id, node);
        }
        br.close();

        return nodeToIdMappings;
    }
}
