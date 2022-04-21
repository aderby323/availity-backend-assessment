package AvailityHW;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

class AvailityHomework {
    public static void main(String[] args) {

        // Function demo
        AvailityHomework obj = new AvailityHomework();
        String parenthesesTest = "((this)) should {work[]!}";
        String parenthesesTest2 = "{this([shouldn't])work..)";
        System.out.println("Testing string: \"" + parenthesesTest + "\": " + obj.ParenthesesChecker(parenthesesTest));
        System.out.println("Testing string: \"" + parenthesesTest2 + "\": " + obj.ParenthesesChecker(parenthesesTest2));
        obj.GroupEnrolleesByInsurance();
    }

    /**
    Validates that all parentheses in a string are properly closed and nested
    @param input The string to be checked
    @return True, if the parentheses are properly closed/nested. Otherwise, false
     */
    public boolean ParenthesesChecker(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        Stack<Character> charStack = new Stack<Character>();
        for (Character c : input.toCharArray()) {
            switch(c) {
                case '(': case '{': case '[':
                    charStack.push(c);
                    break;
                case ')': case '}': case ']':
                    if (charStack.isEmpty()) {
                        return false;
                    }
                    if (c == ')' && charStack.peek() != '(') {
                        return false;
                    }
                    if (c == '}' && charStack.peek() != '{') {
                        return false;
                    }
                    if (c == ']' && charStack.peek() != '[') {
                        return false;
                    }
                    charStack.pop();
                default: break;
            }
        }

        return charStack.isEmpty();
    }

    /**
    Reads a .csv file of enrollees, groups them by insurance companies, and creates dedicated .csv files of the most recent enrollees within those insurance companies. 
    Enrollees in each file are sorted by last name then first name in ascending order with duplicates removed.
     */
    public void GroupEnrolleesByInsurance() {
        try (BufferedReader reader = new BufferedReader(new FileReader(String.format("%s/AvailityHW/MOCK_DATA.csv", System.getProperty("user.dir"))))){
            HashMap<String, HashMap<String,Enrollee>> enrolleesByInsurance = new HashMap<String, HashMap<String,Enrollee>>();
            String newLine = "";

            while((newLine = reader.readLine()) != null) {
                String[] lineValues = newLine.split(",");
                if (lineValues[0].equalsIgnoreCase("id")) { // Filter the header
                    continue;
                }

                int version = 0;
                try {
                    version = Integer.parseInt(lineValues[3]);
                } catch (NumberFormatException nFormatException) {
                    System.out.println("Version is not a valid number. Skipping row.");
                    nFormatException.printStackTrace();
                    continue;
                }

                Enrollee enrollee = new Enrollee(lineValues[0], lineValues[1], lineValues[2], version, lineValues[4]);
                
                //Check the hashmap of enrollees for existing entries
                if (enrolleesByInsurance.containsKey(lineValues[4])) {
                    HashMap<String, Enrollee> mapEnrollees = enrolleesByInsurance.get(lineValues[4]);
                    Enrollee existing = mapEnrollees.get(enrollee.userId);
                    if (existing == null) {
                        mapEnrollees.put(enrollee.userId, enrollee); // If enrollee does not exist, add them to the collection
                        continue;
                    }
                    if (existing.version < enrollee.version) {
                        mapEnrollees.replace(lineValues[0], enrollee); // If we find a more recent version of an enrollee, use that instead
                    }
                }
                else { // Add new insurance and new map of enrollees
                    HashMap<String,Enrollee> map = new HashMap<String,Enrollee>();
                    map.put(enrollee.userId, enrollee);
                    enrolleesByInsurance.put(lineValues[4], map);
                }
            }

            for (Map.Entry<String, HashMap<String,Enrollee>> e : enrolleesByInsurance.entrySet()) {
                String key = e.getKey();
                Enrollee[] list = e.getValue()
                    .values()
                    .stream()
                    .sorted((x, y) -> x.firstName.compareTo(y.firstName))
                    .sorted((x, y) -> x.lastName.compareTo(y.lastName))
                    .toArray(Enrollee[]::new);

                String fileName = String.format("AvailityHW\\results\\%s_Enrollees.csv", key);
                FileWriter fileWriter = new FileWriter(fileName, false);
                fileWriter.write("");
                fileWriter.close();

                try (PrintWriter writer = new PrintWriter(fileName)) {
                    StringBuilder sb = new StringBuilder();
                    for (Enrollee enrollee : list) {
                        sb.append(enrollee.userId);
                        sb.append(',');
                        sb.append(enrollee.firstName);
                        sb.append(',');
                        sb.append(enrollee.lastName);
                        sb.append(',');
                        sb.append(enrollee.version);
                        sb.append(',');
                        sb.append(enrollee.insuranceCompany);
                        sb.append('\n');
                    }

                    writer.write(sb.toString());
                    writer.close();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            reader.close();
        }
        catch (FileNotFoundException fnfException) {
            System.out.println("File was not found");
            fnfException.printStackTrace();
        }
        catch (IOException ioException) {
            System.out.println("I/O exception occured");
            ioException.printStackTrace();
        }
    }
}