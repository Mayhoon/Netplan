package main;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public float faz, fez, saz, sez, gp, fp, processTime;
    public String name;
    public List<Node> previousNodes;
    public List<Node> nextNodes;

    public Node(String name, float processTime) {
        this.name = name;
        this.processTime = processTime;
        previousNodes = new ArrayList<>();
        nextNodes = new ArrayList<>();

        faz = 0f;
        fez = 0f;
        saz = 0f;
        sez = 0f;
        gp = 0f;
        fp = 0f;
    }

    public void addNextNode(Node node) {
        nextNodes.add(node);
    }

    public void addPreviousNode(Node node) {
        previousNodes.add(node);
    }

    public void setFAZ(float value) {
        if (faz <= value) {
            faz = value;
            calcFEZ();
        }
    }

    private void calcFEZ() {
        fez = faz + processTime;
        for (Node node : nextNodes) {
            node.setFAZ(fez);
        }
    }

    public void setSEZ(float value) {
        if (nextNodes.size() == 0) {
            sez = fez;
        } else if (sez == 0) {
            sez = value;
        } else if (sez > value) {
            sez = value;
        }
        calcSAZ();
    }

    private void calcSAZ() {
        saz = sez - processTime;
        for (Node node : previousNodes) {
            node.setSEZ(saz);
        }
    }

    public void calcGP() {
        gp = saz - faz;
    }

    public void calcFP() {
        for (Node node : nextNodes) {
            fp = node.faz - fez;
        }
    }

    public void getPath() {
        if (gp == 0) {
            System.out.print(" --> " + name);
            for (Node nextNode : nextNodes) {
                nextNode.getPath();
            }
        }
    }

    public void debug() {
        System.out.println("----------------------------------");
        System.out.println(faz + "\t\t" + name + "\t\t" + fez);
        System.out.println(processTime + "\t\t" + gp + "\t\t" + fp);
        System.out.println(saz + "\t\t\t\t" + sez);
        System.out.println("----------------------------------");
    }
}
