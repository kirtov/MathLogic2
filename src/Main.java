import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static String parstr;
    private static String help;
    private static ArrayList<Expression> axioms; //аксиомы
    private static ArrayList<Expression> allExpressions; //начальный вывод(в выражениях)
    private static ArrayList<String> strExpressions; //начальный вывод(в строках)
    private static ArrayList<Expression> allAdmissions; //начальные допущения(в выражениях)
    private static ArrayList<String> strAdmissions; //начальные допущения(в строках)
    private static ArrayList<Integer> modusPonens;
    private static Expression alpha;
    private static String start, strbeta, stralpha;
    private static Map<String, Expression> map = new HashMap<String, Expression>();

    public static void main(String[] args) throws IOException {
        axioms = new ArrayList<Expression>();
        allExpressions = new ArrayList<Expression>();
        allAdmissions = new ArrayList<Expression>();
        strExpressions = new ArrayList<String>();
        strAdmissions = new ArrayList<String>();
        modusPonens = new ArrayList<Integer>();
        makeAxioms();
        Scanner sc = new Scanner(new FileReader("input.txt"));
        String now = sc.nextLine();
        Expression exp;
        makeAdmissions(now);
        boolean flag;
        while (sc.hasNext()) {
            modusPonens.add(-1);
            help = "";
            now = sc.nextLine();
            parstr = now;
            strExpressions.add(parstr);
            parstr = parstr.replaceAll("->", ">");
            exp = parse(0, parstr.length() - 1);
            allExpressions.add(exp);
            flag = axiomSatisfy(exp);
            if (!flag) {
                flag = checkMP(exp);
            }
            if (!flag) {
                flag = checkAdmissions(exp);
            }
            if (!flag) {
                System.out.println("Ошибка в " + allExpressions.size());
                return;
            }
            //System.out.println(parstr + "   " + help);
        }
        System.out.println(start);
        allAdmissions.remove(allAdmissions.size() - 1);
        strAdmissions.remove(strAdmissions.size() - 1);
        for (int i = 0; i < allExpressions.size(); i++) {
            help = "";
            exp = allExpressions.get(i);
            now = strExpressions.get(i);
            if (checkAdmissions(exp)) {
                System.out.println(now);
                System.out.println("(" + now + ")->((" + stralpha + ")->(" + now + "))");
                System.out.println("(" + stralpha + ")->(" + now + ")");
                //System.out.println("   " + help);
                continue;
            }
            if (axiomSatisfy(exp)) {
                System.out.println(now);
                System.out.println("(" + now + ")->(("+ stralpha + ")->(" + now + "))");
                System.out.println("(" + stralpha + ")->(" + now + ")");
                //System.out.println("   " + help);
                continue;
            }
            if (equalsTree(exp, alpha)) {
                printLemm(now);
                help = "Лемма";
                //System.out.println("   " + help);
                continue;
            }
            if (modusPonens.get(i) != -1) {
                printMP(strExpressions.get(i), strExpressions.get(modusPonens.get(i)));
                help = "MP " + Integer.toString(i + 1)   + " " + Integer.toString(modusPonens.get(i) + 1);
                //System.out.println("   " + help);
                continue;
            }
        }
        //System.out.println(stralpha + "->" + strbeta);


    }

    private static boolean checkMP(Expression exp) {
        Expression temp, left;
        for (int i = 0; i < allExpressions.size(); i++) {
            temp = allExpressions.get(i);
            if (temp.op != '>') continue;

            if (equalsTree(temp.right, exp)) {
                left = temp.left;

                for (int j = 0; j < allExpressions.size(); j++) {
                    if (equalsTree(left, allExpressions.get(j))) {
                        help = "MP " + Integer.toString(i + 1) + " " + Integer.toString(j + 1);
                        modusPonens.set(modusPonens.size() - 1, j);
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private static boolean checkAdmissions(Expression exp) {
        for (int i = 0; i < allAdmissions.size(); i++) {
            if (equalsTree(exp, allAdmissions.get(i))) {
                help = "Допущение " + Integer.toString(i + 1);
                return true;
            }
        }
        return false;
    }

    private static void printLemm(String strExp) {
        String temp = "((" + strExp + ")->(" + strExp + "))";
        System.out.println("(" + strExp + ")->" + temp);
        System.out.println("((" + strExp + ")->" + temp + ")->((" + strExp + ")->" + temp + "->(" + strExp + "))->" + temp);
        System.out.println("((" + strExp + ")->(" + temp + "->(" + strExp + ")))->" + temp);
        System.out.println("(" + strExp + ")->" + "(" + temp + "->(" + strExp + "))");
        System.out.println("(" + strExp + ")->(" + strExp + ")");
    }

    private static void printMP(String beta1, String beta2) {
        String temp = "((" + stralpha + ")->(" + beta1 + "))";
        String temp1 = "((" + beta2 + ")->(" + beta1 + "))";
        System.out.println("((" + stralpha + ")->(" + beta2 + "))->(((" + stralpha + ")->" + temp1 + ")->" + temp + ")");
        System.out.println("(((" + stralpha + ")->" + temp1 + ")->" + temp + ")");
        System.out.println("(" + stralpha + ")->(" + beta1 + ")");
    }

    private static void makeAdmissions(String first) {
        String allAdm = first.substring(0, first.indexOf('|'));
        parstr = first.substring(first.indexOf('|') + 2, first.length());
        strbeta = parstr;
        int last = 0;
        for (int i = 0; i < allAdm.length(); i++) {
            if (allAdm.charAt(i) == ',') {
                parstr = allAdm.substring(last, i);
                strAdmissions.add(parstr);
                parstr = parstr.replaceAll("->", ">");
                allAdmissions.add(parse(0, parstr.length() - 1));
                last = i + 1;
            }
            if (i == allAdm.length() - 1) {
                parstr = allAdm.substring(last, i + 1);
                strAdmissions.add(parstr);
                stralpha = parstr;
                parstr = parstr.replaceAll("->", ">");
                allAdmissions.add(parse(0, parstr.length() - 1));
                alpha = allAdmissions.get(allAdmissions.size() - 1);
                if (last == 0) {
                    start = "|-(" + stralpha + ")->(" + strbeta + ")";
                } else start = first.substring(0, last - 1) + "|-(" + stralpha + ")->(" + strbeta + ")";
            }
        }

    }

    private static boolean axiomSatisfy(Expression exp) {
        for (int i = 0; i < axioms.size(); i++) {
            map.clear();
            if (equals(exp, axioms.get(i))) {
                help = "Аксиома " + Integer.toString(i + 1);
                return true;
            }
        }
        return false;
    }

    private static boolean equals(Expression my, Expression axiom) {
        if (my == null && axiom == null) return true;
        if (my == null || axiom == null) return false;
        if (my.op == axiom.op && my.op != 's') return (equals(my.left, axiom.left) && equals(my.right, axiom.right));
        if (axiom.op == 's') {
            if (!map.containsKey(axiom.literal)){
                map.put(axiom.literal, my);
                return true;
            }
            if (equalsTree(my, map.get(axiom.literal))) return true;
            else return false;
        }
        return false;
    }

    private static boolean equalsTree(Expression one, Expression two) {
        if (one == null && two == null) return true;
        if (one == null || two == null) return false;
        return (one.op == two.op && one.literal.equals(two.literal) && equalsTree(one.right, two.right) && equalsTree(one.left, two.left));
    }

    private static void makeAxioms() {
        parstr = "a>(b>a)";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "(c>b)>(c>b>d)>(c>d)";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "c>b>(c&b)";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "c&a>c";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "c&a>a";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "c>c|b";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "b>c|b";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "(c>d)>(b>d)>(c|b>d)";
        axioms.add(parse(0, parstr.length() - 1));
        parstr = "((c)>(b))>((c)>!(b))>!(c)";
        axioms.add(parse(0, parstr.length() - 1));
    }

    private static Expression parse(int l, int r) {
        int balance = 0;
        if (l > r) return null;
        for (int i = l; i <= r; i++) {
            if (parstr.charAt(i) == '(') {
                balance++;
                continue;
            }
            if (parstr.charAt(i) == ')') {
                balance--;
                continue;
            }
            if (balance == 0 && parstr.charAt(i) == '>') {
                return new Expression(parse(l, i - 1), parse(i + 1, r), '>');
            }
        }
        balance = 0;
        for (int i = l; i <= r; i++) {
            if (parstr.charAt(i) == '(') {
                balance++;
                continue;
            }
            if (parstr.charAt(i) == ')') {
                balance--;
                continue;
            }
            if (balance == 0 && parstr.charAt(i) == '|') {
                return new Expression(parse(l, i - 1), parse(i + 1, r), '|');
            }
        }
        balance = 0;
        for (int i = l; i <= r; i++) {
            if (parstr.charAt(i) == '(') {
                balance++;
                continue;
            }
            if (parstr.charAt(i) == ')') {
                balance--;
                continue;
            }
            if (balance == 0 && parstr.charAt(i) == '&') {
                return new Expression(parse(l, i - 1), parse(i + 1, r), '&');
            }
        }
        balance = 0;
        for (int i = l; i <= r; i++) {
            if (parstr.charAt(i) == '(') {
                balance++;
                continue;
            }
            if (parstr.charAt(i) == ')') {
                balance--;
                continue;
            }
            if (balance == 0 && parstr.charAt(i) == '!') {
                return new Expression(null, parse(i + 1, r), '!');
            }
        }
        if (parstr.charAt(l) != '(') {
            return new Expression(parstr.substring(l,r + 1));
        }
        return parse(l + 1, r - 1);
    }

    static class Expression {
        Expression left, right;
        String literal;
        char op;
        public Expression(){}
        public Expression(String literal) {
            this.literal = literal;
            this.op = 's'; //symbol
            this.left = null;
            this.right = null;
        }
        public Expression(Expression left, Expression right, char operation) {
            this.left = left;
            this.right = right;
            this.op = operation;
            this.literal = "null";
        }
    }
}
