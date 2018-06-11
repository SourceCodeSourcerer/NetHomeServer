package nu.nethome.home.start;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class DocGenerator {

    static HashMap<String, Item> items = new HashMap<>();
    static HashMap<String, Feature> features = new HashMap<>();
    static List<Feature> featureList = new ArrayList<>();

    static public void main(String[] arg) {


        try (BufferedReader itemReader = new BufferedReader(new FileReader("ItemsDoc.csv"));
             BufferedReader featureReader = new BufferedReader(new FileReader("FeaturesDoc.csv"))
        ) {
            String line;
            while ((line = featureReader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    Feature feature = addFeature(parts[0], parts[1]);
                } else if (parts.length == 1) {
                    Feature feature = addFeature(parts[0], "");
                }
            }
            while ((line = itemReader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    Item item = addItem(parts[0], parts[2]);
                    Feature feature = features.get(parts[1]);
                    if (feature == null) {
                        System.out.printf("Missing feature: %s. Exiting", parts[1]);
                        System.exit(-1);
                    }
                    feature.addItem(item);
                }
            }
        } catch (IOException e) {
            System.out.printf("Error %s. Exiting", e.getMessage());
        }
        for (Feature feature : featureList) {
            System.out.printf("\n%s\n", feature.name);
            System.out.printf("----------------------\n");
            System.out.printf("%s\n", feature.description);
            for (Item item : feature.items) {
                System.out.printf("%s: %s\n", item.name, item.description);
            }
        }
        printFeatureList();
        printFeatureIndex();
    }

    private static void printFeatureList() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("features.txt"))) {
            for (Feature feature : featureList) {
                pw.printf("<div class=\"card\"><a href=\"http://opennethome.org/features/feature-index/#%s\">%s</a></div>\n", feature.name, feature.name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printFeatureIndex() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("featureIndex.txt"))) {
            for (Feature feature : featureList) {
                pw.printf("<h2 id=\"%s\">%s</h2>\n", feature.name, feature.name);
                pw.printf("<em>%s</em>%s", feature.description, feature.description.isEmpty() ? "" : "\n");
                for (Item item : feature.items) {
                    pw.printf("<strong class=\"item\"><a href=\"http://wiki.nethome.nu/doku.php?id=%s\" target=\"_blank\" rel=\"noopener noreferrer\">%s</a></strong>&nbsp;%s\n", item.name, item.name, item.description);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Feature addFeature(String feature, String desc) {
        Feature result = features.get(feature);
        if (result == null) {
            result = new Feature(feature, desc);
            features.put(feature, result);
            featureList.add(result);
        }
        return result;
    }

    static Item addItem(String name, String description) {
        Item result = items.get(name);
        if (result == null) {
            result = new Item(name, description);
            items.put(name, result);
        }
        return result;
    }

    static class Item {
        public final String name;
        public final String description;

        public Item(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    static class Feature {
        public final String name;
        public final String description;
        public final List<Item> items;

        public Feature(String name, String description) {
            this.name = name;
            this.description = description;
            this.items = new ArrayList<>();
        }

        public void addItem(Item item) {
            items.add(item);
        }
    }
}


