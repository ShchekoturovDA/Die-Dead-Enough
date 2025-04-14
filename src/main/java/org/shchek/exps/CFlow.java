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

    public static Node add(Node orig, Lex cond, String exp, List<CodeBlock> codeSector){
        Node dest = new Node(new ArrayList<>(), new ArrayList<>(), codeSector, orig.getEnd() + 1, orig.getEnd() + 1 + codeSector.size());
        Edge edge = new Edge(orig, dest, cond, exp);
        edge.getDestination().addOut(edge);
        orig.addOut(edge);
        return dest;
    }

    public static void subdivide(Node div, int line){
        List<CodeBlock> past  = div.getCodeSector().subList(line, div.getCodeSector().size());
        div.setCodeSector(div.getCodeSector().subList(0, line - 1));
        Node next = new Node(null, div.getOut(), past, line, div.getEnd());
        div.setEnd(line - 1);
        Edge edge = new Edge(div, next, null, null);
        List<Edge> edges = new ArrayList<>();
        edges.add(edge);
        div.setOut(edges);
        next.setIn(edges);
    }
}
