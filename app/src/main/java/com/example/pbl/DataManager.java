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

        // Animals (Class 1) - 15 items
        List<Word> animals1 = new ArrayList<>();
        animals1.add(new Word("Cat", "ಬೆಕ್ಕು", "बिल्ली", R.drawable.cat));
        animals1.add(new Word("Dog", "ನಾಯಿ", "कुत्ता", R.drawable.dog));
        animals1.add(new Word("Cow", "ಹಸು", "गाय", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Pig", "ಹಂದಿ", "सुअर", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Hen", "ಕೋಳಿ", "मुರ್ಗಿ", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Sheep", "ಕುರಿ", "भेड़", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Goat", "ಮೇಕೆ", "बकरी", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Horse", "ಕುದುರೆ", "घोड़ा", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Duck", "ಬಾತುಕೋಳಿ", "बत्तख", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Bird", "ಹಕ್ಕಿ", "पक्षी", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Rabbit", "ಮೊಲ", "खरगोश", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Rat", "ಇಲಿ", "चूहा", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Fish", "ಮೀನು", "मछली", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Frog", "ಕಪ್ಪೆ", "मेंढक", android.R.drawable.ic_menu_gallery));
        animals1.add(new Word("Elephant", "ಆನೆ", "हाथी", android.R.drawable.ic_menu_gallery));
        class1.put("Animals", animals1);

        // Fruits & Vegetables (Class 1) - 15 items
        List<Word> fruits1 = new ArrayList<>();
        fruits1.add(new Word("Apple", "ಸೇಬು", "सेब", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Banana", "ಬಾಳೆಹಣ್ಣು", "केला", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Mango", "ಮಾವು", "आम", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Orange", "ಕಿತ್ತಳೆ", "संतरा", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Papaya", "ಪಪ್ಪಾಯಿ", "पपीता", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Lemon", "ನಿಂಬೆ", "नींबू", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Guava", "ಸೀಬೆಹಣ್ಣು", "अमरूद", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Grapes", "ದ್ರಾಕ್ಷಿ", "अंगूर", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Pineapple", "ಅನಾನಸ್", "अनानास", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Watermelon", "ಕಲ್ಲಂಗಡಿ", "तरबूज", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Tomato", "ಟೊಮೆಟೊ", "टमाटर", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Potato", "ಆಲೂಗಡ್ಡೆ", "आलू", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Onion", "ಈರುಳ್ಳಿ", "प्याज", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Carrot", "ಕ್ಯಾರೆಟ್", "गाजर", android.R.drawable.ic_menu_gallery));
        fruits1.add(new Word("Brinjal", "ಬದನೆಕಾಯಿ", "बैंगन", android.R.drawable.ic_menu_gallery));
        class1.put("Fruits", fruits1);

        // Daily Use (Class 1) - 15 items
        List<Word> daily1 = new ArrayList<>();
        daily1.add(new Word("Water", "ನೀರು", "पानी", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Milk", "ಹಾಲು", "दूध", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Food", "ಊಟ", "खाना", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Plate", "ತಟ್ಟೆ", "थाली", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Spoon", "ಚಮಚ", "चम्मच", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Bed", "ಹಾಸಿಗೆ", "बिस्तर", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Soap", "ಸೋಪು", "साबुन", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Door", "ಬಾಗಿಲು", "दरवाजा", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Window", "ಕಿಟಕಿ", "खिड़की", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Chair", "ಕುರ್ಚಿ", "कुर्सी", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Table", "ಮೇಜು", "मेज", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Cup", "ಕಪ್", "कप", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Box", "ಪೆಟ್ಟಿಗೆ", "डिब्बा", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Bag", "ಚೀಲ", "ಬಸ್ತಾ", android.R.drawable.ic_menu_gallery));
        daily1.add(new Word("Key", "ಚಾವಿ", "चाबी", android.R.drawable.ic_menu_gallery));
        class1.put("Daily Use", daily1);

        // School (Class 1) - 10 items
        List<Word> school1 = new ArrayList<>();
        school1.add(new Word("Book", "ಪುಸ್ತಕ", "किताब", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Pen", "ಪೆನ್", "कलम", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Bag", "ಚೀಲ", "बस्ता", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Slate", "ಹಲಗೆ", "स्लेट", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Chalk", "ಸುಣ್ಣದ ಕಡ್ಡಿ", "चाक", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Bell", "ಗಂಟೆ", "घंटी", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Paper", "ಕಾಗದ", "कागज", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Desk", "ಡೆಸ್ಕ್", "डेस्क", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("Teacher", "ಶಿಕ್ಷಕರು", "शिक्षक", android.R.drawable.ic_menu_gallery));
        school1.add(new Word("School", "ಶಾಲೆ", "स्कूल", android.R.drawable.ic_menu_gallery));
        class1.put("School", school1);

        // Numbers (Class 1) - 10 items
        List<Word> numbers1 = new ArrayList<>();
        numbers1.add(new Word("One", "ಒಂದು", "एक", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Two", "ಎರಡು", "दो", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Three", "ಮೂರು", "तीन", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Four", "ನಾಲ್ಕು", "चार", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Five", "ಐದು", "पाँच", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Six", "ಆರು", "छह", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Seven", "ಏಳು", "सात", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Eight", "ಎಂಟು", "आठ", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Nine", "ಒಂಬತ್ತು", "नौ", android.R.drawable.ic_menu_gallery));
        numbers1.add(new Word("Ten", "ಹತ್ತು", "दस", android.R.drawable.ic_menu_gallery));
        class1.put("Numbers", numbers1);

        // Sentences (Class 1) - 15 items
        List<Word> sentences1 = new ArrayList<>();
        sentences1.add(new Word("I am happy", "ನಾನು ಸಂತೋಷವಾಗಿದ್ದೇನೆ", "मैं खुश हूँ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("This is a pen", "ಇದು ಪೆನ್", "यह एक कलम है", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("I like fruit", "ನನಗೆ ಹಣ್ಣು ಇಷ್ಟ", "मुझे फल पसंद हैं", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Open the book", "ಪುಸ್ತಕವನ್ನು ತೆರೆಯಿರಿ", "किताब खोलो", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Sit down", "ಕುಳಿತುಕೊಳ್ಳಿ", "बैठ जाओ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Stand up", "ಎದ್ದು ನಿಲ್ಲಿ", "खड़े हो जाओ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Come here", "ಇಲ್ಲಿ ಬಾ", "यहाँ आओ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Drink water", "ನೀರು ಕುಡಿಯಿರಿ", "पानी पियो", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Good morning", "ಶುಭೋದಯ", "सुप्रभात", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("Thank you", "ಧನ್ಯವಾದಗಳು", "धन्यवाद", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("What is your name?", "ನಿಮ್ಮ ಹೆಸರೇನು?", "आपका नाम क्या है?", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("My name is...", "ನನ್ನ ಹೆಸರು...", "मेरा नाम ... है", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("This is my bag", "ಇದು ನನ್ನ ಚೀಲ", "यह मेरा बस्ता है", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("I go to school", "ನಾನು ಶಾಲೆಗೆ ಹೋಗುತ್ತೇನೆ", "मैं स्कूल जाता हूँ", android.R.drawable.ic_menu_gallery));
        sentences1.add(new Word("I love my school", "ನಾನು ನನ್ನ ಶಾಲೆಯನ್ನು ಪ್ರೀತಿಸುತ್ತೇನೆ", "मुझे अपना स्कूल पसंद है", android.R.drawable.ic_menu_gallery));
        class1.put("Sentences", sentences1);

        standardData.put(1, class1);

        // --- Standard 2 ---
        Map<String, List<Word>> class2 = new HashMap<>();

        // Animals (Class 2) - 15 items
        List<Word> animals2 = new ArrayList<>();
        animals2.add(new Word("Elephant", "ಆನೆ", "हाथी", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Tiger", "ಹುಲಿ", "बाघ", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Lion", "ಸಿಂಹ", "शेर", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Monkey", "ಕೋತಿ", "बंदर", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Rabbit", "ಮೊಲ", "खरगोश", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Giraffe", "ಜಿರಾಫೆ", "जिराफ", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Zebra", "ಜೆಬ್ರಾ", "जेबरा", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Bear", "ಕರಡಿ", "भालू", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Deer", "ಜಿಂಕೆ", "हिरण", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Snake", "ಹಾವು", "साँप", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Camel", "ಒಂಟೆ", "ऊँट", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Donkey", "ಕತ್ತೆ", "गधा", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Buffalo", "ಎಮ್ಮೆ", "भैंस", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Peacock", "ನವಿಲು", "मोर", android.R.drawable.ic_menu_gallery));
        animals2.add(new Word("Parrot", "ಗಿಳಿ", "तोता", android.R.drawable.ic_menu_gallery));
        class2.put("Animals", animals2);

        // Fruits & Veg (Class 2) - 15 items
        List<Word> fruits2 = new ArrayList<>();
        fruits2.add(new Word("Orange", "ಕಿತ್ತಳೆ", "संतरा", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Grapes", "ದ್ರಾಕ್ಷಿ", "अंगूर", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Pineapple", "ಅನಾನಸ್", "अनानास", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Watermelon", "ಕಲ್ಲಂಗಡಿ", "तरबूज", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Pomegranate", "ದಾಳಿಂಬೆ", "अनार", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Strawberry", "ಸ್ಟ್ರಾಬೆರಿ", "स्ट्रॉबेरी", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Cherry", "ಚೆರ್ರಿ", "चेरी", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Coconut", "ತೆಂಗಿನಕಾಯಿ", "नारियल", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Jackfruit", "ಹಲಸಿನ ಹಣ್ಣು", "कटहल", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Beetroot", "ಬೀಟ್‌ರೂಟ್", "चुकंदर", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Cabbage", "ಕೋಸುಗಡ್ಡೆ", "पत्तागोभी", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Cauliflower", "ಹೂಕೋಸು", "फूलगोभी", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Chilli", "ಮೆಣಸಿನಕಾಯಿ", "मिर्च", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Garlic", "ಬೆಳ್ಳುಳ್ಳಿ", "लहसुन", android.R.drawable.ic_menu_gallery));
        fruits2.add(new Word("Ginger", "ಶುಂಠಿ", "अदरक", android.R.drawable.ic_menu_gallery));
        class2.put("Fruits", fruits2);

        // Daily Use (Class 2) - 15 items
        List<Word> daily2 = new ArrayList<>();
        daily2.add(new Word("Chair", "ಕುರ್ಚಿ", "ಕುರ್ಸಿ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Table", "ಮೇಜು", "ಮೆಜ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Door", "ಬಾಗಿಲು", "ದರವಾಜಾ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Window", "ಕಿಟಕಿ", "ಖಿಡಕಿ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Fan", "ಫ್ಯಾನ್", "ಪಂಖಾ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Clock", "ಗಡಿಯಾರ", "ಘಡಿ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Mirror", "ಕನ್ನಡಿ", "ಐನಾ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Comb", "ಬಾಚಣಿಗೆ", "ಕಂಧಿ", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Glass", "ಲೋಟ", "गिलास", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Bowl", "ಬಟ್ಟಲು", "कटोरी", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Broom", "ಪೊರಕೆ", "झाड़ू", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Umbrella", "ಛತ್ರಿ", "छाता", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Shoes", "ಶೂಗಳು", "जूते", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Plate", "ತಟ್ಟೆ", "थाली", android.R.drawable.ic_menu_gallery));
        daily2.add(new Word("Spoon", "ಚಮಚ", "चम्मच", android.R.drawable.ic_menu_gallery));
        class2.put("Daily Use", daily2);

        // School (Class 2) - 10 items
        List<Word> school2 = new ArrayList<>();
        school2.add(new Word("Pencil", "ಪೆನ್ಸಿಲ್", "पेंसिल", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Eraser", "ರಬ್ಬರ್", "रबड़", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Blackboard", "ಕಪ್ಪು ಹಲಗೆ", "श्यामपट्ट", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Sharpener", "ಶಾರ್ಪನರ್", "शार्पनर", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Ruler", "ಅಳತೆ ಪಟ್ಟಿ", "पटरी", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Teacher", "ಶಿಕ್ಷಕರು", "शिक्षक", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Student", "ವಿದ್ಯಾರ್ಥಿ", "छात्र", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Library", "ಗ್ರಂಥಾಲಯ", "पुस्तकालय", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Playground", "ಆಟದ ಮೈದಾನ", "खेल का मैदान", android.R.drawable.ic_menu_gallery));
        school2.add(new Word("Classroom", "ತರಗತಿ ಕೊಠಡಿ", "कक्षा", android.R.drawable.ic_menu_gallery));
        class2.put("School", school2);

        // Numbers (Class 2) - 10 items
        List<Word> numbers2 = new ArrayList<>();
        numbers2.add(new Word("Ten", "ಹತ್ತು", "दस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Twenty", "ಇಪ್ಪತ್ತು", "बीस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Thirty", "ಮೂವತ್ತು", "तीस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Forty", "ನಲವತ್ತು", "चालीस", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Fifty", "ಐವತ್ತು", "पचास", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Sixty", "ಅರವತ್ತು", "साठ", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Seventy", "ಎಪ್ಪತ್ತು", "सत्तर", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Eighty", "ಎಂಬತ್ತು", "अस्सी", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Ninety", "ತೊಂಬತ್ತು", "नब्बे", android.R.drawable.ic_menu_gallery));
        numbers2.add(new Word("Hundred", "ನೂರು", "सौ", android.R.drawable.ic_menu_gallery));
        class2.put("Numbers", numbers2);

        // Sentences (Class 2) - 15 items
        List<Word> sentences2 = new ArrayList<>();
        sentences2.add(new Word("I go to school", "ನಾನು ಶಾಲೆಗೆ ಹೋಗುತ್ತೇನೆ", "मैं स्कूल जाता हूँ", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("The sky is blue", "ಆಕಾಶವು ನೀಲಿ ಬಣ್ಣದ್ದಾಗಿದೆ", "आसमान नीला है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("I love my parents", "ನಾನು ನನ್ನ ಪೋಷಕರನ್ನು ಪ್ರೀತಿಸುತ್ತೇನೆ", "मैं अपने माता-पिता से प्यार करता हूँ", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Wash your hands", "ನಿಮ್ಮ ಕೈಗಳನ್ನು ತೊಳೆಯಿರಿ", "अपने हाथ धोएं", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Brush your teeth", "ನಿಮ್ಮ ಹಲ್ಲುಗಳನ್ನು ಉಜ್ಜಿಕೊಳ್ಳಿ", "अपने दांत साफ करें", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Eat healthy food", "ಆರೋಗ್ಯಕರ ಆಹಾರವನ್ನು ಸೇವಿಸಿ", "स्वस्थ भोजन करें", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Birds fly in sky", "ಪಕ್ಷಿಗಳು ಆಕಾಶದಲ್ಲಿ ಹಾರುತ್ತವೆ", "पक्षी आसमान में उड़ते हैं", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Sun rises in east", "ಸೂರ್ಯನು ಪೂರ್ವದಲ್ಲಿ ಉದಯಿಸುತ್ತಾನೆ", "सूरज पूर्व में उगता है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("I have a big ball", "ನನ್ನ ಹತ್ತಿರ ದೊಡ್ಡ ಚೆಂಡು ಇದೆ", "मेरे पास एक बड़ी गेंद है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("Please give me pen", "ದಯವಿಟ್ಟು ನನಗೆ ಪೆನ್ ಕೊಡಿ", "कृपया मुझे कलम दें", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("I like to play", "ನನಗೆ ಆಟವಾಡಲು ಇಷ್ಟ", "मुझे खेलना पसंद है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("This is my house", "ಇದು ನನ್ನ ಮನೆ", "यह मेरा घर है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("That is a tree", "ಅದು ಮರ", "वह एक पेड़ है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("The cow gives milk", "ಹಸು ಹಾಲು ಕೊಡುತ್ತದೆ", "गाय दूध देती है", android.R.drawable.ic_menu_gallery));
        sentences2.add(new Word("God is great", "ದೇವರು ದೊಡ್ಡವನು", "भगवान महान है", android.R.drawable.ic_menu_gallery));
        class2.put("Sentences", sentences2);

        standardData.put(2, class2);
    }

    public static List<Word> getWordsForCategory(int standard, String category) {
        Map<String, List<Word>> classData = standardData.getOrDefault(standard, new HashMap<>());
        List<Word> originals = classData.getOrDefault(category, new ArrayList<>());
        List<Word> copies = new ArrayList<>();
        for (Word w : originals) {
            Word copy = new Word(w.getEnglish(), w.getKannada(), w.getHindi(), w.getImageResId());
            copy.setStandard(standard);
            copy.setCategory(category);
            copies.add(copy);
        }
        return copies;
    }

    public static List<Word> getAllDefaultContent(String type) {
        List<Word> allContent = new ArrayList<>();
        boolean isSentences = "Sentences".equalsIgnoreCase(type);

        for (Map.Entry<Integer, Map<String, List<Word>>> entry : standardData.entrySet()) {
            int standard = entry.getKey();
            for (Map.Entry<String, List<Word>> catEntry : entry.getValue().entrySet()) {
                String category = catEntry.getKey();
                boolean isCategorySentences = "Sentences".equalsIgnoreCase(category);

                if ((isSentences && isCategorySentences) || (!isSentences && !isCategorySentences)) {
                    for (Word w : catEntry.getValue()) {
                        // Create a copy to avoid modifying the static data shared across the app
                        Word copy = new Word(w.getEnglish(), w.getKannada(), w.getHindi(), w.getImageResId());
                        copy.setStandard(standard);
                        copy.setCategory(category);
                        allContent.add(copy);
                    }
                }
            }
        }
        return allContent;
    }
}
