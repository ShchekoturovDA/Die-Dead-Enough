package org.shchek.exps;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
        if(edge.getOrigin() == this){
            out.add(edge);
        } else {
            System.out.println("WTF?");
        }
    }

    public void addIn(Edge edge){
        if(edge.getDestination() == this){
            in.add(edge);
        } else {
            System.out.println("WTF?");
        }
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
            str.append(e.getCondition() + ": [" + e.getDestination().start + " - " + e.getDestination().end + "], ");
        }
        return str.toString();
    }

    public void print(List<Integer> check){
        System.out.println(toString());
        check.add(this.start);
        for (Edge e: out){
            if(!check.contains(e.getDestination().start)) {
                e.getDestination().print(check);
            }
        }
    }

}
