package edu.rit.cs.project1;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCount_Seq_Improved {

    // This function converts the byte array data recieved from the client back to a csv file.
    public static String get_CSV(byte[] data){
        try {

            File file = new File("reviews.csv");

            // Initialize a pointer
            // in file using OutputStream
            OutputStream
                    os
                    = new FileOutputStream(file);

            // Starts writing the bytes in it
            os.write(data);

            // Close the file
            os.close();
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        return "reviews.csv";

    }

    /**
     * Read and parse all reviews
     * @param dataset_file
     * @return list of reviews
     */
    public static List<AmazonFineFoodReview> read_reviews(String dataset_file) {
        List<AmazonFineFoodReview> allReviews = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dataset_file))){
            String reviewLine = null;
            // read the header line
            reviewLine = br.readLine();

            //read the subsequent lines
            while ((reviewLine = br.readLine()) != null) {
                allReviews.add(new AmazonFineFoodReview(reviewLine));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return allReviews;
    }

    /**
     * Writes the list of words and their counts to file
     * @param wordcount
     */
    public static File write_word_count( Map<String, Integer> wordcount){

        try {

            File file = new File("output.txt");
            // Initialize a pointer
            // in file using OutputStream
            OutputStream os = new FileOutputStream(file);

            for(String word : wordcount.keySet()) {
                String str = word + " : " + wordcount.get(word) + "\n"; //write word: wordcount newline
                os.write(str.getBytes());
            }

            // Close the file
            os.close();
            return file;
        }

        catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        return null;
    }

    /**
     * Emit 1 for every word and store this as a <key, value> pair
     * @param allReviews
     * @return
     */
    public static List<KV<String, Integer>> map(List<AmazonFineFoodReview> allReviews) {
        List<KV<String, Integer>> kv_pairs = new ArrayList<KV<String, Integer>>();

        for(AmazonFineFoodReview review : allReviews) {
            Pattern pattern = Pattern.compile("([a-zA-Z]+)");
            Matcher matcher = pattern.matcher(review.get_Summary());

            while(matcher.find())
                kv_pairs.add(new KV(matcher.group().toLowerCase(), 1));
        }
        return kv_pairs;
    }


    /**
     * count the frequency of each unique word
     * @param kv_pairs
     * @return a list of words with their count
     */
    public static Map<String, Integer> reduce(List<KV<String, Integer>> kv_pairs) {
        Map<String, Integer> results = new HashMap<>();

        for(KV<String, Integer> kv : kv_pairs) {
            if(!results.containsKey(kv.getKey())) {
                results.put(kv.getKey(), kv.getValue());
            } else{
                int init_value = results.get(kv.getKey());
                results.replace(kv.getKey(), init_value, init_value+kv.getValue());
            }
        }
        return results;
    }


    public static File word_count(byte[] data) throws IOException {

        String filePath = get_CSV(data); // Get filepath for reconstructed temp csv

        List<AmazonFineFoodReview> allReviews = read_reviews(filePath);

        System.out.println("Finished reading all reviews, now performing word count...");

        List<KV<String, Integer>> kv_pairs = map(allReviews);

        Map<String, Integer> results = reduce(kv_pairs);

        File file = write_word_count(results); // create file with written text output

        return file;
    }

}
