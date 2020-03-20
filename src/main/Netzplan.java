package main;

import java.util.*;

public class Netzplan {
    public static final String GREEN = "\u001B[32m";
    public static final String ORANGE = "\u001b[34m";
    public static final String COLOR_RESET = "\u001B[0m";

    public static void main(String[] args) {
        new Netzplan();
    }

    List<Node> net;
    List<String> inputs_NodeNames;
    List<Float> inputs_ProcessingTime;
    List<String> inputs_ConnectedNodes;

    Node startingNode;
    Scanner scanner;
    InputType inputType;

    public Netzplan() {
        scanner = new Scanner(System.in);
        net = new ArrayList<>();
        inputs_NodeNames = new ArrayList<>();
        inputs_ProcessingTime = new ArrayList<>();
        inputs_ConnectedNodes = new ArrayList<>();

        getInput();
        validate();
        getStartingNode();
        createNet();
        fillNet();
        iterateToEnd();
        iterateToStart();
        calculateOptimalPath();
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
        System.out.print("Do you know all previous or following nodes? \n(prev / foll):\t\t");
        String input = scanner.next();
        if (input.equals("prev")) {
            inputType = InputType.PREVIOUS_NODES;
        } else if (input.equals("foll")) {
            inputType = InputType.NEXT_NODES;
        } else {
            System.out.println("Invalid Input.");
            System.exit(0);
        }
        System.out.println("------------------------");
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
            System.out.print("Previous nodes:\t\t");
            inputs_ConnectedNodes.add(scanner.next());
        } else if (inputType == InputType.NEXT_NODES) {
            System.out.print("Following nodes:\t");
            inputs_ConnectedNodes.add(scanner.next());
        }
    }

    private boolean askToRepeat() {
        System.out.print("Add another one? \n(y/n)\t\t\t\t");
        String input = scanner.next();
        if (input.equals("n")) {
            return false;
        } else if (input.equals("y")) {
            System.out.println("------------------------");
            return true;
        } else {
            System.out.println("Invalid Input.");
            System.exit(0);
            return false;
        }
    }

    private void validate() {
        String[] nodeNames;
        for (int i = 0; i < inputs_ConnectedNodes.size(); i++) {
            if (inputs_ConnectedNodes.get(i).equals("0")) {
                nodeNames = inputs_ConnectedNodes.get(i).split(",");
                for (String nodeName : nodeNames) {
                    int occurrences = Collections.frequency(Arrays.asList(nodeNames), nodeName);
                    if (occurrences > 1) {
                        System.out.println(inputs_NodeNames.get(i) + " contains " + occurrences + " duplicates. Exiting...");
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void createNet() {
        for (int i = 0; i < inputs_NodeNames.size(); i++) {
            String current = inputs_NodeNames.get(i);
            if (!current.equals(startingNode.name)) {
                // Check if the node is already initialized
                boolean found = false;
                for (int p = 0; p < net.size(); p++) {
                    if (net.get(p).name.equals(current)) {
                        found = true;
                    }
                    // At the end of the iteration
                    // add it to the net if it is not marked as found
                    else if (p == net.size() - 1 && !found) {
                        Node node = new Node(current, inputs_ProcessingTime.get(i));
                        net.add(node);
                    }
                }
            }
        }
    }

    private void getStartingNode() {
        System.out.println("------------------------");
        for (int i = 0; i < inputs_NodeNames.size(); i++) {
            if (inputs_ConnectedNodes.get(i).equals("0") && inputType == InputType.PREVIOUS_NODES) {
                System.out.println("Starting node: " + inputs_NodeNames.get(i));
                startingNode = new Node(inputs_NodeNames.get(i), inputs_ProcessingTime.get(i));
                net.add(startingNode);
                break;
            } else if (!inputs_ConnectedNodes.get(i).equals("0") && inputType == InputType.NEXT_NODES) {
                System.out.println("Starting node: " + inputs_NodeNames.get(i));
                startingNode = new Node(inputs_NodeNames.get(i), inputs_ProcessingTime.get(i));
                net.add(startingNode);
                break;
            }
        }
    }

    private void fillNet() {
        System.out.println("------------------------");
        System.out.println(GREEN + "Net plan:" + COLOR_RESET);

        for (int i = 0; i < net.size(); i++) {
            String[] connectedNodes = inputs_ConnectedNodes.get(i).split(",");

            if (!net.get(i).name.equals(startingNode.name) && inputType == InputType.PREVIOUS_NODES) {
                //Check the net if there are any nodes that match to the list
                // of previous nodes that the current node holds and connect them
                for (String connectedNode : connectedNodes) {
                    for (Node node : net) {
                        if (node.name.equals(connectedNode)) {
                            net.get(i).previousNodes.add(node); // B now knows A
                            net.get(i).previousNodes.get(net.get(i).previousNodes.size() - 1).addNextNode(net.get(i)); // A now knows B
                        }
                    }
                }
            }
            //Next nodes    net.get(i).name.equals(startingNode.name) &&
            else if (inputType == InputType.NEXT_NODES) {
                for (String connectedNode : connectedNodes) {
                    for (Node potentialNextNode : net) {
                        //If the name matches the name in the list of next nodes, connect both
                        if (potentialNextNode.name.equals(connectedNode)) {
                            net.get(i).nextNodes.add(potentialNextNode); //A knows B
                            net.get(i).nextNodes.get(net.get(i).nextNodes.size() - 1).addPreviousNode(net.get(i));
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

    private void calculateOptimalPath() {
        calcGP();
        calcFP();
        getPath();
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
        System.out.println(ORANGE + "Critical path:" + COLOR_RESET);
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

enum InputType {
    PREVIOUS_NODES, NEXT_NODES
}


