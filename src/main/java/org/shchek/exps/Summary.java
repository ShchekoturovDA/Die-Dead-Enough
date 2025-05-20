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
    private String methodOrField;//Название метода
    private Double result; //Процент мёртвого кода
    private List<Node> deadNodes; //Участки мёртвого кода
    private int before; //Цикломатическая сложность до упрощения
    private int after; //Цикломатическая сложность после упрощения
    private List<Module> modules; //Используемые модули

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
        builder.append("Cyclomation before: " + before + ", after: " + after + "\n");
        builder.append("Using modules:\n");
        for(Module module : modules){
            builder.append(module.getName() + " : " + module.getUsage());
        }
        return builder.toString();
    }
}

