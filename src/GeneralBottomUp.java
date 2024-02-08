import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeMap;

// Futtatáskor az első argumentum a grammatikát tartalmazó file neve, a második argumentum az elemzendő szó,
// a végén # szóvégjellel.
//A grammatika file formátuma olyan, mint a mellékelt grammar.txt fileé: az első sor a kezdőszimbólum, a második a
// terminálisok, a harmadik a nemterminálisok halmaza
// Ezután egy-egy sorban a sor elején álló nemterminálisra vonatkozó szabály-jobboldalak szóközzel elválasztva
public class GeneralBottomUp {
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

    }

    public static String word;
    public static String startingSymbol;
    public static ArrayList<String> terminals = new ArrayList<String>();
    public static ArrayList<String> nonTerminals = new ArrayList<String>();
    public static TreeMap<String,ArrayList<String>> grammar = new TreeMap<>();
    public static TreeMap<String,ArrayList<SzamozottJobboldal>> szamozottGrammar = new TreeMap<>();


    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage: java BottomUpGeneral <File> <Word>.");
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
            szamozottGrammar.put(baloldal, new ArrayList<SzamozottJobboldal>());
            for (String jobboldal : grammar.get(baloldal)) {
                SzamozottJobboldal<String,Integer> tmp2=new SzamozottJobboldal<>("",0);
                tmp2.setL(jobboldal);
                tmp2.setR(szabalyszam);
                szamozottGrammar.get(baloldal).add(tmp2);
                szabalyszam++;
            }
        }
    }

    public static String[] toArray(String input){
        return input.split("\\s");
    }

    public static void parse() {
        int hossz = word.length();

        char allapot='q';
        int pozicio=0;
        String jel;
        String jobbTetejuFelso="";
        StringBuffer jobbTetejuVerem =new StringBuffer("");
        String balTetejuFelso="";
        StringBuffer balTetejuVerem =new StringBuffer("");

        int szabalyEleje;
        int szabalyHossza;
        boolean megvolt;

        while (allapot!='t') {
            megvolt=false;

            if (!balTetejuVerem.isEmpty()) {
                balTetejuFelso=balTetejuVerem.substring(0,1);
            }
            if (!jobbTetejuVerem.isEmpty()) {
                jobbTetejuFelso=jobbTetejuVerem.substring(jobbTetejuVerem.length()-1,jobbTetejuVerem.length());
            }

            jel=String.valueOf(word.charAt(pozicio));

            System.out.printf("%s, %d, %s, %s,\n", allapot, pozicio, jobbTetejuVerem,balTetejuVerem);
            System.out.println("------------------");

            // 3. sikeres elemzes
            if ((pozicio==word.length()-1) && (jobbTetejuVerem.toString().equals("S"))){
                allapot='t';
                System.out.printf("%s, %d, %s, %s,\n", allapot, pozicio, jobbTetejuVerem,balTetejuVerem);
                System.out.println("A "+word+" szo generalhato");
                break;
            }

            switch (allapot) {
                case 'q':
                    // 1. redukálás
                    for (String nt: nonTerminals) {
                        for (SzamozottJobboldal jobboldal: szamozottGrammar.get(nt)) {
                            String szabaly=jobboldal.getL().toString();
                            szabalyEleje=jobbTetejuVerem.lastIndexOf(szabaly);
                            szabalyHossza=szabaly.length();
                            if ((szabalyEleje!=-1) && (szabalyEleje==jobbTetejuVerem.length()-szabalyHossza)) {
                                jobbTetejuVerem.replace(szabalyEleje,jobbTetejuVerem.length(),nt);
                                balTetejuVerem.insert(0,jobboldal.getR().toString());

                                megvolt=true;
                                break;
                            }
                        }
                        if (megvolt){
                            break;
                        }
                    }
                    if (megvolt){
                        continue;
                    }

                    // 2. léptetés. Akkor ér ide a futás, ha nem tudott redukálni
                    if (pozicio<word.length()-1) {
                        pozicio=pozicio+1;
                        jobbTetejuVerem.append(jel);
                        balTetejuVerem.insert(0,"s");
                        //System.out.printf("poz: %s \n",pozicio);
                        continue;
                    }

                    // 4. átmenet backtrack állapotba, mert nem lehet redukálni és léptetni sem (hisz vége a szónak)
                    if (pozicio==word.length()-1 && !jobbTetejuVerem.toString().equals("S")){
                        allapot='b';
                        continue;
                    }
                    break;
                case 'b':
                    // 5. a visszalépés esetei
                    // I.-II.-III. aleset így kezdődik
                    if (nonTerminals.contains(jobbTetejuFelso)){
                        // visszatesszük az A helyére a gammát
                        Integer szabalyIndex=Integer.valueOf(balTetejuFelso);
                        balTetejuVerem.replace(0,1,"");
                        for (SzamozottJobboldal jobboldal: szamozottGrammar.get(jobbTetejuFelso)) {
                            if (jobboldal.getR().equals(szabalyIndex)) {
                                String gamma = jobboldal.getL().toString();
                                jobbTetejuVerem.replace(jobbTetejuVerem.length() - 1, jobbTetejuVerem.length(), gamma);
                                break;
                            }
                        }
                        // I. aleset: most megkeressük, hogy esetleg lehet-e más szabály szerint redukálni
                        // csak azt a találatot fogadjuk el, aminek a szabályindexe nagyobb az A-->gamma indexénél
                        String elozoszab = "";
                        for (var szamozottSzab : szamozottGrammar.get(jobbTetejuFelso)) {
                            if (szamozottSzab.getR().equals(Integer.valueOf(balTetejuFelso))) {
                                elozoszab=szamozottSzab.getL().toString();
                            }
                        }
                        for (var NT : nonTerminals) {
                            for (SzamozottJobboldal szamozottSzab: szamozottGrammar.get(NT)) {
                                var szabalySzam = Integer.valueOf(szamozottSzab.getR().toString());
                                if (szabalySzam < Integer.valueOf(balTetejuFelso) && elozoszab.equals(szamozottSzab.getL().toString())) {
                                    allapot = 'q';
                                    jobbTetejuVerem.replace(jobbTetejuVerem.lastIndexOf(szamozottSzab.getL().toString()),jobbTetejuVerem.length(),NT);
                                    balTetejuVerem.insert(0,szamozottSzab.getR().toString());
                                    megvolt=true;
                                }
                            }
                        }
                        if (megvolt){
                            continue;
                        }

                        // II. aleset, ha az előző pontban nem találtunk nagyobb indexű szabályt de még nincs vége a szónak
                        // ilyenkor léptetni kell
                        if (pozicio < hossz-1) {
                            allapot='q';
                            pozicio+=1;
                            jobbTetejuVerem.append(String.valueOf(word.charAt(pozicio-1)));
                            balTetejuVerem.insert(0,"s");
                            megvolt=true;
                        }
                        if (megvolt){
                            continue;
                        }
                        // III. aleset, nem tudunk léptetni sem, ekkor marad amit a közös bevezető részben csináltunk
                        continue;
                    }
                    // IV. aleset, a léptetést vissza lehet csinálni
                    if ((allapot=='b') && (terminals.contains(jobbTetejuFelso)) && (balTetejuFelso.equals("s"))
                            && (pozicio>0)){
                        pozicio=pozicio-1;
                        balTetejuVerem.replace(0,1,"");
                        jobbTetejuVerem.replace(jobbTetejuVerem.length() - 1, jobbTetejuVerem.length(), "");
                        continue;
                    }
                    break;
            }

            if (!megvolt){
                System.out.println(word+" nem generálható");
                allapot='t';
            }

        }

    }
}
