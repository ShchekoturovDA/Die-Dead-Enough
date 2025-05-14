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

    /**
     * Метод для поиска вершины содержащую строку с номером
     * @param num номер
     * @param begl массив номеров первых строк обработанных вершин
     */
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

        for (Edge e: out){
            if(e.getCondition() == Lex.RET){
                str.append(" OUT ");
            } else {
                str.append(e.getCondition() + ": [" + e.getDestination().start + " - " + e.getDestination().end + "], ");
            }
        }
        return str.toString();
    }

    /**
     * Метод для рекурсивного вывода всего графа по каждой его вершине
     * @param begl массив номеров первых строк обработанных вершин
     */
    public void print(List<Integer> begl){
        System.out.println(this);
        begl.add(start);
        for (Edge e: out){
            if(!(e.getCondition() == Lex.RET) && !begl.contains(e.getDestination().getStart())) {
                e.getDestination().print(begl);
            }
        }
    }

    /**
     * Метод для удаления рёбер, по которым переход заведомо невозможен
     * @param begl массив номеров первых строк обработанных вершин
     */
    public void drop(List<Integer> begl){
        begl.add(start);
        for (int i = 0; i < out.size(); i++){
            if(!begl.contains(out.get(i).getDestination().getStart())) {
                out.get(i).getDestination().drop(begl);
            }
            if(out.get(i).getCondition() == null){
                out.remove(i);
            }
        }
    }

    public void nulEdge(int i, List<Node> dead) {
        Edge e = out.get(i);
        e.setCondition(null);
        if(e.getDestination().getIn().stream().filter(es -> es.getCondition() != null).toList().isEmpty()){
            dead.add(e.getDestination());
        };
        out.remove(i);
        out.add(i, e);
    }
}
