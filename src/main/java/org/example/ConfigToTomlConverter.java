package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ConfigToTomlConverter {

    // Регулярные выражения для синтаксического анализа
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\".*$");
    private static final Pattern TABLE_START_PATTERN = Pattern.compile("^table\\($");
    private static final Pattern CONST_PATTERN = Pattern.compile("^(\\w+)\\s*->\\s*(\\w+)$");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\^\\[(.*)]");

    // Карта для хранения констант
    private final Map<String, Object> constants = new HashMap<>();

    public void parseFile(String inputFile, String outputFile) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        List<String> outputLines = new ArrayList<>();
        boolean insideTable = false;
        StringBuilder tableContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();

            // Пропускаем комментарии
            if (COMMENT_PATTERN.matcher(line).matches()) continue;

            // Обработка начала таблицы
            if (TABLE_START_PATTERN.matcher(line).matches()) {
                insideTable = true;
                tableContent.setLength(0);  // очищаем содержимое для новой таблицы
                continue;
            }

            // Обработка строк внутри таблицы
            if (insideTable) {
                if (line.equals(")")) {  // конец таблицы
                    insideTable = false;
                    outputLines.addAll(parseTable(tableContent.toString()));
                    continue;
                } else {
                    tableContent.append(line).append(" ");  // добавляем строки таблицы
                    continue;
                }
            }

            // Обрабатываем объявления констант
            Matcher constMatcher = CONST_PATTERN.matcher(line);
            if (constMatcher.find()) {
                String value = constMatcher.group(1);
                String name = constMatcher.group(2);
                constants.put(name, parseValue(value));
                continue;
            }

            // Обрабатываем выражения
            Matcher exprMatcher = EXPRESSION_PATTERN.matcher(line);
            if (exprMatcher.find()) {
                outputLines.add("calculated_value = " + parseExpression(exprMatcher.group(1)));
                continue;
            }

            throw new IllegalArgumentException("Syntax error in line: " + line);
        }

        // Записываем в выходной файл
        Files.write(Paths.get(outputFile), outputLines);
    }

    private List<String> parseTable(String tableContent) {
        List<String> tomlLines = new ArrayList<>();
        String[] entries = tableContent.split(",");
        for (String entry : entries) {
            String[] parts = entry.split("=>");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid table entry: " + entry);
            }
            String key = parts[0].trim();
            String value = parts[1].trim();
            tomlLines.add(key + " = " + parseValue(value));
        }
        return tomlLines;
    }

    private Object parseValue(String value) {
        value = value.trim();
        if (value.startsWith("'") && value.endsWith("'")) {
            return value;  // строка
        } else if (value.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(value);  // число
        } else if (constants.containsKey(value)) {
            return constants.get(value);  // константа
        } else {
            throw new IllegalArgumentException("Unknown value format: " + value);
        }
    }

    private String parseExpression2(String expression) {
        boolean isMin = expression.contains("min(");
        expression = expression.replace("min(","");
        expression=expression.replace("sqrt(","");
        expression = expression.replace(")","");
        expression = expression.replace(",","");
        expression = expression.trim();
        String[] tokens = expression.split("\\s+");
        Double[] values = new Double[tokens.length];
        int i = 0;
        for (String token : tokens) {
            token = token.trim();
            String val = String.valueOf(constants.getOrDefault(token,token));
            values[i] = Double.parseDouble(val);
            i++;
        }
        if(isMin){
            Double min = values[0];
            for (int j = 0; j < values.length; j++) {
                min = Math.min(min,values[j]);
            }
            return min.toString();
        }else{
            return String.valueOf(Math.sqrt(values[0]));
        }
    }

    private String parseExpression(String expression) {
        expression = expression.trim();
        if(expression.contains("min(")||expression.contains("sqrt(")) {
            return parseExpression2(expression);
        }
        String[] tokens = expression.split("\\s+");
        if (tokens.length != 3) throw new IllegalArgumentException("Invalid expression: " + expression);

        String op1 = tokens[0];
        String operator = tokens[1];
        String op2 = tokens[2];
        double val1 = Double.parseDouble(constants.getOrDefault(op1, op1).toString());
        double val2 = Double.parseDouble(constants.getOrDefault(op2, op2).toString());
        switch (operator) {
            case "+": return String.valueOf(val1 + val2);
            case "-": return String.valueOf(val1 - val2);
            default: throw new IllegalArgumentException("Unsupported operator in expression: " + operator);
        }
    }

    public static void runner(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ConfigToTomlConverter <input file> <output file>");
            return;
        }

        ConfigToTomlConverter converter = new ConfigToTomlConverter();
        try {
            converter.parseFile(args[0], args[1]);
            System.out.println("Conversion completed. Output saved to " + args[1]);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
