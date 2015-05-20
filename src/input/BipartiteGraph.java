package input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author avishekanand
 */
public class BipartiteGraph {
    ArrayList<Integer> leftSet, rightSet;
    
    ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<>();
    
    public BipartiteGraph(String graphFile) throws FileNotFoundException, IOException{
        leftSet = new ArrayList<>();
        rightSet = new ArrayList<>();
        
        HashSet<Integer> rightUniqueSet = new HashSet<>();
        
        BufferedReader br = new BufferedReader(new FileReader(graphFile));
        while (br.ready()) {            
            String line = br.readLine();
            String[] nodes = line.split("\\s+");
            
            int leftNode = Integer.parseInt(nodes[0]);
            
            leftSet.add(leftNode);
            ArrayList<Integer> edges = new ArrayList<Integer>();
            
            if (nodes.length > 1) {
                for (int i = 1; i < nodes.length; i++) {
                    int rightNode = Integer.parseInt(nodes[i]);
                    //add to right set
                    if (!rightUniqueSet.contains(rightNode)) {
                        rightSet.add(rightNode);
                        rightUniqueSet.add(rightNode);
                    }
                    
                    //add to adjacency List
                    edges.add(rightNode);
                }
            }
            
            adjacencyList.add(edges);
        }
        br.close();
    }

    public void printGraph() {
        for (int i=0; i < leftSet.size(); i++) {
            System.out.print(leftSet.get(i) + " - ");
            for (Integer leftnode : adjacencyList.get(i)) {
                System.out.print(leftnode + ",");
            }
            
            System.out.println("");
        }
    }
    
    public int getLeftSetSize(){
        return leftSet.size();
    }
    
    public int getRightSetSize(){
        return rightSet.size();
    }
    
    public HashMap<Integer, ArrayList<Integer>> getHashBasedAdjacencyList(){
        HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
        
        for (int i=0; i < leftSet.size(); i++) {
            map.put(leftSet.get(i), adjacencyList.get(i));
        }
        
        return map;
    }
}
