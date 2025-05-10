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
public class CFlow {
    Variable[] variables;
    Node start;
    List<Integer> begs;

    public void addBeg(int beg){
        begs.add(beg);
    }

    public static Node add(Node orig, Lex cond, String exp, List<CodeBlock> codeSector, int beg){
        Node dest = new Node(new ArrayList<>(), new ArrayList<>(), codeSector, beg, beg + codeSector.size());
        if (codeSector.size() != 0){
            dest.setEnd(codeSector.getLast().getNum());
        }
        add(orig, dest, cond, exp);
//        Edge edge = new Edge(orig, dest, cond, exp);
//        dest.addIn(edge);
//        orig.addOut(edge);
        return dest;
    }

    public static Node add(Node orig, Node dest, Lex cond, String exp){
        Edge edge = new Edge(orig, dest, cond, exp);
        dest.addIn(edge);
        orig.addOut(edge);
        return dest;
    }

    public static Node subdivide(Node div, int line){
        int var = 0;
        for(CodeBlock cb : div.getCodeSector()){
            if(cb.getNum() >= line){
                break;
            }
            var++;
        }
        List<CodeBlock> past = div.getCodeSector().subList(var, div.getCodeSector().size());
        div.setCodeSector(div.getCodeSector().subList(0, var - 1));
        Node next = new Node(new ArrayList<>(), null, past, line, div.getEnd());
        div.setEnd(line - 1);
        List<Edge> eNext = new ArrayList<>();
        for (Edge e: div.getOut()){
            e.setOrigin(next);
            eNext.add(e);
        }
        next.setOut(eNext);
        div.setOut(new ArrayList<>());
        add(div, next, Lex.GOTO, null);
        return next;
    }

    public Node searchLine(int num){
        return start.searchLine(num);
    }

    public String toString(){
        return start.toString();
    }

    public void printGraph(){
        start.print(new ArrayList<Integer>());
    }
}
