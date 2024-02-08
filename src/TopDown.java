import java.io.*;
import java.util.*;
/*
Futtatáskor az első argumentum a grammatikát tartalmazó file neve, a második argumentum az elemzendő szó.
A grammatika file formátuma olyan, mint a mellékelt grammar.txt fileé: az első sor a kezdőszimbólum, a második a
terminálisok, a harmadik a nemterminálisok halmaza
Ezután egy-egy sorban a sor elején álló nemterminálisra vonatkozó szabály-jobboldalak szóközzel elválasztva
*/

public class TopDown{
    public static String word;
    public static String startingSymbol;
    public static ArrayList<String> terminals = new ArrayList<String>();
    public static ArrayList<String> nonTerminals = new ArrayList<String>();
    public static TreeMap<String,ArrayList<String>> grammar = new TreeMap<>();

    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage: java TopDownGeneral <File> <Word>.");
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

        while(input.hasNextLine() && line <= 3){
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            if(line == 2) { terminals.addAll(tmp); }
            if(line == 3) { nonTerminals.addAll(tmp); }
            tmp.clear();
            line++;
        }

        while(input.hasNextLine()){
            tmp.addAll(Arrays.<String>asList(toArray(input.nextLine())));
            String leftSide = tmp.get(0);
            tmp.remove(0);
            grammar.put(leftSide, new ArrayList<String>());
            grammar.get(leftSide).addAll(tmp);
            tmp.clear();
        }
        input.close();
    }


    public static String[] toArray(String input){
        return input.split("\\s");
    }

    public static void parse() {
        char allapot='q';
        int pozicio=0;
        String jel;  // az elemzett szo aktuális poziciojan talalhato szimbolum
        Stack<Integer> indexverem =new Stack<>();
        String jobbTetejuFelso="";
        StringBuffer jobbTetejuVerem = new StringBuffer("");
        String balTetejuFelso="";
        StringBuffer balTetejuVerem = new StringBuffer(startingSymbol);

        int epoch=0;
        boolean error = false;
        //System.out.println(grammar);
        while (allapot!='t' && epoch <40 && !error) {
            epoch++;
            if (!balTetejuVerem.isEmpty()) {
                balTetejuFelso = balTetejuVerem.substring(0,1);
            }
            if (!jobbTetejuVerem.isEmpty()) {
                jobbTetejuFelso = jobbTetejuVerem.substring(jobbTetejuVerem.length()-1,jobbTetejuVerem.length());
            }

            jel=String.valueOf(word.charAt(pozicio));

            System.out.printf("%s, %d, %s, %s, %s, %s\n", allapot, pozicio, jobbTetejuVerem,balTetejuVerem,indexverem, pozicio==word.length()-1);
            System.out.println("------------------");


            switch (allapot) {
                case 'q':
                    // 1. kiterjesztes
                    if ((allapot=='q') && (nonTerminals.contains(balTetejuFelso))){
                        jobbTetejuVerem.append(balTetejuFelso);
                        balTetejuVerem.replace(0,1,grammar.get(balTetejuFelso).get(0));
                        indexverem.push(0);
                        break;
                    }

                    // 2. Illesztés
                    if (allapot=='q' && jel.equals(balTetejuFelso)) {
                        jobbTetejuVerem.append(balTetejuFelso);
                        balTetejuVerem.replace(0,1, "");
                        pozicio++;
                        break;
                    }

                    // 3. sikeres elemzes
                    if ((pozicio==word.length()-1) && (balTetejuVerem.isEmpty())){
                        allapot='t';
                        System.out.println("A "+word+" szo generalhato");
                        break;
                    }

                    // 4. Sikertelen illesztés
                    if (!jel.equals(balTetejuFelso) /*&& terminals.contains(balTetejuFelso)*/) {
                        allapot='b';
                        break;
                    }
                    break;

                case 'b':
                    System.out.println("Back Track");
                    // 5. BackTrack

                    if (terminals.contains(jobbTetejuFelso) || balTetejuFelso=="$") {
                        balTetejuVerem.insert(0, jobbTetejuFelso);
                        jobbTetejuVerem.deleteCharAt(jobbTetejuVerem.length()-1);
                        pozicio--;
                        break;
                    }
                    int alkalmazottSzabaly;

                    if (indexverem.isEmpty() && balTetejuFelso.equals(startingSymbol)) {
                        //II. Nem generálható
                        System.out.println("A "+word+" szo nem generalhato");
                        error=true;
                        allapot='b';
                        break;
                    } else {
                        alkalmazottSzabaly = indexverem.pop();
                    }

                    // 6. backtrack a kiterjesztésben
                    // III. Visszaalakítás
                    if (nonTerminals.contains(jobbTetejuFelso)) {
                        jobbTetejuVerem.deleteCharAt(jobbTetejuVerem.length()-1);
                        balTetejuVerem.replace(0, grammar.get(jobbTetejuFelso)
                                .get(alkalmazottSzabaly).toString().length(), jobbTetejuFelso);

                        balTetejuFelso=balTetejuVerem.substring(0,1);
                        if (jobbTetejuVerem.isEmpty()) {
                            jobbTetejuFelso="";
                        } else {
                            jobbTetejuFelso=jobbTetejuVerem.substring(jobbTetejuVerem.length()-1,jobbTetejuVerem.length());
                        }


                        if (!(grammar.get(balTetejuFelso).size()-1 > alkalmazottSzabaly)) {
                            allapot='b';
                            break;
                        }

                    }
                    //III. van alternatív szabály

                    if (grammar.get(balTetejuFelso).size()-1 > alkalmazottSzabaly) {
                        var nextSzabaly = alkalmazottSzabaly+1;
                        jobbTetejuVerem.append(balTetejuFelso);
                        balTetejuVerem.replace(0,1,grammar.get(balTetejuFelso).get(nextSzabaly));
                        indexverem.push(nextSzabaly);
                        allapot='q';
                        break;
                    }

                    break;
            }
        }

    }
}

