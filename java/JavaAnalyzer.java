//This is where my java reading level analyzer will go
// javac JavaAnalyzer.java
// java JavaAnalyzer


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.lang.Math;
import java.lang.Character;
import java.util.*;

class JavaAnalyzer {

	public static void main(String args[]) {

		String translationsDir = "/pub/pounds/CSC330/translations";
		File folder = new File(translationsDir);
		ArrayList<String> listOfTranslations = listFilesForFolder(folder);
		Set<String> easyWordList = getEasyWordList();
		listOfTranslations.forEach( (tran) -> analyzeFile(tran, easyWordList));
	}

	public static ArrayList<String> listFilesForFolder(File folder) {
		ArrayList<String> tmp = new ArrayList<String>();
    for (final File fileEntry : folder.listFiles()) {
        tmp.add(fileEntry.getName());
    }
		return tmp;
	}

	public static void analyzeFile(String fileName, Set<String> easyWords) {
      File myFile = new File("/pub/pounds/CSC330/translations/" + fileName);
			ArrayList<String> words = getWordList(myFile);
			double numSentences = getSentencesAndCleanWords(words);
			double numSyllables = getNumSyllables(words);
			double numDifficult = getNumDifficult(words, easyWords);
			int flesch = CalculateFleschScore(words, numSentences, numSyllables);
			double num2 = CalculateFleschKincaidGradeLevel(words, numSentences, numSyllables);
			double num3 = CalculateDaleChallScore(words, numSentences, numSyllables, numDifficult);
			System.out.println(fileName + " - Flesch: " + flesch + ", Flesch-Kincaid: " + num2 + ", DaleChall: " + num3);
	}

	public static Set<String> getEasyWordList() {
		Set<String> easyWords = new HashSet<String>();
		try {
			File myFile = new File("/pub/pounds/CSC330/dalechall/wordlist1995.txt");
			Scanner myReader = new Scanner(new FileInputStream(myFile));
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				easyWords.add(data);
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		return easyWords;
	}

	public static ArrayList<String> getWordList(File file) {
		ArrayList<String> wordList = new ArrayList<String>();
 		try {
			Scanner myReader = new Scanner(new FileInputStream(file));
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				List<String> dataArray = Arrays.asList(data.split(" "));
				dataArray.forEach( (str) -> {
					if (str != "" && !str.matches(".*\\d.*")) {
						wordList.add(str);
					}
				});
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return wordList;
	}

	public static double getSentencesAndCleanWords(ArrayList<String> words) {
		String punctuation = ".:;?!";
		double numSentences = 0;
		for (int i = 0; i < words.size(); i++) {
			String tmp = words.get(i);
			if (wordContainsAnyFromList(tmp, punctuation)) {
				numSentences++;
			}
			words.set(i, cleanWord(tmp));
		}
		return numSentences;
	}

	public static String cleanWord(String word) {
		for (int i = 0; i < word.length(); i++) {
			if (!Character.isLetter(word.charAt(i)) || word.charAt(i) == '\"' || word.charAt(i) == '#') {
				word = charRemoveAt(word, i);
			}
		}
		return word;
	}

	public static String charRemoveAt(String s, int p) {
		return s.substring(0,p) + s.substring(p+1);
	}

	public static double getNumDifficult(ArrayList<String> words, Set<String> easyWords) {

		double numDifficult = 0;
		for (String s : words) {
			if (!easyWords.contains(s)) {
				numDifficult++;
			}
		}

		return numDifficult;
	}

	public static double getNumSyllables(ArrayList<String> words) {
		String vowels = "aeiouyAEIOUY";
		double numSyllables = 0;
		for (int i = 0; i < words.size(); i++)
		{
				int numToAdd = 0;
				String word = words.get(i);
				for (int j = 0; j < word.length(); j++)
				{
					if (vowels.indexOf(word.charAt(j)) != -1) {
						if (j == word.length() - 1) {
							if (word.charAt(j) == 'e' || word.charAt(j) == 'E') {
								if (word.length() > 1) {
									if (word.charAt(j-1) == 'l' || word.charAt(j-1) == 'L') {
										if (word.length() > 2) {
											if (vowels.indexOf(word.charAt(j-2)) == -1) {
												numToAdd++;
											}
										} else {
											numToAdd++;
										}
									}
								} else {
									numToAdd++;
								}
							} else {
								numToAdd++;
							}
						} else if (vowels.indexOf(word.charAt(j+1)) != -1) {
							numToAdd++;
							j++;
						} else {
							numToAdd++;
						}
					}
				}
				if (numToAdd == 0) {
					numToAdd = 1;
				}
				numSyllables+= numToAdd;
			}

		return numSyllables;
	}

	public static int CalculateFleschScore(ArrayList<String> wordList, double numSentences, double numSyllables)
	{
			// add words to list
			double alpha = numSyllables / (double) wordList.size();
			double beta = (double) wordList.size() / numSentences;

			double index = 206.835 - (alpha * 84.6) - (beta * 1.015);
			return (int) Math.round(index);
	}

	public static double CalculateFleschKincaidGradeLevel(ArrayList<String> wordList, double numSentences, double numSyllables)
	{
			double alpha = numSyllables / (double) wordList.size();
			double beta = (double) wordList.size() / numSentences;

			double grade = (alpha * 11.8) + (beta * 0.39) - 15.59;
			return Math.round(grade * 10.0) / 10.0;
	}

	public static double CalculateDaleChallScore(ArrayList<String> wordList, double numSentences, double numSyllables, double numDifficult)
	{
			double alpha = numDifficult / (double) wordList.size();
			double beta = (double) wordList.size() / numSentences;

			double diffPercent = alpha * 100;
			double grade = (beta * 0.0496) + (.1579 * diffPercent);
			if (diffPercent > 5)
			{
					grade += 3.6365;
			}
			return Math.round(grade * 10.0) / 10.0;
	}

	public static boolean wordContainsAnyFromList(String str, String charList) {

      if (str == null || str.length() == 0) {
          return false;
      }
      for (int i = 0; i < str.length(); i++) {
          char ch = str.charAt(i);
          if (charList.indexOf(ch) != -1) {
            return true;
          }
      }
      return false;
  }

}
