package org.shchek.exps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CFlow {
    Map<Integer, Variable> variables = new HashMap<>();
    Node start;
    List<Integer> begs = new ArrayList<Integer>();
    private Stack<Variable> jStack2 = new Stack<Variable>();
    List<Node> dead = new ArrayList<>();

    public void addBeg(int beg){
        begs.add(beg);
    }

    /**
     * Метод для добавления к графу новой вершины
     * @param orig вершина из которой проводится ребро
     * @param cond условие
     * @param exp аргумент
     * @param codeSector список инструкций, помещаемых в новую вершину
     * @param beg номер начальной строки
     * @return новая вершина
     */
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

    /**
     * Метод для добавления к графу новой вершины
     * @param orig вершина из которой проводится ребро
     * @param dest вершина в которую проводится ребро
     * @param cond условие
     * @param exp аргумент
     * @return новая вершина
     */
    public static Node add(Node orig, Node dest, Lex cond, String exp){
        Edge edge = new Edge(orig, dest, cond, exp);
        dest.addIn(edge);
        orig.addOut(edge);
        return dest;
    }

    /**
     * Метод для подразделения вершины в определённом месте
     * @param div разделяемая вершина
     * @param line номер строки, по которой проводится разделение
     * @return новая вершина
     */
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

    //Методы аналогичные методам в классе Node, вызываются для начальной вершины графа
    public Node searchLine(int num){
        return start.searchLine(num, new ArrayList<Integer>());
    }

    public String toString(){
        return start.toString();
    }

    public void printGraph(){
        start.print(new ArrayList<Integer>());
    }

    public void drop(){
        start.drop(new ArrayList<Integer>());
    }

    public Variable<Integer> isub(Variable<Integer> var1, Variable<Integer> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Integer.valueOf(v - var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Double> dsub(Variable<Double> var1, Variable<Double> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Double.valueOf(v - var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Integer> iadd(Variable<Integer> var1, Variable<Integer> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Integer.valueOf(v + var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Double> dadd(Variable<Double> var1, Variable<Double> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Double.valueOf(v + var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Integer> imul(Variable<Integer> var1, Variable<Integer> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Integer.valueOf(v * var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Double> dmul(Variable<Double> var1, Variable<Double> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Double.valueOf(v * var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Integer> idiv(Variable<Integer> var1, Variable<Integer> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Integer.valueOf(v / var2.getValues().iterator().next()));
        }
        return var1;
    }

    public Variable<Double> ddiv(Variable<Double> var1, Variable<Double> var2){
        if(var2.getValues().size() == 1) {
            var1.getValues().stream().forEach(v -> Double.valueOf(v / var2.getValues().iterator().next()));
        }
        return var1;
    }


    public void follow(Node node, Map<Integer, Variable> vars, Stack<Variable>jStack, List<Node> dead){
        for(CodeBlock cb : node.getCodeSector()){
            switch (cb.getLexem()){
                case DUP -> {
                    jStack.push(jStack.peek());
                }
                case NEW -> {
                }
                case POP -> {
                    jStack.pop();
                }
                case ISUB -> {
                    jStack.push(isub(jStack.pop(), jStack.pop()));
                }
                case DSUB -> {
                    jStack.push(dsub(jStack.pop(), jStack.pop()));
                }
                case IADD -> {
                    jStack.push(iadd(jStack.pop(), jStack.pop()));
                }
                case DADD -> {
                    jStack.push(dadd(jStack.pop(), jStack.pop()));
                }
                case IMUL -> {
                    jStack.push(imul(jStack.pop(), jStack.pop()));
                }
                case DMUL -> {
                    jStack.push(dmul(jStack.pop(), jStack.pop()));
                }
                case IDIV -> {
                    jStack.push(idiv(jStack.pop(), jStack.pop()));
                }
                case DDIV -> {
                    jStack.push(ddiv(jStack.pop(), jStack.pop()));
                }
                case DCONST -> {
                    Set<Double> cs = new HashSet<>();
                    cs.add(Double.valueOf(cb.getArg()));
                    Variable<Double> c = new Variable<>();
                    c.setValues(cs);
                    jStack.push(c);
                }
                case ACONST -> {
                    Set<Double> cs = new HashSet<>();
                    cs.add(null);
                    Variable<Double> c = new Variable<>();
                    c.setValues(cs);
                    jStack.push(c);
                }
                case ICONST, BIPUSH -> {
                    Set<Integer> cs = new HashSet<>();
                    cs.add(Integer.valueOf(cb.getArg()));
                    Variable<Integer> c = new Variable<>();
                    c.setValues(cs);
                    jStack.push(c);

                }
                case ASTORE -> {

                }
                case ISTORE -> {
                    int i = Integer.parseInt(cb.getArg());
                    Variable var = null;
                    if(vars.keySet().contains(i)) {
                        var = vars.get(i);
                    }else{
                        vars.put(i, new Variable(new HashSet<Integer>(), false, null));
                        var = vars.get(i);
                    }
                    var.setValues(jStack.pop().getValues());
                    vars.put(i, var);
                }
                case DSTORE -> {
                    int i = Integer.parseInt(cb.getArg());
                    Variable var = vars.get(i);
                    var.setValues(jStack.pop().getValues());
                    vars.remove(i);
                    vars.put(i, var);
                }
                case ALOAD -> {
                }
                case ILOAD -> {
                    jStack.push(vars.get(Integer.parseInt(cb.getArg())));
                }
                case DLOAD -> {
                    jStack.push(vars.get(Integer.parseInt(cb.getArg())));
                }
            }
        }
        Variable v1 = null;
        for(Edge e: node.getOut()){
            if(e.getCondition() != Lex.RET && e.getCondition() != Lex.GOTO && e.getCondition() != null){
                v1 = jStack.pop();
                break;
            }
        }

        for(int i = 0; i < node.getOut().size(); i++){
            if(node.getOut().get(i).getCondition() == Lex.RET){
                continue;
            }
            if(node.getOut().get(i).getExpression() != null && node.getOut().get(i).getCondition() != Lex.GOTO) {
                switch (node.getOut().get(i).getCondition()) {
                    case IFEQ -> {
                        if(!v1.getValues().contains(0)){
                            node.nulEdge(i, dead);
                        }
                    }
                    case IFGE -> {
                        if(v1.getValues().stream().filter(k -> Integer.valueOf(k.toString()) >= 0).toArray().length == 0){
                            node.nulEdge(i, dead);
                        }
                    }
                    case IFLE -> {
                        if(v1.getValues().stream().filter(k -> Integer.valueOf(k.toString()) <= 0).toArray().length == 0){
                            node.nulEdge(i, dead);
                        }
                    }
                    case IFLT -> {
                        if(v1.getValues().stream().filter(k -> Integer.valueOf(k.toString()) < 0).toArray().length == 0){
                            node.nulEdge(i, dead);
                        }
                    }
                    case IFGT -> {
                        if(v1.getValues().stream().filter(k -> Integer.valueOf(k.toString()) > 0).toArray().length == 0){
                            node.nulEdge(i, dead);
                        }
                    }
                }
            }
            if(node.getOut().get(i).getExpression() != null) {
                Map<Integer, Variable> prev = vars;
                Stack<Variable> pStack = jStack;
                follow(node.getOut().get(i).getDestination(), prev, pStack, dead);
            }
        }
    }

    public List<Node> checkVars(){
        jStack2.clear();
        follow(start, new HashMap<>(), new Stack<Variable>(), dead);
        drop();
        return dead;
    }
}
