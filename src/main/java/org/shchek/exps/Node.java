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

    public Node searchLine(int num, List<Integer> begl){
        if(start <= num && end >= num){
            return this;
        } else {
            begl.add(start);
            Node ret = null;
            for (Edge e: out){
                if(!begl.contains(e.getDestination().getStart())) {
                    ret = e.getDestination().searchLine(num, begl);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            return ret;
        }
    }

    public String toString(){
        StringBuilder str = new StringBuilder("[" + start + " - " + end + "] -> ");
        if(!codeSector.isEmpty() && codeSector.getLast().getLexem() == Lex.RET){
            str.append("OUT");
        }
        for (Edge e: out){
            str.append(e.getCondition() + ": [" + e.getDestination().start + " - " + e.getDestination().end + "], ");
        }
        return str.toString();
    }

    public void print(List<Integer> check){
        System.out.println(this);
        check.add(start);
        for (Edge e: out){
            if(!check.contains(e.getDestination().getStart())) {
                e.getDestination().print(check);
            }
        }
    }

    public void drop(List<Integer> check){
        check.add(start);
        for (int i = 0; i < out.size(); i++){
            if(!check.contains(out.get(i).getDestination().getStart())) {
                out.get(i).getDestination().drop(check);
            }
            if(out.get(i).getCondition() == null){
                out.remove(i);
            }
        }
    }

}
