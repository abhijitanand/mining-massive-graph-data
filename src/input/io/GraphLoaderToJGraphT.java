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
import java.util.zip.GZIPInputStream;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author avishekanand
 */
public class GraphLoaderToJGraphT {
    
    BufferedReader _input;
    
    DirectedGraph<Integer, DefaultEdge> webGraph;
    
    public GraphLoaderToJGraphT(String input) throws IOException{
        _input = getReader(input);
        
        int nodes = 0;
        int edges = 0;
        
        //first line contians node and edge information
        if(_input.ready()){
            String[] line = _input.readLine().split("\\s+");
            
            nodes = Integer.parseInt(line[3]);
            edges = Integer.parseInt(line[6]);
            
            //System.out.println("Nodes : " + nodes + " , edges : " + edges);
        }
        
        //_input.close();
        // create a graph based on URL objects
        webGraph = constructDirectedWebGraph(_input, nodes);
    }
    
    public BufferedReader getReader(String inputFile) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader (
                                                new GZIPInputStream(new FileInputStream(inputFile))));
        
        return br;
    }
    
    public static void main(String[] args) throws IOException {
        GraphLoaderToJGraphT graphLoader = new GraphLoaderToJGraphT("/Users/avishekanand/research/data/de-yr-graphs/de-2000.gz");
        
        System.out.println(graphLoader.webGraph.toString());
        
    }

    public DirectedGraph getWebGraph(){
        return webGraph;
    }
            
            
    private DirectedGraph<Integer, DefaultEdge> constructDirectedWebGraph(BufferedReader _input, int nodes) throws IOException {
        DirectedGraph<Integer, DefaultEdge> g =
            new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        
        ArrayList<Integer>[] adjacencyLists = new ArrayList[nodes];
        
        HashSet<Integer> uniqueNodes = new HashSet<Integer>();

        int[] nodeIDs = new int[nodes];
        
        try {
            int index = 0;
            // add vertices
            while (_input.ready()) {                
                String[] list = _input.readLine().split("\\s+");
                
                int nodeID = Integer.parseInt(list[0]);
                uniqueNodes.add(nodeID);
                
                g.addVertex(nodeID);
                
                adjacencyLists[index] = new ArrayList<>();
                nodeIDs[index] = nodeID;
                
                //cases where a node doesnt have edges
                if (list.length <= 1){
                    continue;
                }
                
                for (int i = 1; i < list.length; i++){
                    int target = Integer.parseInt(list[i]);
                    
                    if (!uniqueNodes.contains(target)) {
                        g.addVertex(target);
                    }
                    adjacencyLists[index].add(target);
                }
                index++;
            }
            

            // add edges to create linking structure
            for (int i = 0; i < adjacencyLists.length; i++) {
                int source = nodeIDs[i];
                
                if(adjacencyLists[i] == null || adjacencyLists[i].isEmpty()){
                    continue;
                }
                
                for (int target : adjacencyLists[i]){
                    g.addEdge(source, target);
                }
            }
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return g;
    }
    
    
    public TIntObjectHashMap<String> createIdToLabelMappings(String mapFile) throws FileNotFoundException, IOException{
        TIntObjectHashMap<String> nodeToIdMappings = new TIntObjectHashMap<>();
        
        BufferedReader br = new BufferedReader(new InputStreamReader (
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
