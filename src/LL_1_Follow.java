import java.io.*;
import java.util.*;

// Futtatáskor az első argumentum a grammatikát tartalmazó file neve, a második argumentum az elemzendő szó.
//A grammatika file formátuma olyan, mint a mellékelt grammar.txt fileé: az első sor a kezdőszimbólum, a második a
// terminálisok, a harmadik a nemterminálisok halmaza
// Ezután egy-egy sorban a sor elején álló nemterminálisra vonatkozó szabály-jobboldalak szóközzel elválasztva

public class LL_1_Follow {
    public static class SzamozottJobboldal<String, Integer> {
        private String l;
        private Integer r;

        public SzamozottJobboldal(String l, Integer r) {
            this.l = l;
            this.r = r;
        }

        public String getL() {
            return l;
        }

        public Integer getR() {
            return r;
        }

        public void setL(String l) {
            this.l = l;
        }

        public void setR(Integer r) {
            this.r = r;
        }

        @Override
        public java.lang.String toString() {
            return "(" + l +", " + r +")";
        }
    }

    public static String word;
    public static String startingSymbol;
    public static String lambda = "&";
    public static ArrayList<String> terminals = new ArrayList<>();
    public static ArrayList<String> extendedTerminals = new ArrayList<>();
    public static ArrayList<String> nonTerminals = new ArrayList<>();
    public static TreeMap<String,ArrayList<String>> grammar = new TreeMap<>();
    public static TreeMap<String,ArrayList<SzamozottJobboldal>> szamozottGrammar = new TreeMap<>();
    public static TreeMap<String,TreeMap<String,SzamozottJobboldal>> llTablazat =new TreeMap<>();
    public static TreeMap<String,TreeMap<String,SzamozottJobboldal>> followTablazat =new TreeMap<>();


    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage: java LL1egyszeruTablazattal <File> <Word>.");
            System.exit(1);
        } else {
            readInput(args);
            parse();
        }
    }

    public static void readInput(String[] args){
        parseGrammar(args);
        System.out.println("Word: " + word);
        System.out.println("\nG = (" + terminals.toString().replace("[", "{").replace("]", "}")
                + ", " + nonTerminals.toString().replace("[", "{").replace("]", "}")
                + ", P, " + startingSymbol + ")\n\nWith Productions P as:");
        for(String bal: grammar.keySet()){
            System.out.println(bal + " -> " + grammar.get(bal));
        }
        for (String bal : szamozottGrammar.keySet()) {
            System.out.println(" -------- ");
            for (SzamozottJobboldal jobb : szamozottGrammar.get(bal)) {
                System.out.print(bal+" --> ");
                System.out.println(jobb.getL()+" "+jobb.getR());
            }
        }
        System.out.println("+--------+");

    }
    public static Scanner openFile(String file){
        try{
            return new Scanner(new File(file));
        }catch(FileNotFoundException e){
            System.out.println("Error: Can't find or open the file: " + file + ".");
            System.exit(1);
            return null;
        }
    }

    public static void parseGrammar(String[] args) {
        Scanner input = openFile(args[0]);
        ArrayList<String> tmp = new ArrayList<>();
        int line = 2;

        word = args[1];
        startingSymbol = input.next();
        input.nextLine();

        while (input.hasNextLine() && line <= 3) {
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            if (line == 2) {
                terminals.addAll(tmp);

                extendedTerminals.addAll(tmp);
                extendedTerminals.add("#");
            }
            if (line == 3) {
                nonTerminals.addAll(tmp);
            }
            tmp.clear();
            line++;
        }

        while (input.hasNextLine()) {
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            String leftSide = tmp.get(0);
            tmp.remove(0);
            grammar.put(leftSide, new ArrayList<String>());
            grammar.get(leftSide).addAll(tmp);
            tmp.clear();
        }
        input.close();

        int szabalyszam = 0;
        for (String baloldal : grammar.keySet()) {
            szamozottGrammar.put(baloldal, new ArrayList< SzamozottJobboldal>());
            for (String jobboldal : grammar.get(baloldal)) {
                SzamozottJobboldal<String,Integer> tmp2=new SzamozottJobboldal<>("",0);
                tmp2.setL(jobboldal);
                tmp2.setR(szabalyszam);
                szamozottGrammar.get(baloldal).add(tmp2);
                szabalyszam++;
            }
        }

        makeFirstTable();
        makeFollowTable();

        System.out.println("Táblák:\n");

        System.out.println(followTablazat);
        System.out.println(llTablazat);
        parseTables(llTablazat, followTablazat);
        System.out.println(llTablazat);
    }

    private static void parseTables(TreeMap<String,TreeMap<String,SzamozottJobboldal>> firstTable,
                                    TreeMap<String,TreeMap<String,SzamozottJobboldal>> followTable) {
        for (var NT : nonTerminals) {
            TreeMap<String, SzamozottJobboldal> nonTerm = new TreeMap<>();
            for (var term : extendedTerminals) {
                if (followTable.get(NT).keySet().contains(term) && !firstTable.get(NT).keySet().contains(term)) {
                    nonTerm.put(term, followTable.get(NT).get(term));
                } else if (firstTable.get(NT).keySet().contains(term)) {
                    nonTerm.put(term, firstTable.get(NT).get(term));
                }
            }
            firstTable.put(NT, nonTerm);
        }
    }
    private static void initializeTable(TreeMap<String,TreeMap<String,SzamozottJobboldal>> tabla) {
        for (var nonTerm : nonTerminals) {
            TreeMap<String, SzamozottJobboldal> nonTermFirst = new TreeMap<>();
            for (var term : terminals) {
                nonTermFirst.put(term, new SzamozottJobboldal("hiba", -1));
            }
            tabla.put(nonTerm, nonTermFirst);
        }
    }
    private static void fillFirst(String input /*Only one char, nonterm*/, TreeMap<String, TreeMap<String, SzamozottJobboldal>> table) {
        TreeMap<String, SzamozottJobboldal> nonterm_First = new TreeMap<>();
        for (var szabaly : szamozottGrammar.get(input)) {
            var firstChar = String.valueOf(szabaly.getL().toString().charAt(0));
            if (terminals.contains(firstChar)) {
                nonterm_First.put(firstChar, szabaly);
            } else if (nonTerminals.contains(firstChar)) {
                fillFirst(firstChar,table);
                var lista = table.get(firstChar);
                for (var first_szab : terminals) {
                    if (lista.get(first_szab)!=null){
                        nonterm_First.put(first_szab,szabaly);
                    }
                }
            }
        }
        table.put(input, nonterm_First);
    }
    private static void makeFirstTable() {
        initializeTable(llTablazat);

        for (var NT : nonTerminals) {
            fillFirst(NT, llTablazat);
        }
        /*
        //Ellenőrzések:
        for (var NT : nonTerminals) {
            System.out.println(String.format("Nem terminális: %s", NT));
            ArrayList<String> szab = new ArrayList<>();
            for (var szabaly : grammar.get(NT)) {
                szab.add(szabaly);
                System.out.printf("Grammar.get %s, %s\n",NT,szabaly);
            }

            System.out.println(szab);
        }*/
    }

    private static void makeFollowTable() {
        initializeTable(followTablazat);

        for (var NT : nonTerminals) {
            getFollow(NT);
        }
        //followTableCorrigation();
    }

    //Works as intended: visszaad egy listát, hogy mely terminálisok tartoznak a String firstjébe
    private static ArrayList<String> getFirst(String input) {
        ArrayList<String> output = new ArrayList<>();
        for (int i=0; i<input.length(); i++) {
            var jel = String.valueOf(input.charAt(i));
            if (terminals.contains(jel)) {
                output.add(jel);
                return output;
            } else if (nonTerminals.contains(jel)) {
                if (grammar.get(jel).contains(lambda)) {
                    output.addAll(llTablazat.get(jel).keySet());
                } else {
                    output.addAll(llTablazat.get(jel).keySet());
                    return output;
                }
            }
        }
        return output;
    }

    private static String getBeta(SzamozottJobboldal szabaly, String NonTerm, int Index) {
        String beta ="";
        if (getChar(szabaly, Index).equals(NonTerm)) {
            if (szabaly.getL().toString().length() > Index) {
                beta = szabaly.getL().toString().substring(Index+1);
            } else {
                beta = "";
            }
        }
        return beta;
    }
    private static void getFollow(String KeresettNT) {
        TreeMap<String, SzamozottJobboldal> nonterm_Follow = new TreeMap<>();
        if (KeresettNT.equals("S")) {
            SzamozottJobboldal szab = new SzamozottJobboldal<>("#", -2);
            nonterm_Follow.put("#", szab);

        }
        for (var SzabalyNT : nonTerminals) {
            var szabalyok = szamozottGrammar.get(SzabalyNT);
            for (var szabaly : szabalyok) { //Végig megy a szabályokon
                int szabalyhossz = szabaly.getL().toString().length();
                String beta = "";
                if (szabaly.getL().toString().contains(KeresettNT)) {
                    for (int idx = 0; idx<szabalyhossz; idx++) {
                        beta = getBeta(szabaly,KeresettNT, idx);
                        if (beta!="") {
                            if (!grammar.get(SzabalyNT).contains(lambda)) {
                                for (var b : followTablazat.get(SzabalyNT).keySet()) {
                                    String first = beta + b;
                                    var firstElems = getFirst(first);
                                    //System.out.println(String.format("SzabalyNT: %s, First: %s, beta: %s \n\n", SzabalyNT, firstElems, beta));
                                    for (var firstElem : firstElems) {
                                        SzamozottJobboldal szab2 = new SzamozottJobboldal("&", -2);
                                        nonterm_Follow.put(firstElem,szab2);
                                    }

                                }
                            }
                        }
                        else if (idx == szabalyhossz-1 && getChar(szabaly,idx).equals(KeresettNT)) { //A szó végén van- potenciális out of range
                            var lista = followTablazat.get(SzabalyNT);
                            if (!KeresettNT.equals(SzabalyNT)) {
                                for (var followElem : lista.keySet()) {
                                    nonterm_Follow.put(followElem,lista.get(followElem));
                                }
                            }
                        }

                    }
                }
            }
        }
        followTablazat.put(KeresettNT,nonterm_Follow);
    }


    private static void followTableCorrigation() {
        for (var NT : nonTerminals) {
            TreeMap<String, SzamozottJobboldal> Follow_entry = new TreeMap<>();
            for (var term : extendedTerminals) {
                var oldFollowEntry = followTablazat.get(NT).get(term);
                System.out.println(String.format("NT: %s, T: %s, Entry: %s", NT, term, oldFollowEntry));
                for (var szabaly : szamozottGrammar.get(NT)) {
                    if (szabaly.getL().equals(lambda)) {
                        Follow_entry.put(NT, szabaly);
                    }
                }/*
                if (oldFollowEntry != null) {
                    Follow_entry.put(NT, oldFollowEntry);
                }*/
            }
            followTablazat.put(NT,Follow_entry);
        }
    }
    private static String getChar(SzamozottJobboldal szabaly, int poz) {
        if (poz < szabaly.getL().toString().length())
            return String.valueOf(szabaly.getL().toString().charAt(poz));
        else {
            return "";
        }
    }

    public static String[] toArray(String input){
        return input.split("\\s");
    }

    public static void parse() {
        int hossz = word.length();
        int pozicio=0;
        String jel;
        String jobboldal;

        StringBuffer balTetejuVerem =new StringBuffer(startingSymbol+"#");
        String balTetejuFelso="";
        StringBuffer szabalySorozat=new StringBuffer("");

        Boolean hiba=false;
        Boolean kesz=false;

        while (!hiba && !kesz) {

            System.out.println(nonTerminals);
            System.out.println(terminals);
            balTetejuFelso=balTetejuVerem.substring(0,1);
            jel=String.valueOf(word.charAt(pozicio));

            System.out.print(word.substring(pozicio)+", ");
            System.out.print(balTetejuVerem+", ");
            System.out.println(szabalySorozat);
            System.out.println("------------------");

            // 3. sikeres elemzes
            if ((pozicio==word.length()-1) && (balTetejuFelso.equals("#"))){
                kesz=true;
                System.out.println("A "+word+" szo generalhato");
                continue;
            }


            // 1. kiterjesztes
            if (nonTerminals.contains(balTetejuFelso)){
                jobboldal=llTablazat.get(balTetejuFelso).get(jel).getL().toString();
                if (jobboldal=="hiba") {
                    hiba=true;
                    System.out.println("A "+word+" szo nem generalhato");
                } else if (jobboldal.equals(lambda)) {
                    balTetejuVerem.replace(0,1,"");
                    szabalySorozat.append(llTablazat.get(balTetejuFelso).get(jel).getR().toString()); //A szabály szám még nincs rendben
                }
                else {
                    balTetejuVerem.replace(0, 1, jobboldal);
                    szabalySorozat.append(llTablazat.get(balTetejuFelso).get(jel).getR().toString());
                }
                continue;
            }

            // 2. és 4. sikeres vagy sikertelen illesztes az inputhoz
            if (terminals.contains(balTetejuFelso)||balTetejuFelso.equals("#")) {
                // 2. sikeres illesztes
                if (jel.equals(balTetejuFelso)) {
                    balTetejuVerem.deleteCharAt(0);
                    pozicio = pozicio + 1;
                } else { // 4. sikertelen illesztes
                    hiba = true;
                    System.out.println("A "+word+" szo nem generalhato");
                }
                continue;
            }

        }

    }


}


