package org.shchek.exps;/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.util.BCELifier;

import javax.imageio.stream.FileImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Отображает команды из .class файла с помощью javap и BCEL.
 */
final class ClassDumper {

    private final FileImageInputStream file;
    private final String fileName;
    private int superclassNameIndex;
    private int major;
    private int minor; // Версия компилятора
    private int accessFlags; // Флаги доступа
    private int[] interfaces; // Интерфейсы
    private ConstantPool constantPool; // Константы
    private Constant[] constantItems; // Константы
    private Attribute[] attributes; // Атрибуты

    /**
     * Читает класс из полученного потока.
     *
     * @param file Поток
     * @param fileName Название файла
     */
    public ClassDumper(final FileImageInputStream file, final String fileName) {
        this.fileName = fileName;
        this.file = file;
    }

    private String constantToString(final int index) {
        final Constant c = constantItems[index];
        return constantPool.constantToString(c);
    }

    /**
     * Метод обрабатывает class-файл, получает все составляющие, указанные в полях у ClassDumper
     *
     * @throws IOException если есть ошибки ввода вывода.
     * @throws ClassFormatException
     */
    public void dump() throws IOException, ClassFormatException {
        try {
            // Проверка магического числа
            processID();
            // Версия компилятора
            processVersion();
            // Обработка набора констант
            processConstantPool();
            // Информация о классе
            processClassInfo();
            // Используемые интерфейсы
            processInterfaces();
            // Обработка полей
            processFields();
            // Обработка методов
            processMethods();
            // Обработка атрибутов
            processAttributes();
            System.out.println("End");
        } finally {
            // Закрытие файла
            try {
                if (file != null) {
                    file.close();
                }
            } catch (final IOException ioe) {
                // Закрывающее исключение
            }
        }
    }

    /**
     * Processes information about the attributes of the class.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processAttributes() throws IOException, ClassFormatException {
        final int attributesCount = file.readUnsignedShort();
        attributes = new Attribute[attributesCount];

        System.out.printf("%nAttributes(%d):%n", attributesCount);

        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = Attribute.readAttribute(file, constantPool);
            // indent all lines by two spaces
            final String[] lines = attributes[i].toString().split("\\r?\\n");
            for (final String line : lines) {
                System.out.println("  " + line);
            }
        }
    }

    /**
     * Processes information about the class and its super class.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processClassInfo() throws IOException, ClassFormatException {
        accessFlags = file.readUnsignedShort();
        /*
         * Interfaces are implicitly abstract, the flag should be set according to the JVM specification.
         */
        if ((accessFlags & Const.ACC_INTERFACE) != 0) {
            accessFlags |= Const.ACC_ABSTRACT;
        }
        if ((accessFlags & Const.ACC_ABSTRACT) != 0 && (accessFlags & Const.ACC_FINAL) != 0) {
            throw new ClassFormatException("Class " + fileName + " can't be both final and abstract");
        }

        System.out.printf("%nClass info:%n");
        System.out.println("  flags: " + BCELifier.printFlags(accessFlags, BCELifier.FLAGS.CLASS));
        int classNameIndex = file.readUnsignedShort();
        System.out.printf("  this_class: %d (", classNameIndex);
        System.out.println(constantToString(classNameIndex) + ")");

        superclassNameIndex = file.readUnsignedShort();
        System.out.printf("  super_class: %d (", superclassNameIndex);
        if (superclassNameIndex > 0) {
            System.out.printf("%s", constantToString(superclassNameIndex));
        }
        System.out.println(")");
    }

    /**
     * Processes constant pool entries.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processConstantPool() throws IOException, ClassFormatException {
        byte tag;
        final int constantPoolCount = file.readUnsignedShort();
        constantItems = new Constant[constantPoolCount];
        constantPool = new ConstantPool(constantItems);

        // constantPool[0] is unused by the compiler
        System.out.printf("%nConstant pool(%d):%n", constantPoolCount - 1);

        for (int i = 1; i < constantPoolCount; i++) {
            constantItems[i] = Constant.readConstant(file);
            // i'm sure there is a better way to do this
            if (i < 10) {
                System.out.printf("    #%1d = ", i);
            } else if (i < 100) {
                System.out.printf("   #%2d = ", i);
            } else {
                System.out.printf("  #%d = ", i);
            }
            System.out.println(constantItems[i]);

            // All eight byte constants take up two spots in the constant pool
            tag = constantItems[i].getTag();
            if (tag == Const.CONSTANT_Double || tag == Const.CONSTANT_Long) {
                i++;
            }
        }
    }

    /**
     * Constructs object from file stream.
     *
     * @param file Input stream
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processFieldOrMethod() throws IOException, ClassFormatException {
        final int accessFlags = file.readUnsignedShort();
        final int nameIndex = file.readUnsignedShort();
        System.out.printf("  nameIndex: %d (", nameIndex);
        System.out.println(constantToString(nameIndex) + ")");
        System.out.println("  accessFlags: " + BCELifier.printFlags(accessFlags, BCELifier.FLAGS.METHOD));
        final int descriptorIndex = file.readUnsignedShort();
        System.out.printf("  descriptorIndex: %d (", descriptorIndex);
        System.out.println(constantToString(descriptorIndex) + ")");

        final int attributesCount = file.readUnsignedShort();
        final Attribute[] attributes = new Attribute[attributesCount];
        System.out.println("  attribute count: " + attributesCount);

        for (int i = 0; i < attributesCount; i++) {
            // going to peek ahead a bit
            file.mark();
            final int attributeNameIndex = file.readUnsignedShort();
            final int attributeLength = file.readInt();
            // restore file location
            file.reset();
            // Usefull for debugging
            // System.out.printf(" attribute_name_index: %d (", attribute_name_index);
            // System.out.println(constantToString(attribute_name_index) + ")");
            // System.out.printf(" atribute offset in file: %x%n", + file.getStreamPosition());
            // System.out.println(" atribute_length: " + attribute_length);

            // A stronger verification test would be to read attribute_length bytes
            // into a buffer. Then pass that buffer to readAttribute and also
            // verify we're at EOF of the buffer on return.

            final long pos1 = file.getStreamPosition();
            attributes[i] = Attribute.readAttribute(file, constantPool);
            String[] codeLines = attributes[i].toString().split("\n");
            createGraph(codeLines);
            final long pos2 = file.getStreamPosition();
            if (pos2 - pos1 != attributeLength + 6) {
                System.out.printf("%nattributeLength: %d pos2-pos1-6: %d pos1: %x(%d) pos2: %x(%d)%n", attributeLength, pos2 - pos1 - 6, pos1, pos1, pos2,
                    pos2);
            }
            System.out.printf("  ");
            System.out.println(attributes[i]);
        }
    }

    public static void createGraph(String[] code){
        Node start = new Node();
        start.setCodeSector(new ArrayList<CodeBlock>());
        start.setOut(new ArrayList<Edge>());
        CFlow graph = new CFlow();
        graph.setStart(start);
        for(String cs : code){
            if(cs.isEmpty()){
                break;
            } else {
                List<String> lk = Arrays.stream(cs.split("\t| |_|:|#|%")).filter(s -> !s.isEmpty()).toList();
                System.out.println(lk);
                /*if (lk.size() < 3){
                    continue;
                }*/
                if(!lk.get(0).matches("\\d+")){
                    continue;
                }/* else if (Integer.valueOf(lk.get(0)) < start.getStart()) {
                    break;
                }*/

                if(graph.begs.contains(Integer.valueOf(lk.get(0)))  && start.getStart() != Integer.parseInt(lk.get(0))){
                    Node des = graph.searchLine(Integer.parseInt(lk.get(0)));
                    graph.printGraph();
                    start = CFlow.add(start, des, Lex.GOTO, "dum");
                }
                switch(lk.get(1)){
                    case("invokestatic"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.INVOKESTATIC, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("invokespecial"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.INVOKESPECIAL, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("invokeinterface"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.INVOKEINTERFACE, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("invokevirtual"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.INVOKEVIRTUAL, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("astore"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ASTORE, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("aload"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ALOAD, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("dstore"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.DSTORE, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("istore"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ISTORE, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("dload"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.DLOAD, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("iload"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ILOAD, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("aconst"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ACONST, null));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("iconst"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.ICONST, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("bipush"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.BIPUSH, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("dconst"):
                        start.getCodeSector().add(new CodeBlock(Integer.parseInt(lk.get(0)), Lex.DCONST, lk.get(2)));
                        start.setEnd(Integer.parseInt(lk.get(0)));
                        break;
                    case("isub"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.ISUB, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("dsub"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.DSUB, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("iadd"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.IADD, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("dadd"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.DADD, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("imul"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.IMUL, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("dmul"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.DMUL, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("idiv"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.IDIV, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("ddiv"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.DDIV, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("new"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.NEW, lk.get(2)));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("dup"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.DUP, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("pop"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.POP, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("ldc"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.LDC, lk.get(2)));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("ldc2"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.LDC2, lk.get(2)));
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        break;
                    case("goto"):
                        if (Integer.valueOf(lk.get(2)) == Integer.valueOf(lk.get(0)) + 3) break;
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        Node s1 = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(s1 != null){
                            if(s1.getStart() != Integer.valueOf(lk.get(2))){
                                s1 = CFlow.subdivide(s1, Integer.valueOf(lk.get(2)));
                            }
                            graph.addBeg(s1.getStart());
                            CFlow.add(start, s1, Lex.GOTO, lk.get(2));
                        } else {
                            CFlow.add(start, Lex.GOTO, lk.get(2), new ArrayList<CodeBlock>(), Integer.parseInt(lk.get(2)));
                            graph.addBeg(Integer.parseInt(lk.get(2)));
                        }
                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, null, null, new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }

                        break;
                    case("ifeq"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        Node sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IFEQ, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IFEQ, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IFNEQ, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("iflt"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IFLT, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IFLT, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IFGE, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("ifgt"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IFGT, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IFGT, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IFLE, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("ifge"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IFGE, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IFGE, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IFLT, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("ifle"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IFLE, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IFLE, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IFGT, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("if_icmple"):
                        start.setEnd(Integer.valueOf(lk.get(0)));
                        sear = graph.searchLine(Integer.valueOf(lk.get(2)));
                        if(sear != null){
                            if(sear.getStart() != Integer.valueOf(lk.get(2))){
                                sear = CFlow.subdivide(sear, Integer.valueOf(lk.get(2)));
                            }
                            CFlow.add(start, sear, Lex.IF_ICMPLE, lk.get(2));
                            graph.addBeg(sear.getStart());
                        } else {
                            CFlow.add(start, Lex.IF_ICMPLE, lk.get(2), new ArrayList<CodeBlock>(), Integer.valueOf(lk.get(2)));
                            graph.addBeg(Integer.valueOf(lk.get(2)));
                        }

                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, Lex.IF_ICMPGT, String.valueOf(start.getEnd() + 3), new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }
                        break;
                    case("return"):

                        start.setEnd(Integer.valueOf(lk.get(0)));
                        CFlow.add(start, new Node(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0, 0), Lex.RET, null);
                        s1 = graph.searchLine(start.getEnd() + 3);
                        if(s1 != null){
                            start = s1;
                        } else {
                            start = CFlow.add(start, null, null, new ArrayList<CodeBlock>(), start.getEnd() + 3);
                            graph.addBeg(start.getStart());
                        }

                        break;

                    case("dreturn"):
                        start.getCodeSector().add(new CodeBlock(Integer.valueOf(lk.get(0)), Lex.RET, null));
                        start.setEnd(Integer.valueOf(lk.get(0)));

                }


            }
        }
        graph.printGraph();
        graph.drop();
        graph.printGraph();
//        graph.checkVars();
        graph.printGraph();
    }

    /**
     * Processes information about the fields of the class, i.e., its variables.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processFields() throws IOException, ClassFormatException {
        final int fieldsCount = file.readUnsignedShort();
        // fields = new Field[fieldsCount];

        // sometimes fields[0] is magic used for serialization
        System.out.printf("%nFields(%d):%n", fieldsCount);

        for (int i = 0; i < fieldsCount; i++) {
            processFieldOrMethod();
            if (i < fieldsCount - 1) {
                System.out.println();
            }
        }
    }

    /**
     * Checks whether the header of the file is ok. Of course, this has to be the first action on successive file reads.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processID() throws IOException, ClassFormatException {
        final int magic = file.readInt();
        if (magic != Const.JVM_CLASSFILE_MAGIC) {
            throw new ClassFormatException(fileName + " is not a Java .class file");
        }
        System.out.println("Java Class Dump");
        System.out.println("  file: " + fileName);
        System.out.printf("%nClass header:%n");
        System.out.printf("  magic: %X%n", magic);
    }

    /**
     * Processes information about the interfaces implemented by this class.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processInterfaces() throws IOException, ClassFormatException {
        final int interfacesCount = file.readUnsignedShort();
        interfaces = new int[interfacesCount];

        System.out.printf("%nInterfaces(%d):%n", interfacesCount);

        for (int i = 0; i < interfacesCount; i++) {
            interfaces[i] = file.readUnsignedShort();
            // i'm sure there is a better way to do this
            if (i < 10) {
                System.out.printf("   #%1d = ", i);
            } else if (i < 100) {
                System.out.printf("  #%2d = ", i);
            } else {
                System.out.printf(" #%d = ", i);
            }
            System.out.println(interfaces[i] + " (" + constantPool.getConstantString(interfaces[i], Const.CONSTANT_Class) + ")");
        }
    }

    /**
     * Processes information about the methods of the class.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processMethods() throws IOException, ClassFormatException {
        final int methodsCount = file.readUnsignedShort();
        // methods = new Method[methodsCount];

        System.out.printf("%nMethods(%d):%n", methodsCount);

        for (int i = 0; i < methodsCount; i++) {
            processFieldOrMethod();
            if (i < methodsCount - 1) {
                System.out.println();
            }
        }
    }

    /**
     * Processes major and minor version of compiler which created the file.
     *
     * @throws IOException if an I/O error occurs.
     * @throws ClassFormatException
     */
    private void processVersion() throws IOException, ClassFormatException {
        minor = file.readUnsignedShort();
        System.out.printf("  minor version: %s%n", minor);

        major = file.readUnsignedShort();
        System.out.printf("  major version: %s%n", major);
    }

}

final class DumpClass {

    public static void main(final String[] args) throws IOException {

        if (args.length != 1) {
            throw new IllegalArgumentException("Require file name as only argument");
        }

        try (FileImageInputStream file = new FileImageInputStream(new File(args[0]))) {

            final ClassDumper cd = new ClassDumper(file, args[0]);
            cd.dump();
        }

        System.out.printf("End of Class Dump%n");

    }
}
