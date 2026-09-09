package ru.vlsu.pasib.lab1.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PasswordPolicy {
    private static final String PUNCTUATION ="!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private PasswordPolicy(){}

    public  record RequirementResult(String description, boolean passed){
        public String toLine(){
            return (passed ? "[+] " : "[-] ")+description;
        }
    }

    public record CheckResult(List<RequirementResult> requirements){
        public boolean allPassed(){
            for(RequirementResult r : requirements){
                if (!r.passed()){
                    return false;
                }
            }
            return true;
        }

        public String toReport(){
            StringBuilder sb = new StringBuilder();
            for (RequirementResult r:requirements){
                sb.append(r.toLine()).append('\n');
            }
            return sb.toString();
        }
    }

    public static CheckResult check(String password, int minLength){
        if (password==null){
            password = "";
        }

        List<RequirementResult> results = new ArrayList<>();

        results.add(new RequirementResult(
                "Длина не меньше " +minLength + " (сейчас: "+ password.length()+")",
                password.length() >=minLength));

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasPunct = false;

        Set<Character> seen = new HashSet<>();
        boolean hasRepeats = false;

        for (int i=0;i<password.length(); i++){
            char c = password.charAt(i);
            if (Character.isLowerCase(c)) {
                hasLower = true;
            }
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (PUNCTUATION.indexOf(c) >= 0) {
                hasPunct = true;
            }
            if (!seen.add(c)) {
                hasRepeats = true;
            }
        }

        results.add(new RequirementResult(
                "Есть строчные и прописные буквы",
                hasLower && hasUpper));

                results.add(new RequirementResult(
                "Есть цифры и знаки препинания",
                hasDigit && hasPunct));


        results.add(new RequirementResult(
                "Нет повторяющихся символов",
                !hasRepeats));

        return new CheckResult(List.copyOf(results));

    }

}
