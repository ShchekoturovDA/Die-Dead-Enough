package org.shchek.exps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Edge {
    private Node origin;
    private Node destination;
    private Lex condition;
    private String expression;
}