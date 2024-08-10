import java.io.*;
import java.util.*;

/**
 * PS5
 * Implements Hidden Markov Models to determine the most appropriate transitions for parts of speech
 * Dartmouth CS 10, Winter 2024.
 * Authors: Firdavskhon Babaev & Dhanush Balaji.
 */

public class PSE {
    public static String initial = "#"; //initial state
    public static Map<String, Map<String, Double>> transitions; // Holds all transitions and their probabilities
    public static Map<String, Map<String, Double>> observations;

    public PSE(){
    }

    /**
     * Processes training files with tags and sentences to map transitions from current to next states and tags to words
     * @param tagsFilePath Path to the training file containing tags
     * @param sentencesFilePath path to training file that has sentences
     * @throws IOException
     */
    public static void NFAtrain(String tagsFilePath, String sentencesFilePath) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(tagsFilePath));
            BufferedReader inSentences = new BufferedReader(new FileReader(sentencesFilePath));
            transitions = new HashMap<>();
            observations = new HashMap<>();
            String line;
            ArrayList<String[]> tagsList = new ArrayList<>();

            // Processes each line in the tags file
            while ((line = in.readLine()) != null) {
                String[] allTransitions = line.split(" ");
                tagsList.add(allTransitions);

                if (!transitions.containsKey(initial)) {
                    transitions.put(initial, new HashMap<>());
                    transitions.get(initial).put(allTransitions[0], 1.0); // Maps the first PSE tag of the file
                } else {
                    if (!transitions.get(initial).containsKey(allTransitions[0])) {
                        transitions.get(initial).put(allTransitions[0], 1.0);
                    } else {
                        transitions.get(initial).put(allTransitions[0], transitions.get(initial).get(allTransitions[0]) + 1); // updates transition count
                    }
                }
                for (int i = 0; i < allTransitions.length - 1; i++) {
                    if (!transitions.containsKey(allTransitions[i])) { // Add new PSE to the map if not already present
                        transitions.put(allTransitions[i], new HashMap<>());
                        if (!Objects.equals(allTransitions[i + 1], ".")) {
                            transitions.get(allTransitions[i]).put(allTransitions[i + 1], 1.0); // Set the frequency for the following PSE
                        }
                    } else {
                        if (!transitions.get(allTransitions[i]).containsKey(allTransitions[i + 1])) { // inserting new transition into the nested map
                            transitions.get(allTransitions[i]).put(allTransitions[i + 1], 1.0);
                        } else { // updates frequency for existing transitions
                            transitions.get(allTransitions[i]).put(allTransitions[i + 1], transitions.get(allTransitions[i]).get(allTransitions[i + 1]) + 1);
                        }
                    }
                }
            }
            in.close();
            String read;
            int index = 0;

            while ((read = inSentences.readLine()) != null) {
                String[] allObservations = read.toLowerCase().split(" "); // Splits sentence into words
                String[] tags = tagsList.get(index); // Retrieves corresponding tags for the sentence

                for (int i = 0; i < tags.length; i++) {
                    if (!observations.containsKey(tags[i])) { // Adds new tag to the map if not already there
                        observations.put(tags[i], new HashMap<>());
                        observations.get(tags[i]).put(allObservations[i], 1.0); // initializes the word frequency
                    } else {
                        if (!observations.get(tags[i]).containsKey(allObservations[i])) { // Add new word to the nested map
                            observations.get(tags[i]).put(allObservations[i], 1.0);
                        } else { // Updates frequency for existing words
                            observations.get(tags[i]).put(allObservations[i], observations.get(tags[i]).get(allObservations[i]) + 1);
                        }
                    }
                }
                index++;
            }
            inSentences.close(); // Closes the sentences file reader
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Calculates logarithms of probabilities for normalized data from either the transition or observation maps.
     * @param scores Either the observation map or the transition map, based on input from the main method
     */
    public static void probabilitiesInLogs(Map<String, Map<String, Double>> scores){
        for(String state : scores.keySet()){
            double normby = 0.0;
            for(String PSE : scores.get(state).keySet()){
                normby += scores.get(state).get(PSE); // Sums all frequencies in the inner map
            }
            for(String PSE : scores.get(state).keySet()){
                double log = Math.log((scores.get(state).get(PSE))/normby);
                scores.get(state).put(PSE, log); // Updates the map with the logarithmic value
            }
        }
    }

    /**
     * Implements the Viterbi algorithm
     * @param sentence The sentence to be tagged
     * @return An ArrayList containing tags for the input sentence
     */
    public static ArrayList<String> viterbiAlgorithm(String sentence) {
        Set<String> currStates = new HashSet<>(); //
        Map<String, Double> currScores = new HashMap<>();

        ArrayList<Map<String, String>> backTrack = new ArrayList<>();
        ArrayList<String> possiblePSEPath = new ArrayList<>(); // stores the states with the highest scores

        currStates.add(initial);
        currScores.put(initial, 0.0);
        double Score;
        double unseen = -100;

        String [] words = sentence.toLowerCase().split(" "); // Splits the input sentence into words
        // Iterates over each word in the sentence
        for(int i = 0; i < words.length;  i++) {
            Map<String, String> nextStates = new HashMap<>();
            Map<String, Double> nextScores = new HashMap<>();
            Map<String, String> backTrackMap = new HashMap<>();

            Double highestNextScore = null;
            String bestPSETransition = null;

            for (String currState : currStates) {
                for (String nextState : transitions.get(currState).keySet()) {
                    if (!nextState.equals(".")) {
                        nextStates.put(nextState, currState);
                    }
                    if (!observations.get(nextState).containsKey(words[i])) { // Applies a penalty for unseen words
                        Score = currScores.get(currState) + transitions.get(currState).get(nextState) + unseen;
                    }
                    else { // Calculates total score for seen words
                        Score = currScores.get(currState) + transitions.get(currState).get(nextState) + observations.get(nextState).get(words[i]);
                    }
                    if (!nextScores.containsKey(nextState) || Score > nextScores.get(nextState)) {
                        nextScores.put(nextState, Score);
                        backTrackMap.put(nextState, currState);

                        if(highestNextScore == null || Score > highestNextScore){
                            highestNextScore = Score;
                            bestPSETransition = nextState;
                        }
                    }
                }
            }
            currStates = nextStates.keySet();
            currScores = nextScores;
            backTrack.add(backTrackMap);
            possiblePSEPath.add(bestPSETransition);
        }

        ArrayList<String> PESPath = new ArrayList<String>();
        String PSEtags = possiblePSEPath.get(possiblePSEPath.size() - 1); // Retrieves the final state with the highest score
        PESPath.add(PSEtags);

        // backtracking to determine the sequence of states that leaad to the final state
        for (int i = 0; i < possiblePSEPath.size() - 1; i++) {
            PSEtags = backTrack.get(backTrack.size() - 1 - i).get(PSEtags);
            PESPath.add(0, PSEtags);
        }

        return PESPath;
    }

    /**
     * Reads test files and prints the most appropriate tags
     */
    public static void readFiles(String tagsFilePath, String sentenceFilePath) throws IOException {
        try {
            BufferedReader inTags = new BufferedReader(new FileReader(tagsFilePath));
            BufferedReader inSentence = new BufferedReader(new FileReader(sentenceFilePath));
            String lineTags;

            ArrayList<String[]> allTags = new ArrayList<>(); // collect all tags from the tags file
            while ((lineTags = inTags.readLine()) != null) {
                String[] tagLine = lineTags.split(" ");
                allTags.add(tagLine);
            }
            inTags.close();
            int match = 0; // Counter for correctly matched
            int notMatch = 0; // Counter for incorrectly matched ones

            int index = 0;
            String read;
            while ((read = inSentence.readLine()) != null) {
                ArrayList<String> possiblePSEtags = viterbiAlgorithm(read);
                String[] tagsFromFile = allTags.get(index);

                for (int i = 0; i < tagsFromFile.length; i++) {
                    if (possiblePSEtags.get(i).equals(tagsFromFile[i])) {
                        match++; // Increments counter for correct matches
                    } else {
                        notMatch++;
                    }
                }
                index++;
            }
            inSentence.close();
            System.out.println(match + " words are tagged correctly, but " + notMatch + " words are incorrectly matched using the Viterbi algorithm.");
            double total = match + notMatch;
            double percentage = (match / total) * 100;
            System.out.println("Accuracy: " + percentage + "%");
            System.out.println();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static void hardCoded() {
        transitions = new HashMap<>(); // Initializing maps
        observations = new HashMap<>();

        transitions.put("#", new HashMap<>());
        transitions.get("#").put("Noun", Math.log(2.0/7.0));
        transitions.get("#").put("Verb", Math.log(5.0/7.0));

        transitions.put("Noun", new HashMap<>());
        transitions.get("Noun").put("Adjective", Math.log(1.0));
        transitions.put("Verb", new HashMap<>());
        transitions.get("Verb").put("Conjunction", Math.log(2.0/8.0));
        transitions.get("Verb").put("Adjective", Math.log(6.0/8.0));

        transitions.put("Conjunction", new HashMap<>());
        transitions.get("Conjunction").put("Noun", Math.log(1.0/3.0));
        transitions.get("Conjunction").put("Verb", Math.log(1.0/3.0));
        transitions.get("Conjunction").put("Adjective", Math.log(1.0/3.0));

        transitions.put("Adjective", new HashMap<>());
        transitions.get("Adjective").put("Noun", Math.log(2.0/9.0));
        transitions.get("Adjective").put("Verb", Math.log(6.0/9.0));
        transitions.get("Adjective").put("Conjunction", Math.log(1.0/9.0));

        // populates the observation map with pre-determined probabilities
        observations.put("Noun", new HashMap<>());
        observations.get("Noun").put("book", Math.log(1.0));
        observations.put("Verb", new HashMap<>());
        observations.get("Verb").put("read", Math.log(5.0/12));
        observations.get("Verb").put("write", Math.log(5.0/12));
        observations.get("Verb").put("study", Math.log(2.0/12));
        observations.put("Conjunction", new HashMap<>());
        observations.get("Conjunction").put("and", Math.log(1.0));
        observations.put("Adjective", new HashMap<>());
        observations.get("Adjective").put("interesting", Math.log(6.0/9.0));
        observations.get("Adjective").put("difficult", Math.log(1.0/9.0));
        observations.get("Adjective").put("enjoyable", Math.log(2.0/9.0));

        // Demonstrate the algorithm with hard-coded examples
        System.out.println("Tags for 'read interesting book': " + viterbiAlgorithm("read interesting book."));
        System.out.println("Tags for 'write difficult and enjoyable book': " + viterbiAlgorithm("write difficult and enjoyable book"));
        System.out.println("Tags for 'study enjoyable difficult and interesting book': " + viterbiAlgorithm("study enjoyable difficult and interesting book"));
        System.out.println("Tags for 'enjoyable write and read interesting book': " + viterbiAlgorithm("enjoyable write and read interesting book"));
        System.out.println("Tags for 'study read and write difficult book': " + viterbiAlgorithm("study read and write difficult book"));
    }

    public static void main(String[] args) throws IOException{
        NFAtrain("PS5/simple-train-tags.txt","PS5/simple-train-sentences.txt" );
        probabilitiesInLogs(transitions);
        probabilitiesInLogs(observations);
        System.out.println("test: Simple");
        readFiles("PS5/simple-test-tags.txt", "PS5/simple-test-sentences.txt");

        NFAtrain("PS5/brown-train-tags.txt", "PS5/brown-train-sentences.txt");
        probabilitiesInLogs(transitions);
        probabilitiesInLogs(observations);
        System.out.println("test: Brown");
        readFiles("PS5/brown-test-tags.txt", "PS5/brown-test-sentences.txt");

        // Demonstrates the algorithm with hard-coded training data
        System.out.println("Hard coded examples: ");
        hardCoded();

        System.out.println();
        System.out.println("Console Test");
        NFAtrain("PS5/simple-train-tags.txt","PS5/simple-train-sentences.txt" );
        probabilitiesInLogs(transitions);
        probabilitiesInLogs(observations);
        System.out.println("Write a sentence or enter 'q' to quit: ");
        Scanner in = new Scanner(System.in);
        boolean mode = true;
        while (mode) {
            String sentence = in.nextLine();
            ArrayList<String> arr = viterbiAlgorithm(sentence); // applies the Viterbi algorithm to user input
            System.out.println(arr);
            if (sentence.equals("q")) mode = false;
        }

        System.out.println();
        System.out.println("Testing with Brown tags training");
        NFAtrain("PS5/brown-train-tags.txt", "PS5/brown-train-sentences.txt");
        probabilitiesInLogs(transitions);
        probabilitiesInLogs(observations);
        Scanner brown = new Scanner(System.in);
        mode = true;
        while(mode){
            String sent = brown.nextLine();
            ArrayList<String> arr = viterbiAlgorithm(sent);
            System.out.println(arr);
            if (sent.equals("q")) mode = false;
        }
    }
}
