package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Netzplan {
    public static final String GREEN = "\u001B[32m";
    public static final String COLOR_RESET = "\u001B[0m";

    public static void main(String[] args) {
        Netzplan netzplan = new Netzplan();
        netzplan.getInput();
        //netzplan.validate();
        netzplan.createNet();
        netzplan.fillNet();
        netzplan.iterateToEnd();
        netzplan.iterateToStart();
        netzplan.calculateOptimalPath();
    }

    List<Node> net;
    List<String> inputs_NodeNames;
    List<Float> inputs_ProcessingTime;
    List<String> inputs_PreviousNodes;
    List<String> inputs_FollowingNodes;

    Node startingNode;
    Scanner scanner;
    InputType inputType;

    public Netzplan() {
        scanner = new Scanner(System.in);
        net = new ArrayList<>();
        inputs_NodeNames = new ArrayList<>();
        inputs_ProcessingTime = new ArrayList<>();
        inputs_PreviousNodes = new ArrayList<>();
    }

    private void getInput() {
        try {
            getInputType();
            do {
                inputNodeName();
                inputProcessingTime();
                inputRelatedNodes();
            } while (askToRepeat());
        } catch (InputMismatchException e) {
            e.printStackTrace();
        }
    }

    private void getInputType() {
        System.out.print("Do you know the previous or following node? \n(prev / foll):\t\t");
        String input = scanner.next();
        if (input.equals("prev")) {
            inputType = InputType.PREVIOUS_NODES;
        } else if (input.equals("foll")) {
            inputType = InputType.NEXT_NODES;
        } else {
            System.out.println("Invalid Input.");
            System.exit(0);
        }
        System.out.println("------------------");
    }

    private void inputNodeName() {
        while (true) {
            System.out.print("Node name:\t\t\t");
            String input = scanner.next();
            if (!inputs_NodeNames.contains(input)) {
                inputs_NodeNames.add(input);
                break;
            } else {
                System.out.println("Node name already exists. Please try again:");
            }
        }
    }

    private void inputProcessingTime() {
        System.out.print("Process time:\t\t");
        inputs_ProcessingTime.add(scanner.nextFloat());
    }

    private void inputRelatedNodes() {
        if (inputType == InputType.PREVIOUS_NODES) {
            System.out.print("Previous nodes\n(Ex: A,B):\t\t\t");
            inputs_PreviousNodes.add(scanner.next());
        } else if (inputType == InputType.NEXT_NODES) {
            System.out.print("Following nodes\n(Ex: A,B):\t\t\t");
            inputs_FollowingNodes.add(scanner.next());
        }
    }

    // Check wether the user wants to add another node to the net or not
    private boolean askToRepeat() {
        System.out.print("Add another one? \n(y/n)\t\t\t\t");
        String input = scanner.next();
        if (input.equals("n")) {
            return false;
        } else if (input.equals("y")) {
            System.out.println("------------------");
            return true;
        } else {
            System.out.println("Invalid Input.");
            System.exit(0);
            return false;
        }
    }

//    private void validate() {
//        System.out.println("-------------------");
//        String[] nodeNames;
//
//        for (int i = 0; i < inputs_PreviousNodes.size(); i++) {
//            if (inputs_PreviousNodes.get(i).equals("0")) {
//                nodeNames = inputs_PreviousNodes.get(i).split(",");
//                for (String nodeName : nodeNames) {
//                    int occurrences = Collections.frequency(Arrays.asList(nodeNames), nodeName);
//                    if (occurrences > 1) {
//                        System.out.println(inputs_NodeNames.get(i) + " contains " + occurrences + " duplicates. Exiting...");
//                        System.exit(0);
//                    }
//                }
//            }
//        }
//        System.out.println("-------------------");
//        System.out.println("The whole table is valid! Generate net...");
//    }

    private void createNet() {
        System.out.println("-------------------");
        // Check for the node that requires the starting node
        for (int i = 0; i < inputs_NodeNames.size(); i++) {
            if (inputs_PreviousNodes.get(i).equals("0")) {
                System.out.println("Starting node: " + inputs_NodeNames.get(i));
                startingNode = new Node(inputs_NodeNames.get(i), inputs_ProcessingTime.get(i));
                net.add(startingNode);
            }
            String currentNodenName = inputs_NodeNames.get(i);
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
                        Node node = new Node(currentNodenName, inputs_ProcessingTime.get(i));
                        net.add(node);
                    }
                }
            }
        }
        System.out.println("Net plan created.");
    }

    private void fillNet() {
        System.out.println("-------------------");
        System.out.println(GREEN + "Net plan:" + COLOR_RESET);
        for (int i = 0; i < net.size(); i++) {
            // Ignore the starting node
            if (!net.get(i).name.equals(startingNode.name)) {
                // Add the previous nodes
                String[] previousNodeNames = inputs_PreviousNodes.get(i).split(",");
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

    private void calculateOptimalPath() {
        calcGP();
        calcFP();
        getPath();
    }
}

enum InputType {
    PREVIOUS_NODES, NEXT_NODES
}
