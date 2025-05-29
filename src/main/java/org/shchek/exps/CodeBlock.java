package org.shchek.exps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodeBlock {
    private int num;
    private Lex lexem;
    private String arg;

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(getNum() + ": " + getLexem() + " ");
        if(getArg() != null){
            builder.append(getArg());
        }
        builder.append("\n");
        return builder.toString();
    }
}
