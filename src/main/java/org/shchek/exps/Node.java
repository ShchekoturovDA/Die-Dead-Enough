package org.shchek.exps;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private List<Edge> in;
    private List<Edge> out;
    private List<CodeBlock> codeSector;
    private int start;
    private int end;

    public void addOut(Edge edge){
        out.add(edge);
    }

    public void addIn(Edge edge){
        in.add(edge);
    }

    public Node searchLine(int num){
        if(start <= num && end >= num){
            return this;
        } else {
            Node ret = null;
            for (Edge e: out){
                ret = e.getDestination().searchLine(num);
                if(ret != null){
                    return ret;
                }
            }
            return ret;
        }
    }

    public String toString(){
        StringBuilder str = new StringBuilder("[" + start + " - " + end + "] -> ");
        for (Edge e: out){
            str.append("[" + e.getDestination().start + " - " + e.getDestination().end + "], ");
        }
        return str.toString();
    }

    public void print(){
        System.out.println(toString());
        for (Edge e: out){
            e.getDestination().print();
        }
    }

}
