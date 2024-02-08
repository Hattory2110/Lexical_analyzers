import java.io.*;
import java.lang.reflect.AnnotatedType;
import java.util.*;

// Futtatáskor az első argumentum a grammatikát tartalmazó file neve, a második argumentum az elemzendő szó.
//A grammatika file formátuma olyan, mint a mellékelt grammar.txt fileé: az első sor a kezdőszimbólum, a második a
// terminálisok, a harmadik a nemterminálisok halmaza
// Ezután egy-egy sorban a sor elején álló nemterminálisra vonatkozó szabály-jobboldalak szóközzel elválasztva

public class LL_1_v2 {
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
    public static ArrayList<String> nonTerminals = new ArrayList<>();
    public static TreeMap<String,ArrayList<String>> grammar = new TreeMap<>();
    public static TreeMap<String,ArrayList<SzamozottJobboldal>> szamozottGrammar = new TreeMap<>();
    public static TreeMap<String,TreeMap<String,SzamozottJobboldal>> llTablazat =new TreeMap<>();
    public static TreeMap<String,TreeMap<String,SzamozottJobboldal>> llextendedTablazat =new TreeMap<>();


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
        /*
        makeFirstTable();
        System.out.println(llTablazat);
        System.out.println("\n");*/
        makeExtendedTable();
        System.out.println(llTablazat);
    }
    private static void initializeFirstTable(TreeMap<String,TreeMap<String,SzamozottJobboldal>> tabla) {
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
        initializeFirstTable(llTablazat);
        for (var NT : nonTerminals) {
            fillFirst(NT, llTablazat);
        }
        //Ellenőrzések:
        for (var NT : nonTerminals) {
            System.out.println(String.format("Nem terminális: %s", NT));
            ArrayList<String> szab = new ArrayList<>();
            for (var szabaly : grammar.get(NT)) {
                szab.add(szabaly);
                System.out.printf("Grammar.get %s, %s\n",NT,szabaly);
            }

            System.out.println(szab);
        }
    }


    private static void makeExtendedTable() {
        initializeFirstTable(llTablazat);
        for (var NT : nonTerminals) {
            fill_lambdaFirstTable(NT, llTablazat);
        }
    }

    private static String getChar(SzamozottJobboldal szabaly, int poz) {
        return String.valueOf(szabaly.getL().toString().charAt(poz));
    }
    private static void fill_lambdaFirstTable(String input, TreeMap<String, TreeMap<String, SzamozottJobboldal>> table) {
        TreeMap<String, SzamozottJobboldal> nonterm_First = new TreeMap<>();
        for (var szabaly : szamozottGrammar.get(input)) {
            var szab_hossz = szabaly.getL().toString().length();
            for (int pozicio=0;pozicio<szab_hossz; pozicio++) {
                var chatAtPoz = getChar(szabaly, pozicio);
                var szab = szabaly.getL().toString().substring(0,pozicio+1)+szabaly.getL().toString().substring(pozicio+1,szab_hossz);
                var reductedszabaly = new SzamozottJobboldal<>(szab,szabaly.getR());

                if (terminals.contains(chatAtPoz)) {
                    nonterm_First.put(chatAtPoz, reductedszabaly);
                    break;
                } else if (nonTerminals.contains(chatAtPoz)) {
                    fill_lambdaFirstTable(chatAtPoz, table);
                    var lista = table.get(chatAtPoz);
                    for (var first_szab : terminals) {
                        if (lista.get(first_szab) != null) {
                            nonterm_First.put(first_szab, reductedszabaly);
                        }
                    }
                    if (!grammar.get(chatAtPoz).contains(lambda)) {
                        break;
                    }
                }
            }
        }
        table.put(input, nonterm_First);
    }
    /*
    private static void calculateFirstFollow(String input) {
        //Ennek ki kell bővítenie a first táblát
        //A nem terminálisokra hívódik meg,
        //és végig megy a szabályaikon
        //Mivel alapvetően diszjunktnak kell lennie nem foglalkozik azzal hogy baj hogy felül ír már meglevő kapcsolatot
        //
        //Megkapja a nem terminálist, végig megy a szabályain, hogy megnézze hogy van-e valahol törlés
        //ha terminális az első karakter akkor vége
        //ha nem terminális, akkor megnézi annak a karakternek a szabályait,
        // ha nincs közöttük törlés, akkor csak simán lekéri a first-jét
        // ha van törlés a saját first-jét és a followot összeadja (diszjunktnek kell lenniük)

        //Tehát végig kell futni a tábla oszlopain, hogy ki lehessen egészíteni
        //majd le kell kérni az oszlop fején levő nem terminálisnak a szabályait
        //azok segítségével kell kibővíteni a táblát
        // loop (nem terminális) { loop (szabályok) { loop (szabály_karakter) { termin {visszatér}/nemtermin {visszatér} /törlés {visszatér, és tovább megy}}}}
        // follow számolás
        // First(S) = First(ABC) -> First(A) + Follow(A) = First(A) + First(BC) = First(A) + First(B) + Follow(B)
        // = First(A) + First(B) + First(C)
        // Nem terminális first-je a szabályai first-jének az együttese
        //Nemterminális -> Szabálya -> karakterek amég van törlés


    }

    private static ArrayList<String> getFirst(String cha) {
        ArrayList<String> firsts = new ArrayList<>();
        if (nonTerminals.contains(cha)) {
            //System.out.println(String.format("NT: %s", cha));
            //System.out.println(llTablazat.get(cha).keySet());
            firsts.addAll(llTablazat.get(cha).keySet());
        } else if (terminals.contains(cha)) {
            firsts.add(cha);
        }
        calcFollow("&");
        return firsts;
    }

    private static void calcFollow(String input) {
        for (var cha : input.toCharArray()) {
            var jel = String.valueOf(cha);
            if (grammar.get(jel).contains(lambda)) {
                System.out.println("Eljutott");
            }
        }
    }
    private static void calculateFollow(String input, ArrayList<String> row) { //Ez fog meghívódni minden szabályra, és kisámolja a follow-t (hozzáadja a listához a karakterek first-jeit)
        var firstChar = String.valueOf(input.charAt(0));
        if (terminals.contains(firstChar)) {
            row.add(firstChar);
        } else if (nonTerminals.contains(firstChar)) {
            calculateFollow(input.substring(1, input.length()-1), row);
        } else if (firstChar.equals(lambda)) {
            //Csak törlés van, akkor kihagja
            //calculateFollow(input.substring(1, input.length()-1), row);
        }
    }

    /* Számolás menete.
    * S -> ABC   Az S nek kell a first-je
    * Ami A nak a first je, kivéve, ha A szabályai között van törlő
    * Ha van törlő, akkor nézzük a follow-ot (BC)
    * Ami pedig a B first je, kivéve ha van a B szabályaiban töblés
    * és igy tovább amég vagy van törlés a terminálisokban, vagy véget nem ért az input
    */
    /*
    private static ArrayList<String> getFollow(String input) { //Várhatóan szabály jobboldalakat nézek meg, és azokon kell végig mennem
        /*
        Rekurzív használat:
        -Van egy String bementet
        -Ha az első karakter szabályaiban van &, akkor tovább ugrok a láncon és azt vizsgálom

        Tehát végig megyek a láncon amég & szabáyt találok a karakterben, és a kapott terminálisokat elmentem

        ArrayList<String> termins = new ArrayList<>();
        for (var letter : input.toCharArray()) {
            System.out.println(letter);
        }

        return termins;
    }
    private static void extendFirstTable() {
        /*
        Follow megnézi hogy egy szabály alkalmazásakor a karakter törölhető-e
        és ha igen akkor kell számolni a follow-ot
        ami az adja eredményül, ami a folytatódó karakterlánc first halmaza
        (azok a karakterek kellenek a láncból amik nem törlődnek)

        for (var NT : nonTerminals) {

        }
    }*/

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

                //System.out.printf("%s, %s, %s\n",jobboldal, balTetejuFelso, jel);
                var tomb=llTablazat.get(balTetejuFelso);
                if (tomb.containsKey(jel)) {
                    jobboldal=llTablazat.get(balTetejuFelso).get(jel).getL().toString();
                    if (jobboldal=="hiba") {
                        hiba=true;
                        System.out.println("A "+word+" szo nem generalhato");
                    }
                    else {
                        balTetejuVerem.replace(0, 1, jobboldal);
                        szabalySorozat.append(llTablazat.get(balTetejuFelso).get(jel).getR().toString());
                    }
                } else if (grammar.get(balTetejuFelso).contains(lambda)) {
                    balTetejuVerem.replace(0, 1, "");
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
                }
                continue;
            }

        }

    }


}


