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
public class Summary {
    String methodOrField;//Название метода
    Double result; //Процент мёртвого кода
    List<Node> deadNodes; //Участки мёртвого кода

    public String toString(){
        StringBuilder builder =new StringBuilder();
        builder.append(methodOrField + " is dead for " + result + "%\n");
        int i = 0;
        for(Node node : deadNodes){
            i++;
            builder.append("Segment " + i + ":\n");
            for(CodeBlock cb : node.getCodeSector()){
                builder.append(cb.getNum() + ": " + cb.getLexem() + " ");
                if(cb.getArg() != null){
                    builder.append(cb.getArg());
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}

