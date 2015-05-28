package input.io;

import gnu.trove.TObjectIntHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author avishekanand
 */
public class GraphLoader {
    public GraphLoader(){
        
    }
    
    private void encodeFile(String filename, String outfile, String mapFile) throws IOException{
        //get node identifiers from triad
        TObjectIntHashMap<String> nodeMappings = loadMapFile(mapFile);
        
        ArrayList<Integer>[] adjacencyList = new ArrayList[nodeMappings.size()];
        //construct write adjacency lists
        int nodeCount = 0;
        int edgeCounts = 0;
        
        BufferedReader br = new BufferedReader(new FileReader(filename));
        while (br.ready()){
            String line = br.readLine();
            String[] edge = line.split("\\s+");
            
            int source = nodeMappings.get(edge[0]);
            int target = nodeMappings.get(edge[1]);
            
            //Get index and store edge to the index list
            if (adjacencyList[source] == null) {
                adjacencyList[source] = new ArrayList<>();
                nodeCount++;
            }
            adjacencyList[source].add(target);
            edgeCounts++;
        }
        br.close();
        
        //write adjacency list to output file
        GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File(outfile)));

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
        
        //BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outfile)));
        
        bw.write("# nodes : " + nodeCount + " edges : " + edgeCounts + "\n");
        //write adj list
        for (int node = 0; node < adjacencyList.length; node++) {
            ArrayList<Integer> list = adjacencyList[node];
            
            if (list == null) {
                continue;
            }
            Collections.sort(list);
            bw.write(node + "");
            //System.out.print(node);
            
            //remove self loops
            for (Integer target : list) {
                if(target == node){
                    continue;
                }
                
                bw.write("\t" + target);
                //System.out.print("\t" + target);
            }
            bw.write("\n");
            //System.out.println("");
        }
        bw.close();
    }
    
    private void createMapFile(String inputFile, String mapFile) throws IOException{
        NodeDegree[] compressInputGraph = compressInputGraph(inputFile);
        
         GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(new File(mapFile)));
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));
        
        //BufferedWriter bw = new BufferedWriter(new FileWriter(mapFile));
        
        for (NodeDegree node : compressInputGraph) {
            bw.write(node.nodeName + "\t" + node.id + "\n");
        }
        bw.close();
    }

    private TObjectIntHashMap<String> loadMapFile(String mapFile) throws FileNotFoundException, IOException{
        TObjectIntHashMap<String> nodeToIdMappings = new TObjectIntHashMap<>();
        
         //BufferedReader br = new BufferedReader(new FileReader(mapFile));
         BufferedReader br = new BufferedReader(new InputStreamReader (
                                                new GZIPInputStream(new FileInputStream(mapFile))));
         
         while (br.ready()) {            
            String[] line = br.readLine().split("\\s+");
            
            String node = line[0];
            int id = Integer.parseInt(line[1]);
            
             //System.out.println(node + "\t" + id);
            
            nodeToIdMappings.adjustOrPutValue(node, id, id);
        }
        br.close();
        
        return nodeToIdMappings;
    }
    
    private NodeDegree[] compressInputGraph(String filename) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        
        TObjectIntHashMap<String> targetCounter = new TObjectIntHashMap<>();
        
        //Assume directed edges being streamed in source (tab) target (tab) weight
        int edges = 0;
        //first iteration for compression
        while (br.ready()){
            String line = br.readLine();
            String target = line.split("\\s+")[1];
            
            targetCounter.adjustOrPutValue(target, 1, 1);
            edges++;
        }
        br.close();
        System.out.println("Input graph parsed and indegrees initialized");
        
        
        NodeDegree[] _indegrees = new NodeDegree[targetCounter.size()];
        int i = 0;
        for (Object node : targetCounter.keys()) {
            _indegrees[i++] = new NodeDegree((String)node, targetCounter.get((String)node));
        }
        //Sorted acc. to indegrees for best compression
        Arrays.sort(_indegrees);
        System.out.println("Compression-based sorting done..");
        
        //Assigning identifiers
        int id = 0;
        for (NodeDegree node : _indegrees) {
            node.id = id++;
        }
        
        System.out.println("Nodes : " + _indegrees.length + "\tEdges : " + edges);
        return _indegrees;
    }
    
    class NodeDegree implements Comparable<NodeDegree>{
        String nodeName;
        int indegree;
        int id;
        
        public NodeDegree(String name, int degree) {
            nodeName = name;
            indegree = degree;
        }

        @Override
        public int compareTo(NodeDegree o) {
            if (this.indegree > o.indegree) {
               return -1;
            } else if (this.indegree < o.indegree){
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        GraphLoader gir = new GraphLoader();
        
        if (args.length == 0) {
            System.out.println("Usage : java -jar graphlog.jar [OPTIONS] <files> ... ");
            System.out.println("\t -c <input graph filename> <output mapfile name>");
            System.out.println("\t -e <input graph filename> <output file name> <map filename>");
        }
        
        if(args[0].equals("-c")){
            gir.createMapFile(args[1], args[2]);
        } else if(args[0].equals("-e")){
            System.out.println("Encoding File " + args[1] + " with map : " + args[3]);
            gir.encodeFile(args[1], args[2], args[3]);
        }
    }
}


