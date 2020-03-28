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
    List<String> nodeNames;
    List<Float> processingTime;
    List<String> connectedNodes;

    Node startingNode;
    Scanner scanner;
    InputType inputType;

    public Netzplan() {
        scanner = new Scanner(System.in);
        net = new ArrayList<>();
        nodeNames = new ArrayList<>();
        processingTime = new ArrayList<>();
        connectedNodes = new ArrayList<>();

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
        printSeparator();
        getInputType();

        try {
            System.out.print("Aus einer Excel Datei (.xls)\nimportieren?(j/n)\t\t\t");
            String input = scanner.next();
            if (input.equals("j")) {
                System.out.print("Pfad zur Datei:\t\t\t\t");
                OfficeReader reader = new OfficeReader(nodeNames, processingTime, connectedNodes);
                reader.readFromFile(scanner.next());
                nodeNames = reader.getNodeNames();
                processingTime = reader.getProcessingTime();
                connectedNodes = reader.getConnectedNodes();
            } else {
                do {
                    printSeparator();
                    inputNodeName();
                    inputProcessingTime();
                    inputConnectedNodes();
                } while (askToRepeat());
            }
        } catch (InputMismatchException e) {
            e.printStackTrace();
        }
    }

    private void getInputType() {
        System.out.print("Sind alle (v)orherigen oder\n(n)achfolgenden Prozesse bekannt?\t");
        String input = scanner.next();
        if (input.equals("v")) {
            inputType = InputType.PREVIOUS_NODES;
        } else if (input.equals("n")) {
            inputType = InputType.NEXT_NODES;
        } else {
            System.out.println("Invalide Eingabe");
            System.exit(0);
        }
        printSeparator();
    }

    private void inputNodeName() {
        while (true) {
            System.out.print("Prozessname:\t\t");
            String input = scanner.next();
            if (!nodeNames.contains(input)) {
                nodeNames.add(input);
                break;
            } else {
                System.out.println("Prozessname existiert bereits. Bitte noch einmal neu versuchen:");
            }
        }
    }

    private void inputProcessingTime() {
        System.out.print("Prozessdauer:\t\t");
        processingTime.add(scanner.nextFloat());
    }

    private void inputConnectedNodes() {
        if (inputType == InputType.PREVIOUS_NODES) {
            System.out.print("Vorheriger Prozess:\t");
            connectedNodes.add(scanner.next());
        } else if (inputType == InputType.NEXT_NODES) {
            System.out.print("Naechster Prozess:\t");
            connectedNodes.add(scanner.next());
        }
    }

    private boolean askToRepeat() {
        System.out.print("Neuer Prozess? (j/n)\t");
        String input = scanner.next();
        if (input.equals("n")) {
            return false;
        } else if (input.equals("j")) {
            printSeparator();
            return true;
        } else {
            System.out.println("Invalide Eingabe");
            System.exit(0);
            return false;
        }
    }

    private void validate() {
        String[] nodeNames;
        for (int i = 0; i < connectedNodes.size(); i++) {
            if (connectedNodes.get(i).equals("NONE")) {
                nodeNames = connectedNodes.get(i).split(",");
                for (String nodeName : nodeNames) {
                    int occurrences = Collections.frequency(Arrays.asList(nodeNames), nodeName);
                    if (occurrences > 1) {
                        System.out.println(this.nodeNames.get(i) + " enthaelt " + occurrences + " Duplikate...");
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void createNet() {
        for (int i = 0; i < nodeNames.size(); i++) {
            String current = nodeNames.get(i);
            if (!current.equals(startingNode.name)) {
                // Check if the node is already initialized
                boolean found = false;
                for (int p = 0; p < net.size(); p++) {
                    if (net.get(p).name.equals(current)) {
                        found = true;
                    }
                    else if (p == net.size() - 1 && !found) {
                        Node node = new Node(current, processingTime.get(i));
                        net.add(node);
                    }
                }
            }
        }
    }

    private void getStartingNode() {
        printSeparator();
        for (int i = 0; i < nodeNames.size(); i++) {
            if (connectedNodes.get(i).equals("NONE") && inputType == InputType.PREVIOUS_NODES) {
                System.out.println("Erster Prozess: " + nodeNames.get(i));
                startingNode = new Node(nodeNames.get(i), processingTime.get(i));
                net.add(startingNode);
                break;
            } else if (!connectedNodes.get(i).equals("NONE") && inputType == InputType.NEXT_NODES) {
                System.out.println("Erster Prozess: " + nodeNames.get(i));
                startingNode = new Node(nodeNames.get(i), processingTime.get(i));
                net.add(startingNode);
                break;
            }
        }
    }

    private void fillNet() {
        printSeparator();
        System.out.println(GREEN + "Netzplan:" + COLOR_RESET);

        for (int i = 0; i < net.size(); i++) {
            String[] connectedNodes = this.connectedNodes.get(i).split(",");

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
        System.out.println(GREEN + "Kritischer Pfad:" + COLOR_RESET);
        for (Node node : net) {
            if (node.previousNodes.size() == 0) {
                System.out.print(node.name);
                for (Node nextNode : node.nextNodes) {
                    nextNode.getPath();
                }
            }
        }
    }

    private void printSeparator() {
        System.out.println("----------------------------------");
    }
}

enum InputType {
    PREVIOUS_NODES, NEXT_NODES
}


