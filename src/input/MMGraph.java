package input;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author avishekanand
 */
public class MMGraph {

    int[] nodes;
    
    ArrayList<Integer>[] adjacencyList;
    
    public MMGraph(int _nodes){
        adjacencyList = new ArrayList[_nodes];
        //construct adjacency list backbone
        for (ArrayList<Integer> node : adjacencyList) {
            node = new ArrayList<Integer>();
        }
    }
    
    public void createBipartiteGraph(int _maxDegree){
        ArrayList<Integer> leftSet = new ArrayList<>();
        ArrayList<Integer> rightSet = new ArrayList<>();
        
        //create left and right sets
        for (int i = 0; i < adjacencyList.length; i++) {
            if (Math.random() >= 0.5) {
                leftSet.add(i);
            } else {
                rightSet.add(i);
            }
        }
        
        Random r = new Random();
        
        int rightNodesCount = rightSet.size();
        //create random edges between left and right sets
        for (Integer leftindex : leftSet) {
            
            int degree = (int)Math.floor(r.nextDouble()*rightNodesCount);
            
            for (int i = 0; i < degree; i++) {
                int rightindex = (int)Math.floor(r.nextDouble()*rightNodesCount);
            
                ArrayList<Integer> leftnode = adjacencyList[leftindex];
            
                if (leftnode == null) {
                    leftnode = new ArrayList<>();
                    leftnode.add(rightindex);
                } else {
                    boolean present = false;
                    for(Integer node : leftnode){
                        if (node == rightindex) {
                            present = true;
                            break;
                        }
                    }
                    
                    if (!present) {
                        leftnode.add(rightindex);
                    }
                }
            }
        }
        
        System.out.println("Graph Constructed...");
        for (int i = 0; i < adjacencyList.length; i++) {
            System.out.print(i + " : ");
            if (adjacencyList[i]==null) {
                continue;
            }
            for (Integer nodes : adjacencyList[i]) {
                System.out.print (nodes + ", ");
            }
            System.out.println("");
        }
    }
    
    public void addEdge(int source, int target){
        
    }
    
    public static void main(String[] args) {
        MMGraph graph = new MMGraph(5);
        graph.createBipartiteGraph(3);
    }
    
}
