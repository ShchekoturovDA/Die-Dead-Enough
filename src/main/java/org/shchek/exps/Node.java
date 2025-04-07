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

}
