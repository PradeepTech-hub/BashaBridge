package com.example.pbl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static final Map<Integer, Map<String, List<Word>>> standardData = new HashMap<>();

    static {
        // --- Standard 1 ---
        Map<String, List<Word>> class1 = new HashMap<>();

        // Animals (Class 1)
        List<Word> animals1 = new ArrayList<>();
        animals1.add(new Word("Cat", "ಬೆಕ್ಕು", "बिल्ली", R.drawable.cat));
        animals1.add(new Word("Dog", "ನಾಯಿ", "कुत्ता", R.drawable.dog));
        animals1.add(new Word("Cow", "ಹಸು", "गाय", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Pig", "ಹಂದಿ", "सुअर", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Hen", "ಕೋಳಿ", "मुर्गी", android.R.drawable.ic_menu_gallery));
        class1.put("Animals", animals1);

        // Fruits (Class 1)
        List<Word> fruits1 = new ArrayList<>();
        fruits1.add(new Word("Apple", "ಸೇಬು", "सेब", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Banana", "ಬಾಳೆಹಣ್ಣು", "केला", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Mango", "ಮಾವು", "आम", android.R.drawable.ic_menu_gallery));
        class1.put("Fruits", fruits1);

        // Daily Use (Class 1)
        List<Word> daily1 = new ArrayList<>();
        daily1.add(new Word("Water", "ನೀರು", "पानी", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Milk", "ಹಾಲು", "दूध", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Food", "ಊಟ", "खाना", android.R.drawable.ic_menu_gallery));
        class1.put("Daily Use", daily1);

        // School (Class 1)
        List<Word> school1 = new ArrayList<>();
        school1.add(new Word("Book", "ಪುಸ್ತಕ", "किताब", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Pen", "ಪೆನ್", "कलम", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Bag", "ಚೀಲ", "बस्ता", android.R.drawable.ic_menu_gallery));
        class1.put("School Objects", school1);

        // Numbers (Class 1)
        List<Word> numbers1 = new ArrayList<>();
        numbers1.add(new Word("One", "ಒಂದು", "एक", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Two", "ಎರಡು", "दो", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Three", "ಮೂರು", "तीन", android.R.drawable.ic_menu_gallery));
        class1.put("Numbers", numbers1);

        // Sentences (Class 1)
        List<Word> sentences1 = new ArrayList<>();
        sentences1.add(new Word("I am happy", "ನಾನು ಸಂತೋಷವಾಗಿದ್ದೇನೆ", "मैं खुश हूँ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("This is a pen", "ಇದು ಪೆನ್", "यह एक कलम है", android.R.drawable.ic_menu_gallery));
        class1.put("Sentences", sentences1);

        standardData.put(1, class1);

        // --- Standard 2 ---
        Map<String, List<Word>> class2 = new HashMap<>();

        // Animals (Class 2)
        List<Word> animals2 = new ArrayList<>();
        animals2.add(new Word("Elephant", "ಆನೆ", "हाथी", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Tiger", "ಹುಲಿ", "बाघ", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Lion", "ಸಿಂಹ", "शेर", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Monkey", "ಕೋತಿ", "बंदर", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Rabbit", "ಮೊಲ", "खरगोश", android.R.drawable.ic_menu_gallery));
        class2.put("Animals", animals2);

        // Fruits (Class 2)
        List<Word> fruits2 = new ArrayList<>();
        fruits2.add(new Word("Orange", "ಕಿತ್ತಳೆ", "संतरा", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Grapes", "ದ್ರಾಕ್ಷಿ", "अंगूर", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Pineapple", "ಅನಾನಸ್", "अनानास", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Watermelon", "ಕಲ್ಲಂಗಡಿ", "तरबूज", android.R.drawable.ic_menu_gallery));
        class2.put("Fruits", fruits2);

        // Daily Use (Class 2)
        List<Word> daily2 = new ArrayList<>();
        daily2.add(new Word("Chair", "ಕುರ್ಚಿ", "कुर्सी", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Table", "ಮೇಜು", "मेज", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Door", "ಬಾಗಿಲು", "दरवाजा", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Window", "ಕಿಟಕಿ", "खिड़की", android.R.drawable.ic_menu_gallery));
        class2.put("Daily Use", daily2);

        // School (Class 2)
        List<Word> school2 = new ArrayList<>();
        school2.add(new Word("Pencil", "ಪೆನ್ಸಿಲ್", "पेंसिल", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Eraser", "ರಬ್ಬರ್", "रबड़", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Blackboard", "ಕಪ್ಪು ಹಲಗೆ", "श्यामपट्ट", android.R.drawable.ic_menu_gallery));
        class2.put("School Objects", school2);

        // Numbers (Class 2)
        List<Word> numbers2 = new ArrayList<>();
        numbers2.add(new Word("Ten", "ಹತ್ತು", "दस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Twenty", "ಇಪ್ಪತ್ತು", "बीस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Fifty", "ಐವತ್ತು", "पचास", android.R.drawable.ic_menu_gallery));
        class2.put("Numbers", numbers2);

        // Sentences (Class 2)
        List<Word> sentences2 = new ArrayList<>();
        sentences2.add(new Word("I go to school", "ನಾನು ಶಾಲೆಗೆ ಹೋಗುತ್ತೇನೆ", "मैं स्कूल जाता हूँ", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("The sky is blue", "ಆಕಾಶವು ನೀಲಿ ಬಣ್ಣದ್ದಾಗಿದೆ", "आसमान नीला है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("I love my parents", "ನಾನು ನನ್ನ ಪೋಷಕರನ್ನು ಪ್ರೀತಿಸುತ್ತೇನೆ", "मैं अपने माता-पिता से प्यार करता हूँ", android.R.drawable.ic_menu_gallery));
        class2.put("Sentences", sentences2);

        standardData.put(2, class2);
    }

    public static List<Word> getWordsForCategory(int standard, String category) {
        Map<String, List<Word>> classData = standardData.getOrDefault(standard, new HashMap<>());
        return classData.getOrDefault(category, new ArrayList<>());
    }
}
