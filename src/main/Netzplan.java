package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Netzplan {

    public static void main(String[] args) {
        Netzplan netzplan = new Netzplan();
        netzplan.getNodes();
        netzplan.validate();
        netzplan.createNet();
        netzplan.fillNet();
        netzplan.iterateToEnd();
        netzplan.iterateToStart();
        netzplan.calcGP();
        netzplan.calcFP();
        netzplan.getPath();
    }

    List<Node> net;
    List<String> allNodeNames;
    List<Float> allNodeProcessingTimes;
    List<String> allpreviousNodeNames;
    List<String> allnextNodeNames;

    Node startingNode;
    Scanner scanner;
    Input givenInput;

    public Netzplan() {
        scanner = new Scanner(System.in);
        net = new ArrayList<>();
        allNodeNames = new ArrayList<>();
        allNodeProcessingTimes = new ArrayList<>();
        allpreviousNodeNames = new ArrayList<>();
    }

    private void getNodes() {
        System.out.print("Do you know the previous or following node? \n(prev / foll):\t\t");
        String input;
        try {
            input = scanner.next();
            if (input.equals("prev")) {
                givenInput = Input.PREVIOUS_NODES;
            } else if (input.equals("foll")) {
                givenInput = Input.NEXT_NODES;
            } else {
                System.out.println("Invalid Input.");
                System.exit(0);
            }
            System.out.println("------------------");

            while (true) {
                while (true) {
                    System.out.print("Node name:\t\t");
                    input = scanner.next();
                    if (!allNodeNames.contains(input)) {
                        allNodeNames.add(input);
                        break;
                    } else {
                        System.out.println("Node name already exists. Please try again:");
                    }
                }

                // Get the porcessing time
                System.out.print("Process time:\t\t");
                allNodeProcessingTimes.add(scanner.nextFloat());

                // Get the name of either the following or the previous node
                if (givenInput == Input.PREVIOUS_NODES) {
                    System.out.print("Previous nodes\n(Ex: A,B):\t\t");
                    allpreviousNodeNames.add(scanner.next());

                } else if (givenInput == Input.NEXT_NODES) {
                    System.out.print("Following nodes\n(Ex: A,B):\t\t");
                    allnextNodeNames.add(scanner.next());
                }

                // Check wether the user wants to add another node to the net or not
                System.out.print("Add another one? y/n\t");
                input = scanner.next();
                if (input.equals("n")) {
                    break;
                } else if (input.equals("y")) {
                    System.out.println("------------------");
                }else {
                    System.out.println("Invalid Input.");
                    System.exit(0);
                }
            }
        } catch (InputMismatchException e) {
            e.printStackTrace();
        }
    }

    private void validate() {
        System.out.println("-------------------");
        String[] nodeNames;

        for (int i = 0; i < allpreviousNodeNames.size(); i++) {
            // Check if the current node is the starting node
            if (allpreviousNodeNames.get(i).equals("0")) {
                System.out.println("Starting node: " + allNodeNames.get(i));
                startingNode = new Node(allNodeNames.get(i), allNodeProcessingTimes.get(i));
                net.add(startingNode);
            } else {
                // Check if the given previous nodes are valid
                nodeNames = allpreviousNodeNames.get(i).split(",");
                for (int d = 0; d < nodeNames.length; d++) {
                    int occurrences = Collections.frequency(Arrays.asList(nodeNames), nodeNames[d]);
                    if (occurrences > 1) {
                        System.out.println(allNodeNames.get(i) + " contains " + occurrences + " duplicates. Exiting...");
                        System.exit(0);
                    }
                }
            }
        }
        System.out.println("-------------------");
        System.out.println("The whole table is valid! Generate net...");
    }

    private void createNet() {
        System.out.println("-------------------");
        
        // Check for the node that requires the starting node
        for (int i = 0; i < allNodeNames.size(); i++) {
            
            String currentNodenName = allNodeNames.get(i);
            // Ignore the starting node name
            if (!currentNodenName.equals(startingNode.name)) {
                
                // Check if the node is already initialized
                boolean found = false;  
                for (int netIndex = 0; netIndex < net.size(); netIndex++) {
                    
                    // If so then do nothing 
                    if (net.get(netIndex).name.equals(currentNodenName)) {
                        //System.out.println("Node " + currentNodenName + " already in the net");
                        found = true;
                    }
                    // If not add it to the net
                    else if (netIndex == net.size() - 1 && !found) {
                        // System.out.println("Node with the name " + currentNodenName
                        //         + " not found in the net. Adding the node to the net...");
                        Node node = new Node(currentNodenName, allNodeProcessingTimes.get(i));
                        net.add(node);
                    }
                }
            }
        }
        System.out.println("Net plan created.");
    }

    private void fillNet() {
        System.out.println("-------------------");
        System.out.println("\n\nNet plan:");
        for (int i = 0; i < net.size(); i++) {
            
            // Ignore the starting node
            if (!net.get(i).name.equals(startingNode.name)) {
               
                // Add the previous nodes
                String[] previousNodeNames = allpreviousNodeNames.get(i).split(",");
                for (int p = 0; p < previousNodeNames.length; p++) {
                    for (Node nodes : net) {
                        if (nodes.name.equals(previousNodeNames[p])) {
                            net.get(i).previousNodes.add(nodes); // B now knows A
                            net.get(i).previousNodes.get(net.get(i).previousNodes.size() - 1).addNextNode(net.get(i)); // A now knows B                                                                                        
                        }
                    }
                }
            }
        }
    }

    private void iterateToEnd() {
        for (Node node : net) {
            if (node.previousNodes.size() == 0) {
                node.setFAZ(0);
            }
        }
    }

    private void iterateToStart() {
        for (Node node : net) {
            if (node.nextNodes.size() == 0) {
                node.setSEZ(node.faz);
            }
        }
    }

    private void calcGP() {
        for (Node node : net) {
            node.calcGP();
        }
    }

    private void calcFP() {
        for (Node node : net) {
            node.calcFP();
            node.debug();
        }
    }

    private void getPath() {
        for (Node node : net) {
            if (node.previousNodes.size() == 0) {
                System.out.print(node.name);
                for (Node nextNode : node.nextNodes) {
                    nextNode.getPath();
                }
            }
        }
    }
}

enum Input {
    PREVIOUS_NODES, NEXT_NODES
}
